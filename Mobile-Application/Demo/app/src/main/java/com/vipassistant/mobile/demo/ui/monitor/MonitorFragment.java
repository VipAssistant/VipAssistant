package com.vipassistant.mobile.demo.ui.monitor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.vipassistant.mobile.demo.R;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.vipassistant.mobile.demo.ui.constants.Constants.mapRefreshMillis;
import static com.vipassistant.mobile.demo.ui.utils.Utils.buildLoadingDialog;

public class MonitorFragment extends Fragment {

    private MonitorViewModel monitorViewModel;
    private final Handler refresher = new Handler();
    private TextView beaconStatusText;
    private ImageView beaconStatusIcon;
    private Button beaconStatusReportBtn;
    private final String allGoodText = "You get signals from all the beacons in this building, which means you are good to go!";
    private final String problemText = "Oops! You get signals only from %s out of 5 beacons in this building, you can send a status report to VipAssistant Support team to let them know about this issue.";
    private ProgressDialog sendingReportLoading;
    private int sendingStatusReport = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        monitorViewModel = ViewModelProviders.of(this).get(MonitorViewModel.class);
        View root = inflater.inflate(R.layout.fragment_monitor, container, false);

        beaconStatusIcon = (ImageView) root.findViewById(R.id.beacon_status_icon);
        beaconStatusText = (TextView) root.findViewById(R.id.beacon_status_text);
        beaconStatusReportBtn = (Button) root.findViewById(R.id.beacon_status_report_button);
        sendingReportLoading = buildLoadingDialog(getActivity(), "Sending Status Report...");

        /* Also now set-up Handler for periodic Map refreshing */
        this.refresher.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateMonitorFragmentPeriodically();
                refresher.postDelayed(this, mapRefreshMillis);
            }
        }, mapRefreshMillis);

        beaconStatusReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayMonitorReportDialog();
            }
        });
        return root;
    }

    private void updateMonitorFragmentPeriodically() {
        /* TODO: Update graph */
        if (sendingStatusReport > 0) {
            sendingStatusReport--;
        } else if (sendingStatusReport == 0) {
            sendingReportLoading.dismiss();
            sendingStatusReport--;
        }
//        if (all the beacons are ok) {
//            beaconStatusText.setText(allGoodText);
//            beaconStatusIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_done_all_w_24dp));
//            beaconStatusReportBtn.setVisibility(View.INVISIBLE);
//        } else {
//            String message = String.format(problemText, numberOfBeaconsThatIsOk);
//            beaconStatusText.setText(message);
//            beaconStatusIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_sync_problem_w_24dp));
//            beaconStatusReportBtn.setVisibility(View.VISIBLE);
//        }
    }

    private void displayMonitorReportDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setIcon(R.drawable.mon_dialog);
        dialogBuilder.setTitle("Thank you for letting us now");
        dialogBuilder.setMessage("VipAssistant Support Team is going to review this issue and take actions for the problematic beacons immediately.");
        dialogBuilder.setPositiveButton("You're Welcome", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendingStatusReport = 3;
                sendingReportLoading.show();
                dialog.dismiss();
            }
        });
    }
}