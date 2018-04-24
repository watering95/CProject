package com.example.water.cproject.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.water.cproject.MainActivity;
import com.example.water.cproject.R;
import com.example.water.cproject.ble.BLE;
import com.example.water.cproject.genuino.IMU;
import com.example.water.cproject.machine.Machine;

import java.util.Locale;

/**
 * Created by watering on 18. 3. 15.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Fragment1 extends Fragment {

    private BLE ble;
    private Machine machine;
    private View mView;
    private MainActivity mainActivity;
    private TextView peripheralName;
    private TextView peripheralAddress;
    private TextView deviceStatus;
    private TextView motorSpeed;
    private TextView motorState;
    private TextView angleX, angleY, angleZ;

    public Fragment1() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        this.machine = mainActivity.machine;
        this.ble = machine.getControlBoard().getBLE();

        mainActivity.setFrag1Callback(new MainActivity.Frag1Callback() {
            @Override
            public void updateMachineState(int resourceId) {
                deviceStatus.setText(resourceId);
            }
            @Override
            public void updateAngle(IMU imu) {
                angleX.setText(String.format(Locale.getDefault(), "Roll : %.03f",imu.getX()));
                angleY.setText(String.format(Locale.getDefault(), "Pitch : %.03f",imu.getY()));
                angleZ.setText(String.format(Locale.getDefault(), "Yaw : %.03f",imu.getZ()));
            }

            @Override
            public void updatePeripheral(String name, String address) {
                peripheralName.setText(name);
                peripheralAddress.setText(address);
            }
            @Override
            public void updateMotorState(int state) {
                switch(state) {
                    case 2:
                        motorState.setText("Forward");
                        break;
                    case 4:
                        motorState.setText("Left Turn");
                        break;
                    case 3:
                        motorState.setText("Right Turn");
                        break;
                    case 5:
                        motorState.setText("Backward");
                        break;
                    case 0:
                        motorState.setText("Stop");
                        break;
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment1, container, false);

        initLayout();

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(ble.getConnectState()) {
            deviceStatus.setText(R.string.ble_connected);
            peripheralName.setText(ble.getName());
            peripheralAddress.setText(ble.getPeripheralAddress());
        }
        else {
            deviceStatus.setText(R.string.ble_disconnected);
        }
        motorSpeed.setText(String.valueOf(machine.getRunSpeed()));
    }

    private void initLayout() {
        peripheralName = mView.findViewById(R.id.deviceName);
        peripheralAddress = mView.findViewById(R.id.deviceAddress);
        deviceStatus = mView.findViewById(R.id.deviceStatus);
        motorSpeed = mView.findViewById(R.id.motorSpeed);
        motorState = mView.findViewById(R.id.motorState);

        motorSpeed.setText("0");
        motorState.setText("Stop");

        angleX = mView.findViewById(R.id.anglex);
        angleY = mView.findViewById(R.id.angley);
        angleZ = mView.findViewById(R.id.anglez);

        SeekBar sbSpeed = mView.findViewById(R.id.seekBarSpeed);
        SeekBar sbSpeedLeft = mView.findViewById(R.id.seekBarLeftSpeed);
        SeekBar sbSpeedRight = mView.findViewById(R.id.seekBarRightSpeed);

        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mainActivity.machine.setRunSpeed(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress;
                progress = mainActivity.machine.getRunSpeed();
                motorSpeed.setText(String.valueOf(progress));
            }
        });
        sbSpeedLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mainActivity.machine.setSpeedOffsetLeft(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbSpeedRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mainActivity.machine.setSpeedOffsetRight(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
