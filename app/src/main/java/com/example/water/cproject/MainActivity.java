package com.example.water.cproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner BLEScanner;

    private Machine machine;
    private final ScheduledJob scheduledJob = new ScheduledJob();
    private final Timer jobScheduler = new Timer();

    private boolean isScanningMachine;
    private boolean isFindMachine;
    private TextView peripheralName;
    private TextView peripheralAddress;
    private TextView deviceStatus;
    private TextView motorSpeed;
    private TextView motorState;
    private TextView gyroX, gyroY, gyroZ;
    private TextView accelerometerX, accelerometerY, accelerometerZ;

    private static final boolean SCAN_START = true;
    private static final boolean SCAN_STOP = false;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final long READ_PERIOD = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        machine = new Machine();
        handler = new Handler();
        peripheralName = findViewById(R.id.deviceName);
        peripheralAddress = findViewById(R.id.deviceAddress);
        deviceStatus = findViewById(R.id.deviceStatus);
        motorSpeed = findViewById(R.id.motorSpeed);
        motorState = findViewById(R.id.motorState);

        motorSpeed.setText("0");
        motorState.setText("Stop");

        gyroX = findViewById(R.id.gx);
        gyroY = findViewById(R.id.gy);
        gyroZ = findViewById(R.id.gz);
        accelerometerX = findViewById(R.id.ax);
        accelerometerY = findViewById(R.id.ay);
        accelerometerZ = findViewById(R.id.az);

        SeekBar sbSpeed = findViewById(R.id.seekBarSpeed);
        SeekBar sbSpeedLeft = findViewById(R.id.seekBarLeftSpeed);
        SeekBar sbSpeedRight = findViewById(R.id.seekBarRightSpeed);

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
                motorSpeed.setText(String.valueOf(progress));
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

            }
        });

        //BLE Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionResult = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Need Permission").setMessage("This Function need Permission \"COARSE LOCATION\". Continue?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
                                    }
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "Cancel Function", Toast.LENGTH_SHORT).show();
                        }
                    }).create().show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
                }
            }
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BLEScanner = bluetoothAdapter.getBluetoothLeScanner();
        // Checks if Bluetooth LE Scanner is available.
        if (BLEScanner == null) {
            Toast.makeText(this, R.string.ble_scanner_not_find, Toast.LENGTH_SHORT).show();
            finish();
        }

        jobScheduler.scheduleAtFixedRate(scheduledJob, 1000, READ_PERIOD);
    }
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, machine.makeGattUpdateIntentFilter());
        machine.bindService(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        scanBLEDevice(SCAN_START);
    }
    @Override
    protected void onPause() {
        super.onPause();
        scanBLEDevice(SCAN_STOP);
    }
    @Override
    protected void onStop() {
        super.onStop();
        machine.commClose();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (machine.getControlBoard().getBLE().getConnectState()) {
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            if (isFindMachine) {
                menu.findItem(R.id.menu_scan).setVisible(false);
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_stop).setVisible(true);
                menu.findItem(R.id.menu_refresh).setActionView(null);
            } else {
                if (isScanningMachine) {
                    menu.findItem(R.id.menu_scan).setVisible(false);
                    menu.findItem(R.id.menu_connect).setVisible(false);
                    menu.findItem(R.id.menu_disconnect).setVisible(false);
                    menu.findItem(R.id.menu_stop).setVisible(true);
                    menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    menu.findItem(R.id.menu_scan).setVisible(true);
                    menu.findItem(R.id.menu_connect).setVisible(false);
                    menu.findItem(R.id.menu_disconnect).setVisible(false);
                    menu.findItem(R.id.menu_stop).setVisible(false);
                    menu.findItem(R.id.menu_refresh).setActionView(null);
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                scanBLEDevice(SCAN_START);
                break;
            case R.id.menu_stop:
                scanBLEDevice(SCAN_STOP);
                break;
            case R.id.menu_connect:
                machine.commConnect();
                break;
            case R.id.menu_disconnect:
                machine.commDisconnect();
                break;
        }
        return true;
    }

    public void onClick(View v) {
        if (!machine.getControlBoard().getBLE().getConnectState()) return;
        switch (v.getId()) {
            case R.id.buttonRun:
                machine.transferMovingOperation(machine.MACHINE_FORWARD);
                break;
            case R.id.buttonStop:
                machine.transferMovingOperation(machine.MACHINE_STOP);
                break;
            case R.id.buttonRight:
                machine.transferMovingOperation(machine.MACHINE_RIGHT);
                break;
            case R.id.buttonLeft:
                machine.transferMovingOperation(machine.MACHINE_LEFT);
                break;
            case R.id.buttonBack:
                machine.transferMovingOperation(machine.MACHINE_BACKWARD);
                break;
            default:
                machine.transferMovingOperation(machine.MACHINE_STOP);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @SuppressWarnings("NullableProblems") String[] permissions, @SuppressWarnings("NullableProblems") int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                break;
        }
    }

    private void displayConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceStatus.setText(resourceId);
            }
        });
    }
    private void displayMotorState(int state) {
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
    private void displayData(Gyroscope gyro) {
        gyroX.setText(String.format(Locale.getDefault(), "%.03f",gyro.getX()));
        gyroY.setText(String.format(Locale.getDefault(), "%.03f",gyro.getY()));
        gyroZ.setText(String.format(Locale.getDefault(), "%.03f",gyro.getZ()));
    }
    private void displayData(Accelerometer accelerometer) {
        accelerometerX.setText(String.format(Locale.getDefault(), "%.03f",accelerometer.getX()));
        accelerometerY.setText(String.format(Locale.getDefault(), "%.03f",accelerometer.getY()));
        accelerometerZ.setText(String.format(Locale.getDefault(), "%.03f",accelerometer.getZ()));
    }

    private void scanBLEDevice(boolean isScan) {
        final String DEFAULT_BLE_ADDRESS = "98:4F:EE:10:7F:E5";

        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(DEFAULT_BLE_ADDRESS).build();

        filters.add(filter);

        if (isScan) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BLEScanner.stopScan(scanCallback);
                    isScanningMachine = false;
                    if(!machine.getControlBoard().getBLE().getConnectState()) {
                        isFindMachine = false;
                        peripheralName.setText("No Device");
                        peripheralAddress.setText("No Device");
                        displayConnectionState(R.string.stop_ble_scan);
                    }
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            BLEScanner.startScan(filters, settings, scanCallback);
            isScanningMachine = true;
            isFindMachine = false;
            displayConnectionState(R.string.scan_ble);
        } else {
            BLEScanner.stopScan(scanCallback);
            isScanningMachine = false;
            if(!machine.getControlBoard().getBLE().getConnectState()) {
                isFindMachine = false;
                peripheralName.setText("No Device");
                peripheralAddress.setText("No Device");
                displayConnectionState(R.string.stop_ble_scan);
            }
        }
        invalidateOptionsMenu();
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(MainActivity.this, String.valueOf(errorCode), Toast.LENGTH_SHORT).show();
            isScanningMachine = false;
            isFindMachine = false;
            peripheralName.setText("No Device");
            peripheralAddress.setText("No Device");
            displayConnectionState(R.string.fail_ble_scan);
            invalidateOptionsMenu();
        }

        private void processResult(final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isFindMachine && result != null) {
                        BLE ble = machine.getControlBoard().getBLE();

                        ble.setName(result.getDevice().getName());
                        peripheralName.setText(ble.getName());
                        ble.setPeripheralAddress(result.getDevice().getAddress());
                        peripheralAddress.setText(ble.getPeripheralAddress());
                        displayConnectionState(R.string.success_ble_scan);
                        isFindMachine = true;
                        machine.commConnect();
                        invalidateOptionsMenu();
                    }
                }
            });
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Gyroscope gyro = machine.getControlBoard().getGyroscope();
            Accelerometer accelerometer = machine.getControlBoard().getAccelerometer();

            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                machine.getControlBoard().getBLE().setConnectState(true);
                scanBLEDevice(SCAN_STOP);
                displayConnectionState(R.string.ble_connected);
                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                displayConnectionState(R.string.ble_disconnected);
                machine.getControlBoard().getBLE().setConnectState(false);
                isFindMachine = false;
                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Select Genuino services and characteristics on the user interface.
                machine.getGattServices();
                machine.readMachineState();
            } else if (IMUService.ACTION.equals(action)) {
                Bundle bundle = intent.getBundleExtra(IMUService.DATA);

                int state = bundle.getInt("state");
                float imu[] = bundle.getFloatArray("imu");

                machine.setMachineState(state);

                assert imu != null;
                gyro.updateData(imu[0], imu[1], imu[2]);
                accelerometer.updateData(imu[3], imu[4], imu[5]);

                displayData(gyro.getData());
                displayData(accelerometer.getData());
                displayMotorState(machine.getMachineState());
            }
        }
    };

    class ScheduledJob extends TimerTask {
        public void run() {
            if(machine.getControlBoard().getBLE().getConnectState()) {
                machine.readMachineState();
            }
        }
    }
}

