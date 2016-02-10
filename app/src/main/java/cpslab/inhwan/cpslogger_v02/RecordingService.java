package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Inhwan on 2015-10-01.
 */

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import android.telephony.*;
import android.widget.*;

public class RecordingService extends Service {

    MediaRecorder mRecorder = null;
    String sd = Environment.getExternalStorageDirectory().getAbsolutePath();

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        mRecorder.reset();
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Calendar calendar = Calendar.getInstance();
        String sd = Environment.getExternalStorageDirectory().getAbsolutePath();
        String Path = sd + "/" + calendar.get(Calendar.HOUR_OF_DAY) + "h" + calendar.get(Calendar.MINUTE) + "m" + calendar.get(Calendar.SECOND) + "s" + ".3gp";
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

        Toast.makeText(this, "Recording is started", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }


}