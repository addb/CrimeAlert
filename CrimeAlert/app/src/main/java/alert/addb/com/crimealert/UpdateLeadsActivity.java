package alert.addb.com.crimealert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class UpdateLeadsActivity extends AppCompatActivity {
    TextView loc,type,status;
    EditText content;
    String crimetype,crimeid;
    double latitude,longitude;
    String json="";
    String crime_type="",response="";
    String urlIP="http://192.168.0.20:8180/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_leads);
        //loc=(TextView)findViewById(R.id.cLocation);
        //type=(TextView)findViewById(R.id.ctype);
        status=(TextView)findViewById(R.id.status);
        content=(EditText)findViewById(R.id.crime_update);

        urlIP=this.getResources().getString(R.string.urlIP2);//*************
        Intent intent = getIntent();
        crimeid=intent.getStringExtra("crimeid");
        crime_type=intent.getStringExtra("type");
        //type.setText(crime_type);
        //loc.setText(crimeid);
        Log.d("cid", "onCreate: "+crimeid);
        MyLocation my = new MyLocation(this);
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                //Got the location!
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

        };
        MyLocation myLocation = new MyLocation(UpdateLeadsActivity.this);
        myLocation.getLocation(this, locationResult);
    }
    public void updateLead(View view) throws ExecutionException, InterruptedException {
        String crime_update_message=content.getText().toString();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
        Date dateobj = new Date();
        String ts = df.format(dateobj);

        //Toast.makeText(getApplication(), ts,
         //       Toast.LENGTH_LONG).show();

        JSONObject locObject = new JSONObject(); // to hold lat lon in json object
        try {
            locObject.put("Latitude", latitude);
            locObject.put("Longitude", longitude);
            locObject.put("Timestamp", ts);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jObject = new JSONObject();// obj to hold respone to server
        JSONObject leadObject=new JSONObject();
        final SharedPreferences mSharedPreference = getApplicationContext().getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        String email = (mSharedPreference.getString("mEmail", ""));
        JSONArray locArray = new JSONArray(); //array of locations
        locArray.put(locObject);

        try {
            //jObject.put("Type", crime_type);
            //jObject.put("Approved",false);
            jObject.put("Timestamp",ts);
            jObject.put("Email", email);
            jObject.put("Description",crime_update_message);
            leadObject.put("_id",crimeid);
             leadObject.put("Lead",jObject.toString());// = new JSONObject(jObject.toString());
            //jObject.put("Location", locArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        json = leadObject.toString();
        Log.d("sharedPref", "message to server " + json);
        Log.d("ReportCrime", "onLocationChanged: lat" + latitude + " lon " + longitude);
        response = new JSONAsyncTask(urlIP + "CrimeAlertMiddleware/crime/updateleads", json).execute().get();
        if (response.equals(""))
            status.setText("Server Error!");
        else if (response.equals("615"))
            status.setText("Something Went wrong!");
        else if (response.equals("200")) {
            status.setText("Leads Updated");
        }
        else {
            status.setText("Something terribly went wrong!");
        }

    }
    class JSONAsyncTask extends AsyncTask<Void, Void, String> {
        String json, uri;String responseString = null;

        public JSONAsyncTask(String uri, String json) {
            this.json = json;
            this.uri = uri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("AsyncTask STARTED!!!!!!!!!!!" + " data sent to " + uri);
        }

        @Override
        protected String doInBackground(Void... urls) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            //String responseString = null;
            try {
                HttpPost httpPost = new HttpPost(uri);
                StringEntity se=new StringEntity(json);
                httpPost.setEntity(se);
                httpPost.setHeader("Content-type", "application/json");
                response=httpclient.execute(httpPost);//
                StatusLine statusLine = response.getStatusLine();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                Log.d("response", "doInBackground: "+responseString);

                if(responseString.equals("exceptions.NoSuchUserException: User Not Found"))
                    Log.d("login", "doInBackground: Error in login");

                return responseString;

            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return "";
                //TODO Handle problems..
            } catch (IOException e) {
                e.printStackTrace();
                return "";
                //TODO Handle problems..
            }

        }



        @Override
        protected void onPostExecute(String responseString) {
            super.onPostExecute(responseString);

            Log.d("reponse","Message sent to server!");
        }

    }
}
