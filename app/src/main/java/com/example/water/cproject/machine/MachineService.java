package com.example.water.cproject.machine;

import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;

import com.example.water.cproject.ble.gattAttributes;

import java.nio.ByteBuffer;

import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.example.water.cproject.ble.BLEService.ACTION_DATA_AVAILABLE;

/**
 * Created by watering on 18. 3. 13.
 */

@SuppressWarnings("DefaultFileTemplate")
public class MachineService extends Service {

    public final static String ACTION = "ACTION_DATA_TRANSFER";
    public final static String DATA = "IMU_DATA";

    public MachineService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void initialize() {
        registerReceiver(broadcastReceiver, makeGattUpdateIntentFilter());
    }

    private final IBinder binder = new MachineService.LocalBinder();
    private void broadcastUpdate(final String action, Bundle data) {
        final Intent intent = new Intent(action);

        intent.putExtra(DATA, data);
        sendBroadcast(intent);
    }
    private String getString(byte[] data) {
        return new String(data);
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(ACTION_DATA_AVAILABLE);

        return intentFilter;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Bundle bundleBLE = new Bundle();

            if (ACTION_DATA_AVAILABLE.equals(action)) {
                ByteBuffer receiveBuffer = ByteBuffer.wrap(intent.getByteArrayExtra(gattAttributes.STATE_MACHINE));
                Map<String, byte[]> byteMaps = new ArrayMap<>();
                String key[] = {"machineMode", "machineState", "angleX", "angleY", "angleZ"};

                byte b;

                for (String aKey : key) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(20);

                    do {
                        if(receiveBuffer.hasRemaining()) b = receiveBuffer.get();
                        else break;

                        if (b != ',') {
                            byteBuffer.put(b);
                        } else {
                            byteBuffer.flip();
                            byte[] data = new byte[byteBuffer.remaining()];
                            byteBuffer.get(data);
                            byteMaps.put(aKey, data);
                            byteBuffer.clear();
                        }
                    } while (b != ',');
                }

                try {
                    int mode = Integer.valueOf(getString(byteMaps.get(key[0])));
                    int state = Integer.valueOf(getString(byteMaps.get(key[1])));
                    float[] angle = new float[3];

                    for (int i = 0; i < 3; i++) {
                        angle[i] = Float.valueOf(getString(byteMaps.get(key[i + 2])));
                    }
                    byteMaps.clear();

                    bundleBLE.putInt("mode", mode);
                    bundleBLE.putInt("state", state);
                    bundleBLE.putFloatArray("angle", angle);

                    broadcastUpdate(ACTION, bundleBLE);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
    };

    public class LocalBinder extends Binder {
        public MachineService getService() {
            return MachineService.this;
        }
    }
}
