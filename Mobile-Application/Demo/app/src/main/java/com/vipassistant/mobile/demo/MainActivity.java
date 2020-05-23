package com.vipassistant.mobile.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.androidadvance.topsnackbar.TSnackbar;
import com.google.android.material.navigation.NavigationView;
import com.vipassistant.mobile.demo.ui.home.HomeFragment;
import com.vipassistant.mobile.demo.ui.mapnavigation.MapNavigationViewModel;
import com.vipassistant.mobile.demo.ui.model.Directive;
import com.vipassistant.mobile.demo.ui.utils.HomeArrayAdapter;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private MapNavigationViewModel mapNavVM;
    private Menu optionsMenu;
    private Boolean vipModeOn = true;
    private TextToSpeech mTTS;
    private Handler handler = new Handler();
    private Queue<Directive> voiceOutputQueue = new LinkedList<>();
    private Integer voiceOutputHandlerRefreshDuration = 1000;
    private TSnackbar snackbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_heatmap, R.id.nav_monitor,
                R.id.nav_corona_demo, R.id.nav_map_nav)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        mapNavVM = ViewModelProviders.of(this).get(MapNavigationViewModel.class);

        /* Dont let phone go sleep while the app is running */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("VoiceOutput - TTS", "Language not supported");
                    } else {
                        voiceOutputQueue.add(new Directive("Welcome to VipAssistant.", 1200));
                        voiceOutputQueue.add(new Directive("Do you want to use VipAssistant in Visually Impaired mode or in Non-Visually Impaired mode?", 5000));
                        voiceOutputQueue.add(new Directive("Clicking anywhere on the screen except Non-Visually Impaired mode button will result in selecting visually impaired mode.", 5500));
                        voiceOutputQueue.add(new Directive("You can always switch back and forth between these modes.", 3000));
                        voiceOutput(voiceOutputQueue.remove());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!voiceOutputQueue.isEmpty()) {
                                    voiceOutput(voiceOutputQueue.remove());
                                }
                                handler.postDelayed(this, voiceOutputHandlerRefreshDuration);
                            }
                        }, voiceOutputHandlerRefreshDuration);
                    }
                } else {
                    Log.e("VoiceOutput - TTS", "TTS Initialization failed");
                }
            }
        });
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setIcon(R.drawable.nav_directions);
        dialogBuilder.setTitle("First things first");
        dialogBuilder.setMessage("Please select the mode you want to use VipAssistant with.\n\n" +
                "You can always switch back and forth between these modes.\n\n" +
                "Beware: Clicking anywhere but non-visually impaired mode button will result in selecting visually impaired mode.");
        dialogBuilder.setPositiveButton("VIP mode", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent myIntent = new Intent(MainActivity.this, VIPMainActivity.class);
//                myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(myIntent);
                voiceOutputQueue.clear();
                snackbar.dismiss();
                mTTS.stop();
                dialog.dismiss();
            }
        });
        dialogBuilder.setNeutralButton("non-VIP Mode", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                vipModeOn = false;
                snackbar.dismiss();
                voiceOutputQueue.clear();
                mTTS.stop();
                dialog.dismiss();
            }
        });
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Intent myIntent = new Intent(MainActivity.this, VIPMainActivity.class);
//                myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(myIntent);
                snackbar.dismiss();
                voiceOutputQueue.clear();
                mTTS.stop();
            }
        });
        dialogBuilder.show().show();
    }

    /**
     * Create a snackbar + also voice output given for given line
     * @param outputString
     */
    private void voiceOutput(Directive outputDirective) {
        voiceOutputHandlerRefreshDuration = outputDirective.getDurationInMillis();
        View view = findViewById(android.R.id.content).getRootView();
        displaySnackbar(view, outputDirective.getStringToOutput(), outputDirective.getDurationInMillis());
        speak(outputDirective.getStringToOutput());
    }

    private void displaySnackbar(View view, String line, int duration) {
        snackbar = TSnackbar.make(view, line, TSnackbar.LENGTH_LONG);
        snackbar.setIconLeft(R.drawable.ic_record_voice_over_w_24dp, 32);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#1a1b29"));
        snackbar.setDuration(duration);
        TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#ffffff"));
        textView.setMaxLines(5);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
    }

    private void speak(String line) {
//        float pitch = (float) mSeekBarPitch.getProgress() / 50;
//        if (pitch < 0.1) pitch = 0.1f;
//        float speed = (float) mSeekBarSpeed.getProgress() / 50;
//        if (speed < 0.1) speed = 0.1f;

//        mTTS.setPitch(pitch);
//        mTTS.setSpeechRate(speed);

        /* QUEUE_ADD means new speeches are appended to the queue to be said after current
         * also could've used QUEUE_FLUSH which means new speech cancels ongoing one */
        mTTS.speak(line, TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.switch_vip:
                Intent myIntent = new Intent(MainActivity.this, VIPMainActivity.class);
//                myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(myIntent);
                return true;
            case R.id.caching_toggle:
                if (mapNavVM.getCachingActivated()) {
                    mapNavVM.setCachingToastActivated(false);
                    String newDisplayTitle = "Display Caching Logs: Off";
                    optionsMenu.getItem(2).setTitle(newDisplayTitle);
                }
                mapNavVM.setCachingActivated(!mapNavVM.getCachingActivated());
                String newTitle = item.getTitle().equals("Caching: On") ? "Caching: Off" : "Caching: On";
                item.setTitle(newTitle);
                return true;
            case R.id.show_caching_messages_toggle:
                mapNavVM.setCachingToastActivated(!mapNavVM.getCachingToastActivated());
                String newDisplayTitle = item.getTitle().equals("Display Caching Logs: On") ?
                        "Display Caching Logs: Off" : "Display Caching Logs: On";
                item.setTitle(newDisplayTitle);
                return true;
            case R.id.show_help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showHelp() { // todo: not works
        String[] textString = {"Navigate Yourself to Anywhere Inside Buildings!", "Discover the World!",
                "Save and Share Your Location!", "Display Realtime Building Heatmaps!", "Monitor the State of Your App!",
                "Toggle Map Caching!", "Switch to VIP Mode!", "Display Location Calculation Demo!"};

        int[] drawableIds = {R.drawable.ic_near_me_white_24dp, R.drawable.ic_public_w_24dp, R.drawable.ic_location_on_w_24dp,
                R.drawable.ic_whatshot_w_24dp, R.drawable.ic_graphic_eq_w_24dp, R.drawable.ic_cached_white_24dp,
                R.drawable.ic_visibility_white_24dp, R.drawable.ic_navigation_white_opak_24dp};

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        HomeArrayAdapter adapter = new HomeArrayAdapter(layoutInflater, textString, drawableIds);
        View layoutView = layoutInflater.inflate(R.layout.help_list, null);
        RelativeLayout rl = (RelativeLayout) layoutView.findViewById(R.id.helplist_view);
        ListView lv = (ListView) rl.getChildAt(0);
        lv.setEnabled(false);
        lv.setOnItemClickListener(null);
        lv.setAdapter(adapter);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setIcon(R.drawable.ic_help_outline_black_24dp);
        dialogBuilder.setTitle("Help");
        dialogBuilder.setView(rl);
        dialogBuilder.setPositiveButton("Ok..", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.show().show();
    }

    public void redirectToHome() { // todo: not works
        Fragment fragment = new HomeFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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
