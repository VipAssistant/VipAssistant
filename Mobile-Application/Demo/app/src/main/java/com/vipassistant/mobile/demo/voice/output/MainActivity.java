package com.vipassistant.mobile.demo.voice.output;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;
import com.vipassistant.mobile.demo.R;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
	private TextToSpeech mTTS;
	private EditText mEditText;
	private SeekBar mSeekBarPitch;
	private SeekBar mSeekBarSpeed;
	private Button mButtonSpeak;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mButtonSpeak = findViewById(R.id.button_speak);

		mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS) {
					int result = mTTS.setLanguage(Locale.ENGLISH); // TODO: Try to use Locale.TURKISH

					if (result == TextToSpeech.LANG_MISSING_DATA
							|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
						Log.e("Text-To-Speech-Module", "Language not supported");
					} else {
						mButtonSpeak.setEnabled(true);
					}
				} else {
					Log.e("Text-To-Speech-Module", "Initialization failed");
				}
			}
		});

		mEditText = findViewById(R.id.edit_text);
		mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
		mSeekBarSpeed = findViewById(R.id.seek_bar_speed);

		mButtonSpeak.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				speak();
			}
		});
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
	protected void onDestroy() {
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}

		super.onDestroy();
	}
}
