package com.vipassistant.mobile.demo.ui.BLEScanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import android.os.Handler;
import android.bluetooth.le.*;

import androidx.fragment.app.Fragment;

public class BleScanner {

    private BLEScannerFragment bleScannerFragment;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isScanning;
    private Handler handler;
    private long scanPeriod;
    private int minSignalStrength;

    public BleScanner(BLEScannerFragment bleScannerFragment, long scanPeriod, int minSignalStrength){
        this.bleScannerFragment = bleScannerFragment;
        this.scanPeriod = scanPeriod;
        this.minSignalStrength = minSignalStrength;
        handler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) bleScannerFragment.getActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter =bluetoothManager.getAdapter();
    }

    public boolean getScanningState() {
        return this.isScanning;
    }

    public void start () {
        /* if bluetooth is not open, request from user to open bluetooth */
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bleScannerFragment.startActivityForResult(enableBtIntent, BLEScannerFragment.REQUEST_ENABLE_BT);
        }
        else {
            scanLeDevice(true);
        }

        /* if location permissions are not given request it from user */
//        https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            bleScannerFragment.startActivityForResult(enableBtIntent, BLEScannerFragment.REQUEST_ENABLE_BT);
//        }
//
//        else{
//            scanLeDevice(true);
//        }
    }

    public void stop(){
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable && !isScanning) {
            Utilities.toast(bleScannerFragment.getActivity(), "Scanning...");

            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utilities.toast(bleScannerFragment.getActivity(), "Finished BLE Device Scanning");

                    isScanning = false;
                    BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                    bluetoothLeScanner.stopScan(bleScanCallback);

                    bleScannerFragment.stopScan();

                }
            }, scanPeriod);

            isScanning = true;
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(bleScanCallback);
            /* instead of the above, bluetoothAdapter.startLeScan(uuids, mLeScanCallback) can be used
             * if the beacon list and their uuids are known before
             * */
        }
        else {
            isScanning = false;
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.stopScan(bleScanCallback);
        }
    }

    private ScanCallback bleScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    if(result.getRssi() > minSignalStrength){
                        bleScannerFragment.addDevice(result.getDevice(), result.getRssi());
                    }
                }
            };
}

