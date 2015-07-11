package server;

import server.transaction.Entity;
import server.transaction.Query;


public class Protocol {
 
	private static final int WAITING = 0;
    private static final int SENTKNOCKKNOCK = 1;
 
    private int state = WAITING;
 
 
    public Message processInput(Message theInput) {
        Message theOutput = null;
 
        if (state == WAITING) {
            theOutput = new Message(Message.TYPE_QUERY,new Query());
            state = SENTKNOCKKNOCK;
        } else if (state == SENTKNOCKKNOCK) {
        	
        
        } 
        
        return theOutput;
    }
}
