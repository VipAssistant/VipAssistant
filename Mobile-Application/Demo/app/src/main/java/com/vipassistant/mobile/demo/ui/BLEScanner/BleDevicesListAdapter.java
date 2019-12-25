package com.vipassistant.mobile.demo.ui.BLEScanner;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.vipassistant.mobile.demo.R;

public class BleDevicesListAdapter extends ArrayAdapter<BleDevice> {

    BLEScannerFragment bleScannerFragment;
    int layoutResourceID;
    ArrayList<BleDevice> devices;

    public BleDevicesListAdapter(BLEScannerFragment bleScannerFragment, int resource, ArrayList<BleDevice> objects) {
        super(bleScannerFragment.getActivity(), resource, objects); // TODO Check

        this.bleScannerFragment = bleScannerFragment;
        layoutResourceID = resource;
        devices = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) bleScannerFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }

        BleDevice device = devices.get(position);
        String name = device.getBleName();
        String address = device.getMacAddress();
        int rssi = device.getRssiValue();

        TextView tv_name = convertView.findViewById(R.id.tv_name);
        if (name != null && name.length() > 0) {
            tv_name.setText(name);
        }
        else {
            tv_name.setText("No Name");
        }

        TextView tv_rssi = convertView.findViewById(R.id.tv_rssi);
        tv_rssi.setText("RSSI: " + Integer.toString(rssi));

        TextView tv_macaddr = convertView.findViewById(R.id.tv_macaddr);
        if (address != null && address.length() > 0) {
            tv_macaddr.setText(device.getMacAddress());
        }
        else {
            tv_macaddr.setText("No Address");
        }

        TextView tv_distance = convertView.findViewById(R.id.tv_distance);
        double distance = Math.pow(10d, ((double) -60 - rssi) / (10 * 2));
        DecimalFormat stringDistance = new DecimalFormat("#.00");
        tv_distance.setText("Distance: " + String.format("%.2f", (distance)));

        return convertView;
    }
}

