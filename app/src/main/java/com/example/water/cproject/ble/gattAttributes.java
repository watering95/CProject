package com.example.water.cproject.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
/**
 * Created by water on 2017-04-18.
 */

@SuppressWarnings("DefaultFileTemplate")
public class gattAttributes {
    private static final String UUID_STATE_SERVICE = "0000bbb0-0000-1000-8000-00805f9b34fb";
    public static final String UUID_STATE_MACHINE = "0000bbb1-0000-1000-8000-00805f9b34fb";

    private static final String UUID_MOTOR_SERVICE = "00000174-0000-1000-8000-00805f9b34fb";
    public static final String UUID_MOTOR_DIRECTION = "00000175-0000-1000-8000-00805f9b34fb";
    public static final String UUID_MOTOR_SPEED = "00000176-0000-1000-8000-00805f9b34fb";

    private static final String UUID_OPERATE_SERVICE = "00000177-0000-1000-8000-00805f9b34fb";
    public static final String UUID_OPERATE_MODE = "00000178-0000-1000-8000-00805f9b34fb";
    public static final String UUID_OPERATE_PID = "00000179-0000-1000-8000-00805f9b34fb";

    public final static String STATE_MACHINE =
            "com.example.bluetooth.le.state_machine";
    private final static String MOTOR_DIRECTION =
            "com.example.bluetooth.le.motor_direction";
    private final static String MOTOR_SPEED =
            "com.example.bluetooth.le.motor_speed";
    private final static String OPERATE_MODE =
            "com.example.bluetooth.le.operate_mode";
    private final static String OPERATE_PID =
            "com.example.bluetooth.le.operate_pid";

    private final static String EXTRA_DATA =
            "com.example.bluetooth.le.extra_data";

    @SuppressWarnings("unchecked")
    private static final HashMap<String, String> attributes = new HashMap();

    static {
        attributes.put(UUID_STATE_SERVICE, "Machine Service");
        attributes.put(UUID_STATE_MACHINE, "Machine State");

        attributes.put(UUID_MOTOR_SERVICE, "Motor Service");
        attributes.put(UUID_MOTOR_DIRECTION, "Motor Direction");
        attributes.put(UUID_MOTOR_SPEED, "Motor Speed");

        attributes.put(UUID_OPERATE_SERVICE, "Operate Service");
        attributes.put(UUID_OPERATE_MODE, "Auto mode");
        attributes.put(UUID_OPERATE_PID, "PID Gain");
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
            case UUID_MOTOR_SPEED:
                intent.putExtra(MOTOR_SPEED, String.valueOf(changeIntByteOrder(data,ByteOrder.LITTLE_ENDIAN)));
                break;
            case UUID_OPERATE_MODE:
                intent.putExtra(OPERATE_MODE, String.valueOf(changeIntByteOrder(data,ByteOrder.LITTLE_ENDIAN)));
                break;
            case UUID_OPERATE_PID:
                intent.putExtra(OPERATE_PID, String.valueOf(changeIntByteOrder(data,ByteOrder.LITTLE_ENDIAN)));
                break;
            case UUID_STATE_MACHINE:
                intent.putExtra(STATE_MACHINE, data);
                break;
            default:
                // For all other profiles, writes the imu formatted in HEX.
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
