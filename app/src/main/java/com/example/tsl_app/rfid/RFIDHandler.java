package com.example.tsl_app.rfid;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TriggerInfo;

import java.util.ArrayList;

public class RFIDHandler implements Readers.RFIDReaderEventHandler {

    final static String TAG = "RFID_SAMPLE";
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static RFIDHandler mThis;
    private final BaseActivity context;
    String readername = "RFD8500123";
    private EventHandler eventHandler;
    private ResponseHandlerInterface handler = null;
    private int MAX_POWER = 27;
    public int initialPower = MAX_POWER;

    public RFIDHandler(int initialPower, Context activity) {
        this.initialPower = initialPower;
        context = (BaseActivity) activity;
        InitSDK();
    }


    public static RFIDHandler getInstance(int initialPower, Context ctx) {
        if (mThis != null) {
            return mThis;
        }
        mThis = new RFIDHandler(initialPower, ctx);
        return mThis;
    }

    public boolean isReaderConnected() {
        if (reader != null && reader.isConnected()) return true;
        else {
            Log.d(TAG, "reader is not connected");
            return false;
        }
    }

    void onDestroy() {
        dispose();
    }

    private void InitSDK() {
        Log.d(TAG, "InitSDK");

        new CreateInstanceTask().execute();
    }


    private synchronized void GetAvailableReader() {
        Log.d(TAG, "GetAvailableReader");
        if (readers != null) {
            Readers.attach(this);
            try {
                if (readers.GetAvailableRFIDReaderList() != null) {
                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                    if (!availableRFIDReaderList.isEmpty()) {
                        // if single reader is available then connect it
                        if (availableRFIDReaderList.size() == 1) {
                            readerDevice = availableRFIDReaderList.get(0);
                            reader = readerDevice.getRFIDReader();
                        } else {
                            // search reader specified by name
                            for (ReaderDevice device : availableRFIDReaderList) {
                                if (device.getName().equals(readername)) {
                                    readerDevice = device;
                                    reader = readerDevice.getRFIDReader();
                                }
                            }
                        }
                    }
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            }
        }
    }

    // handler for receiving reader appearance events
    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderAppeared " + readerDevice.getName());
        new ConnectionTask().execute();
        connect();
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderDisappeared " + readerDevice.getName());
        if (readerDevice.getName().equals(reader.getHostName())) disconnect();
    }

