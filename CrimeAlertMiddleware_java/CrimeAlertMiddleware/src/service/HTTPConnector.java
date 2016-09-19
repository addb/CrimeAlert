package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

public class HTTPConnector {

	public List<String> sendPost(Document crimeEvent) {
		List<String> deviceIds = new ArrayList<String>();

		try{
			URL obj = new URL(Constants.MIDDLEWARE2_UCI);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setDoOutput(true);
			OutputStreamWriter os = new OutputStreamWriter(con.getOutputStream());
			os.write(crimeEvent.toJson());
			os.flush();
			os.close();
			int responseCode = con.getResponseCode();
			System.out.println("POST Response Code :: " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) { //success
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				String [] ids = response.toString().split(",");
				deviceIds = Arrays.asList(ids);
				//deviceIds = Arrays.asList(response.toString());							
			} 
		} catch(Exception e){
			e.printStackTrace();
			
		}

		return deviceIds;
	}
}
