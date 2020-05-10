package com.vipassistant.mobile.demo.ui.MapNavigation;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.eegeo.indoors.IndoorMapView;
import com.eegeo.mapapi.EegeoApi;
import com.eegeo.mapapi.EegeoMap;
import com.eegeo.mapapi.MapView;
import com.eegeo.mapapi.bluesphere.BlueSphere;
import com.eegeo.mapapi.camera.CameraAnimationOptions;
import com.eegeo.mapapi.camera.CameraPosition;
import com.eegeo.mapapi.camera.CameraUpdateFactory;
import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.map.OnInitialStreamingCompleteListener;
import com.eegeo.mapapi.map.OnMapReadyCallback;
import com.eegeo.mapapi.markers.Marker;
import com.eegeo.mapapi.markers.MarkerOptions;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestOptions;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestResponse;
import com.eegeo.mapapi.services.mapscene.MapsceneService;
import com.eegeo.mapapi.services.mapscene.OnMapsceneRequestCompletedListener;
import com.eegeo.mapapi.services.routing.*;
import com.eegeo.mapapi.widgets.RouteView;
import com.eegeo.mapapi.widgets.RouteViewOptions;
import com.vipassistant.mobile.demo.MainActivity;
import com.vipassistant.mobile.demo.R;

import java.util.ArrayList;
import java.util.List;

public class MapNavigationFragment extends Fragment implements OnMapsceneRequestCompletedListener, OnRoutingQueryCompletedListener {

	private MapNavigationViewModel mapNavigationViewModel;
	private MapView m_mapView;
	private EegeoMap m_eegeoMap = null;
	private IndoorMapView m_interiorView = null;
	private ProgressDialog loadingDialog;
	private Marker navigationMarker, outNavigationMarker;
	private BlueSphere m_bluesphere = null;
	private List<RouteView> m_routeViews = new ArrayList<RouteView>();
	private View root;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); // TODO: may not be needed
		mapNavigationViewModel = ViewModelProviders.of(this).get(MapNavigationViewModel.class);
		root = inflater.inflate(R.layout.fragment_map_nav, container, false);

		((MainActivity) getActivity()).toggleFabVisibility();

		EegeoApi.init(getActivity(), getString(R.string.eegeo_api_key));
		m_mapView = (MapView) root.findViewById(R.id.mapView);
		m_mapView.onCreate(savedInstanceState);

		loadingDialog = new ProgressDialog(getActivity());
		loadingDialog.setMessage("Loading Map Data...");
		loadingDialog.setIndeterminate(false);
		loadingDialog.show();

		final OnMapsceneRequestCompletedListener mapsceneRequestCompletedListener = this;
		final OnRoutingQueryCompletedListener routingQueryCompletedListener = this;

		m_mapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final EegeoMap map) {
				m_eegeoMap = map;

				MapsceneService mapsceneService = map.createMapsceneService();
				mapsceneService.requestMapscene(
						new MapsceneRequestOptions("https://wrld.mp/4bdda73")
								.onMapsceneRequestCompletedListener(mapsceneRequestCompletedListener)
				);

				RelativeLayout uiContainer = (RelativeLayout) root.findViewById(R.id.eegeo_ui_container);
				m_interiorView = new IndoorMapView(m_mapView, uiContainer, m_eegeoMap);

				Button findMeBtn = (Button) root.findViewById(R.id.findMeButton);
				findMeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						centerLocation();
					}
				});

				Button demoBtn = (Button) root.findViewById(R.id.demoButton);
				demoBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						navigateToLocation();
					}
				});

				map.addInitialStreamingCompleteListener(new OnInitialStreamingCompleteListener() {
					@Override
					public void onInitialStreamingComplete() {
						loadingDialog.dismiss();
						initializeLocation();
					}
				});

				RoutingService routingService = map.createRoutingService();

				routingService.findRoutes(new RoutingQueryOptions()
						.addIndoorWaypoint(new LatLng(39.891756, 32.783188), 1)
						.addIndoorWaypoint(new LatLng(39.892025, 32.783200), 2)
						.onRoutingQueryCompletedListener(routingQueryCompletedListener));
			}
		});
		return root;
	}

	private void initializeLocation() {
		this.outNavigationMarker = m_eegeoMap.addMarker(new MarkerOptions()
				.position(new LatLng(39.891756, 32.783188)) // TODO: get from BEACON
				.labelText("You Are Here!"));

		this.m_bluesphere = m_eegeoMap.getBlueSphere();
		this.m_bluesphere.setEnabled(true);
		this.m_bluesphere.setPosition(new LatLng(39.891756, 32.783188));
		this.m_bluesphere.setIndoorMap("EIM-71597625-a9b6-4753-b91f-1c0e74fc966d", 1);
		this.m_bluesphere.setBearing(180);

//		this.navigationMarker = m_eegeoMap.addMarker(new MarkerOptions()
//				.position(new LatLng(39.891756, 32.783188)) // TODO: get from BEACON
//				.indoor("EIM-71597625-a9b6-4753-b91f-1c0e74fc966d", 1)
//				.labelText("You Are Here!"));

	}

	private void updateLocation() {
		// TODO: Update this.navigationMarker periodically using this and Beacon data
	}

	private void centerLocation() {
		CameraPosition position = new CameraPosition.Builder()
				.target(39.891756, 32.783188) // TODO: get from BEACON
				.indoor("EIM-71597625-a9b6-4753-b91f-1c0e74fc966d", 1)
				.zoom(19)
				.bearing(270)
				.build();
		CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder()
				.build();
		m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationOptions);
		Toast.makeText(getActivity(), "Centered Your Location", Toast.LENGTH_LONG).show();
	}

	public void navigateToLocation() {
		/*
			Found Routes Dialog showing:
				- desc
				- ETA and dist
				- preview route button
				- Start navigation button

			OnPreviewRoutesClick

			OnStartNavClickk
		 */
//		NavigationDialogFragment nDialog = new NavigationDialogFragment();
//		nDialog.show(getFragmentManager(), "Diag");
//		Toast.makeText(getActivity(), "Started Demo Routing", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onMapsceneRequestCompleted(MapsceneRequestResponse response) {
		if (response.succeeded()) {
			String message = "Mapscene '" + response.getMapscene().name + "' loaded"; // TODO: may remove later.
			Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getActivity(), "Failed to load mapscene", Toast.LENGTH_LONG).show();
		}
	}
	@Override
	public void onRoutingQueryCompleted(RoutingQuery query, RoutingQueryResponse response) {
		if(response.succeeded()) {
			Toast.makeText(getActivity(), "Found routes", Toast.LENGTH_LONG).show();
		}
		else{
			Toast.makeText(getActivity(), "Failed to find routes", Toast.LENGTH_LONG).show();
		}

		for (Route route: response.getResults()) {
			RouteViewOptions options = new RouteViewOptions()
					.color(Color.argb(128, 255, 0, 0))
					.width(8.0f);
			RouteView routeView = new RouteView(m_eegeoMap, route, options);
			m_routeViews.add(routeView);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		m_mapView.onResume();
		((MainActivity) getActivity()).toggleFabVisibility();
	}

	@Override
	public void onPause() {
		super.onPause();
		m_mapView.onPause();
		((MainActivity) getActivity()).toggleFabVisibility();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (m_eegeoMap != null) {
			for (RouteView routeView: m_routeViews) {
				routeView.removeFromMap();
			}
		}

		m_mapView.onDestroy();
	}
}