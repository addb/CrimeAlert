package alert.addb.com.crimealert;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public  void viewCrime(View view){
        Intent i=new Intent(this,ReportCrime.class);
        startActivity(i);
    }
    //****************Method to hande Report button click
    public void sendReport(View view){
        Intent i=new Intent(this,ReportNewCrime.class);
        startActivity(i);
    }

    //****************Method to hande Report button click
    public void sendSOS(View view){
        startService(new Intent(this,LocationService.class));
        Toast.makeText(getApplication(), "Location Tracker is On!",
                Toast.LENGTH_LONG).show();
    }
    public void stopSOS(View view){
        stopService(new Intent(this,LocationService.class));
        Toast.makeText(getApplication(), "Location Tracker is Off!",
                Toast.LENGTH_LONG).show();
    }
}
