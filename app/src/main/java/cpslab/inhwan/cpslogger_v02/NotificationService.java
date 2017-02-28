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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.IllegalFormatCodePointException;
import java.util.List;

public class NotificationService extends NotificationListenerService {
    Context context;

    boolean startTrigger;
    String name = "Notification";

    Logger notiLogger = new Logger(name);

    broadcastReceiver nReceiver;
    IncomingSms smsMan;
    IncomingMMS mmsMan;

    static boolean fileOpen;

    Hashtable<String, Integer> senderList;
    Hashtable<String, Integer> mmsList;
    Hashtable<String, Integer> smsList;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        Log.v(name, "Create");

        //Broadcast receiver from main activity to destroy notification service
        nReceiver = new broadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cpslab.inhwan.cpslogger_v02.NotificationService");
        registerReceiver(nReceiver, intentFilter);

        //SMS and SNS listener
        smsMan = new IncomingSms();
        mmsMan = new IncomingMMS();
        IntentFilter intentFilterSMS = new IntentFilter();
        intentFilterSMS.addAction("cpslab.inhwan.cpslogger_v02.NotificationService");
        intentFilterSMS.addAction("android.provider.Telephony.SMS_RECEIVED");
        IntentFilter intentFilterMMS = new IntentFilter();
        try{
            intentFilterMMS.addAction("cpslab.inhwan.cpslogger_v02.NotificationService");
            intentFilterMMS.addAction("android.provider.Telephony.MMS_RECEIVED");
            intentFilterMMS.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
            intentFilterMMS.addDataType("application/vnd.wap.mms-message");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        registerReceiver(smsMan, intentFilterSMS);
        registerReceiver(mmsMan, intentFilterMMS);

        //Hash table of sender list
        senderList = importHashTable("Notification");
        mmsList = importHashTable("MMS");
        smsList = importHashTable("SMS");

        startTrigger = false;
        fileOpen = true;
    }

