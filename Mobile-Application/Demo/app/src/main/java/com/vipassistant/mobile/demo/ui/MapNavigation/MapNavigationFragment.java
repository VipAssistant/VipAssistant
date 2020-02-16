package com.vipassistant.mobile.demo.ui.MapNavigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.eegeo.indoors.IndoorMapView;
import com.eegeo.mapapi.EegeoApi;
import com.eegeo.mapapi.EegeoMap;
import com.eegeo.mapapi.MapView;
import com.eegeo.mapapi.map.OnMapReadyCallback;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestOptions;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestResponse;
import com.eegeo.mapapi.services.mapscene.MapsceneService;
import com.eegeo.mapapi.services.mapscene.OnMapsceneRequestCompletedListener;
import com.vipassistant.mobile.demo.R;

public class MapNavigationFragment extends Fragment implements OnMapsceneRequestCompletedListener {

    private MapNavigationViewModel mapNavigationViewModel;
    private MapView m_mapView;
    private EegeoMap m_eegeoMap = null;
    private IndoorMapView m_interiorView = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // TODO: may not be needed
        mapNavigationViewModel = ViewModelProviders.of(this).get(MapNavigationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_map_nav, container, false);

        EegeoApi.init(getActivity(), getString(R.string.eegeo_api_key));
        m_mapView = (MapView) root.findViewById(R.id.mapView);
        m_mapView.onCreate(savedInstanceState);

        final OnMapsceneRequestCompletedListener listener = this;

        m_mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final EegeoMap map) {
//                m_eegeoMap = map;
//
//                RelativeLayout uiContainer = (RelativeLayout) root.findViewById(R.id.eegeo_ui_container);
//                m_interiorView = new IndoorMapView(m_mapView, uiContainer, m_eegeoMap);
//
//                Toast.makeText(getActivity(), "Map is Ready, Rendering Now!", Toast.LENGTH_LONG).show();

                m_eegeoMap = map;
                MapsceneService mapsceneService = map.createMapsceneService();
                mapsceneService.requestMapscene(
                        new MapsceneRequestOptions("https://wrld.mp/fbdfb87")
                            .onMapsceneRequestCompletedListener(listener)
                );
            }
        });
        return root;
    }

    @Override
    public void onMapsceneRequestCompleted(MapsceneRequestResponse response) {
        if(response.succeeded()) {
            String message = "Mapscene '" + response.getMapscene().name + "' loaded";
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getActivity(), "Failed to load mapscene", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        m_mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m_mapView.onDestroy();
    }
}