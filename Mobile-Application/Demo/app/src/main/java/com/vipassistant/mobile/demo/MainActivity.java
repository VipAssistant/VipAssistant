package com.vipassistant.mobile.demo;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.vipassistant.mobile.demo.ui.mapnavigation.MapNavigationFragment;
import com.vipassistant.mobile.demo.ui.mapnavigation.MapNavigationViewModel;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private MapNavigationViewModel mapNavVM;
    private Menu optionsMenu;

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
//                showHelp(); tODO?
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
