package alert.addb.com.crimealert;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ADDB Inc on 08-06-2016.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "crime_record";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CRIME_ID = "crime_id";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_CRIME_TYPE = "crime_type";
    public static final String COLUMN_EMAIL = "reporter_email";
    public static final String COLUMN_CRIME_DESCRIPTION = "crime_description";

    public static final String COLUMN_LATITUDE = "crime_lat";
    public static final String COLUMN_LONGITUDE = "crime_lon";
    public static final String COLUMN_TIMESTAMP = "crime_timestamp";



    private static final String DATABASE_NAME = "crime.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CRIME_ID
            + " text  null, "+ COLUMN_CRIME_TYPE +
            "          text not null,"+COLUMN_EMAIL +
            "          text not null,"+COLUMN_CRIME_DESCRIPTION +
            "          text not null,"+COLUMN_LATITUDE +
            "          text not null,"+COLUMN_LONGITUDE +
            "          text not null,"+COLUMN_TIMESTAMP +
            "          text not null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME );
        onCreate(db);
    }

}
