package generic.ws.restjson.services;

import java.util.HashMap;
import java.util.Vector;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

import server.Protocol;
import server.transaction.Reply;
import server.transaction.Service;

@Path("/v1")
public class Services {

	protected static final Logger LOGGER = Logger.getLogger(Services.class.getName());
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/services")
	public  HashMap<String,Vector<Service>> services( @Context SecurityContext sc ) {
		LOGGER.info("Retrieve services ...");
		return Protocol._serviceMap;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/mergedServices")
	public  HashMap<String,Vector<String>> mergedServices( @Context SecurityContext sc ) {
		LOGGER.info("Retrieve services ...");
		
		HashMap<String, Vector<String>> aServices = new HashMap<String,Vector<String>>();
		for (Vector<Service> services:Protocol._serviceMap.values()){
			for (Service srv:services) {
				aServices.put(srv.type, srv.actioncodes);
			}
		}
		return aServices;
	}
		
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/callservice/{type}/{actioncode}")
	public String[] callservice( @Context SecurityContext sc, @PathParam("type") String iType,  @PathParam("actioncode") String iActionCode  ) {
		LOGGER.info("Executing simple code with argument :"+iType);
		String hash = Protocol.callServices(iType, iActionCode);
		
		Protocol.stats(iType, iActionCode, "");
		
		if (Protocol.isReplyExpected(iType)){
			Reply aReply = Protocol.waitForMessage(hash);
			String aValue = aReply.getActionsValue(iType);
			Protocol.stats(iType, iActionCode, aValue);
			return new String[]{aValue};
		}
		
		return new String[]{"OK"};
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/simplePost")
	public Object simplePost( @Context SecurityContext sc,
									   String[] iList) { 
		return null;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{state}/otherMIME/")
	public byte[] simpleOtherMIMEType( @Context SecurityContext sc, @PathParam("state") String iState) throws Exception {
		try {
			return "Stream".getBytes();
		} catch (Exception e) {
			LOGGER.info("Simple Error");
			throw new NotFoundException(e.getMessage());		
		}
		
	}
	
	
}
