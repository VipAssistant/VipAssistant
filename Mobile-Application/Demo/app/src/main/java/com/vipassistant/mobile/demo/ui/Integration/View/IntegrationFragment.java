package com.vipassistant.mobile.demo.ui.Integration.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.vipassistant.mobile.demo.R;
import com.vipassistant.mobile.demo.ui.Integration.ViewModel.IntegrationViewModel;

public class IntegrationFragment extends Fragment {

    private IntegrationViewModel integrationViewModel;
//    private Button mButtonGetStarted;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        integrationViewModel = ViewModelProviders.of(this).get(IntegrationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_integration, container, false);
//        final TextView textView = root.findViewById(R.id.text_home);
//        homeViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

//        mButtonGetStarted = root.findViewById(R.id.get_started);
//        mButtonGetStarted.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                promptUser(v);
//            }
//        });
        return root;
    }

//    public void promptUser(View v) {
//        AppBeginDialog dialog = AppBeginDialog.newInstance(this);
//        dialog.setCancelable(false);
//        dialog.show(getSupportFragmentManager(), GAME_BEGIN_DIALOG_TAG);
//    }
}