package server;


import java.net.*;
import java.io.*;

import server.transaction.Entity;
import server.transaction.Query;
import server.transaction.Reply;

import com.google.gson.Gson;

public class ServerThread extends Thread {

	private Socket socket = null;
	public MsgThreadSender senderThread;
	DataOutputStream out; 

	public ServerThread(Socket socket) {
		super("ServerThread");
		this.socket = socket;
	}

	public void run() {


		try {
			out = new DataOutputStream(socket.getOutputStream());

			DataInputStream in = new DataInputStream(socket.getInputStream());


			senderThread = new MsgThreadSender(out);

			Message inputLine, outputLine;
			Protocol protocol = new Protocol();
			outputLine = protocol.processInput(this, null);
			sendMessage(outputLine);

			while ((inputLine = readMessage(in)) != null) {
				outputLine = protocol.processInput(this, inputLine);
				if (outputLine!=null) {
					sendMessage(outputLine);
					if (outputLine._type==Message.TYPE_CLOSE)
						break;
				}
			}

			socket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void sendMessage(Message iMessage){
		senderThread.sendMessage(out, iMessage);
	}

	private Message readMessage(DataInputStream aStream){

		try {

			int aType = aStream.readInt();
			System.out.println(aType);
			int aSize = aStream.readInt();
			System.out.println(aSize);
			byte[] aData = new byte[aSize];

			aStream.readFully(aData);
			System.out.println(new String(aData));

			Gson aGson = new Gson();

			if (aType==Message.TYPE_QUERY) {
				return new Message(Message.TYPE_QUERY, (Entity) aGson.fromJson(new String(aData), Query.class));
			} else if (aType==Message.TYPE_REPLY) {
				return new Message(Message.TYPE_REPLY, (Entity) aGson.fromJson(new String(aData), Reply.class));
			}	


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

}

