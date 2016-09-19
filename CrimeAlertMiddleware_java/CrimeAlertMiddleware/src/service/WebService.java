package service;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.util.JSONParseException;

@Path("/")
public class WebService {
	 ExecutorService service = Executors.newFixedThreadPool(100);       
     Future<Document>  task = null;

    @POST
	@Path("/user/register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String registerUser(String postData){
		JSONObject postJson=null;
		try{
			postJson = new JSONObject(postData);
			DatabaseService databaseService = new DatabaseService();
			databaseService.setOption(1);
			databaseService.setJson(postJson);
			task = service.submit(databaseService);
			Document result = task.get();
		} catch(JSONException e){
			return Constants.JSON_PARSE_ERROR;
		} catch (JSONParseException e) {
			return Constants.JSON_PARSE_ERROR;
		} catch(Exception e){
			if(e.getMessage().contains("User already exists"))
				return Constants.USER_EXISTS_ERROR;
			else
				return e.getMessage();
		}
		return Constants.SUCCESS;		
	}
	
	@POST
	@Path("/user/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String userLogin(String postData){
		JSONObject postJson=null;
		try{
			postJson = new JSONObject(postData);
			DatabaseService databaseService = new DatabaseService();
			databaseService.setOption(2);
			databaseService.setJson(postJson);
			task = service.submit(databaseService);
			Document result = task.get();
		} catch(JSONException e){
			return Constants.JSON_PARSE_ERROR;
		} catch (JSONParseException e) {
			return Constants.JSON_PARSE_ERROR;
		} catch(Exception e){
			if(e.getMessage().contains("User Not Found"))
				return Constants.USER_NOT_FOUND;
			else
				return e.getMessage();
		}
		return Constants.SUCCESS;		
	}
	
	@POST
	@Path("/notify/distress")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String distressCall(String postData){
		JSONObject postJson=null;
		Document result=null;
		try{
			postJson = new JSONObject(postData);
			DatabaseService databaseService = new DatabaseService();
			databaseService.setOption(3);
			databaseService.setJson(postJson);
			task = service.submit(databaseService);
			result = task.get();
		} catch(JSONException e){
			return Constants.JSON_PARSE_ERROR;
		} catch (JSONParseException e) {
			return Constants.JSON_PARSE_ERROR;
		} catch(Exception e){
			if(e.getMessage().contains("User Not Found"))
				return Constants.USER_NOT_FOUND;
			else if(e.getMessage().contains("Broadcast failed"))
				return Constants.BROADCAST_FAILURE;
			else
				return e.getMessage();
		}
		return result.toJson();
		//return Constants.SUCCESS;
	}
	
	@POST
	@Path("/notify/crime")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String crimeReport(String postData){
		JSONObject postJson=null;
		Document result=null;
		try{
			postJson = new JSONObject(postData);
			DatabaseService databaseService = new DatabaseService();
			databaseService.setOption(6);
			databaseService.setJson(postJson);
			task = service.submit(databaseService);
			result = task.get();
		} catch(JSONException e){
			return Constants.JSON_PARSE_ERROR;
		} catch (JSONParseException e) {
			return Constants.JSON_PARSE_ERROR;
		} catch(Exception e){
			if(e.getMessage().contains("User Not Found"))
				return Constants.USER_NOT_FOUND;
			else
				return e.getMessage();
		}
		return Constants.SUCCESS;
	}
	
	@POST
	@Path("/crime/updatelocation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String updateCrimeLocation(String postData){
		JSONObject postJson=null;
		Document result=null;
		try{
			postJson = new JSONObject(postData);
			DatabaseService databaseService = new DatabaseService();
			databaseService.setOption(4);
			databaseService.setJson(postJson);
			task = service.submit(databaseService);
			result = task.get();
		} catch(JSONException e){
			return Constants.JSON_PARSE_ERROR;
		} catch (JSONParseException e) {
			return Constants.JSON_PARSE_ERROR;
		} catch(Exception e){
			if(e.getMessage().contains("Crime Record Not Found"))
				return Constants.CRIME_NOT_FOUND;
			else
				return e.getMessage();
		}
		
		return Constants.SUCCESS;
	}
	
	@POST
	@Path("/crime/updateleads")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String updateCrimeLeads(String postData){
		JSONObject postJson=null;
		Document result=null;
		try{
			postJson = new JSONObject(postData);
			DatabaseService databaseService = new DatabaseService();
			databaseService.setOption(5);
			databaseService.setJson(postJson);
			task = service.submit(databaseService);
			result = task.get();
		} catch(JSONException e){
			return Constants.JSON_PARSE_ERROR;
		} catch (JSONParseException e) {
			return Constants.JSON_PARSE_ERROR;
		} catch(Exception e){
			if(e.getMessage().contains("Crime Record Not Found"))
				return Constants.CRIME_NOT_FOUND;
			else if(e.getMessage().contains("User Not Found")){
				return Constants.USER_NOT_FOUND;
			}else
				return e.getMessage();
		}
		
		return Constants.SUCCESS;
	}
	
}
