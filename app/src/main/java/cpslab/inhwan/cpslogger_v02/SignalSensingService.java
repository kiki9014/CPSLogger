package cpslab.inhwan.cpslogger_v02;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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
    boolean logRunning;

    MyPhoneState phoneListener;
    TelephonyManager telephony;

    BroadcastReceiver broadReceiver;

    bluetoothTh bTh;

    @Override
    public void onCreate(){
        super.onCreate();
        phoneListener = new MyPhoneState();
        telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CELL_INFO);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CELL_LOCATION);

        broadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    String devAddr = ((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getAddress();
                    String devName = ((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getName();
                    int rssi = (int) intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                    String bluetoothData = "bluetooth " + devAddr + " " + devName + " " + rssi;

                    Log.d(name+"|bluetooth",bluetoothData);

                    sigLogger.writeData(bluetoothData);
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    Log.d(name + "|bluetooth", "Discovery end");
                }
            }
        };

        bTh = new bluetoothTh();

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
        logRunning = true;

        bTh.start();

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
        logRunning = false;
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

    private class bluetoothTh extends Thread{
        BluetoothAdapter bluetooth;

        public bluetoothTh(){
            if(Build.VERSION.SDK_INT >= 18){
                BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                bluetooth = bm.getAdapter();
            }
            else
                bluetooth = BluetoothAdapter.getDefaultAdapter();
        }

        public void run(){
            while (logRunning){
                try{
                    if(bluetooth.isEnabled()){
                        getApplicationContext().registerReceiver(broadReceiver,new IntentFilter(BluetoothDevice.ACTION_FOUND));
                        getApplicationContext().registerReceiver(broadReceiver,new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

                        bluetooth.startDiscovery();

                        sleep(600000);
                    }
                    else{
                        Log.d("BLUETOOTH","Bluetooth is not enabled");
                        sleep(600000);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
