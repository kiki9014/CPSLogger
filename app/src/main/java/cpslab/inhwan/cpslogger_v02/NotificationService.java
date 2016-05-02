package cpslab.inhwan.cpslogger_v02;

/*
 * Created by Hyunjun on 2/12/2016.
 */

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Objects;

public class NotificationService extends NotificationListenerService {
    Context context;

    boolean startTrigger;
    static String name = "Notification";

    static Logger notiLogger = new Logger(name);

    broadcastReceiver nReceiver;
    IncomingSms smsMan;
    IncomingMMS mmsMan;

    static boolean fileOpen;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        Log.v(name, "Create");

        nReceiver = new broadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cpslab.inhwan.cpslogger_v02.NotificationService");
        registerReceiver(nReceiver, intentFilter);
//
        smsMan = new IncomingSms();
        mmsMan = new IncomingMMS();
//        IntentFilter intentFilterSMS = new IntentFilter();
//        intentFilterSMS.addAction("cpslab.inhwan.cpslogger_v02.NotificationService");
//        registerReceiver(smsMan,intentFilterSMS);

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
        if(acticeNoti != null) {
            for (StatusBarNotification sbn : acticeNoti) {
                getNotiData(sbn, true);
            }
        }

        Toast.makeText(this,"NotificationCollecting is Started",Toast.LENGTH_SHORT).show();

        notiLogger.writeData("Notification Listen Service is started");

        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.playbackcomplete");
        iF.addAction("com.android.music.queuechanged");
        registerReceiver(mMReceiver,iF);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v(name, "Service will be destroyed");

        startTrigger = false;
        Log.v(name, "Service is destroyed");
        unregisterReceiver(nReceiver);

