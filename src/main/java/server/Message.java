package server;

import server.transaction.Entity;

import com.google.gson.Gson;

public class Message {

	public final static int TYPE_QUERY = 0;
	public final static int TYPE_REPLY = 1;
	public final static int TYPE_CLOSE = 2;
	
	public int _type;
	public Entity _object;
	
	public Message(int iType, Entity iObject){
		_type = iType;
		_object = iObject;
	}
	
	@Override
	public String toString(){
		Gson aGson = new Gson();
		String iJson = aGson.toJson(_object);
		return iJson;
	}
	
}
