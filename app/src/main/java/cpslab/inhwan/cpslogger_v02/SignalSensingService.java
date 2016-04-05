package cpslab.inhwan.cpslogger_v02;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SignalSensingService extends Service {

    static String name = "Signal";

    Logger sigLogger = new Logger(name);

    boolean fileOpen;

    MyPhoneStateListener phoneListener;
    TelephonyManager telephony;

    public SignalSensingService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        phoneListener = new MyPhoneStateListener();
        telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        fileOpen = true;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "Signal Sensing is started", Toast.LENGTH_SHORT).show();		//toast message
        Log.d(name, "SignalSensingStart");

        if(!fileOpen){
            sigLogger.createFile(name);
            fileOpen = true;
        }

        return START_STICKY;		//Sticky n Unsticky: what is the difference?
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        telephony.listen(phoneListener,PhoneStateListener.LISTEN_NONE);

        if(fileOpen){
            sigLogger.closeFile(name);
            fileOpen = false;
        }
        Log.d(name,"Service Ended");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class MyPhoneStateListener extends PhoneStateListener{
        public int signalStrengths;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength){
            super.onSignalStrengthsChanged(signalStrength);

            String signal = signalStrength.toString();

            Log.d(name,signal);
            sigLogger.writeData(signal);

        }
    }
}
