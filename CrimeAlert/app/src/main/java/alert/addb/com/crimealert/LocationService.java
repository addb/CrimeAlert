package alert.addb.com.crimealert;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.opengl.GLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by ADDB Inc on 05-06-2016.
 */
public class LocationService extends Service {
    private double latitude,longitude;
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 1f;
    String urlIP="http://192.168.0.20:8180/";
    String json=null,response="",id="";
    int serverPingCount=0;
    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            String p=location.getProvider();
            latitude=location.getLatitude();
            longitude=location.getLongitude();
            double speed=location.getSpeed();

            /*Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();*/
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            Date dateobj = new Date();
            String ts=df.format(dateobj);
            //Toast.makeText(getApplication(), ts,
              //      Toast.LENGTH_LONG).show();
            JSONObject locObject =new JSONObject(); // to hold lat lon in json object
            try {
                locObject.put("Speed",speed);
                locObject.put("Latitude",latitude);
                locObject.put("Longitude",longitude);
                locObject.put("Timestamp",ts);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject jObject = new JSONObject();// obj to hold respone to server

            final SharedPreferences mSharedPreference= getApplicationContext().getSharedPreferences("MyPREFERENCES",Context.MODE_PRIVATE);
            String email=(mSharedPreference.getString("mEmail", ""));
            JSONArray locArray = new JSONArray(); //array of locations
            locArray.put(locObject);

            urlIP=getApplicationContext().getResources().getString(R.string.urlIP2);
            try {
                jObject.put("Type","User Distress");
                //jObject.put("Approved",false);
                jObject.put("Email",email);
//                jObject.put("Latitude",latitude);
//                jObject.put("Longitude",longitude);
                jObject.put("Location",locArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            json = jObject.toString();
            Log.d("sharedPref", "message to server "+json);
            Log.d("SOS", "onLocationChanged: lat"+latitude+" lon "+longitude+" speed "+speed);
            //Log.e(TAG, "onLocationChanged:" + location);
            mLastLocation.set(location);
            if(serverPingCount==0) {
                try {
                    //response = new JSONAsyncTask("http://192.168.0.20:8180/CrimeAlertMiddleware/notify/distress", json).execute().get();
                    response = new JSONAsyncTask(urlIP+"CrimeAlertMiddleware/notify/distress", json).execute().get();
//                    if(response.equals("200"))
//                        Log.d("sosInit", "New Crime Record (User Distress) created!");
//                    else
//                        Log.d("sosInit", "Something went wrong while creating crime record!!!");
                    JSONObject SOSresponse= null;
                    try {
                        SOSresponse = new JSONObject(response);
                        JSONObject oid= new JSONObject(SOSresponse.get("_id").toString());
                                id=oid.get("$oid").toString();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("SOS", "onSOSResponse: "+id);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                serverPingCount++;

            }
            else   //serverPingCount >0 i.e for continuous update of location on location change
            {
                JSONObject updateLocationObj=new JSONObject();
                String Soid="";
                try {
//                    updateLocationObj.put("Latitude", latitude);
//                    updateLocationObj.put("Longitude", longitude);
                    updateLocationObj.put("Location",locObject);
                    updateLocationObj.put("_id",id);
                    json=updateLocationObj.toString();
                    Log.d(TAG, "send data on movement"+json);
                    //response = new JSONAsyncTask("http://192.168.0.20:8180/CrimeAlertMiddleware/crime/updatelocation", json).execute().get();  // semds to update lcoation
                    response = new JSONAsyncTask(urlIP+"CrimeAlertMiddleware/crime/updatelocation", json).execute().get();  // semds to update lcoation
                    JSONObject SOSresponse= null;
//                    try {
//                        //SOSresponse = new JSONObject(response);
//                         //Soid=SOSresponse.get("_id").toString();
//                        //id=oid.get("$oid").toString();
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                    if(response.equals("200"))
                    Log.d("SOS", "onSOSResponse: sending update to server");
                    else
                        Log.d("SOS", "onLocationChanged: SOMETHING went WRONG!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                serverPingCount++;
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
        Log.d(TAG, "completely distroyed!");
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
//    double longitude, latitude;
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//    @Override
//    public void onStart(Intent intent, int startId) {
//        // TODO Auto-generated method stub
//
//        Log.d("Myloc", "gotLocation: Inside Service");
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable(){
//            @Override
//            public void run(){
//                Log.d("loc", "run: runiing w/o locations");
//                MyLocation my=new MyLocation(LocationService.this);
//                Log.d("loc", "run: runiing w/o locations");
//                MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
//                    @Override
//                    public void gotLocation(Location location) {
//                        //Got the location!
//
//                        longitude = location.getLongitude();
//                        latitude = location.getLatitude();
//                        Log.d("Myloc", "gotLocation: "+latitude+" "+longitude);
//                    }
//                };
//                MyLocation myLocation = new MyLocation(LocationService.this);
//                myLocation.getLocation(LocationService.this, locationResult);
//
//            }
//        }, 2000);
//        /*Timer timer = new Timer();
//        timer.schedule( new GetLocation(LocationService.this), 0, 8000);*/
//
//    }
//
//    @Override
//    public void onDestroy() {
//        // TODO Auto-generated method stub
//        super.onDestroy();
//    }
//
//
//
//
//
//
//
//
//   /* private final static int INTERVAL = 10 ; //2 minutes
//    Handler mHandler = new Handler();
//
//    Runnable mHandlerTask = new Runnable()
//    {
//        double longitude, latitude;
//        @Override
//        public void run() {
//            /////
//            Log.d("loc", "run: runiing w/o locations");
//            MyLocation my=new MyLocation(LocationService.this);
//            Log.d("loc", "run: runiing w/o locations");
//            MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
//                @Override
//                public void gotLocation(Location location) {
//                    //Got the location!
//
//                    longitude = location.getLongitude();
//                    latitude = location.getLatitude();
//                    Log.d("Myloc", "gotLocation: "+latitude+" "+longitude);
//                }
//            };
//            MyLocation myLocation = new MyLocation(LocationService.this);
//            myLocation.getLocation(LocationService.this, locationResult);
//
//
//            ///
//            mHandler.postDelayed(mHandlerTask, INTERVAL);
//        }
//
//
//    void startRepeatingTask()
//    {
//        mHandlerTask.run();
//    }
//
//    void stopRepeatingTask()
//    {
//        mHandler.removeCallbacks(mHandlerTask);
//    }
//    };*/
//   /* class GetLocation extends TimerTask {
//
//        double longitude, latitude;
//        private Context context;
//
//        public GetLocation(Context context){
//            this.context=context;
//        }
//
//        public void run() {
//            //get the location
//            //Log.d("loc", "run: Running but without location");
//            MyLocation my=new MyLocation(context);
//            MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
//                @Override
//                public void gotLocation(Location location) {
//                    //Got the location!
//
//                    longitude = location.getLongitude();
//                    latitude = location.getLatitude();
//                    Log.d("Myloc", "gotLocation: "+latitude+" "+longitude);
//                }
//            };
//            MyLocation myLocation = new MyLocation(context);
//            myLocation.getLocation(context, locationResult);
//        }
//    };*/

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
