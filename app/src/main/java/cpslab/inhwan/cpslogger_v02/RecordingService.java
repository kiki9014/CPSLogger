package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Inhwan on 2015-10-01.
 */

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import android.telephony.*;
import android.util.Log;
import android.widget.*;

public class RecordingService extends Service {

    MediaRecorder mRecorder = null;
    String sd = Environment.getExternalStorageDirectory().getAbsolutePath();

    boolean isPaused = false;

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        mRecorder.reset();
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        startRecord();

        Toast.makeText(this, "Recording is started", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public class broadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            boolean data = intent.getBooleanExtra("requestPause", false);

            if(isPaused && !data)
                startRecord();
            else if(!isPaused && data)
                stopRecord();
        }
    }

    private void startRecord(){

        Calendar calendar = Calendar.getInstance();
        sd = Environment.getExternalStorageDirectory().getAbsolutePath();

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");
        String currentDate = simpleDateFormat.format(date);

        File dir = new File(sd + "/CPSLogger/recorded/" + currentDate);
        if(!dir.exists()){
            dir.mkdirs();
//            Log.i("record", "Directory is created");
        }
        else {
//            Log.i("record", "Directory is already exist");
        }
        String Path = sd + "/CPSLogger/recorded/" + currentDate + "/" + (calendar.get(Calendar.MONTH)+1) + "M" + calendar.get(Calendar.DAY_OF_MONTH) + "d" + calendar.get(Calendar.HOUR_OF_DAY) + "h" + calendar.get(Calendar.MINUTE) + "m" + calendar.get(Calendar.SECOND) + "s" + ".cps";
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        } else {
            mRecorder.reset();
        }
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(Path);

        try {
            mRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
        isPaused = false;
    }

    //Instead pause and resume, service is stop and restart recording
    private void stopRecord(){
        mRecorder.stop();
        mRecorder.reset();
        isPaused = true;
    }
}