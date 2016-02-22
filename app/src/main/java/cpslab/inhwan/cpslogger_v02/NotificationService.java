package cpslab.inhwan.cpslogger_v02;

/*
 * Created by Hyunjun on 2/12/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class NotificationService extends NotificationListenerService {
    Context context;

    boolean startTrigger;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        Log.v("Package","Create");

        startTrigger = false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        if(startTrigger) {
            getNotiData(sbn);
        }
        else
            Log.i("Noti", "Not Start Collecting");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Log.i("Msg", "Notification Removed");
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        startTrigger = true;

        //StatusBarNotification[] acticeNoti = getActiveNotifications();
        //for (StatusBarNotification sbn:acticeNoti) {
            //getNotiData(sbn);
        //}

        Toast.makeText(this,"NotificationCollecting is Started",Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    public void getNotiData(StatusBarNotification sbn){
        String pack = sbn.getPackageName();
        String ticker = "tickerTest";
        //String ticker = sbn.getNotification().tickerText.toString();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = extras.getCharSequence("android.text").toString();

        Log.i("Package", pack);
        Log.i("Ticker", ticker);
        Log.i("Title", title);
        Log.i("Text", text);

        Intent msgrcv = new Intent("Msg");
        msgrcv.putExtra("package", pack);
        msgrcv.putExtra("ticker", ticker);
        msgrcv.putExtra("title", title);
        msgrcv.putExtra("text", text);

        LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
    }
}
