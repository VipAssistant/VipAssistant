package com.vipassistant.mobile.demo.ui.VoiceOutput;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.vipassistant.mobile.demo.R;

import java.util.Locale;

public class VoiceOutputFragment extends Fragment {

    private VoiceOutputViewModel voiceOutputViewModel;
    private TextToSpeech mTTS;
    private EditText mEditText;
    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private Button mButtonSpeak;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) { // TODO what is container bundle inflater etc.
//        super.onCreate(savedInstanceState); TODO hmm
//        setContentView(R.layout.activity_main); TODO fragment_voutput

        voiceOutputViewModel = ViewModelProviders.of(this).get(VoiceOutputViewModel.class);
        View root = inflater.inflate(R.layout.fragment_voutput, container, false);

//        final TextView textView = root.findViewById(R.id.text_voutput);
//        voiceOutputViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        mButtonSpeak = root.findViewById(R.id.button_speak);

        mTTS = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH); // TODO: Try to use Locale.TURKISH

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("VoiceOutput - TTS", "Language not supported");
                    } else {
                        mButtonSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("VoiceOutput - TTS", "Initialization failed");
                }
            }
        });

        mEditText = root.findViewById(R.id.edit_text);
        mSeekBarPitch = root.findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = root.findViewById(R.id.seek_bar_speed);

        mButtonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        return root; // TODO: check this
    }

    private void speak() {
        String text = mEditText.getText().toString();
        float pitch = (float) mSeekBarPitch.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = (float) mSeekBarSpeed.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);

        /* QUEUE_ADD means new speeches are appended to the queue to be said after current
         * also could've used QUEUE_FLUSH which means new speech cancels ongoing one */
        mTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    public void onDestroyView() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroyView();
    }
}