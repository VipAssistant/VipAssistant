package com.vipassistant.mobile.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.vipassistant.mobile.demo.ui.mapnavigation.MapNavigationViewModel;
import com.vipassistant.mobile.demo.ui.utils.HomeArrayAdapter;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private MapNavigationViewModel mapNavVM;
    private Menu optionsMenu;
    private Boolean vipModeOn = true;

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

        // todo: vo/vin below
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
                dialog.dismiss();
            }
        });
        dialogBuilder.setNeutralButton("non-VIP Mode", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                vipModeOn = false;
                dialog.dismiss();
            }
        });
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Intent myIntent = new Intent(MainActivity.this, VIPMainActivity.class);
//                myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });
        dialogBuilder.show().show();
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
                    optionsMenu.getItem(1).setTitle(newDisplayTitle);
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

    public void showHelp() {
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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
