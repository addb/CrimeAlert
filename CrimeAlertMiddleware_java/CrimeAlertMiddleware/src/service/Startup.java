package service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class Startup implements ServletContextListener{
	static MongoClientURI connectionString;
	static MongoClient mongoClient;
	static MongoDatabase crimeDB;
	
	public void connectDB(){
		try{
			connectionString = new MongoClientURI(Constants.MONGO_URI);
			mongoClient = new MongoClient(connectionString);
			crimeDB = mongoClient.getDatabase(Constants.MONGO_DB);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public static MongoClientURI getConnectionString() {
		return connectionString;
	}

	public static MongoClient getMongoClient() {
		return mongoClient;
	}

	public static MongoDatabase getCrimeDB() {
		return crimeDB;
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		connectDB();
	}

}
