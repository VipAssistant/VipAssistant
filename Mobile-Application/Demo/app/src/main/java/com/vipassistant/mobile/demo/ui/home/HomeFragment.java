package com.vipassistant.mobile.demo.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.vipassistant.mobile.demo.R;
import com.vipassistant.mobile.demo.ui.utils.HomeArrayAdapter;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ListView listView1;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        String[] textString = {"Navigate Yourself to Anywhere Inside Buildings!", "Discover the World!",
                "Display Realtime Building Heatmaps!", "Save and Share Your Location", "Monitor the State of Your App",
                "Toggle Map Caching", "Switch to VIP Mode", "Display Location Calculation Demo"};
        int[] drawableIds = {R.drawable.ic_near_me_white_24dp, R.drawable.ic_public_w_24dp, R.drawable.ic_location_on_w_24dp,
                R.drawable.ic_whatshot_w_24dp, R.drawable.ic_graphic_eq_w_24dp, R.drawable.ic_cached_white_24dp,
                R.drawable.ic_visibility_white_24dp, R.drawable.ic_navigation_white_opak_24dp};

        HomeArrayAdapter adapter = new HomeArrayAdapter(inflater, textString, drawableIds);

        listView1 = (ListView) root.findViewById(R.id.menuList);
        listView1.setEnabled(false);
        listView1.setOnItemClickListener(null);
        listView1.setAdapter(adapter);

        return root;
    }
}