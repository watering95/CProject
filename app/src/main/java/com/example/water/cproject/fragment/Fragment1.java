package com.example.water.cproject.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.water.cproject.genuino.Accelerometer;
import com.example.water.cproject.genuino.Gyroscope;
import com.example.water.cproject.MainActivity;
import com.example.water.cproject.R;

import java.util.Locale;

/**
 * Created by watering on 18. 3. 15.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Fragment1 extends Fragment {

    private View mView;
    private MainActivity mainActivity;
    private TextView peripheralName;
    private TextView peripheralAddress;
    private TextView deviceStatus;
    private TextView motorSpeed;
    private TextView motorState;
    private TextView gyroX, gyroY, gyroZ;
    private TextView accelerometerX, accelerometerY, accelerometerZ;

    public Fragment1() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        mainActivity.setFrag1Callback(new MainActivity.Frag1Callback() {
            @Override
            public void updateMachineState(int resourceId) {
                deviceStatus.setText(resourceId);
            }
            @Override
            public void updateGyro(Gyroscope gyro) {
                gyroX.setText(String.format(Locale.getDefault(), "%.03f",gyro.getX()));
                gyroY.setText(String.format(Locale.getDefault(), "%.03f",gyro.getY()));
                gyroZ.setText(String.format(Locale.getDefault(), "%.03f",gyro.getZ()));
            }
            @Override
            public void updateAccelerometer(Accelerometer accelerometer) {
                accelerometerX.setText(String.format(Locale.getDefault(), "%.03f",accelerometer.getX()));
                accelerometerY.setText(String.format(Locale.getDefault(), "%.03f",accelerometer.getY()));
                accelerometerZ.setText(String.format(Locale.getDefault(), "%.03f",accelerometer.getZ()));
            }
            @Override
            public void updatePeripheral(String name, String address) {
                peripheralName.setText(name);
                peripheralAddress.setText(address);
            }
            @Override
            public void updateMotorState(int state) {
                switch(state) {
                    case 1:
                        motorState.setText("Run");
                        break;
                    case 4:
                        motorState.setText("Back");
                        break;
                    case 3:
                        motorState.setText("Left");
                        break;
                    case 2:
                        motorState.setText("Right");
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

    private void initLayout() {
        peripheralName = mView.findViewById(R.id.deviceName);
        peripheralAddress = mView.findViewById(R.id.deviceAddress);
        deviceStatus = mView.findViewById(R.id.deviceStatus);
        motorSpeed = mView.findViewById(R.id.motorSpeed);
        motorState = mView.findViewById(R.id.motorState);

        motorSpeed.setText("0");
        motorState.setText("Stop");

        gyroX = mView.findViewById(R.id.gx);
        gyroY = mView.findViewById(R.id.gy);
        gyroZ = mView.findViewById(R.id.gz);
        accelerometerX = mView.findViewById(R.id.ax);
        accelerometerY = mView.findViewById(R.id.ay);
        accelerometerZ = mView.findViewById(R.id.az);

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
