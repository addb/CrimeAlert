package service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSONParseException;

import exceptions.BroadcastFailedException;
import exceptions.NoSuchCrimeException;
import exceptions.NoSuchUserException;
import exceptions.UserExistsException;

public class DatabaseService implements Callable<Document>{
	MongoDatabase crimeDB;
	MongoCollection<Document> mongoCollection;
	int option; // decides which function to call
	JSONObject postJson;
	Object id;
	
	public DatabaseService(){
		crimeDB = Startup.getCrimeDB();
	}
	public void setOption(int val){
		this.option = val;
	}
	public void setJson(JSONObject postJson){
		this.postJson = postJson;
	}
	public Document findUser() throws NoSuchUserException, JSONException{
		try{
			System.out.println("find user");
			mongoCollection = crimeDB.getCollection(Constants.MONGO_USER_COLLECTION);
			FindIterable<Document> entry = mongoCollection.find(Filters.and(Filters.eq("Email",postJson.get("Email")),Filters.eq("Password",postJson.get("Password")))).limit(1);
			if(entry.first()!=null){
				return entry.first();
			} else {
				throw new NoSuchUserException("User Not Found");
			}
		} catch(NoSuchUserException e){
			throw e;
		} catch (JSONException e) {
			throw e;
		}
	}
	public Document saveUser() throws JSONParseException, JSONException, UserExistsException{
		try{
			System.out.println("save user");
			//JSONArray point = new JSONArray("["+postJson.get("Latitude")+","+postJson.get("Longitude")+"]");
			mongoCollection = crimeDB.getCollection(Constants.MONGO_USER_COLLECTION);
			Document user = new Document("Name",postJson.get("Name"))
							.append("Role", postJson.get("Role"))
							.append("Email", postJson.get("Email"))
							.append("Password", postJson.get("Password"))
							.append("Current_Location_Address", postJson.get("Street")+" "+postJson.get("City")+" "+postJson.get("Country"))
							.append("Current_Street", postJson.get("Street"))
							.append("Current_City", postJson.get("City"))
							.append("Current_Country", postJson.get("Country"))
							.append("Latitude", postJson.get("Latitude").toString())
							.append("Longitude", postJson.get("Longitude").toString())
							.append("GCMToken", postJson.get("GCMToken"));
			
			//check if user already exists
			FindIterable<Document> entry = mongoCollection.find(Filters.eq("Email",postJson.get("Email"))).limit(1);
			//if not create user
			if(entry.first()==null){
				mongoCollection.insertOne(user);
				return user;
			} else {
				throw new UserExistsException("User already exists");
			}
			
		} catch (UserExistsException e){
			throw e;
		} catch (JSONParseException e){
			throw e;
		} catch (JSONException e) {
			throw e;
		}
	}
	
	public Document createEmergency() throws JSONException,JSONParseException, NoSuchUserException, BroadcastFailedException, IOException{
		//crime record on user generated distress
		Document crimeEvent;
		try{
			System.out.println("create emergency");
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		    Date dateobj = new Date();
		    
			mongoCollection = crimeDB.getCollection(Constants.MONGO_CRIME_COLLECTION);
			JSONArray locations = (JSONArray) postJson.get("Location");
			ArrayList<Document> locationInfo = new ArrayList<Document>();
			for(int i = 0; i < locations.length(); i++){
				locationInfo.add(Document.parse(locations.getJSONObject(i).toString()));
			}
			crimeEvent = new Document("Type",postJson.get("Type"))
								.append("Priority",Constants.HIGH)
								.append("Email", postJson.get("Email"))
								.append("Location", locationInfo)
								.append("Timestamp", df.format(dateobj))
								.append("Approved", "true")
								.append("Description", "User generated distress call");								
			
			//check if user exists
			MongoCollection<Document>userCollection = crimeDB.getCollection(Constants.MONGO_USER_COLLECTION);
			FindIterable<Document> entry = userCollection.find(Filters.eq("Email",postJson.get("Email"))).limit(1);
			//append user info
			if(entry.first()!=null){
				entry.forEach(new Block<Document>() {
				    @Override
				    public void apply(Document document) {
				    	id =document.get("_id");
				    	System.out.println("id "+id.toString());
				    }
				});
				crimeEvent.append("Victim", id);				
			} else {
				throw new NoSuchUserException("User Not Found");
			}
			System.out.println("Crime event "+crimeEvent.toJson());
			mongoCollection.insertOne(crimeEvent);			
		    
			//connect to second middleware get users in vicinity
			HTTPConnector httpConnector = new HTTPConnector();
			List<String> deviceIds= httpConnector.sendPost(crimeEvent);
			
			if(!deviceIds.isEmpty()){
				GCMBroadcast gcmBroadcast = new GCMBroadcast();
				gcmBroadcast.connect(deviceIds, crimeEvent);
			}
			
		} catch (JSONParseException e){
			throw e;
		} catch (JSONException e) {
			throw e;
		} catch (BroadcastFailedException e){
			throw e;
		} 
		
		return crimeEvent;
	}
	
