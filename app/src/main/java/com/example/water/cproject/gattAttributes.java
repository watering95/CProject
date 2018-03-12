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
    private static final String UUID_MACHINE_SERVICE = "0000bbb0-0000-1000-8000-00805f9b34fb";
    static final String UUID_MACHINE_STATE = "0000bbb1-0000-1000-8000-00805f9b34fb";

    private static final String UUID_MOTOR_SERVICE = "00000174-0000-1000-8000-00805f9b34fb";
    static final String UUID_MOTOR_DIRECTION = "00000175-0000-1000-8000-00805f9b34fb";
    static final String UUID_MOTOR_LEFT_SPEED = "00000176-0000-1000-8000-00805f9b34fb";
    static final String UUID_MOTOR_RIGHT_SPEED = "00000177-0000-1000-8000-00805f9b34fb";

    final static String MACHINE_STATE =
            "com.example.bluetooth.le.MACHINE_STATE";
    private final static String MOTOR_DIRECTION =
            "com.example.bluetooth.le.MOTOR_DIRECTION";
    private final static String MOTOR_LEFT_SPEED =
            "com.example.bluetooth.le.MOTOR_LEFT_SPEED";
    private final static String MOTOR_RIGHT_SPEED =
            "com.example.bluetooth.le.MOTOR_RIGHT_SPEED";

    private final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    @SuppressWarnings("unchecked")
    private static final HashMap<String, String> attributes = new HashMap();

    static {
        attributes.put(UUID_MACHINE_SERVICE, "Machine Service");
        attributes.put(UUID_MACHINE_STATE, "Motor State");

        attributes.put(UUID_MOTOR_SERVICE, "Motor Service");
        attributes.put(UUID_MOTOR_DIRECTION, "Motor Direction");
        attributes.put(UUID_MOTOR_LEFT_SPEED, "Motor Left Speed");
        attributes.put(UUID_MOTOR_RIGHT_SPEED, "Motor Right Speed");
    }

    static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
    static Intent intentPutExtra(String action, BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();

        switch(characteristic.getUuid().toString()) {
            case UUID_MOTOR_DIRECTION:
                intent.putExtra(MOTOR_DIRECTION, String.valueOf(changeIntByteOrder(data,ByteOrder.LITTLE_ENDIAN)));
                break;
            case UUID_MOTOR_LEFT_SPEED:
                intent.putExtra(MOTOR_LEFT_SPEED, String.valueOf(changeIntByteOrder(data,ByteOrder.LITTLE_ENDIAN)));
                break;
            case UUID_MOTOR_RIGHT_SPEED:
                intent.putExtra(MOTOR_RIGHT_SPEED, String.valueOf(changeIntByteOrder(data,ByteOrder.LITTLE_ENDIAN)));
                break;
            case UUID_MACHINE_STATE:
                intent.putExtra(MACHINE_STATE, data);
                break;
            default:
                // For all other profiles, writes the data formatted in HEX.
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for(byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                    intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
                }
                break;
        }
        return intent;
    }

    static private int changeIntByteOrder(byte[] v, ByteOrder order) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.put(v).flip();
        return b.order(order).getInt();
    }
}
