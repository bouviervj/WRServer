package server.transaction;

import java.util.Vector;

public class Reply extends Comm implements Entity {

	public Vector<Service> services;
	public Vector<Service> actions;
	
	public String getActionsValue(String iType){
		if (actions!=null) {
			for (Service srv:actions) {
				if (srv.type.equals(iType) && srv.value!=null) {
					return srv.value;
				}
			}
		}
		return "KO";
	}
	
}
