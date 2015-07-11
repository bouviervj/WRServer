package server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.att.m2x.java.M2XClient;
import com.att.m2x.java.M2XDevice;
import com.att.m2x.java.M2XStream;

import server.transaction.Action;
import server.transaction.Entity;
import server.transaction.Query;
import server.transaction.Reply;
import server.transaction.Service;


public class Protocol {

	private static final int WAITING = 0;
	private static final int SENTKNOCKKNOCK = 1;

	private int state = WAITING;

	public static HashMap<String,Vector<Service>> _serviceMap = new HashMap<String,Vector<Service>>();
	public static HashMap<String,Reply> _repliesMap = new HashMap<String,Reply>();
	
	public static M2XDevice deviceLight;
	public static M2XStream streamLight;
	public static M2XStream streamLightOn;
	public static M2XStream streamLightOff;
	
	public static M2XDevice deviceTemperature;
	public static M2XStream streamTemperature;
	
	static {
		
		M2XClient client = new M2XClient("ad3abc434190389d8d14df56fcc691f4");
		deviceLight = client.device("712b1d5519fabfcb19bb5a206e47cb4f");
		streamLight = deviceLight.stream("light");
		streamLightOn = deviceLight.stream("lighton");
		streamLightOff = deviceLight.stream("lightoff");
		
		deviceTemperature = client.device("dc2cd9c1cd7739fef52afee7747c5fbf");
		streamTemperature = deviceTemperature.stream("temperature");
		
	}


	public Message processInput(ServerThread thread, Message theInput) {
		Message theOutput = null;

		if (state == WAITING) {
			theOutput = new Message(Message.TYPE_QUERY,new Query());
			state = SENTKNOCKKNOCK;
		} else if (state == SENTKNOCKKNOCK) {

			if (theInput._type==Message.TYPE_REPLY) {

				Reply reply = (Reply) theInput._object;
				if (reply.services!=null) {

					thread.clientname = reply.from;
					for (Service srv:reply.services) {
						srv._thread = thread;
					}
					publishServices(reply);
				} else if (reply.actions!=null) {
					_repliesMap.put(reply.to, reply);
				}

			}

		} 

		return theOutput;
	}

	public void publishServices(Reply reply){

		_serviceMap.put(reply.from, reply.services);

	}

	public static String timeHash(){

		String aDate = (new Date()).toString();

		MessageDigest md;
		try {
			byte[] bytesOfMessage = aDate.getBytes("UTF-8");
			
			md = MessageDigest.getInstance("MD5");

			byte[] thedigest = md.digest(bytesOfMessage);

			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<thedigest.length;i++) {
				String hex=Integer.toHexString(0xff & thedigest[i]);
				if(hex.length()==1) hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return "failure";

	}

	public static Service findService(String iType){
		for (Vector<Service> services:_serviceMap.values()){
			for (Service srv:services) {
				if (srv.type.equals(iType)) {
					return srv;
				}
			}
		}
		return null;
	}
	
	public static boolean isReplyExpected(String iType){
		Service srv = findService(iType);
		if (srv!=null) {
			return srv.reply!=null?srv.reply.equals("true"):false;
		} 
		return false;
	}
	
	public static Reply waitForMessage(String iHash){
		
		boolean found = false;
		while (!found) {
			if (_repliesMap.containsKey(iHash)) {
				found = true;
			} else {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return _repliesMap.get(iHash);
		
	}
	
	public static String callServices(String iType, String iActionCode){

		String hash = timeHash();
		
		for (Vector<Service> services:_serviceMap.values()){

			for (Service srv:services) {
				if (srv.type.equals(iType)) {

					Action act = new Action();
					act.type = iType;
					act.actioncode = iActionCode;

					Query aQuery = new Query();
					aQuery.from = hash;
					aQuery.to = "device";

					aQuery.actions = new Vector<Action>();
					aQuery.actions.add(act);

					Message msg = new Message(Message.TYPE_QUERY, aQuery);
					srv._thread.sendMessage( msg );

				}
			}
			
		}
		
		return hash;

	}
	
	public static void statsLight(){
		
		if (streamLight!=null) {
			try {
				streamLight.updateValue(M2XClient.jsonSerialize(new HashMap<String, Object>()
					{{
					put("value", 1);
				}}));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void statsLightOn(){
		
		if (streamLightOn!=null) {
			try {
				streamLightOn.updateValue(M2XClient.jsonSerialize(new HashMap<String, Object>()
					{{
					put("value", 1);
				}}));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void statsLightOff(){
		
		if (streamLightOff!=null) {
			try {
				streamLightOff.updateValue(M2XClient.jsonSerialize(new HashMap<String, Object>()
					{{
					put("value", 1);
				}}));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void statsTemperature(final String iValue){
		
		if (streamLightOn!=null) {
			try {
				streamLightOn.updateValue(M2XClient.jsonSerialize(new HashMap<String, Object>()
					{{
					put("value", new Float(iValue).floatValue());
				}}));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void stats(String iType, String iActionCode, String iValue){
		
		if (iType.equals("light")){
			statsLight();
			
			if (iActionCode.equals("on")) {
				statsLightOn();
			} else if (iActionCode.equals("off")) {
				statsLightOff();
			}
			
		} else if (iType.equals("temperature")) {
			
			statsTemperature(iValue);
			
		}
		
	}

}
