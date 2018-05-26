package com.example.water.cproject.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    private TextView peripheralName;
    private TextView peripheralAddress;
    private TextView machineStatus;
    private TextView machineSpeed;
    private TextView machineState;
    private TextView machineMode;
    private TextView angleX, angleY, angleZ;
    private EditText pidP, pidI, pidD;
    private SharedPreferences pref;

    public Fragment1() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        this.machine = mainActivity.machine;
        this.ble = machine.getControlBoard().getBLE();
        pref = mainActivity.getSharedPreferences("PID",0);

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

        pidP = mView.findViewById(R.id.pid_p);
        pidI = mView.findViewById(R.id.pid_i);
        pidD = mView.findViewById(R.id.pid_d);

        int[] pid = new int[3];
        pid[0] = pref.getInt("P",1);
        pid[1] = pref.getInt("I",100);
        pid[2] = pref.getInt("D",2);

        machine.setPID(pid[0], pid[1], pid[2]);
        pidP.setText(String.format(Locale.getDefault(),"%d",pid[0]));
        pidI.setText(String.format(Locale.getDefault(),"%d",pid[1]));
        pidD.setText(String.format(Locale.getDefault(),"%d",pid[2]));

        Button btnPID = mView.findViewById(R.id.button_pid);
        btnPID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int p = Integer.valueOf(pidP.getText().toString());
                int i = Integer.valueOf(pidI.getText().toString());
                int d = Integer.valueOf(pidD.getText().toString());
                machine.setPID(p, i, d);
                machine.sendPID();
                SharedPreferences.Editor edit = pref.edit();

                edit.putInt("P",p);
                edit.putInt("I",i);
                edit.putInt("D",d);
                edit.commit();
            }
        });

        SeekBar sbSpeed = mView.findViewById(R.id.seekBarSpeed);
        SeekBar sbSpeedLeft = mView.findViewById(R.id.seekBarLeftSpeed);
        SeekBar sbSpeedRight = mView.findViewById(R.id.seekBarRightSpeed);

        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                machine.setRunSpeed(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress;
                progress = machine.getRunSpeed();
                machineSpeed.setText(String.valueOf(progress));
                machine.sendSpeed();
            }
        });
        sbSpeedLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                machine.setSpeedOffsetLeft(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                machine.sendSpeed();
            }
        });
        sbSpeedRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                machine.setSpeedOffsetRight(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                machine.sendSpeed();
            }
        });
    }
}
