package f1nd.initial.bharath.osdictdemo1;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    static Resources res;
    static String dbPath,dbName;
    SharedPreferences prefs = null;

    public static LocalBroadcastManager mLocalBroadcastManager;
    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.OSdict.action.close")){
                finish();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        res = getResources();
        prefs = getSharedPreferences("com.example.bharath.notify", MODE_PRIVATE);
        //Word of the day feature...
        if(prefs.getBoolean("firstrun", true)){
            //Requesting permissions....
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            JSONObject wordOfTheDay = new JSONObject();
            //wordOfTheDay = d.getWordOfTheDay();
            try {
                wordOfTheDay.put("Barbieee","She was one of a KIND <3, loads of love");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            prefs.edit().putBoolean("firstrun", false).commit();
            prefs.edit().putString("WOD", wordOfTheDay.toString()).commit();
        }else{
            startService();
        }
        createNotification();
        scheduleVerseNotificationService(getApplicationContext());
    }

    public void startService(){
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.OSdict.action.close");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        Intent i = new Intent(MainActivity.this,serviceHandler.class);
        startService(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getPermissions(){
        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"Permisions granted :-)",Toast.LENGTH_SHORT).show();
                    getPermissions();
                    try {
                        dbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
                        dbName = "dict";
                        copyDataBase();
                        startService();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                }
                return;
            }
        }
    }


    static void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput;
        myInput = res.openRawResource(R.raw.dict);
        // Path to the just created empty db
        String outFileName = dbPath + dbName;
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void createNotification() {
        Notification.Builder notif;
        NotificationManager nm;
        notif = new Notification.Builder(getApplicationContext());
        notif.setColor(getApplicationContext().getResources().getColor(R.color.colorPrimary));
        notif.setSmallIcon(R.drawable.back_dialog);

        String wod = prefs.getString("WOD","Hi");
        JSONObject tWOD = null;
        try {
            tWOD = new JSONObject(wod);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Iterator<String> keys = tWOD.keys();
        String str_Name=keys.next();
        String value = tWOD.optString(str_Name);
        notif.setContentTitle(str_Name);
        notif.setContentText(value);

        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notif.setSound(path);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent yesReceive = new Intent();
        yesReceive.setAction("Pause");
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.addAction(R.drawable.back_dialog, "Pause", pendingIntentYes);


        Intent yesReceive2 = new Intent();
        yesReceive2.setAction("Stop");
        PendingIntent pendingIntentYes2 = PendingIntent.getBroadcast(this, 12345, yesReceive2, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.addAction(R.drawable.back_dialog, "Stop", pendingIntentYes2);

        Intent yesReceive3 = new Intent();
        yesReceive3.setAction("Search");
        PendingIntent pendingIntentYes3 = PendingIntent.getBroadcast(this, 12345, yesReceive3, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.addAction(R.drawable.back_dialog, "Search", pendingIntentYes3);

        int notificationID = getTaskId();
        prefs.edit().putInt("noificationID", notificationID).commit();
        nm.notify(notificationID, notif.getNotification());

    }

    public static void scheduleVerseNotificationService(Context mContext) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext,NotificationReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("wordOfTheDay",true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // reset previous pending intent
        alarmManager.cancel(pendingIntent);

        // Set the alarm to start at approximately 08:00 morning.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 26);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.AM_PM,Calendar.AM);

        // if the scheduler date is passed, move scheduler time to tomorrow
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Log.d("Database22", "Elapsed for today..." + calendar.getTimeInMillis());
        }

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

        Log.d("Database22", "" + calendar.getTimeInMillis());
    }
}
