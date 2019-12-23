package com.vipassistant.mobile.demo.ui.BLEScanner;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.vipassistant.mobile.demo.R;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public class BLEScannerFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final int REQUEST_ENABLE_BT = 1;

    private HashMap<String, BleDevice> bleDeviceHashMap;
    private ArrayList<BleDevice> bleDeviceArrayList;
    private BleDevicesListAdapter adapter;

    private Button btn_Scan;
    private BleScanner bleScanner;

    private BLEScannerViewModel BLEScannerViewModel;

    private Context context;
    private PackageManager pm ;

    public BLEScannerFragment(){
        //Empty Constructor
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        BLEScannerViewModel =
                ViewModelProviders.of(this).get(BLEScannerViewModel.class);
        View root = inflater.inflate(R.layout.fragment_blescanner, container, false);
        /*
        final TextView textView = root.findViewById(R.id.text_blescanner);
        BLEScannerViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        context = getActivity();
        pm = context.getPackageManager();

        if (!pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utilities.toast(context, "BLE not supported");
            getActivity().finish();
        }

        bleScanner = new BleScanner(getActivity(), this, 7500, -75);

        bleDeviceHashMap = new HashMap<>();
        bleDeviceArrayList = new ArrayList<>();

        adapter = new BleDevicesListAdapter(getActivity(), R.layout.device_item, bleDeviceArrayList);

        ListView listView = new ListView(context);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        btn_Scan = root.findViewById(R.id.btn_scan);
        ScrollView sc = ((ScrollView) root.findViewById(R.id.scrollView));
        sc.addView(listView);
        root.findViewById(R.id.btn_scan).setOnClickListener(this);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() { super.onResume(); }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == getActivity().RESULT_OK) {
            }
            else if (resultCode == getActivity().RESULT_CANCELED) {
                Utilities.toast(context, "Please turn on Bluetooth");
            }
        }
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_scan:
                Utilities.toast(context, "Scan Button Pressed");

                if (!bleScanner.getScanningState()) {
                    startScan();
                }
                else {
                    stopScan();
                }

                break;
            default:
                break;
        }
    }

    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();
        if (!bleDeviceHashMap.containsKey(address)) {
            BleDevice bleDevice = new BleDevice(device);
            bleDevice.setRssiValue(rssi);

            bleDeviceHashMap.put(address, bleDevice);
            bleDeviceArrayList.add(bleDevice);
        }
        else {
            bleDeviceHashMap.get(address).setRssiValue(rssi);
        }

        adapter.notifyDataSetChanged();
    }

    public void startScan(){
        btn_Scan.setText("Scanning");

        bleDeviceArrayList.clear();
        bleDeviceHashMap.clear();

        adapter.notifyDataSetChanged();

        bleScanner.start();
    }

    public void stopScan() {
        btn_Scan.setText("Scan");

        bleScanner.stop();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

}