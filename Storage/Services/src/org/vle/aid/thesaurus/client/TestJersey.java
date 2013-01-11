package org.vle.aid.thesaurus.client;

import java.util.Collection;
import java.util.HashSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.jersey.spi.resource.Singleton;

/**
 * Class to demonstrate Jersey's capabilities of encoding POJO's
 * 
 * @author emeij
 *
 */
@Path("/testjersey")
public class TestJersey {

  /**
   * Bean to hold some result
   * 
   * @author emeij
   *
   */
  @Singleton
  @XmlRootElement(name = "result")
  public class ResultBean {
      public String status = "Done";
      public Boolean success = true;
      public int time = 25;
      public final Collection<String> items = new HashSet<String>();
  }
 
  // trivial application logic
  ResultBean bean = new ResultBean();

 {{
   bean.items.add("concept_1");
   bean.items.add("concept_2");
   bean.items.add("concept_3");
 }}
 
 /** 
  * Returns HTML
  * @return
  */
 @GET
 @Produces("text/html")
 public String getStatusHTML() {
   StringBuilder b = new StringBuilder();
   
   b.append("<html><body>\n");
   for (String i : bean.items) {
     b.append(i + "<br/>\n");
   }
   b.append("</body></html>\n");
   return b.toString();
 }
 
 /** 
  * Returns Plain text
  * @return
  */
 @GET
 @Produces("text/plain")
 public String getStatusTEXT() {
   StringBuilder b = new StringBuilder();
   
   for (String i : bean.items) {
     b.append(i + "\n");
   }

   return b.toString();
 }
 
 /**
  * Returns a bean
  */
 @GET
 @Produces("application/json")
 public ResultBean getStatusJSON() {
     return bean;
 }

 /** 
  * Sends a bean to the server for processing
  * @param bean 
  *        the items to store
  */
 @PUT
 @Consumes("application/json")
 public synchronized void setStatus(ResultBean bean) {
     this.bean = bean;
 }
}
