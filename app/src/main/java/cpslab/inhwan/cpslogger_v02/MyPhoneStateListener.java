package cpslab.inhwan.cpslogger_v02;

import android.content.Context;
import android.content.Intent;
import android.telephony.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.*;

public class MyPhoneStateListener extends PhoneStateListener {

    Context context;

    private static final String TAG = "my";

    public MyPhoneStateListener(Context ctx){
        super();
        context = ctx;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        String stateData = "";
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                stateData = "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_IDLE " + incomingNumber;
                requestRecord(false);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                stateData = "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_OFFHOOK " + incomingNumber;
                requestRecord(true);
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                stateData = "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_RINGING " + incomingNumber;
                requestRecord(true);
                break;
            default:
                stateData = "MyPhoneStateListener->onCallStateChanged() -> default -> " + Integer.toString(state);
                break;
        }
        Log.i(TAG, stateData);
        savePhonestateData(stateData);
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
        String stateData;
        switch (serviceState.getState()) {
            case ServiceState.STATE_IN_SERVICE:
                stateData = "MyPhoneStateListener->onCallStateChanged() -> STATE_IN_SERVICE";
                serviceState.setState(ServiceState.STATE_IN_SERVICE);
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
                stateData = "MyPhoneStateListener->onCallStateChanged() -> STATE_OUT_OF_SERVICE";
                serviceState.setState(ServiceState.STATE_OUT_OF_SERVICE);
                break;
            case ServiceState.STATE_EMERGENCY_ONLY:
                stateData = "MyPhoneStateListener->onCallStateChanged() -> STATE_EMERGENCY_ONLY";
                serviceState.setState(ServiceState.STATE_EMERGENCY_ONLY);
                break;
            case ServiceState.STATE_POWER_OFF:
                stateData = "MyPhoneStateListener->onCallStateChanged() -> STATE_POWER_OFF";
                serviceState.setState(ServiceState.STATE_POWER_OFF);
                break;
            default:
                stateData = "MyPhoneStateListener->onCallStateChanged() -> default -> " + Integer.toString(serviceState.getState());
                break;
        }
        Log.i(TAG,stateData);
        savePhonestateData(stateData);
    }

    void savePhonestateData(String data){
        Intent i = new Intent("cpslab.inhwan.cpslogger_v02.PhoneStateLogging");
        i.putExtra("data2Log", data);
        context.sendBroadcast(i);
    }

    void requestRecord(boolean ok){
        Intent i = new Intent("cpslab.inhwan.cpslogger_v02.PhoneState");
        i.putExtra("requestPause", ok);
        context.sendBroadcast(i);
    }
}