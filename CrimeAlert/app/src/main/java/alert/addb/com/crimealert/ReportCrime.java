package alert.addb.com.crimealert;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class ReportCrime extends AppCompatActivity{
    RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<CardContent> cardContent=new ArrayList<>();
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_CRIME_ID,
            MySQLiteHelper.COLUMN_CRIME_TYPE,
            MySQLiteHelper.COLUMN_CRIME_DESCRIPTION,
            MySQLiteHelper.COLUMN_EMAIL,MySQLiteHelper.COLUMN_LATITUDE,
            MySQLiteHelper.COLUMN_LONGITUDE,MySQLiteHelper.COLUMN_TIMESTAMP};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_crime);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        mRecyclerView = (RecyclerView) findViewById(R.id.cardrecycle);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList<CardContent>response=new ArrayList<>();

        dbHelper = new MySQLiteHelper(this);
        database = dbHelper.getWritableDatabase();
        Cursor cursor1 = database.query(MySQLiteHelper.TABLE_NAME,
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

            CardContent c=new CardContent();
            c.setCrime_id(c_id);
            c.setCrime_description("Description: \""+c_email+"\"");
            c.setCrime_time(c_time);
            c.setCrime_type(c_type);
            c.setCrime_location("lat :"+c_lat+" lon:"+c_lon);
            response.add(c);


            Log.i("db test", "crimeID: "+c_id+" crime type: "+c_type
                    +" crime decrip "+c_description+" email "+c_email+" lat "+c_lat+
                    " lon "+c_lon+" time "+c_time);
            cursor1.moveToNext();



        }
        cursor1.close();
        //sort the ressponse on timestamp
        mAdapter = new CardAdapter(response);
       // mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }
    public void reportNewCrime(View view){
        //call reportNexCrime Activity
        Intent i=new Intent(this,ReportNewCrime.class);
        startActivity(i);
    }

//    @Override
//    public void itemClicked(View view, int position) {
//
//    }
}
