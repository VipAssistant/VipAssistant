package com.vipassistant.mobile.demo.ui.VoiceOutput;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.vipassistant.mobile.demo.R;

public class VoiceOutputFragment extends Fragment {

    private VoiceOutputViewModel voiceOutputViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        voiceOutputViewModel =
                ViewModelProviders.of(this).get(VoiceOutputViewModel.class);
        View root = inflater.inflate(R.layout.fragment_voutput, container, false);
        final TextView textView = root.findViewById(R.id.text_voutput);
        voiceOutputViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}