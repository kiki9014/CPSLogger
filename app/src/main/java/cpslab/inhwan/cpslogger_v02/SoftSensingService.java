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
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import android.net.TrafficStats;

import java.util.List;

public class SoftSensingService extends Service {

    ClipboardManager clipboardManager;
    boolean mQuit;

    String clipTxt, prevTxt = "null";

    ClipData clipData;

    String[] bookMark, searchKey, smsString;
    String sel1, sel2, colName;
    Cursor histCur, bookCur, keyCur, smsCur;

    ActivityManager.MemoryInfo mi;
    ActivityManager activityManager;
    long availableMems;

    long tx;
    long rx;

    String nameClip = "Clip";
    String nameHist = "Hist";
    String nameBook = "Book";
    String nameKey = "Key";
    String nameMem = "Mem";
    String nameData = "Data";
    Logger clipLogger = new Logger(nameClip);
    Logger histLogger = new Logger(nameHist);
    Logger bookLogger = new Logger(nameBook);
    Logger keyLogger = new Logger(nameKey);
    Logger memLogger = new Logger(nameMem);
    Logger dataLogger = new Logger(nameData);
    boolean fileOpen;

    public void onCreate() {
        super.onCreate();

        clipboardManager =  (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        bookMark = new String[] {Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL};
        searchKey = new String[] {Browser.SearchColumns.DATE, Browser.SearchColumns.SEARCH};
        smsString = new String[] {"_id", "address", "body", "person"};

        sel1 = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
        sel2 = Browser.BookmarkColumns.BOOKMARK + " = 1"; // 0 = history, 1 = bookmark

        Uri uriCustom = Uri.parse("content://com.android.chrome.browser/bookmarks");

        histCur = getContentResolver().query(uriCustom, bookMark, sel1, null, null); // cursor for history
        bookCur = getContentResolver().query(uriCustom, bookMark, sel2, null, null); // cursor for bookmark
        keyCur = getContentResolver().query(Browser.SEARCHES_URI, searchKey, null, null, null);

        // Memory Information
        mi = new ActivityManager.MemoryInfo();
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        fileOpen = true;
    }

    public void onDestroy() {
        super.onDestroy();
        mQuit = true;
        if (histCur != null)
            histCur.close();
        if (bookCur != null)
            bookCur.close();
        if (keyCur != null)
            keyCur.close();

        if(fileOpen){
            clipLogger.closeFile(nameClip);
            histLogger.closeFile(nameHist);
            bookLogger.closeFile(nameBook);
            keyLogger.closeFile(nameKey);
            memLogger.closeFile(nameMem);
            dataLogger.closeFile(nameData);
            fileOpen = false;
        }
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

        if(!fileOpen){
            clipLogger.createFile(nameClip);
            histLogger.createFile(nameHist);
            bookLogger.createFile(nameBook);
            keyLogger.createFile(nameKey);
            memLogger.createFile(nameMem);
            dataLogger.createFile(nameData);
            fileOpen = true;
        }

        return START_STICKY;		//Sticky n Unsticky: what is the difference?
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private class clipTh extends Thread {

        public void run() {

            while(!mQuit){
                try{
                    clipData = clipboardManager.getPrimaryClip();
                    if (clipData != null) {
                        ClipData.Item item = clipData.getItemAt(0);

                        clipTxt = item.toString();

                        Log.i("clipboard", clipTxt);
                        if(fileOpen)
                            clipLogger.writeData("clip,"+Base64.encodeToString(clipTxt.getBytes(),Base64.NO_WRAP));
                    }
                    sleep(60000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }

            }
        }
    }

    private class histTh extends Thread {

        public void run() {
            Log.d("history", "Load start");

            while(!mQuit){
                try{
                    String title = "";
                    String url = "";


                    if (histCur != null && histCur.getCount() > 0 && histCur.moveToFirst()) {
                        while (histCur.isAfterLast() == false) {

                            title = histCur.getString(histCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                            url = histCur.getString(histCur.getColumnIndex(Browser.BookmarkColumns.URL));
                            Log.i("histtitle", title);
                            Log.i("histurl", url);
                            if(fileOpen){
                                histLogger.writeData("histTitle,"+Base64.encodeToString(title.getBytes(),Base64.NO_WRAP));
                                histLogger.writeData("histURL,"+Base64.encodeToString(url.getBytes(),Base64.NO_WRAP));
                            }

                            histCur.moveToNext();
                        }
                    }
                    sleep(60000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            histCur.close();

        }
    }

    private class bookTh extends Thread {

        public void run() {

            while(!mQuit){

                try{

                    String title = "";
                    String url = "";

                    if (bookCur != null && bookCur.getCount() > 0 && bookCur.moveToFirst()) {
                        while (bookCur.isAfterLast() == false) {

                            title = bookCur.getString(bookCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                            url = bookCur.getString(bookCur.getColumnIndex(Browser.BookmarkColumns.URL));
                            // Do something with title and url

                            String book = Base64.encodeToString(title.getBytes(),Base64.NO_WRAP)+","+Base64.encodeToString(url.getBytes(),Base64.NO_WRAP);
                            Log.i("booktitle", title);
                            Log.i("bookurl", url);
                            if(fileOpen){
                                bookLogger.writeData("bookTitle,"+Base64.encodeToString(title.getBytes(),Base64.NO_WRAP));
                                bookLogger.writeData("bookURL,"+Base64.encodeToString(url.getBytes(),Base64.NO_WRAP));
                            }

                            bookCur.moveToNext();
                        }
                    }
                    sleep(60000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            bookCur.close();
        }
    }

    private class keyTh extends Thread {

        public void run() {

            while(!mQuit){

                try{

                    String date = "";
                    String search = "";
                    // Do something with date and search(keyword)

                    if ( keyCur != null && keyCur.getCount() > 0 && keyCur.moveToFirst()) {
                        while (keyCur.isAfterLast() == false) {

                            String key;
                            date = keyCur.getString(keyCur.getColumnIndex(Browser.SearchColumns.DATE));
                            search = keyCur.getString(keyCur.getColumnIndex(Browser.SearchColumns.SEARCH));
                            // Do something with date and search(keyword)

                            key = Base64.encodeToString(date.getBytes(),Base64.NO_WRAP) + "," + Base64.encodeToString(search.getBytes(),Base64.NO_WRAP);
                            if(fileOpen){
                                keyLogger.writeData("keyDate,"+Base64.encodeToString(date.getBytes(),Base64.NO_WRAP));
                                keyLogger.writeData("keySearch,"+Base64.encodeToString(search.getBytes(),Base64.NO_WRAP));
                            }

                            keyCur.moveToNext();
                        }
                    }
                    sleep(60000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            keyCur.close();
        }
    }

    private class smsTh extends Thread {

        public void run() {

            while(!mQuit) {
                try {
//					}

                    sleep(100000);
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
//                    Log.i("AvailMem", Long.toString(availableMems));
                    if(fileOpen)
                        memLogger.writeData("AvaliMem,"+Long.toString(availableMems));

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

                        if(fileOpen){
                            dataLogger.writeData("Uid,"+Integer.toString(packageInfo.uid));
                            dataLogger.writeData("tx,"+Long.toString(tx));
                            dataLogger.writeData("rx,"+Long.toString(rx));
                        }
                    }

                    sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
