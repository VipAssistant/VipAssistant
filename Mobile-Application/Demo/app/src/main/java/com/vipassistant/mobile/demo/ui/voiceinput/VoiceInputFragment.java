package com.vipassistant.mobile.demo.ui.voiceinput;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.vipassistant.mobile.demo.R;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class VoiceInputFragment extends Fragment {

    private VoiceInputViewModel voiceInputViewModel;
    private static final int RCS_INPUT = 1000;
    TextView vinput2;
    ImageButton mVoiceBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        voiceInputViewModel = ViewModelProviders.of(this).get(VoiceInputViewModel.class);
        View root = inflater.inflate(R.layout.fragment_vinput, container, false);

        vinput2 = root.findViewById(R.id.vinput2);
        mVoiceBtn = root.findViewById(R.id.voiceBtn);

        // adding a onclick listener
        mVoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });
        return root;
    }

    private void listen() {

        // vIntent will show text in dialog
        Intent vIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        vIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH); // TODO: we can use Locale.getDefault() for Turkish
        vIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...");

        try {
            startActivityForResult(vIntent, RCS_INPUT);
        } catch (Exception e){
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Now we will receive voice input and process it
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if there is an successful and non-empty voice input
        if(requestCode == RCS_INPUT && resultCode == RESULT_OK && data!=null){
            // array of words are extracted from voice recognizer intent
            ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // show them in text view
            vinput2.setText(String.format("You said %s", res.get(0)));
        }
    }
}