	public Document updateEmergencyLocation() throws NoSuchCrimeException, JSONException{
		try{
			System.out.println("update crime location");
			mongoCollection = crimeDB.getCollection(Constants.MONGO_CRIME_COLLECTION);
			JSONObject locationEntry = new JSONObject(postJson.get("Location").toString());
			final ObjectId id = new ObjectId(postJson.get("_id").toString());
			final Document location = new Document().append("Latitude",locationEntry.get("Latitude"))
											  .append("Longitude", locationEntry.get("Longitude"))
											  .append("Timestamp", locationEntry.get("Timestamp"))
											  .append("Speed", locationEntry.get("Speed"));
			
			FindIterable<Document> entry = mongoCollection.find(Filters.eq("_id",id)).limit(1);
			if(entry.first()!=null){
				entry.forEach(new Block<Document>() {
				    @Override
				    public void apply(final Document document) {
				    	@SuppressWarnings("unchecked")
						ArrayList<Document> locations =(ArrayList<Document>) document.get("Location");
				    	locations.add(location);
				    	mongoCollection.findOneAndUpdate(Filters.eq("_id",id), new Document("$set",document.append("Location",locations)));  			    	
				    }
				});
				
				return location;
			} else {
				throw new NoSuchCrimeException("Crime Record Not Found");
			}
		} catch(NoSuchCrimeException e){
			throw e;
		} catch (JSONException e) {
			throw e;
		}
	}
	
	public Document updateEmergencyLeads() throws NoSuchCrimeException, NoSuchUserException, JSONException{
		try{
			System.out.println("update crime leads");
			mongoCollection = crimeDB.getCollection(Constants.MONGO_CRIME_COLLECTION);
			JSONObject locationEntry = new JSONObject(postJson.get("Lead").toString());
			final ObjectId id = new ObjectId(postJson.get("_id").toString());
			final Document lead = new Document().append("Description",locationEntry.get("Description"))
											    .append("Reporter Email", locationEntry.get("Email"))
											    .append("Timestamp", locationEntry.get("Timestamp"));
			
			MongoCollection<Document> userCollection = crimeDB.getCollection(Constants.MONGO_USER_COLLECTION);
			FindIterable<Document> userEntry = userCollection.find(Filters.eq("Email",locationEntry.get("Email"))).limit(1);
			if(userEntry.first()==null){
				throw new NoSuchUserException("User Not Found");
			}
			
			lead.append("Reporter ID", userEntry.first().get("_id"));
			
			FindIterable<Document> entry = mongoCollection.find(Filters.eq("_id",id)).limit(1);
			if(entry.first()!=null){
				entry.forEach(new Block<Document>() {
				    @Override
				    public void apply(final Document document) {
				    	@SuppressWarnings("unchecked")
						ArrayList<Document> leads =(ArrayList<Document>) document.get("Leads");
				    	if(leads==null)
				    		leads = new ArrayList<Document>();
				    	
				    	leads.add(lead);
				    	mongoCollection.findOneAndUpdate(Filters.eq("_id",id), new Document("$set",document.append("Leads",leads)));  			    	
				    }
				});
				
				return lead;
			} else {
				throw new NoSuchCrimeException("Crime Record Not Found");
			}
		} catch(NoSuchCrimeException e){
			throw e;
		} catch(NoSuchUserException e){
			throw e;
		}catch (JSONException e) {
			throw e;
		}	
	}
	
	public Document reportCrime() throws JSONException, NoSuchUserException, BroadcastFailedException, IOException{
		//crime record on 3rd person report
		Document crimeEvent;
		try{
			System.out.println("create crime report");
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		    Date dateobj = new Date();
		    
		    mongoCollection = crimeDB.getCollection(Constants.MONGO_CRIME_COLLECTION);
			JSONArray locations = (JSONArray) postJson.get("Location");
			ArrayList<Document> locationInfo = new ArrayList<Document>();
			for(int i = 0; i < locations.length(); i++){
				locationInfo.add(Document.parse(locations.getJSONObject(i).toString()));
			}
			crimeEvent = new Document("Type",postJson.get("Type"))
								.append("Priority",Constants.MODERATE)
								.append("Email", postJson.get("Email"))
								.append("Location", locationInfo)
								.append("Timestamp", df.format(dateobj))
								.append("Approved", "false")
								.append("Description", postJson.get("Description"));								
			
			//check if user exists
			MongoCollection<Document>userCollection = crimeDB.getCollection(Constants.MONGO_USER_COLLECTION);
			FindIterable<Document> entry = userCollection.find(Filters.eq("Email",postJson.get("Email"))).limit(1);
			//append user info
			if(entry.first()!=null){
				entry.forEach(new Block<Document>() {
				    @Override
				    public void apply(Document document) {
				    	id =document.get("_id");
				    	System.out.println("id "+id.toString());
				    }
				});
				crimeEvent.append("Reporter", id);				
			} else {
				throw new NoSuchUserException("User Not Found");
			}
			System.out.println("Crime event "+crimeEvent.toJson());
			mongoCollection.insertOne(crimeEvent);		
			
			//connect to second middleware get users in vicinity
			HTTPConnector httpConnector = new HTTPConnector();
			List<String> deviceIds= httpConnector.sendPost(crimeEvent);
			
			if(!deviceIds.isEmpty()){
				GCMBroadcast gcmBroadcast = new GCMBroadcast();
				gcmBroadcast.connect(deviceIds, crimeEvent);
			}
						
		} catch (JSONParseException e){
			throw e;
		} catch (JSONException e) {
			throw e;
		} catch (NoSuchUserException e) {
			throw e;
		}catch(BroadcastFailedException e){
			throw e;
		}catch (IOException e) {
			throw e;
		}
		
		return crimeEvent;
	}
	
	@Override
	public Document call() throws Exception{
		switch (this.option) {
		case 1:	return saveUser();				
		case 2: return findUser();				
		case 3: return createEmergency();
		case 4: return updateEmergencyLocation();
		case 5: return updateEmergencyLeads();
		case 6: return reportCrime();
		default:
			break;
		}
		return null;
	}
}
