package f1nd.initial.bharath.osdictdemo1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Iterator;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean isWordOfDay = intent.getBooleanExtra("wordOfTheDay",false);
        if(isWordOfDay){
            Toast.makeText(context, "Word of the day changed.....", Toast.LENGTH_SHORT).show();
            JSONObject wordOfTheDay = new JSONObject();
            final data d = new data(context);
            wordOfTheDay = d.getWordOfTheDay();
            SharedPreferences prefs  = context.getSharedPreferences("com.example.bharath.notify", context.MODE_PRIVATE);
            prefs.edit().putString("WOD", wordOfTheDay.toString()).commit();
            Notification.Builder notif;
            NotificationManager nm;
            nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            notif = new Notification.Builder(context);
            Iterator<String> keys = wordOfTheDay.keys();
            String str_Name=keys.next();
            String value = wordOfTheDay.optString(str_Name);
            notif.setContentTitle(str_Name);
            notif.setContentText(value);
            notif.setSmallIcon(R.drawable.back_dialog);
            Intent yesReceive = new Intent();
            yesReceive.setAction("Pause");
            PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
            notif.addAction(R.drawable.back_dialog, "Pause", pendingIntentYes);
            //r.setOnClickPendingIntent(R.id.p,pendingIntentYes);


            Intent yesReceive2 = new Intent();
            yesReceive2.setAction("Stop");
            PendingIntent pendingIntentYes2 = PendingIntent.getBroadcast(context, 12345, yesReceive2, PendingIntent.FLAG_UPDATE_CURRENT);
            notif.addAction(R.drawable.back_dialog, "Stop", pendingIntentYes2);
            //r.setOnClickPendingIntent(R.id.s,pendingIntentYes2);

            Intent yesReceive3 = new Intent();
            yesReceive3.setAction("Search");
            PendingIntent pendingIntentYes3 = PendingIntent.getBroadcast(context, 12345, yesReceive3, PendingIntent.FLAG_UPDATE_CURRENT);
            notif.addAction(R.drawable.back_dialog, "Search", pendingIntentYes3);

            nm.notify(prefs.getInt("noificationID",0), notif.getNotification());
        }
        if ("Pause".equals(action)) {
            Toast.makeText(context, "Paused", Toast.LENGTH_SHORT).show();
        }
        else  if ("Stop".equals(action)) {
            Toast.makeText(context, "Stopped", Toast.LENGTH_SHORT).show();
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
            localBroadcastManager.sendBroadcast(new Intent("com.OSdict.action.close"));
            NotificationManager nm;
            nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            nm.cancelAll();
            Intent myService = new Intent(context, serviceHandler.class);
            context.stopService(myService);
        }
        else  if ("Search".equals(action)) {
            Toast.makeText(context, "Searching", Toast.LENGTH_SHORT).show();
        }
    }
}
