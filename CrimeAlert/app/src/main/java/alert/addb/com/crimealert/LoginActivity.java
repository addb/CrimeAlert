package alert.addb.com.crimealert;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.suitebuilder.TestMethod;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object> {
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private TextView mStatusText;
    private View mProgressView;
    private View mLoginFormView;
    SharedPreferences mPref;
    String urlIP="http://192.168.0.20:8180/";
    public static final String MyPREFERENCES = "MyPrefs" ;
    String response="";
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int REQUEST_ACCESS_LOCATION=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        urlIP=this.getResources().getString(R.string.urlIP2);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email); // email of the user
        populateAutoComplete();
        mStatusText = (TextView) findViewById(R.id.status);
        mPasswordView = (EditText) findViewById(R.id.password); // password
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mPref = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        if(mPref.getBoolean("mloginStatus",false)){
            //start main acitvity
            Log.d("login", "Login true");
            startMain();
        }
        else{
            Log.d("login", "Login False ->"+mPref.getBoolean("mloginStatus",false));
        }
    }
    public void login(View view){
        try {
            loginUser();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null,this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            /*Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });*/
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }
    private boolean mayRequestLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED  ) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
           /* Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
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
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void loginUser() throws JSONException, ExecutionException, InterruptedException {

        // Store values at the time of the registration attempt
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        String json = "",json1="";
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
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

        else
        {



            // Generating JSON Object to send it to server
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("Email",email);
            //jsonObject.accumulate("Role","user");
            jsonObject.put("Password",password);

            json = jsonObject.toString();

            /*JSONObject jsonO = new JSONObject();
            jsonO.accumulate("Name","Soumya PAGAL");
            jsonO.accumulate("Role","normal user");
            jsonO.accumulate("Location","Ghatkopar");
            // 4. convert JSONObject to JSON to String*/

            //json1=jsonO.toString();


        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();

        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
           // mStatusText.setText(json);
            //response = new JSONAsyncTask("http://192.168.0.20:8180/CrimeAlertMiddleware/user/login", json).execute().get();
            response = new JSONAsyncTask(urlIP+"CrimeAlertMiddleware/user/login", json).execute().get();
            /*if(email.equals("a@a")&&password.equals("aaaaa"))*/
            if(response.equals(""))
                mStatusText.setText("Server Error!");
            else if(response.equals("620"))
                mStatusText.setText("password or username error!");
            else if(response.equals("200")) {

                //mPref = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);// shifted to oncreate
                SharedPreferences.Editor editor = mPref.edit();
                editor.putBoolean("MloginStatus",true);
                editor.putString("mEmail",email);
                editor.commit();
                startMain();

            }
            else
            {
                mStatusText.setText("Something Went Wrong!");
            }
           //set text view to confirm registeration works  fine

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

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {

    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }

    public void displayMessage(String mes){

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
    public void startMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void register(View view){
        Intent i=new Intent(this,RegisterActivity.class);
        startActivity(i);
    }
}
