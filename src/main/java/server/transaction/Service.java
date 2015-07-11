package server.transaction;

import java.util.Vector;

import server.ServerThread;

public class Service {

	public String type;
	public String value;
	public String reply;
	public Vector<String> actioncodes;
	public transient ServerThread _thread;
	
}
