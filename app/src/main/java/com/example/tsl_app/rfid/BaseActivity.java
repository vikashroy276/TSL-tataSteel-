package com.example.tsl_app.rfid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.tsl_app.activities.rfidmap.ScanRFIDActivity;
import com.example.tsl_app.activities.searchPipe.SearchPipeWithRFIDActivity;
import com.example.tsl_app.BuildConfig;
import com.zebra.rfid.api3.TagData;

public abstract class BaseActivity extends AppCompatActivity implements RFIDHandler.ResponseHandlerInterface {

    public static final String TAG = "BaseActivity";
    public static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1001;
    public static final int  LOCATION_PERMISSION_REQUEST_CODE = 1000;
    public static BaseActivity currentActivity = null;
    protected RFIDHandler rfidHandler;
    public String appVersion = "";
    private String rfidStatus="";
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("BARCODE_ACTION")) {
                String action = intent.getStringExtra("AppAction");
                String barcode = intent.getStringExtra("Barcode");
                onScannedData(barcode);
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter("BARCODE_ACTION");
        filter.addAction("BARCODE_ACTION");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        registerReceiver(receiver, filter);

        appVersion = BuildConfig.VERSION_NAME;

    }

    @Override
    protected void onResume() {
        super.onResume();
        currentActivity = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentActivity == this) {
            currentActivity = null;
        }
    }


    public abstract void onScannedData(String data);

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            rfidHandler.performInventory();
        } else {
            rfidHandler.stopInventory();
        }
    }


    @Override
    public void handleTagData(TagData[] tagData) {
        updateTagData(tagData);
    }

    private void updateTagData(TagData[] tagData) {
        TagData closestTag = null;
        int highestRSSI = Integer.MIN_VALUE;
        for (TagData tag : tagData) {
            int currentRSSI = tag.getPeakRSSI();
            if (currentRSSI > highestRSSI) {
                highestRSSI = currentRSSI;
                closestTag = tag;
            }
            Log.i(TAG, "Tag ID: " + tag.getTagID() + " RSSI: " + currentRSSI);
        }

        if (closestTag != null) {
            Log.d(TAG, "Closest Tag ID: " + closestTag.getTagID() + " RSSI: " + highestRSSI);
            passTheTag(closestTag.getTagID());
        }
    }

    private void passTheTag(String tagData) {
        if (currentActivity != null && currentActivity instanceof ScanRFIDActivity) {
            ((ScanRFIDActivity) currentActivity).handleTagData(tagData);
        } else if (currentActivity != null && currentActivity instanceof SearchPipeWithRFIDActivity) {
            ((SearchPipeWithRFIDActivity) currentActivity).handleTagData(tagData);
        }
    }

    @Override
    public boolean showStatus() {
        return rfidHandler.isReaderConnected();
    }

    public void connectRfid() {
        rfidHandler = RFIDHandler.getInstance(30, this);
        rfidHandler.setHandler(this);
        if (rfidHandler != null && !rfidHandler.isReaderConnected()) {
            rfidStatus=  rfidHandler.connect();
        }
    }

    public void disconnectRfid() {
        if (rfidHandler != null && rfidHandler.isReaderConnected()) {
            rfidHandler.disconnect();
        }
    }

}