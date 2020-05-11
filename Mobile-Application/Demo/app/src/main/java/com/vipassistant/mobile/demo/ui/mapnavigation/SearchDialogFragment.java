package com.vipassistant.mobile.demo.ui.mapnavigation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class SearchDialogFragment extends DialogFragment {

	private AlertDialog.Builder alertDialogBuilder;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle("Found 3 Routes");
		alertDialogBuilder.setMessage("This is a three-button dialog!");
		alertDialogBuilder.setPositiveButton("A", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialogBuilder.setNeutralButton("B", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialogBuilder.setNegativeButton("C", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return alertDialogBuilder.show();
	}
}