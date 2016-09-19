package service;

public class Constants {
	public static final String MONGO_URI = "mongodb://crime_UCI:nocrime123@ds025439.mlab.com:25439/crime_alert_db";
	public static final String MONGO_DB = "crime_alert_db";
	public static final String MONGO_USER_COLLECTION = "User";
	public static final String MONGO_CRIME_COLLECTION = "CrimeEvent";
	
	//Server Response Codes
	public static final String SUCCESS = "200";
	public static final String SERVER_ERROR = "500";
	public static final String CLIENT_ERROR = "600";
	public static final String JSON_PARSE_ERROR = "610";
	public static final String USER_EXISTS_ERROR = "615";
	public static final String USER_NOT_FOUND = "620";
	public static final String CRIME_NOT_FOUND = "622";
	public static final String BROADCAST_FAILURE = "624";
	public static final String MCONNECT_FAILURE = "626";
	
	//Priority levels
	public static final String CRITICAL = "Critical";
	public static final String HIGH = "High";
	public static final String MODERATE = "Moderate";
	public static final String LOW = "Low";
	
	//GCM
	public static final String SENDER_ID = "AIzaSyDZM8oXfkAg7V27xreRjIc_hk-9O7EtkQ4";
	
	//Middleware Connection
	public static final String MIDDLEWARE2 = "http://192.168.0.26:5000";
	public static final String MIDDLEWARE2_UCI = "http://169.234.4.255:5000";
	
}
