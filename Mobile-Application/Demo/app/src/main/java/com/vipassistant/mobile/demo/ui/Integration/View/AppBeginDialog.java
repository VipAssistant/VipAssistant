package com.vipassistant.mobile.demo.ui.Integration.View;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vipassistant.mobile.demo.MainActivity;

public class AppBeginDialog extends DialogFragment {

    private TextInputLayout mainTextLayout;

    private TextInputEditText name;
    private TextInputEditText language;

    private String player1;
    private String player2;

    private View rootView;
    private MainActivity activity;

    public static AppBeginDialog newInstance(MainActivity activity) {
        AppBeginDialog dialog = new AppBeginDialog();
        dialog.activity = activity;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        initViews();
//        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
//                .setView(rootView)
//                .setTitle("Enter Basic Credentials")
//                .setCancelable(false)
//                .setPositiveButton("done", null)
//                .create();
//        alertDialog.setCanceledOnTouchOutside(true);
//        alertDialog.setCancelable(true);
//        alertDialog.setOnShowListener(dialog -> {
//            onDialogShow(alertDialog);
//        });
        return null;
    }

//    private void initViews() {
//        rootView = LayoutInflater.from(getContext())
//                .inflate(R.layout.app_begin_dialog, null, false);
//
//        mainTextLayout = rootView.findViewById(R.id.layout_player1);
//
//        player1EditText = rootView.findViewById(R.id.et_player1);
//        player2EditText = rootView.findViewById(R.id.et_player2);
//        addTextWatchers();
//    }
//
//    private void onDialogShow(AlertDialog dialog) {
//        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//        positiveButton.setOnClickListener(v -> {
//            onDoneClicked();
//        });
//    }
//
//    private void onDoneClicked() {
//        if (isAValidName(player1Layout, player1) & isAValidName(player2Layout, player2)) {
//            activity.onPlayersSet(player1, player2);
//            dismiss();
//        }
//    }
//
//    private boolean isAValidName(TextInputLayout layout, String name) {
//        if (TextUtils.isEmpty(name)) {
//            layout.setErrorEnabled(true);
//            layout.setError(getString(R.string.game_dialog_empty_name));
//            return false;
//        }
//
//        if (player1 != null && player2 != null && player1.equalsIgnoreCase(player2)) {
//            layout.setErrorEnabled(true);
//            layout.setError(getString(R.string.game_dialog_same_names));
//            return false;
//        }
//
//        layout.setErrorEnabled(false);
//        layout.setError("");
//        return true;
//    }
//
//    private void addTextWatchers() {
//        player1EditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                player1 = s.toString();
//            }
//        });
//        player2EditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                player2 = s.toString();
//            }
//        });
//    }
}
