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
    private TextView machineStatus;
    private TextView machineSpeed;
    private TextView machineState;
    private TextView machineMode;
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
                machineStatus.setText(resourceId);
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
                        machineState.setText("Forward");
                        break;
                    case 4:
                        machineState.setText("Left Turn");
                        break;
                    case 3:
                        machineState.setText("Right Turn");
                        break;
                    case 5:
                        machineState.setText("Backward");
                        break;
                    case 0:
                        machineState.setText("Stop");
                        break;
                }
            }
            @Override
            public void updateMachineMode(int mode) {
                switch(mode) {
                    case 1:
                        machineMode.setText("Auto");
                        break;
                    case 0:
                        machineMode.setText("Manual");
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
            machineStatus.setText(R.string.ble_connected);
            peripheralName.setText(ble.getName());
            peripheralAddress.setText(ble.getPeripheralAddress());
        }
        else {
            machineStatus.setText(R.string.ble_disconnected);
        }
        machineSpeed.setText(String.valueOf(machine.getRunSpeed()));
    }

    private void initLayout() {
        peripheralName = mView.findViewById(R.id.deviceName);
        peripheralAddress = mView.findViewById(R.id.deviceAddress);
        machineStatus = mView.findViewById(R.id.machineStatus);
        machineMode = mView.findViewById(R.id.machineMode);
        machineSpeed = mView.findViewById(R.id.motorSpeed);
        machineState = mView.findViewById(R.id.motorState);

        machineMode.setText("Manual");
        machineSpeed.setText("0");
        machineState.setText("Stop");

        angleX = mView.findViewById(R.id.anglex);
        angleY = mView.findViewById(R.id.angley);
        angleZ = mView.findViewById(R.id.anglez);

        SeekBar sbSpeed = mView.findViewById(R.id.seekBarSpeed);
        SeekBar sbSpeedLeft = mView.findViewById(R.id.seekBarLeftSpeed);
        SeekBar sbSpeedRight = mView.findViewById(R.id.seekBarRightSpeed);

        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                machine.setRunSpeed(progress);
                machine.sendLeftSpeed();
                machine.sendRightSpeed();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress;
                progress = machine.getRunSpeed();
                machineSpeed.setText(String.valueOf(progress));
            }
        });
        sbSpeedLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                machine.setSpeedOffsetLeft(progress);
                machine.sendLeftSpeed();
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
                machine.setSpeedOffsetRight(progress);
                machine.sendRightSpeed();
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
