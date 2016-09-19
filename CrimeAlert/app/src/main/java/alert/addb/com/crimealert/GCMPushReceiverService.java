package alert.addb.com.crimealert;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import org.json.JSONArray;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by NgocTri on 4/9/2016.
 */
public class GCMPushReceiverService extends GcmListenerService {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_CRIME_ID,
            MySQLiteHelper.COLUMN_CRIME_TYPE,
            MySQLiteHelper.COLUMN_EMAIL,
            MySQLiteHelper.COLUMN_CRIME_DESCRIPTION,
            MySQLiteHelper.COLUMN_LATITUDE,
            MySQLiteHelper.COLUMN_LONGITUDE,
            MySQLiteHelper.COLUMN_TIMESTAMP};

    String crime_type,crime_id,crime_description,email,crime_lat,crime_lon,crime_timestamp;
    String city,country,street;

    ArrayList<String> mList=new ArrayList<>();

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }
    @Override
    public void onMessageReceived(String from, Bundle data) {
//        String message = data.getString("message");
        try {
            JSONObject dbDetail=new JSONObject(data.get("message").toString());
            crime_id=(new JSONObject(dbDetail.get("_id").toString())).get("$oid").toString();
            crime_description=dbDetail.get("Description").toString();
            crime_type=dbDetail.get("Type").toString();
            email=dbDetail.get("Email").toString();
            JSONArray locArray=dbDetail.getJSONArray("Location");
            crime_lat=((new JSONObject(locArray.get(0).toString())).get("Latitude")).toString();
            crime_lon=((new JSONObject(locArray.get(0).toString())).get("Longitude")).toString();
            crime_timestamp=((new JSONObject(locArray.get(0).toString())).get("Timestamp")).toString();


        } catch (JSONException e) {
            e.printStackTrace();
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        double latitude=Double.parseDouble(crime_lat),longitude=Double.parseDouble(crime_lon);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            street = addresses.get(0).getAddressLine(0);
            city = addresses.get(0).getAddressLine(1);
            country = addresses.get(0).getAddressLine(2);
            //txt.setText(street + "  |  " + city + "  |  " + countryName);


        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.d("gcm", "onMessageReceived: "+crime_type+" at "+ street);
        sendNotification(crime_type+" at "+ street);
        //add to db
        dbHelper = new MySQLiteHelper(this);
        database = dbHelper.getWritableDatabase();
        //333database.delete(MySQLiteHelper.TABLE_NAME, null, null);
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CRIME_ID, crime_id);
        values.put(MySQLiteHelper.COLUMN_CRIME_TYPE, crime_type);
        values.put(MySQLiteHelper.COLUMN_EMAIL, email);
        values.put(MySQLiteHelper.COLUMN_CRIME_DESCRIPTION, crime_description);
        values.put(MySQLiteHelper.COLUMN_LATITUDE, crime_lat);
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, crime_lon);
        values.put(MySQLiteHelper.COLUMN_TIMESTAMP, crime_timestamp);

        long insertId = database.insert(MySQLiteHelper.TABLE_NAME, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_NAME,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        cursor.close();

        /*Cursor cursor1 = database.query(MySQLiteHelper.TABLE_NAME,
                allColumns, null, null, null, null, null);

        cursor1.moveToFirst();
        while (!cursor1.isAfterLast()) {
            String c_id = cursor1.getString(1);
            String c_type=cursor1.getString(2);
            String c_email=cursor1.getString(3);
            String c_description=cursor1.getString(4);

            String c_lat=cursor1.getString(5);
            String c_lon=cursor1.getString(6);
            String c_time=cursor1.getString(7);


            Log.i("db test", "crimeID: "+c_id+" crime type: "+c_type
            +" crime decrip "+c_description+" email "+c_email+" lat "+c_lat+
            " lon "+c_lon+" time "+c_time);
            cursor1.moveToNext();
        }
        cursor1.close();*/
    }
    private void sendNotification(String message) {
        Intent intent = new Intent(this, ReportCrime.class);
        intent.putExtra("message",message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = 0;//Your request code
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        //Setup notification
        //Sound
        //Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Build notification
        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Crime Alert!")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build()); //0 = ID of notification
    }

}
