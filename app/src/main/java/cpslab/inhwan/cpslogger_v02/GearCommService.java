package cpslab.inhwan.cpslogger_v02;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;
import com.samsung.android.sdk.accessory.SAAgent;

import java.io.IOException;
import java.util.HashMap;

public class GearCommService extends Service {
    String serviceName = "GearCommunication";

    String location = "LAB";

    gearService gService;

    gearDataReceiver gDataReceiver;

    @Override
    public void onCreate(){
        super.onCreate();

        gDataReceiver = new gearDataReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cpslab.inhwan.cpslogger_v02.GearCommService");
        registerReceiver(gDataReceiver, intentFilter);

        Log.d(serviceName, "Service is created");

        gService = new gearService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        unregisterReceiver(gDataReceiver);
        Log.d(serviceName, "Service is destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        Log.d(serviceName, "Service Start!!!");

        return START_STICKY;
    }

    public class gearService extends SAAgent{
        HashMap<Integer, gearConnection> mConnectionMap = null;

        public static final int SERVICE_CONNECTION_RESULT_OK = 0;

        public static final int gearService_CHANNEL_ID = 104;

        private final IBinder mBinder = new LocalBinder();

        public gearService(){
            super(serviceName, gearConnection.class);
        }

        public class LocalBinder extends Binder{
            public gearService getService(){
                return gearService.this;
            }
        }

        public class gearConnection extends SASocket{
            private int mConnectionId;

            public gearConnection(){
                super(gearConnection.class.getName());
            }

            @Override
            public void onReceive(int channelId, byte[] data){
                String rxData = new String(data);
                Toast.makeText(getBaseContext(),rxData,Toast.LENGTH_LONG).show();

                if(rxData.equals("Location"))
                    sendData(location);
            }

            @Override
            public void onError(int channelId, String errorString, int error){
                Log.e(serviceName, "Connection has error : " + errorString);
            }

            @Override
            public void onServiceConnectionLost(int errorCode){
                Log.e(serviceName, "onServiceConnectionLost : " + errorCode);
            }

            public void sendData2Gear(final String data){
                final gearConnection uHandler = mConnectionMap.get(mConnectionId);

                if(uHandler == null){
                    Log.e(serviceName, "Cannot get Handler");
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            uHandler.send(gearService_CHANNEL_ID, data.getBytes());
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        @Override
        public void onCreate(){
            super.onCreate();

            SA mAccessory = new SA();
            try{
                mAccessory.initialize(this);
            }
            catch (SsdkUnsupportedException e){

            }
            catch (Exception e1){
                Log.e(serviceName,"Accessory package Initialization faild");
                e1.printStackTrace();
                stopSelf();
            }
        }

        @Override
        public void onServiceConnectionRequested(SAPeerAgent peerAgent){
            acceptServiceConnectionRequest(peerAgent);
        }

        @Override
        public void onFindPeerAgentResponse(SAPeerAgent peerAgent, int arg){
            Log.d(serviceName, "onFindPeerAgentResponse : " + arg);
        }

        @Override
        public void onServiceConnectionResponse(SASocket thisConnection, int result){
            if(result == CONNECTION_SUCCESS){
                if(thisConnection != null){
                    gearConnection gConnection = (gearConnection) thisConnection;

                    if(mConnectionMap == null){
                        mConnectionMap = new HashMap<Integer, gearConnection>();
                    }

                    gConnection.mConnectionId = (int) (System.currentTimeMillis() & 255);

                    Log.d(serviceName, "onServiceConnection connection ID : " + gConnection.mConnectionId);

                    mConnectionMap.put(gConnection.mConnectionId, gConnection);

                    Toast.makeText(getBaseContext(), "onServiceConnection success", Toast.LENGTH_SHORT).show();
                }
                else
                    Log.e(serviceName, "SASocket is null");
            }
            else if(result == CONNECTION_ALREADY_EXIST){
                Log.e(serviceName, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
            }
            else {
                Log.e(serviceName,"onServiceConnectionResponse result error : " + result);
            }
        }

        @Override
        public IBinder onBind(Intent intent){
            return mBinder;
        }

        public void sendData(String data){
            for(gearConnection provider : mConnectionMap.values()){
                provider.sendData2Gear(data);
            }
        }
    }

    public class gearDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            if(intent.getAction().equals("cpslab.inhwan.cpslogger_v02.GearCommService")){
                String data = intent.getStringExtra("data");

                gService.sendData(data);
            }
        }
    }
}
