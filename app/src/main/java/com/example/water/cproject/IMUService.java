package com.example.water.cproject;

import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;

import java.nio.ByteBuffer;

import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.example.water.cproject.BLEService.ACTION_DATA_AVAILABLE;

/**
 * Created by watering on 18. 3. 13.
 */

@SuppressWarnings("DefaultFileTemplate")
public class IMUService extends Service {

    final static String ACTION = "ACTION_DATA_TRANSFER";
    final static String DATA = "IMU_DATA";

    public IMUService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void initialize() {
        registerReceiver(broadcastReceiver, makeGattUpdateIntentFilter());
    }

    private final IBinder binder = new IMUService.LocalBinder();
    private void broadcastUpdate(final String action, float[] data) {
        final Intent intent = new Intent(action);

        intent.putExtra(DATA, data);
        sendBroadcast(intent);
    }
    private String getString(ByteBuffer buffer) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

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

            if (ACTION_DATA_AVAILABLE.equals(action)) {
                ByteBuffer receiveBuffer = ByteBuffer.wrap(intent.getByteArrayExtra(gattAttributes.MACHINE_STATE));
                Map<String, ByteBuffer> byteBuffers = new ArrayMap<>();
                String key[] = {"machineState", "gx", "gy", "gz", "ax", "ay", "az"};

                byte b;

                for (String aKey : key) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(20);

                    do {
                        b = receiveBuffer.get();

                        if (b != ',') {
                            byteBuffer.put(b);
                        } else {
                            byteBuffer.flip();
                            byteBuffers.put(aKey, byteBuffer);
                            byteBuffer.clear();
                        }
                    } while (b != ',');
                }

                try {
                    float data[] = new float[6];
                    for (int i = 0; i < 6; i++) {
                        data[i] = Float.valueOf(getString(byteBuffers.get(key[i + 1])));
                    }
                    byteBuffers.clear();

                    broadcastUpdate(ACTION, data);
                } catch (Exception e) {
                    Log.i(TAG, "Byte[] to String convert Error");
                }
            }
        }
    };

    class LocalBinder extends Binder {
        IMUService getService() {
            return IMUService.this;
        }
    }
}
