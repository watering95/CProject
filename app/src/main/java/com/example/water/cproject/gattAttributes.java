package com.example.water.cproject;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
/**
 * Created by water on 2017-04-18.
 */

@SuppressWarnings("DefaultFileTemplate")
class gattAttributes {

    private static final String UUID_GYRO_X_MEASUREMENT = "0000bbb1-0000-1000-8000-00805f9b34fb";
    private static final String UUID_GYRO_Y_MEASUREMENT = "0000bbb2-0000-1000-8000-00805f9b34fb";
    private static final String UUID_GYRO_Z_MEASUREMENT = "0000bbb3-0000-1000-8000-00805f9b34fb";
    private static final String UUID_ACCL_X_MEASUREMENT = "0000bbb4-0000-1000-8000-00805f9b34fb";
    private static final String UUID_ACCL_Y_MEASUREMENT = "0000bbb5-0000-1000-8000-00805f9b34fb";
    private static final String UUID_ACCL_Z_MEASUREMENT = "0000bbb6-0000-1000-8000-00805f9b34fb";
    static final String UUID_MOTOR_DIRECTION = "0000bbb7-0000-1000-8000-00805f9b34fb";
    static final String UUID_MOTOR_LEFT_SPEED = "0000bbb8-0000-1000-8000-00805f9b34fb";
    static final String UUID_MOTOR_RIGHT_SPEED = "0000bbb9-0000-1000-8000-00805f9b34fb";

    private final static String GYRO_X_DATA =
            "com.example.bluetooth.le.GYRO_X_DATA";
    private final static String GYRO_Y_DATA =
            "com.example.bluetooth.le.GYRO_Y_DATA";
    private final static String GYRO_Z_DATA =
            "com.example.bluetooth.le.GYRO_Z_DATA";
    private final static String ACCL_X_DATA =
            "com.example.bluetooth.le.ACCL_X_DATA";
    private final static String ACCL_Y_DATA =
            "com.example.bluetooth.le.ACCL_Y_DATA";
    private final static String ACCL_Z_DATA =
            "com.example.bluetooth.le.ACCL_Z_DATA";
    private final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    @SuppressWarnings("unchecked")
    private static final HashMap<String, String> attributes = new HashMap();

    static {
        // Sample Services.
        attributes.put("0000bbb0-0000-1000-8000-00805f9b34fb", "Gyro Measurement Service");
        // Sample Characteristics.
        attributes.put(UUID_GYRO_X_MEASUREMENT, "Gyro X Measurement");
        attributes.put(UUID_GYRO_Y_MEASUREMENT, "Gyro Y Measurement");
        attributes.put(UUID_GYRO_Z_MEASUREMENT, "Gyro Z Measurement");
        attributes.put(UUID_ACCL_X_MEASUREMENT, "Accelerometer X Measurement");
        attributes.put(UUID_ACCL_Y_MEASUREMENT, "Accelerometer Y Measurement");
        attributes.put(UUID_ACCL_Z_MEASUREMENT, "Accelerometer Z Measurement");
        attributes.put(UUID_MOTOR_DIRECTION, "Motor Direction");
        attributes.put(UUID_MOTOR_LEFT_SPEED, "Motor Left Speed");
        attributes.put(UUID_MOTOR_RIGHT_SPEED, "Motor Right Speed");
    }

    static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

    static Intent intentPutExtra(Intent intent, BluetoothGattCharacteristic characteristic) {
        if (UUID_GYRO_X_MEASUREMENT.equals(characteristic.getUuid().toString())) {
            intent.putExtra(GYRO_X_DATA, String.valueOf(changeFloatByteOrder(characteristic.getValue(), ByteOrder.LITTLE_ENDIAN)));
        } else if(UUID_GYRO_Y_MEASUREMENT.equals(characteristic.getUuid().toString())) {
            intent.putExtra(GYRO_Y_DATA, String.valueOf(changeFloatByteOrder(characteristic.getValue(),ByteOrder.LITTLE_ENDIAN)));
        } else if(UUID_GYRO_Z_MEASUREMENT.equals(characteristic.getUuid().toString())) {
            intent.putExtra(GYRO_Z_DATA, String.valueOf(changeFloatByteOrder(characteristic.getValue(),ByteOrder.LITTLE_ENDIAN)));
        } else if (UUID_ACCL_X_MEASUREMENT.equals(characteristic.getUuid().toString())) {
            intent.putExtra(ACCL_X_DATA, String.valueOf(changeFloatByteOrder(characteristic.getValue(),ByteOrder.LITTLE_ENDIAN)));
        } else if(UUID_ACCL_Y_MEASUREMENT.equals(characteristic.getUuid().toString())) {
            intent.putExtra(ACCL_Y_DATA, String.valueOf(changeFloatByteOrder(characteristic.getValue(),ByteOrder.LITTLE_ENDIAN)));
        } else if(UUID_ACCL_Z_MEASUREMENT.equals(characteristic.getUuid().toString())) {
            intent.putExtra(ACCL_Z_DATA, String.valueOf(changeFloatByteOrder(characteristic.getValue(),ByteOrder.LITTLE_ENDIAN)));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        return intent;
    }

    static private float changeFloatByteOrder(byte[] v, ByteOrder order) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.put(v).flip();
        return b.order(order).getFloat();
    }

    static private int changeIntByteOrder(byte[] v, ByteOrder order) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.put(v).flip();
        return b.order(order).getInt();
    }
}