    public synchronized String connect() {
        if (reader != null && !reader.isConnected()) {
            Log.d(TAG, "connect " + reader.getHostName());
            try {
                if (!reader.isConnected()) {
                    reader.connect();
                    ConfigureReader();
                    if (handler != null) {
                        handler.handleSetText("Connected");
                    }
                    return "Connected";
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
                String des = e.getResults().toString();
                return "Connection failed";
            }
        }
        return "";
    }

    private void ConfigureReader() {
        Log.d(TAG, "ConfigureReader " + reader.getHostName());
        if (reader.isConnected()) {
            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                // receive events from reader
                if (eventHandler == null) eventHandler = new EventHandler();
                reader.Events.addEventsListener(eventHandler);
                // HH event
                reader.Events.setHandheldEvent(true);
                // tag event with tag data
                reader.Events.setTagReadEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                // set trigger mode as rfid so scanner beam will not come
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                // set start and stop triggers
                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
                // power levels are index based so maximum power supported get the last one
                MAX_POWER = reader.ReaderCapabilities.getTransmitPowerLevelValues().length - 1;
                // set antenna configurations
                Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
                config.setTransmitPowerIndex(initialPower );//changed from max_power
                config.setrfModeTableIndex(0);
                config.setTari(0);
                reader.Config.Antennas.setAntennaRfConfig(1, config);
                // Set the singulation control
                Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
                s1_singulationControl.setSession(SESSION.SESSION_S1);
                s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
                // delete any prefilters
                reader.Actions.PreFilters.deleteAll();
                //
            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void setPower(int power) {
        try {
            this.initialPower = power;
            Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(power * 10);
            reader.Config.Antennas.setAntennaRfConfig(1, config);
            disconnect();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            },800);

        } catch (InvalidUsageException | OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public synchronized  void disconnect() {
        Log.d(TAG, "disconnect " + reader);
        try {
            if (reader != null) {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                if (handler != null) {
                    handler.handleSetText("Disconnected");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void dispose() {
        try {
            if (readers != null) {
                reader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void performInventory() {
        // check reader connection
        if (!isReaderConnected()) return;
        try {
            reader.Actions.Inventory.perform();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopLocate() throws OperationFailureException, InvalidUsageException {
        if (reader != null && reader.Actions.TagLocationing != null) {
            reader.Actions.TagLocationing.Stop();
        }
    }

    public synchronized void locate(String epc) {
        // Ensure the reader is connected
        if (!isReaderConnected()) {
            Log.w("RFIDHandler", "Reader is not connected.");
            return;
        }

        // Validate EPC
        if (epc == null || epc.isEmpty()) {
            Log.w("RFIDHandler", "Invalid EPC provided.");
            return;
        }

        try {
            // Perform locationing action
            reader.Actions.TagLocationing.Perform(epc, null, null);
            Log.i("RFIDHandler", "Tag locationing started for EPC: " + epc);
        } catch (InvalidUsageException e) {
            Log.e("RFIDHandler", "InvalidUsageException: " + e.getInfo(), e);
            Log.e("RFIDHandler", "InvalidUsageException: " + e.getLocalizedMessage(), e);
        } catch (OperationFailureException e) {
            Log.e("RFIDHandler", "OperationFailureException: " + e.getMessage(), e);
            Log.e("RFIDHandler", "Operation Failure Reason: " + e.getStatusDescription());
        }
    }

    public synchronized void stopInventory() {
        // check reader connection
        if (!isReaderConnected()) return;
        try {
            reader.Actions.Inventory.stop();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public void setHandler(ResponseHandlerInterface status) {
        handler = status;
    }


    public interface ResponseHandlerInterface {

        void handleTagData(TagData[] tagData);

        void handleTriggerPress(boolean pressed);

        void handleSetText(String msg);

        boolean showStatus();
    }

    // Enumerates SDK based on host device
    private class CreateInstanceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "CreateInstanceTask");
            // Based on support available on host device choose the reader type
            InvalidUsageException invalidUsageException = null;
            readers = new Readers(context, ENUM_TRANSPORT.ALL); //changed from USB to all for MC3300
            try {
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            }
            if (invalidUsageException != null) {
                readers.Dispose();
                readers = null;
                if (readers == null) {
                    readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new ConnectionTask().execute();
        }
    }

    private class ConnectionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            Log.d(TAG, "ConnectionTask");
            GetAvailableReader();
            if (reader != null) return connect();
            return "Failed to find or connect reader";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (handler != null) {
                handler.handleSetText(result);
            }
        }
    }

    // Read/Status Notify handler
    // Implement the RfidEventsLister class to receive event notifications
    public class EventHandler implements RfidEventsListener {
        // Read Event Notification
        public void eventReadNotify(RfidReadEvents e) {
            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            TagData[] myTags = reader.Actions.getReadTags(100);
            stopInventory();
            if (myTags != null) {
                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID " + myTags[0].getTagID());
                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ && myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (!myTags[index].getMemoryBankData().isEmpty()) {
                            Log.d(TAG, " Mem Bank Data " + myTags[index].getMemoryBankData());
                        }
                    }
                    if (myTags[0].isContainsLocationInfo()) {
                        short dist = myTags[0].LocationInfo.getRelativeDistance();
                        Log.d(TAG, "Tag relative distance " + dist);
                    }
                }
                // possibly if operation was invoked from async task and still busy
                // handle tag data responses on parallel thread thus THREAD_POOL_EXECUTOR
                new AsyncDataUpdate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, myTags);
            }
        }

        //Status Event Notification
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            if (handler != null) {
                                handler.handleTriggerPress(true);
                            }
                            return null;
                        }
                    }.execute();
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            if (handler != null) {
                                handler.handleTriggerPress(false);
                            }
                            return null;
                        }
                    }.execute();
                }
            }
        }
    }

    private class AsyncDataUpdate extends AsyncTask<TagData[], Void, Void> {
        @Override
        protected Void doInBackground(TagData[]... params) {
            if (handler != null) {
                handler.handleTagData(params[0]);
            }
            return null;
        }
    }

    public String onResume() {
        if (!isReaderConnected()) {
            return connect();
        } else {
            return "Connected";
        }
    }

}
