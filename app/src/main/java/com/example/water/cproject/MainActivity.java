package com.example.water.cproject;

import android.Manifest;
import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.water.cproject.ble.BLE;
import com.example.water.cproject.ble.BLEService;
import com.example.water.cproject.fragment.Fragment1;
import com.example.water.cproject.fragment.Fragment2;
import com.example.water.cproject.genuino.IMU;
import com.example.water.cproject.machine.IMUService;
import com.example.water.cproject.machine.Machine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner BLEScanner;
    private String dataCode;

    public final DBResolver resolver = new DBResolver(this);

    public Machine machine;
    private final ScheduledJob scheduledJob = new ScheduledJob();
    private final Timer jobScheduler = new Timer();

    private boolean isScanningMachine;
    private boolean isFindMachine;

    private static final boolean SCAN_START = true;
    private static final boolean SCAN_STOP = false;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final long READ_PERIOD = 1000;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Fragment1 fragment1;
    private Fragment2 fragment2;

    public Frag1Callback frag1Callback;
    private Frag2Callback frag2Callback;

    public interface Frag1Callback {
        void updateMachineState(int resourceId);
        void updateMotorState(int state);
        void updateAngle(IMU gyro);
        void updatePeripheral(String name, String address);
    }
    public interface Frag2Callback {
        void updateView();
    }

    public void setFrag1Callback(Frag1Callback callback) {
        this.frag1Callback = callback;
    }
    public void setFrag2Callback(Frag2Callback callback) {
        this.frag2Callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resolver.getContentResolver(getContentResolver());

        initLayout();
        initBLE();
        blePermission();

        machine = new Machine();
        handler = new Handler();

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
            case R.id.buttonAutoRun:
                machine.operate(machine.IS_AUTO);
                machine.operate(machine.MOTOR_RUN);
                break;
            case R.id.buttonAutoStop:
                machine.operate(machine.MOTOR_STOP);
                machine.operate(machine.IS_MANUAL);
                break;
            case R.id.buttonRun:
                machine.operate(machine.MOTOR_RUN);
                boolean dataRecord = true;
                makeCode();
                break;
            case R.id.buttonStop:
                machine.operate(machine.MOTOR_STOP);
                dataRecord = false;
                break;
            case R.id.buttonRight:
                machine.operate(machine.MOTOR_RIGHT);
                break;
            case R.id.buttonLeft:
                machine.operate(machine.MOTOR_LEFT);
                break;
            case R.id.buttonBack:
                machine.operate(machine.MOTOR_BACKWARD);
                break;
            default:
                machine.operate(machine.MOTOR_STOP);
                dataRecord = false;
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

    private void initLayout() {
        TabLayout mainTabLayout = findViewById(R.id.tab_main);

        mainTabLayout.setTabTextColors(Color.parseColor("#ffffff"),Color.parseColor("#00ff00"));
        mainTabLayout.addTab(mainTabLayout.newTab().setText("main"));
        mainTabLayout.addTab(mainTabLayout.newTab().setText("monitor"));
        mainTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        fragmentManager = getSupportFragmentManager();
        fragment1 = new Fragment1();
        fragment2 = new Fragment2();

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_main, fragment1).commit();

        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_main, fragment1).commit();
                        break;
                    case 1:
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_main, fragment2).commit();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
    private void blePermission() {
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
    }
    private void initBLE() {
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
    }
    public void displayConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(frag1Callback != null) frag1Callback.updateMachineState(resourceId);
            }
        });
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
                        if(frag1Callback != null) frag1Callback.updatePeripheral("No Device","No Device");
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
                if(frag1Callback != null) frag1Callback.updatePeripheral("No Device","No Device");
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
            frag1Callback.updatePeripheral("No Device","No Device");
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
                        ble.setPeripheralAddress(result.getDevice().getAddress());
                        frag1Callback.updatePeripheral(ble.getName(), ble.getPeripheralAddress());
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
    // ACTION_DATA_AVAILABLE: received imu from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            IMU imu = machine.getControlBoard().getImu();

            assert action != null;
            switch (action) {
                case BLEService.ACTION_GATT_CONNECTED:
                    machine.getControlBoard().getBLE().setConnectState(true);
                    scanBLEDevice(SCAN_STOP);
                    displayConnectionState(R.string.ble_connected);
                    invalidateOptionsMenu();
                    break;
                case BLEService.ACTION_GATT_DISCONNECTED:
                    displayConnectionState(R.string.ble_disconnected);
                    machine.getControlBoard().getBLE().setConnectState(false);
                    isFindMachine = false;
                    invalidateOptionsMenu();
                    break;
                case BLEService.ACTION_GATT_SERVICES_DISCOVERED:
                    // Select Genuino services and characteristics on the user interface.
                    machine.getGattServices();
                    machine.readMachineState();
                    break;
                case IMUService.ACTION:
                    Bundle bundle = intent.getBundleExtra(IMUService.DATA);

                    int state = bundle.getInt("state");
                    float angle[] = bundle.getFloatArray("angle");

                    machine.setMotorState(state);

                    assert angle != null;
                    imu.updateAngle(angle[0], angle[1], angle[2]);

                    if (dataCode != null) {
                        resolver.insertMachine(resolver.getCodeId(dataCode), state, angle);
                    }

                    updateMachineState();

                    break;
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

    private void makeCode() {
        String oldCode = resolver.getLatestCode(getToday());
        int code = 0;

        if(oldCode != null) {
            int codeLength = oldCode.length();
            code = Integer.valueOf(oldCode.substring(codeLength-2,codeLength)) + 1;
        }

        Calendar date = Calendar.getInstance();
        dataCode = String.format(Locale.getDefault(),"%04d%02d%02d%02d", date.get(Calendar.YEAR), date.get(Calendar.MONTH)+1, date.get(Calendar.DATE), code);
        resolver.insertCode(dataCode);
    }

    public String getToday() {
        Calendar today = Calendar.getInstance();

        return String.format(Locale.getDefault(), "%04d-%02d-%02d", today.get(Calendar.YEAR),today.get(Calendar.MONTH)+1,today.get(Calendar.DATE));
    }
    public String getNow() {
        Calendar now = Calendar.getInstance();

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", now.get(Calendar.HOUR),now.get(Calendar.MINUTE),now.get(Calendar.SECOND));
    }
    public String monthChange(String date, int amount) {
        Calendar calendar = strToCalendar(date);

        calendar.add(Calendar.MONTH, amount);

        if (Calendar.getInstance().before(calendar)) {
            Toast.makeText(getApplicationContext(), R.string.toast_date_error, Toast.LENGTH_SHORT).show();
            return date;
        }

        return String.format(Locale.getDefault(),"%d-%02d-%02d",calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DATE));
    }
    public String dateChange(String date, int amount) {
        Calendar calendar = strToCalendar(date);

        calendar.add(Calendar.DATE,amount);

        if (Calendar.getInstance().before(calendar)) {
            Toast.makeText(getApplicationContext(), R.string.toast_date_error, Toast.LENGTH_SHORT).show();
            return date;
        }

        return String.format(Locale.getDefault(),"%d-%02d-%02d",calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DATE));
    }
    private Calendar strToCalendar(String date) {
        Calendar calendar = Calendar.getInstance();

        String year = date.substring(0,4);
        String month = date.substring(5,7);
        String day = date.substring(8,10);

        calendar.set(Integer.parseInt(year),Integer.parseInt(month)-1,Integer.parseInt(day));

        return calendar;
    }
    public String calendarToStr(Calendar calendar) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DATE));
    }

    public void updateMachineState() {
        IMU imu = machine.getControlBoard().getImu();

        frag1Callback.updateAngle(imu.getImu());
        frag1Callback.updateMotorState(machine.getMotorState());
    }
}

