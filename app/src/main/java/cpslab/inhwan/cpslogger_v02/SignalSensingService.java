package cpslab.inhwan.cpslogger_v02;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class SignalSensingService extends Service {

    static String name = "Signal";

    Logger sigLogger = new Logger(name);

    boolean fileOpen;

    MyPhoneState phoneListener;
    TelephonyManager telephony;

    public SignalSensingService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        phoneListener = new MyPhoneState();
        telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CELL_INFO);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CELL_LOCATION);

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

    private class MyPhoneState extends PhoneStateListener{
        private static final String TAG = "Phone";

        public int signalStrengths;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength){
            super.onSignalStrengthsChanged(signalStrength);

            String signal = signalStrength.toString();

            Log.d(name,signal);
            sigLogger.writeData(signal);

        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfos){
            if(cellInfos != null && cellInfos.size() != 0){
                for(CellInfo cellInfo : cellInfos){
                    Log.d(name+"|Info", cellInfo.toString());
                }
            }
        }

        @Override
        public void onCellLocationChanged(CellLocation cellLocation){
            int cid,lac,psc;
            GsmCellLocation cellLoc = (GsmCellLocation)cellLocation;
            cid  = cellLoc.getCid();
            lac = cellLoc.getLac();
            psc = cellLoc.getPsc();

            String cellLocData = "cellLoc " + cid + " " + lac + " " + psc;
            Log.d(name+"|Loc", cellLocData);
            sigLogger.writeData(cellLocData);
        }

    }
}
