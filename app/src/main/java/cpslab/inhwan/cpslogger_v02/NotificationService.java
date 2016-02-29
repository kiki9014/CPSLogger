package cpslab.inhwan.cpslogger_v02;

/*
 * Created by Hyunjun on 2/12/2016.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class NotificationService extends NotificationListenerService {
    Context context;

    boolean startTrigger;
    String name = "Notification";

    Logger notiLogger = new Logger(name);

    broadcastReceiver nReceiver;

    boolean fileOpen;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        Log.v(name, "Create");

        nReceiver = new broadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cpslab.inhwan.cpslogger_v02.NotificationService");
        registerReceiver(nReceiver,intentFilter);

        startTrigger = false;
        fileOpen = true;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        if(startTrigger) {
            getNotiData(sbn,true);
        }
        else
            Log.i(name, "Not Start Collecting");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        if(startTrigger) {
            getNotiData(sbn,false);
        }
        else
            Log.i(name, "Not Start Collecting");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        startTrigger = true;

        if(!fileOpen){
            notiLogger.createFile(name);
            fileOpen = true;
        }
        notiLogger.writeData("Previous Notification");

        StatusBarNotification[] acticeNoti = NotificationService.this.getActiveNotifications();
        for (StatusBarNotification sbn:acticeNoti) {
            getNotiData(sbn,true);
        }

        Toast.makeText(this,"NotificationCollecting is Started",Toast.LENGTH_SHORT).show();

        notiLogger.writeData("Notification Listen Service is started");

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v(name, "Service will be destroyed");

        startTrigger = false;
        Log.v(name, "Service is destroyed");

        if(fileOpen){
            notiLogger.closeFile(name);
            fileOpen = false;
        }
    }

    public void getNotiData(StatusBarNotification sbn, boolean isPosted){
        String pack = Base64.encodeToString(sbn.getPackageName().getBytes(),Base64.NO_WRAP);
        String text, sbText, ticker, title;
        //String ticker = "tickerTest";
        CharSequence tickerSQ = sbn.getNotification().tickerText;
        Bundle extras = sbn.getNotification().extras;
        CharSequence cTitle = extras.getCharSequence("android.title");
        CharSequence cText = extras.getCharSequence("android.text");
        CharSequence cSbText = extras.getCharSequence("android.subtext");

        if(tickerSQ == null)
            ticker = "null";
        else
            ticker = Base64.encodeToString(tickerSQ.toString().getBytes(), Base64.NO_WRAP);
        if(cTitle == null)
            title = "null";
        else
            title = Base64.encodeToString(cTitle.toString().getBytes(), Base64.NO_WRAP);
        if(cText == null)
            text = "null";
        else
            text = Base64.encodeToString(cText.toString().getBytes(),Base64.NO_WRAP);
        if(cSbText == null)
            sbText = "null";
        else
            sbText = Base64.encodeToString(cSbText.toString().getBytes(),Base64.NO_WRAP);

        Log.i("Package", pack);
        Log.i("Ticker", ticker);
        Log.i("Title", title);
        Log.i("Text", text);

        String textToSave;
        String action;
        if(isPosted)
            action = "posted";
        else
            action = "removed";

        textToSave = pack + "," + ticker + "," + title + "," + text + "," + sbText + "," +  action;

        if(fileOpen)
            notiLogger.writeData(textToSave);
    }

    public class broadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            String cmd = intent.getStringExtra("Notification_Event");
            if(cmd.equals("QUIT")){
                stopSelf();
                if(fileOpen){
                    notiLogger.closeFile(name);
                    fileOpen = false;
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        IBinder mBinder = super.onBind(intent);
        return mBinder;
    }
}
