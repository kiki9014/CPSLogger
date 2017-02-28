//package cpslab.inhwan.cpslogger_v02;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Binder;
//import android.os.IBinder;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.samsung.android.sdk.SsdkUnsupportedException;
//import com.samsung.android.sdk.accessory.*;
//
//import java.io.IOException;
//import java.util.HashMap;
//
///**
// * Created by Hyunjun on 3/2/2016.
// */
//public class GearService extends SAAgent {
//    private  static final String name = "GearService";
//
//    String location = "LAB";
//
//    HashMap<Integer, gearConnection> mConnectionMap = null;
//
//    private gearConnection mConnectHandler = null;
//
//    gearDataReceiver gDataReceiver;
//
//    public static final int SERVICE_CONNECTION_RESULT_OK = 0;
//
//    public static final int gearService_CHANNEL_ID = 104;
//
//    private final IBinder mBinder = new LocalBinder();
//
//    public GearService(){
//        super(name, gearConnection.class);
//    }
//
//    public class LocalBinder extends Binder {
//        public GearService getService(){
//            return GearService.this;
//        }
//    }
//
//    public class gearConnection extends SASocket {
//        private int mConnectionId;
//
//        public gearConnection(){
//            super(gearConnection.class.getName());
//        }
//
//        @Override
//        public void onReceive(int channelId, byte[] data){
//            String rxData = new String(data);
//            Toast.makeText(getBaseContext(), rxData, Toast.LENGTH_LONG).show();
//
//            if(mConnectHandler == null){
//                return;
//            }
//
//            if(rxData.equals("Location"))
//                sendData(location);
//        }
//
//        @Override
//        public void onError(int channelId, String errorString, int error){
//            Log.e(name, "Connection has error : " + errorString + " | " + error);
//        }
//
//        @Override
//        public void onServiceConnectionLost(int errorCode){
//            Log.e(name, "onServiceConnectionLost : " + errorCode);
//            if(mConnectionMap != null){
//                mConnectionMap.remove(mConnectionId);
//            }
//            mConnectHandler= null;
//        }
//
//        public void sendData2Gear(final String data){
//            final gearConnection uHandler = mConnectionMap.get(mConnectionId);
//
//            if(uHandler == null){
//                Log.e(name, "Cannot get Handler");
//            }
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try{
//                        mConnectHandler.send(gearService_CHANNEL_ID, data.getBytes());
//                    }
//                    catch (IOException e){
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
//    }
//
//    @Override
//    public void onCreate(){
//        super.onCreate();
//
//        SA mAccessory = new SA();
//        try{
//            mAccessory.initialize(this);
//        }
//        catch (SsdkUnsupportedException e){
//            Log.e(name,"SsdkUnsupported Exception");
//        }
//        catch (Exception e1){
//            Log.e(name,"Accessory package Initialization faild");
//            e1.printStackTrace();
//            stopSelf();
//        }
//
//        gDataReceiver = new gearDataReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("cpslab.inhwan.cpslogger_v02.GearService");
//        registerReceiver(gDataReceiver, intentFilter);
//
//        Log.d(name, "Service is created");
//    }
//
//    @Override
//    protected void onServiceConnectionRequested(SAPeerAgent peerAgent){
//        Log.d(name,"request arrived");
//        acceptServiceConnectionRequest(peerAgent);
//    }
//
//    @Override
//    protected void onFindPeerAgentResponse(SAPeerAgent peerAgent, int arg){
//        Log.d(name, "onFindPeerAgentResponse : " + arg);
//    }
//
////    @Override
////    public void onServiceConnectionResponse(SASocket thisConnection, int result){
////        if(result == CONNECTION_SUCCESS){
////            if(thisConnection != null){
////                gearConnection gConnection = (gearConnection) thisConnection;
////
////                if(mConnectionMap == null){
////                    mConnectionMap = new HashMap<Integer, gearConnection>();
////                }
////
////                gConnection.mConnectionId = (int) (System.currentTimeMillis() & 255);
////
////                Log.d(name, "onServiceConnection connection ID : " + gConnection.mConnectionId);
////
////                mConnectionMap.put(gConnection.mConnectionId, gConnection);
////
////                Toast.makeText(getBaseContext(), "onServiceConnection success", Toast.LENGTH_SHORT).show();
////            }
////            else
////                Log.e(name, "SASocket is null");
////        }
////        else if(result == CONNECTION_ALREADY_EXIST){
////            Log.e(name, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
////        }
////        else {
////            Log.e(name,"onServiceConnectionResponse result error : " + result);
////        }
////    }
//
//    @Override
//    protected void onServiceConnectionResponse(SAPeerAgent peerAgent, SASocket socket, int result){
//        if(result==SAAgent.CONNECTION_SUCCESS){
//            if(socket != null){
//                mConnectHandler = (gearConnection) socket;
//            }
//            else if(result==SAAgent.CONNECTION_ALREADY_EXIST){
//                Log.e(name, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
//            }
//        }
//        else{
//            Log.e(name,"CONNECTION_FAILED");
//        }
//    }
//
//    @Override
//    protected void onError(SAPeerAgent peerAgent,String errorMessage, int errorCode){
//        super.onError(peerAgent,errorMessage,errorCode);
//    }
//
//    @Override
//    public IBinder onBind(Intent intent){
//        return mBinder;
//    }
//
//    public void sendData(String data){
//        for(gearConnection provider : mConnectionMap.values()){
//            provider.sendData2Gear(data);
//        }
//    }
//
//    public class gearDataReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent){
//            if(intent.getAction().equals("cpslab.inhwan.cpslogger_v02.GearService")){
//                String data = intent.getStringExtra("data");
//
//                //gService.sendData(data);
//                Log.d(name,data);
//            }
//        }
//    }
//}