        if(fileOpen){
            notiLogger.closeFile(name);
            fileOpen = false;
        }
        stopSelf();
    }

    public void getNotiData(StatusBarNotification sbn, boolean isPosted){
        String pack = Base64.encodeToString(sbn.getPackageName().getBytes(), Base64.NO_WRAP);
        String text, sbText, ticker, title, bText;
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

        Log.i("Package", new String(Base64.decode(pack,Base64.NO_WRAP)));
        Log.i("Ticker", new String(Base64.decode(ticker,Base64.NO_WRAP)));
        Log.i("Title", new String(Base64.decode(title,Base64.NO_WRAP)));
        Log.i("Text", new String(Base64.decode(text,Base64.NO_WRAP)));
        Log.i("Sub Text", new String(Base64.decode(sbText,Base64.NO_WRAP)));

        String textToSave;
        String action;
        if(isPosted)
            action = "posted";
        else
            action = "removed";

//        textToSave = pack + "," + ticker + "," + title + "," + text + "," + sbText + "," +  action;
        int contentLen;
        if(cText == null)
            contentLen = 0;
        else
            contentLen = cText.length();
        textToSave = pack + "," + ticker + "," + title + "," + contentLen + ","+ action;


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

    public static class IncomingSms extends BroadcastReceiver{
        final SmsManager sms  = SmsManager.getDefault();

        @Override
        public void onReceive(Context context, Intent intent){
            final Bundle bundle = intent.getExtras();

            try{
                if(bundle != null){
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");

                    for(Object pdus:pdusObj){
                        SmsMessage currMessage = SmsMessage.createFromPdu((byte[]) pdus);

                        String phoneNumber = currMessage.getDisplayOriginatingAddress();
                        String sendNumber = phoneNumber;
                        String message = currMessage.getDisplayMessageBody();

                        Log.i("SMSReceive", "From : " + sendNumber + ", Message : " + message);

                        String text2Save = "SMS," + Base64.encodeToString(sendNumber.getBytes(),Base64.NO_WRAP) + "," + Base64.encodeToString(message.getBytes(),Base64.NO_WRAP);
                        if(fileOpen){
                            notiLogger.writeData(text2Save);
                        }
                    }
                }
            }
            catch (Exception e){
                Log.e("SMSReceive", "Exception Error, " + e);
            }
        }
    }

    public static class IncomingMMS extends BroadcastReceiver{
        private Context _context;

        @Override
        public void onReceive(Context $context, final Intent $intent)
        {
            _context = $context;

            Runnable runn = new Runnable()
            {
                @Override
                public void run()
                {
                    parseMMS();
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runn, 6000); // 시간이 너무 짧으면 못 가져오는게 있더라
        }

        private void parseMMS()
        {
            ContentResolver contentResolver = _context.getContentResolver();
            final String[] projection = new String[] { "_id" };
            Uri uri = Uri.parse("content://mms");
            Cursor cursor = contentResolver.query(uri, projection, null, null, "_id desc limit 1");

            if (cursor.getCount() == 0)
            {
                cursor.close();
                return;
            }

            cursor.moveToFirst();
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            cursor.close();

            String number = parseNumber(id);
            String msg = parseMessage(id);
            Log.i("MMSReceiver", "|" + number + "|" + msg);
            String text2Save = "MMS,"+Base64.encodeToString(number.getBytes(),Base64.NO_WRAP)+","+Base64.encodeToString(msg.getBytes(),Base64.NO_WRAP);

            if(fileOpen){
                notiLogger.writeData(text2Save);
            }
        }

        private String parseNumber(String $id)
        {
            String result = null;

            Uri uri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", $id));
            String[] projection = new String[] { "address" };
            String selection = "msg_id = ? and type = 137";// type=137은 발신자
            String[] selectionArgs = new String[] { $id };

            Cursor cursor = _context.getContentResolver().query(uri, projection, selection, selectionArgs, "_id asc limit 1");

            if (cursor.getCount() == 0)
            {
                cursor.close();
                return result;
            }

            cursor.moveToFirst();
            result = cursor.getString(cursor.getColumnIndex("address"));
            cursor.close();

            return result;
        }

        private String parseMessage(String $id)
        {
            String result = null;

            // 조회에 조건을 넣게되면 가장 마지막 한두개의 mms를 가져오지 않는다.
            Cursor cursor = _context.getContentResolver().query(Uri.parse("content://mms/part"), new String[] { "mid", "_id", "ct", "_data", "text" }, null, null, null);

            Log.i("MMSReceiver", "|mms 메시지 갯수 : " + cursor.getCount() + "|");
            if (cursor.getCount() == 0)
            {
                cursor.close();
                return result;
            }

            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                String mid = cursor.getString(cursor.getColumnIndex("mid"));
                if ($id.equals(mid))
                {
                    String partId = cursor.getString(cursor.getColumnIndex("_id"));
                    String type = cursor.getString(cursor.getColumnIndex("ct"));
                    if ("text/plain".equals(type))
                    {
                        String data = cursor.getString(cursor.getColumnIndex("_data"));

                        if (TextUtils.isEmpty(data))
                            result = cursor.getString(cursor.getColumnIndex("text"));
                        else
                            result = parseMessageWithPartId(partId);
                    }
                }
                cursor.moveToNext();
            }
            cursor.close();

            return result;
        }


        private String parseMessageWithPartId(String $id)
        {
            Uri partURI = Uri.parse("content://mms/part/" + $id);
            InputStream is = null;
            StringBuilder sb = new StringBuilder();
            try
            {
                is = _context.getContentResolver().openInputStream(partURI);
                if (is != null)
                {
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    String temp = reader.readLine();
                    while (!TextUtils.isEmpty(temp))
                    {
                        sb.append(temp);
                        temp = reader.readLine();
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
            return sb.toString();
        }
    }

    boolean isMassenger(String packageName){
        if(packageName.equals("com.kakao.talk"))
            return true;
        else
            return false;
    }

    private BroadcastReceiver mMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            Log.d("mReceiver.onReceive ", action + " / " + cmd);
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            Log.d("Music",artist+":"+album+":"+track);
        }
    };
}
