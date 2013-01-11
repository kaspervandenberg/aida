package org.vle.aid.openid;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.expressme.openid.Association;
import org.expressme.openid.Authentication;
import org.expressme.openid.Endpoint;
import org.expressme.openid.OpenIdException;
import org.expressme.openid.OpenIdManager;
import org.expressme.openid.ShortName;
 

public class OpenIDSVL extends HttpServlet {
 

    static final long ONE_HOUR = 3600000L;
    static final long TWO_HOUR = ONE_HOUR * 2L;
    static final String ATTR_MAC = "openid_mac";
    static final String ATTR_ALIAS = "openid_alias";
 

	ShortName shortName = new ShortName();
    OpenIdManager manager;
 

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
 

        //IF proxy
        java.util.Properties props = System.getProperties();
        props.put("proxySet", "true");
        props.put("proxyHost", "PROXY_IPADDRESS");
        props.put("proxyPort", "PROXY_PORT");
 

        manager = new OpenIdManager();
    }
 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {   
     String op = request.getParameter("op");   
     if (op==null) {       
      // check nonce:
      System.out.println(">>>>>>>> openid.response_nonce==>"+request.getParameter("openid.response_nonce"));
      checkNonce(request.getParameter("openid.response_nonce"));       
      // get authentication:       
      HttpSession session = request.getSession();       
      byte[] mac_key = (byte[]) session.getAttribute(ATTR_MAC);       
      String alias = (String) session.getAttribute(ATTR_ALIAS);       
      Authentication authentication = manager.getAuthentication(request, mac_key, alias);       
      String identity = authentication.getIdentity();       
      String email = authentication.getEmail();       
      // TODO: create user if not exist in database:       
       showAuthentication(response.getWriter(), authentication);   
     }   
     else if ("Google".equals(op) || "Yahoo".equals(op)) {
      manager.setReturnTo("http://"+request.getServerName()+":"+request.getServerPort()+"/Services/OpenIDSVL");
      
      // redirect to Google/Yahoo sign on page:       
      //String alias = manager.lookupExtNsAlias(op);      
      Endpoint endpoint = manager.lookupEndpoint(op);
      String alias = endpoint.getAlias();
      System.out.println(">>>>>>>> endpoint.getUrl()==>"+endpoint.getUrl());
      Association association = manager.lookupAssociation(endpoint);       
      HttpSession session = request.getSession();       
      session.setAttribute(ATTR_MAC, association.getRawMacKey());       
      session.setAttribute(ATTR_ALIAS, alias);       
      String url = manager.getAuthenticationUrl(endpoint, association);       
      response.sendRedirect(url);   
     }   
     else {       
      throw new ServletException("Bad parameter op=" + op);   
     }
    }
 

    void showAuthentication(PrintWriter pw, Authentication user) {   
     pw.print("<html><body>");   
     pw.print(" <h2>Hi "+user.getFullname()+"!</h2><p>Congratulations, you have successfully logged-in!</p>");   
     pw.print("<p><b>Indentity:</b> "+user.getIdentity()+"<br>");   
     pw.print("<b>Email:</b> "+user.getEmail()+"<br>");   
     pw.print("<b>Gender:</b> "+user.getGender()+"<br>");   
     pw.print("<b>Firstname:</b> "+user.getFirstname()+"<br>");   
     pw.print("<b>Lastname:</b> "+user.getLastname()+"<br>");   
     pw.print("<b>Language:</b> "+user.getLanguage()+"</p>");   
	 pw.print("<b>Back to <a href=http://dev.adaptivedisclosure.org/search-dev>Aida</a> </b>");
     pw.print("</body></html>");   
     pw.flush();
    }
 

    void checkNonce(String nonce) {
        // check response_nonce to prevent replay-attack:
        if (nonce==null || nonce.length()<20)
            throw new OpenIdException("Verify failed.");
        long nonceTime = getNonceTime(nonce);
        long diff = System.currentTimeMillis() - nonceTime;
        if (diff < 0)
            diff = (-diff);
        if (diff > ONE_HOUR)
            throw new OpenIdException("Bad nonce time.");
        if (isNonceExist(nonce))
            throw new OpenIdException("Verify nonce failed.");
        storeNonce(nonce, nonceTime + TWO_HOUR);
    }
 

    boolean isNonceExist(String nonce) {
        // TODO: check if nonce is exist in database:
        return false;
    }
 

    void storeNonce(String nonce, long expires) {
        // TODO: store nonce in database:
    }
 

    long getNonceTime(String nonce) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .parse(nonce.substring(0, 19) + "+0000")
                    .getTime();
        }
        catch(ParseException e) {
            throw new OpenIdException("Bad nonce time.");
        }
    }
}
