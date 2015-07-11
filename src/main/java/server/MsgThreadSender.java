package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import com.google.gson.Gson;

public class MsgThreadSender implements Runnable {

	Vector<Message> _msgQueue;
	DataOutputStream _out;
	
	MsgThreadSender(DataOutputStream iStream){
		_out = iStream;
		_msgQueue = new Vector<Message>();
		start();
	}
	
	private void start(){
		(new Thread(this)).start();
	}
	
	
	public void run(){
		while (true) {
			if (_msgQueue.size()>0) {
				
				Message msg = _msgQueue.firstElement();
				sendMessage(_out, msg);
				
			} else {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	public void sendMessage(DataOutputStream aStream, Message iMessage){

		Gson aGson = new Gson();
		byte[] aData = aGson.toJson(iMessage._object).getBytes();
		
		System.out.println("Formatted data:"+new String(aData));
		
		try {
			aStream.writeInt(iMessage._type);
			aStream.writeInt(aData.length);
			aStream.write(aData);
			
			aStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
