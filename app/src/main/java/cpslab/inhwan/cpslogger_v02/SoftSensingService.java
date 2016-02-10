package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Inhwan on 2015-10-01.
 */
import android.app.ActivityManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Browser;
import android.telephony.gsm.SmsMessage;
import android.content.ClipboardManager;
import android.util.Log;
import android.widget.Toast;
import android.net.TrafficStats;

import java.util.List;

public class SoftSensingService extends Service {

    ClipboardManager clipboardManager;
    boolean mQuit;

    String clipTxt, prevTxt = "null";

    ClipData clipData;

    Uri messageUri;

    String[] bookMark, searchKey, smsString;
    String sel1, sel2, colName;
    Cursor histCur, bookCur, keyCur, smsCur;

    ActivityManager.MemoryInfo mi;
    ActivityManager activityManager;
    long availableMems;

    long tx;
    long rx;

    public void onCreate() {
        super.onCreate();

        clipboardManager =  (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

//        messageUri = Uri.parse("content://com.sec.mms.provider/message"); //content://com.sec.mms.provider/message or content://sms/

        bookMark = new String[] {Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL};
        searchKey = new String[] {Browser.SearchColumns.DATE, Browser.SearchColumns.SEARCH};
        smsString = new String[] {"_id", "address", "body", "person"};

        sel1 = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
        sel2 = Browser.BookmarkColumns.BOOKMARK + " = 1"; // 0 = history, 1 = bookmark

        histCur = getContentResolver().query(Browser.BOOKMARKS_URI, bookMark, sel1, null, null); // cursor for history
        bookCur = getContentResolver().query(Browser.BOOKMARKS_URI, bookMark, sel2, null, null); // cursor for bookmark
        keyCur = getContentResolver().query(Browser.SEARCHES_URI, searchKey, null, null, null);
//	    startManagingCursor(mCur);

//        smsCur = getContentResolver().query(messageUri, smsString, null, null, null);

        // Memory Information
        mi = new ActivityManager.MemoryInfo();
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    }

    public void onDestroy() {
        super.onDestroy();
        mQuit = true;
        histCur.close();
        bookCur.close();
        keyCur.close();
//		smsCur.close();
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "Soft Sensing is started", Toast.LENGTH_SHORT).show();		//toast message

        mQuit = false;

        clipTh clipth = new clipTh();
        clipth.start();		//start the appThread

        histTh histth = new histTh();
        histth.start();

 		bookTh bookth = new bookTh();
 		bookth.start();

        keyTh keyth = new keyTh();
        keyth.start();

        smsTh smsth = new smsTh();
        smsth.start();

        memTh memth = new memTh();
        memth.start();

        dataTh datath = new dataTh();
        datath.start();

        return START_STICKY;		//Sticky n Unsticky: what is the difference?
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private class clipTh extends Thread {

        public void run() {

//            ContentResolver cr = getContentResolver();

            clipData = clipboardManager.getPrimaryClip();
            if (clipData != null) {

//                        clipboardManager.get

//                        clipTxt = clipboardManager.getText().toString();
                ClipData.Item item = clipData.getItemAt(0);

                clipTxt = item.toString();

                Log.i("clipboard", clipTxt);
            }

//            while(!mQuit) {
//                try {
//
//                    ContentResolver cr = getContentResolver();
//
//                    clipData = clipboardManager.getPrimaryClip();
//                    if (clipData != null) {
//
////                        clipboardManager.get
//
////                        clipTxt = clipboardManager.getText().toString();
//                        ClipData.Item item = clipData.getItemAt(0);
//
//                        clipTxt = item.toString();
//
////                        if (firstFlag) {
////                            prevTxt = clipTxt;
////                            firstFlag = false;
////                        }
//
//                        String a = String.valueOf(clipTxt.equals(prevTxt));
////                        String a = String.valueOf(firstFlag);
//
////                        Log.i("clipPrev", a + prevTxt + clipTxt);
//
//                        if (!clipTxt.equals(prevTxt)) {
//
//                            Log.i("clipboard", clipTxt);
//
//                            prevTxt = clipTxt;
//
//                        }
//
////                        clipboardManager.setText(null);
//                    }
//                    else {
//
//                    }
//                    sleep(30000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    private class histTh extends Thread {

        public void run() {

            histCur.moveToFirst();
            String title = "";
            String url = "";

//            Log.d("histCur", "hist: "+histCur.getCount());

            if (histCur.moveToFirst() && histCur.getCount() > 0) {
                while (histCur.isAfterLast() == false) {

                    title = histCur.getString(histCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                    url = histCur.getString(histCur.getColumnIndex(Browser.BookmarkColumns.URL));
//							url = histCur.getString(histCur.getColumnIndex("URL"));
                    // Do something with title and url
                            Log.i("histtitle", title);
                            Log.i("histurl", url);

                    histCur.moveToNext();
                }
            }

            histCur.close();

//            while(!mQuit) {
//                try {
//
//                    histCur.moveToFirst();
//                    String title = "";
//                    String url = "";
//
//                    Log.d("histCur", "hist: "+histCur.getCount());
//
//                    if (histCur.moveToFirst() && histCur.getCount() > 0) {
//                        while (histCur.isAfterLast() == false) {
//
//                            title = histCur.getString(histCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
//                            url = histCur.getString(histCur.getColumnIndex(Browser.BookmarkColumns.URL));
////							url = histCur.getString(histCur.getColumnIndex("URL"));
//                            // Do something with title and url
////                            Log.i("histtitle", title);
////                            Log.i("histurl", url);
//
//                            histCur.moveToNext();
//                        }
//                    }
////				    histCur.close();
//                    sleep(0);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    private class bookTh extends Thread {

        public void run() {

            bookCur.moveToFirst();

            String title = "";
            String url = "";

            if (bookCur.moveToFirst() && bookCur.getCount() > 0) {
                while (bookCur.isAfterLast() == false) {

                    title = bookCur.getString(bookCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                    url = bookCur.getString(bookCur.getColumnIndex(Browser.BookmarkColumns.URL));
                    // Do something with title and url
                            Log.i("booktitle", title);
                            Log.i("bookurl", url);

                    bookCur.moveToNext();
                }
            }
            bookCur.close();

//            while(!mQuit) {
//                try {
//
//                    bookCur.moveToFirst();
//
//                    String title = "";
//                    String url = "";
//
//                    if (bookCur.moveToFirst() && bookCur.getCount() > 0) {
//                        while (bookCur.isAfterLast() == false) {
//
//                            title = bookCur.getString(bookCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
//                            url = bookCur.getString(bookCur.getColumnIndex(Browser.BookmarkColumns.URL));
//                            // Do something with title and url
////                            Log.i("booktitle", title);
////                            Log.i("bookurl", url);
//
//                            bookCur.moveToNext();
//                        }
//                    }
//                    bookCur.close();
//                    sleep(0);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    private class keyTh extends Thread {

        public void run() {

            keyCur.moveToFirst();

            String date = "";
            String search = "";

//                    Log.i("search count", "count: " + keyCur.getCount());

//            date = keyCur.getString(keyCur.getColumnIndex(Browser.SearchColumns.DATE));
//            search = keyCur.getString(keyCur.getColumnIndex(Browser.SearchColumns.SEARCH));
            // Do something with date and search(keyword)
//                    Log.i("date", date);
//                    Log.i("search", search);

            if (keyCur.moveToFirst() && keyCur.getCount() > 0) {
                while (keyCur.isAfterLast() == false) {

                    date = keyCur.getString(keyCur.getColumnIndex(Browser.SearchColumns.DATE));
                    search = keyCur.getString(keyCur.getColumnIndex(Browser.SearchColumns.SEARCH));
                    // Do something with date and search(keyword)
                            Log.i("date", date);
                            Log.i("search", search);

                    keyCur.moveToNext();
                }
            }
            keyCur.close();

//            while(!mQuit) {
//                try {
//
//                    keyCur.moveToFirst();
//
//                    String date = "";
//                    String search = "";
//
////                    Log.i("search count", "count: " + keyCur.getCount());
//
//                    date = keyCur.getString(keyCur.getColumnIndex(Browser.SearchColumns.DATE));
//                    search = keyCur.getString(keyCur.getColumnIndex(Browser.SearchColumns.SEARCH));
//                    // Do something with date and search(keyword)
////                    Log.i("date", date);
////                    Log.i("search", search);
//
//                    if (keyCur.moveToFirst() && keyCur.getCount() > 0) {
//                        while (keyCur.isAfterLast() == false) {
//
//                            date = keyCur.getString(keyCur.getColumnIndex(Browser.SearchColumns.DATE));
//                            search = keyCur.getString(keyCur.getColumnIndex(Browser.SearchColumns.SEARCH));
//                            // Do something with date and search(keyword)
////                            Log.i("date", date);
////                            Log.i("search", search);
//
//                            keyCur.moveToNext();
//                        }
//                    }
//				    keyCur.close();
//                    sleep(0);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    private class smsTh extends Thread {

        public void run() {

            while(!mQuit) {
                try {

//					Bundle bundle = intent.getExtras();
//					SmsMessage[] msgs = null;
//					String str = "";
//					if (bundle != null)
//					{
//						//---retrieve the SMS message received---
//						Object[] pdus = (Object[]) bundle.get("pdus");
//						msgs = new SmsMessage[pdus.length];
//						for (int i=0; i<msgs.length; i++){
//							msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
//							str += "SMS from " + msgs[i].getOriginatingAddress();
//							str += " :";
//							str += msgs[i].getMessageBody().toString();
//							str += "\n";
//						}
//						//---display the new SMS message---
//						Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
//					}

                    sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class memTh extends Thread {

        public void run() {

            while(!mQuit) {
                try {
                    activityManager.getMemoryInfo(mi);
                    availableMems = mi.availMem / 1048576L;
                    Log.i("AvailMem", Long.toString(availableMems));

                    sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class dataTh extends Thread {

        public void run() {

            while(!mQuit) {
                try {
                    tx = TrafficStats.getTotalTxBytes();
                    rx = TrafficStats.getTotalRxBytes();

                    PackageManager pm = getPackageManager();
                    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                    for (ApplicationInfo packageInfo : packages) {
                        // add packageInfo.uid to UIDs list
                        tx = TrafficStats.getUidTxBytes(packageInfo.uid);
                        rx = TrafficStats.getUidRxBytes(packageInfo.uid);
//                        Log.i("Uid", Integer.toString(packageInfo.uid));
//                        Log.i("Tx", Long.toString(tx));
//                        Log.i("Rx", Long.toString(rx));
                    }

                    sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
