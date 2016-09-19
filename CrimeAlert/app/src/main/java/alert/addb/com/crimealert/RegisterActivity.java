package alert.addb.com.crimealert;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
//import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_ACCESS_LOCATION = 0;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mName;
    private EditText mPasswordView;
    private EditText mVerifyPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mStatusText;
    double latitude;
    double longitude;
    String street, city, country;
    String location_name;
    String response="";
    String urlIP;
    //String urlIP="http://192.168.0.20:8180/";
   // String urlIP="http://169.234.32.56:8180/";
   // String urlIP=this.getResources().getString(R.string.urlIP2);

    //gcm
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "118099300070";
    static final String TAG = "L2C";
    GoogleCloudMessaging gcm;
    SharedPreferences mPref;
    Context context;
    String regid;

    // These strings are hopefully self-explanatory
    private String registrationStatus = "Not yet registered";
    private String broadcastMessage = "No broadcast message";
    String gcmToken; // to send the token to the server
    TextView tvBroadcastMessage;
    TextView tvRegStatusResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        urlIP=this.getResources().getString(R.string.urlIP2);

        // Set up the login form.
        mName = (EditText) findViewById(R.id.name); // name of the user
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email); // email of the user

        mPasswordView = (EditText) findViewById(R.id.password); // password
        mVerifyPasswordView = (EditText) findViewById(R.id.verifyPassword); // verify password

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mStatusText = (TextView) findViewById(R.id.status);
        tvBroadcastMessage = (TextView) findViewById(R.id.tv_message);
        tvRegStatusResult = (TextView) findViewById(R.id.tv_reg_status_result);
        // request location permissions
        mayRequestLocation();

        //get the location
        MyLocation my = new MyLocation(this);
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                //Got the location!
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

        };
        MyLocation myLocation = new MyLocation(RegisterActivity.this);
        myLocation.getLocation(this, locationResult);
//        mRegisterButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                register();
//            }
//        });


        /// GCM

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check type of intent filter
                if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)){
                    //Registration success
                     gcmToken = intent.getStringExtra("token");
                    //Toast.makeText(getApplicationContext(), "GCM token:" + gcmToken, Toast.LENGTH_LONG).show();
                } else if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)){
                    //Registration error
                    Toast.makeText(getApplicationContext(), "GCM registration error!!!", Toast.LENGTH_LONG).show();
                } else {
                    //Tobe define
                }
            }
        };


        //Check status of Google play service in device
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if(ConnectionResult.SUCCESS != resultCode) {
            //Check type of error
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                //So notification
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }
        } else {
            //Start service
            Log.d("gcm", "onCreate: GCMREG started");
            Intent itent = new Intent(this, GCMRegistrationIntentService.class);
            startService(itent);

            Log.d("gcm", "onCreate: GCMREG started");
        }

        ////

        mLoginFormView = findViewById(R.id.registration_form);
        mProgressView = findViewById(R.id.registeration_progress);
    }

    public void register(View view) {
        try {
            registerUser();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }





    private boolean mayRequestLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            /*Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_LOCATION);
                        }
                    });*/
        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_LOCATION);
        }
        return false;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */

    private void registerUser() throws JSONException, ExecutionException, InterruptedException {

        // Store values at the time of the registration attempt.
        String name = mName.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String rePassword = mVerifyPasswordView.getText().toString();

        String json = "", json1 = "";
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            mName.setError(getString(R.string.error_empty_name_field));
            focusView = mName;
            cancel = true;

        }
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if (!TextUtils.isEmpty(rePassword) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mVerifyPasswordView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (!verifyPassword(password, rePassword)) {
            mPasswordView.setError(getString(R.string.error_verifying_password));
            mVerifyPasswordView.setError(getString(R.string.error_verifying_password));
            focusView = mPasswordView;
            cancel = true;
        } else {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                street = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getAddressLine(1);
                country = addresses.get(0).getAddressLine(2);
                //txt.setText(street + "  |  " + city + "  |  " + countryName);


            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject locObject=new JSONObject();
            locObject.put("Latitude",latitude);
            locObject.put("Longitude",longitude);

            JSONArray locArray=new JSONArray();
            locArray.put(locObject);
            // Generating JSON Object to send it to server
            JSONObject jsonObject = new JSONObject();
            try {

            jsonObject.put("Name", name);
            jsonObject.put("Email", email);
            jsonObject.put("Role", "user");
            jsonObject.put("Password", password);
//            jsonObject.put("Location",locArray);
            jsonObject.put("Latitude", latitude);
            jsonObject.put("Longitude", longitude);
            jsonObject.put("Street", street);
            jsonObject.put("City", city);
            jsonObject.put("Country", country);
            jsonObject.put("GCMToken",gcmToken);

            json = jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

            /*JSONObject jsonO = new JSONObject();
            jsonO.accumulate("Name","Soumyaa 1111");
            jsonO.accumulate("Role","normal user");
            //jsonO.accumulate("Location","Ghatkopar");
            // 4. convert JSONObject to JSON to String

            json1=jsonO.toString();*/


        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();

        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            //mStatusText.setText(json);
            Log.d(TAG, "request "+json);
            //response="200";
            response= new JSONAsyncTask(urlIP+"CrimeAlertMiddleware/user/register", json).execute().get();
            if(response.equals(""))
                mStatusText.setText("Server Error!");
            else if(response.equals("615"))
                mStatusText.setText("Account with this email id already exits!");
            else if(response.equals("200")){
//                if(checkPlayServices()){
//
//                    new RegisterGCM().execute();
//
//                }else{
//                    Toast.makeText(getApplicationContext(),"This device is not supported",Toast.LENGTH_SHORT).show();
//                }
                mStatusText.setText("Registered");
                Intent i=new Intent(this,LoginActivity.class);
                startActivity(i);

            }
            else{
                mStatusText.setText("Something Went Wrong!");
            }
        }


    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private boolean verifyPassword(String password, String rePassword) {
        if (password.equals(rePassword))
            return true;
        else
            return false;
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }




    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    class JSONAsyncTask extends AsyncTask<Void, Void, String> {
        String json, uri;

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
            String responseString = null;
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

                return responseString;
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
                e.printStackTrace();
                return  "";
            } catch (IOException e) {
                //TODO Handle problems..
                e.printStackTrace();
                return "";
            }

            //return null;
        }
           /* try{
                HttpPost httpPost = new HttpPost(uri);
                httpPost.setEntity(new StringEntity(json));
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                //ResponseHandler <String> resonseHandler = new BasicResponseHandler();
                //String response = new DefaultHttpClient().execute(httpPost, resonseHandler);
                //Log.i("HTTP Response", "doInBackground: "+response);
                return new DefaultHttpClient().execute(httpPost);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }*/


        @Override
        protected void onPostExecute(String responseString) {
            super.onPostExecute(responseString);
            Log.d("reponse","Message sent to server!");
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.w("Main REG Activity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("Main REG Activity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }
    /*private class RegisterGCM extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(RegisterActivity.this);
                    regid = gcm.register(SENDER_ID);
                    Log.d("RegId",regid);

                    mPref = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putString("REG_ID", regid);
                    editor.commit();

                }

                return  regid;

            } catch (IOException ex) {
                Log.e("Error", ex.getMessage());
                return "Fails";

            }
        }
        @Override
        protected void onPostExecute(String json) {
           super.onPostExecute(json);
        }
    }*/
}