package com.example.voiceinput;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int RCS_INPUT = 1000;
    TextView mTextTv;
    ImageButton mVoiceBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextTv = findViewById(R.id.textTv1);
        mVoiceBtn = findViewById(R.id.voiceBtn);

        // adding a onclick listener
        mVoiceBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

    }

    private void speak() {

        // vIntent will show text in dialog
        Intent vIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        vIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM );
        vIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        vIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "I am listening honey");

        try {
            startActivityForResult(vIntent, RCS_INPUT);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Now we will receive voice input and process it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if there is an successful and non-empty voice input
        if(requestCode==RCS_INPUT && resultCode==RESULT_OK && data!=null){
            // array of words are extracted from voice recognizer intent
            ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // show them in text view
            mTextTv.setText(res.get(0));
        }
    }
}
