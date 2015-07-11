package server;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

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
		return srv.reply!=null?srv.reply.equals("true"):false;
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

}