    //Get data of received notification when new notification is arrived (Posted)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        if(startTrigger) {
            getNotiData(sbn,true);
        }
        else {
        }
    }

    //Get data of removed notification when the notification is removed from noti bar (removed)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        if(startTrigger) {
            getNotiData(sbn,false);
        }
        else {
        }
    }

    //Start NotificationService
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        startTrigger = true;

        if(!fileOpen){
            notiLogger.createFile(name);
            fileOpen = true;
        }
        notiLogger.writeData("Previous Notification");

        //Read exist notification
        StatusBarNotification[] acticeNoti = NotificationService.this.getActiveNotifications();
        if(acticeNoti != null) {
            for (StatusBarNotification sbn : acticeNoti) {
                getNotiData(sbn, true);
            }
        }

        Toast.makeText(this,"NotificationCollecting is Started",Toast.LENGTH_SHORT).show();

        notiLogger.writeData("Notification Listen Service is started");

        return START_NOT_STICKY;
    }

        //Trace stack of unregistration receiver
    public void safelyUnregisterReceiver(BroadcastReceiver receiver){
        try{
            unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    //Stop service
    @Override
    public void onDestroy(){
        Log.v(name, "Service will be destroyed");

        startTrigger = false;
        Log.v(name, "Service is destroyed");
        //Safely unregister receiver
        safelyUnregisterReceiver(nReceiver);
        safelyUnregisterReceiver(smsMan);
        safelyUnregisterReceiver(mmsMan);

        if(fileOpen){
            notiLogger.closeFile(name);
            fileOpen = false;
        }
        //Save Hash table
        exportHashTable(senderList,"Notification");
        exportHashTable(mmsList, "MMS");
        exportHashTable(smsList, "SMS");
        stopSelf();
        super.onDestroy();
    }

    //Get data of single notification
    public void getNotiData(StatusBarNotification sbn, boolean isPosted){
        String pack = Base64.encodeToString(sbn.getPackageName().getBytes(), Base64.NO_WRAP);
        String text, sbText, ticker, title, bText;
        CharSequence tickerSQ = sbn.getNotification().tickerText;
        Bundle extras = sbn.getNotification().extras;
        CharSequence cTitle = extras.getCharSequence("android.title");
        CharSequence cText = extras.getCharSequence("android.text");
        CharSequence cSbText = extras.getCharSequence("android.subtext");

        String titleStr, textStr;
        if(cTitle == null)
            titleStr = "";
        else
            titleStr = cTitle.toString();
        if(cText == null)
            textStr = "";
        else
            textStr = cText.toString();

        //If noti is from messenger, title and content is changed
        if(isMassenger(sbn.getPackageName())){
            if(!senderList.containsKey(cTitle.toString())){
                senderList.put(cTitle.toString(),senderList.size());
            }
            titleStr = Integer.toString(senderList.get(cTitle.toString()));
            textStr = Integer.toString(cText.length());
        }

        if(tickerSQ == null)
            ticker = "null";
        else
            ticker = Base64.encodeToString(tickerSQ.toString().getBytes(), Base64.NO_WRAP);
        if(titleStr.length() == 0)
            title = "null";
        else
            title = Base64.encodeToString(titleStr.getBytes(), Base64.NO_WRAP);
        if(textStr.length() == 0)
            text = "null";
        else
            text = Base64.encodeToString(textStr.getBytes(),Base64.NO_WRAP);
        if(cSbText == null)
            sbText = "null";
        else
            sbText = Base64.encodeToString(cSbText.toString().getBytes(),Base64.NO_WRAP);


        String textToSave;
        String action;
        if(isPosted)
            action = "posted";
        else
            action = "removed";

        int contentLen;
        if(cText == null)
            contentLen = 0;
        else
            contentLen = cText.length();
        textToSave = pack + "," + ticker + "," + title + "," + contentLen + ","+ action;


        if(fileOpen)
            notiLogger.writeData(textToSave);
    }

    //Quit function using broadcast
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

    //SMS manager
    public class IncomingSms extends BroadcastReceiver{
        final SmsManager sms  = SmsManager.getDefault();

        public IncomingSms(){

        }

        @Override
        public void onReceive(Context context, Intent intent){
            final Bundle bundle = intent.getExtras();

            try{
                if(bundle != null){
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");

                    for(Object pdus:pdusObj){
                        SmsMessage currMessage = SmsMessage.createFromPdu((byte[]) pdus);

                        String phoneNumber = currMessage.getDisplayOriginatingAddress();
                        if(!smsList.containsKey(phoneNumber)){
                            smsList.put(phoneNumber,smsList.size());
                        }
                        else {
                        }
                        String sendNumber = Integer.toString(smsList.get(phoneNumber));
                        String message = currMessage.getDisplayMessageBody();


                        String text2Save = "SMS," + Base64.encodeToString(sendNumber.getBytes(),Base64.NO_WRAP) + "," + Base64.encodeToString(Integer.toString(message.length()).getBytes(),Base64.NO_WRAP);//Base64.encodeToString(message.getBytes(),Base64.NO_WRAP);
                        if(fileOpen){
                            notiLogger.writeData(text2Save);
                        }
                    }
                }
            }
            catch (Exception e){
            }
        }
    }

    //MMS manager. It is not my code: from other's blog
    public class IncomingMMS extends BroadcastReceiver{
        private Context _context;

        public IncomingMMS(){

        }

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

            String mmsNumber = parseNumber(id);
            if(!mmsList.containsKey(mmsNumber))
                mmsList.put(mmsNumber,mmsList.size());
            String number = Integer.toString(mmsList.get(mmsNumber));
            String msg = parseMessage(id);
            Log.i("MMSReceiver", "|" + number + "|" + msg);
            msg = Integer.toString(msg.length());
            String text2Save = "MMS,"+Base64.encodeToString(number.getBytes(),Base64.NO_WRAP)+","+Base64.encodeToString(Integer.toString(msg.length()).getBytes(), Base64.NO_WRAP);//Base64.encodeToString(msg.getBytes(),Base64.NO_WRAP);

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
        if(packageName.equals("com.kakao.talk") | packageName.equals("jp.naver.line.android") | packageName.equals("com.Slack") | packageName.equals("com.facebook.orca"))
            return true;
        else
            return false;
    }

    public Hashtable<String, Integer> importHashTable(String name){
        Hashtable<String, Integer> hashT;
        try{
            FileInputStream fileInputStream = openFileInput(name);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            hashT = (Hashtable<String, Integer>) objectInputStream.readObject();
            objectInputStream.close();

            return hashT;
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        hashT = new Hashtable<>();
        return hashT;
    }

    public void exportHashTable(Hashtable<String, Integer> hashT, String name){
        try{
            FileOutputStream fileOutputStream = openFileOutput(name, MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(hashT);
            objectOutputStream.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
