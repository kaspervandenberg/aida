package org.vle.aid.taverna.remote;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.json.JSONObject;


public class AIDRemoteSearch {
	
	public static String [] listFields(String index){
		Call call = createAxisCall(AIDRemoteConfig.DEFAULT_SEARCH_FIELDS_SERVICE, "listFields");
		String[] result = null;
		try {
		    result = (String[]) call.invoke(new Object[] { index});
		} catch (RemoteException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		return result;		
	}
	
	public static String [] getIndexes(){
		Call call = createAxisCall(AIDRemoteConfig.DEFAULT_SEARCH_INDEXES_SERVICE, "listIndexes");
		String[] result = null;
		try {
		    result = (String[]) call.invoke(new Object[0] );
		} catch (RemoteException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		return result;	
	}
	
	public static JSONObject search(String index, String query, Integer start, String field, Integer count){
	    	JSONObject result = new JSONObject();
	    	Call call = createAxisCall(AIDRemoteConfig.DEFAULT_SEARCH_SERVICE,"searchJason");
	    	try {
		    String strResult = (String) call.invoke(new Object [] {index, query, start.toString(), field, count.toString() });
		    result = new JSONObject(strResult);
	    	} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    	
	    	return result;
	}
	
         /** Common routine to setup service call to axis */
	static Call createAxisCall(String axis_server, String operationName) {
		Service service = new Service();
		Call call = null;
		try {
		    call = (org.apache.axis.client.Call) service.createCall();
		    call.setTargetEndpointAddress(axis_server);
		    call.setOperationName(operationName);
		} catch (ServiceException e) {
		    e.printStackTrace();
		}
		return call;
	}

	public static void main(String[] args) {
	     	String lastIndex="", lastField ="";
	    	for(String index : getIndexes()) {
	    	    	//System.out.println(index);
	    	    	lastIndex = index;
	    	    	for(String field : listFields(index)){
	    	    	    	//System.out.println("-->"+field+ " "+lastField+ " "+lastIndex);
	    	    	    	lastField = field;
	    	    	}
	    	}
	    	search("tno", "food", 1, "content", 5);
	}
	
}
