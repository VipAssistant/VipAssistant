package com.vipassistant.mobile.demo.ui.mapnavigation;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
import com.vipassistant.mobile.demo.ui.constants.Constants;
import com.vipassistant.mobile.demo.ui.service.LocationService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.vipassistant.mobile.demo.ui.constants.Constants.*;

public class MapNavigationFragment extends Fragment implements OnMapsceneRequestCompletedListener,
		                                                       OnRoutingQueryCompletedListener {

	private View root;
	private MapNavigationViewModel mapNavigationViewModel;
	private LocationService locationService;
	private MapView m_mapView;
	private EegeoMap m_eegeoMap = null;
	private IndoorMapView m_interiorView = null;
	private Marker navigationMarker, outNavigationMarker;
	private BlueSphere m_bluesphere = null;
	private List<RouteView> m_routeViews = new ArrayList<RouteView>();
	private Handler handler = new Handler();
	private LatLng userLocation;
	private Queue<LatLng> locationQueue = new LinkedList<>();

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapNavigationViewModel = ViewModelProviders.of(this).get(MapNavigationViewModel.class);
		root = inflater.inflate(R.layout.fragment_map_nav, container, false);
		ProgressDialog mapLoadingDialog = buildLoadingDialog(getActivity(), "Loading Map Data...");
		mapLoadingDialog.show();

		/* Initialize Map */
		EegeoApi.init(getActivity(), getString(R.string.eegeo_api_key));
		m_mapView = (MapView) root.findViewById(R.id.mapView);
		m_mapView.onCreate(savedInstanceState);
		final OnMapsceneRequestCompletedListener mapSceneRequestCompletedListener = this;
		final OnRoutingQueryCompletedListener routingQueryCompletedListener = this;

		/* Initialize LocationService */
		this.locationService = new LocationService(allLocations);

		m_mapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final EegeoMap map) {
				m_eegeoMap = map;

				MapsceneService mapsceneService = map.createMapsceneService();
				mapsceneService.requestMapscene(
						new MapsceneRequestOptions(mapSceneLink)
								.onMapsceneRequestCompletedListener(mapSceneRequestCompletedListener)
				);

				map.addInitialStreamingCompleteListener(new OnInitialStreamingCompleteListener() {
					@Override
					public void onInitialStreamingComplete() {
						mapLoadingDialog.dismiss();
						initializeLocation();
					}
				});

				RelativeLayout uiContainer = (RelativeLayout) root.findViewById(R.id.eegeo_ui_container);
				m_interiorView = new IndoorMapView(m_mapView, uiContainer, m_eegeoMap);

				Button findMeBtn = (Button) root.findViewById(R.id.findMeButton);
				findMeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						centerCurrentLocation();
					}
				});

				Button searchBtn = (Button) root.findViewById(R.id.searchButton);
				searchBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						displaySearchDialog();
					}
				});

				Button shareBtn = (Button) root.findViewById(R.id.shareLocationButton);
				shareBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						displayShareDialog();
					}
				});

				Button saveBtn = (Button) root.findViewById(R.id.saveLocationButton);
				saveBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						displaySaveDialog();
					}
				});

				// TODO
				RoutingService routingService = map.createRoutingService();

//				routingService.findRoutes(new RoutingQueryOptions()
//						.addIndoorWaypoint(, 1)
//						.addIndoorWaypoint(new LatLng(39.892025, 32.783200), 2)
//						.onRoutingQueryCompletedListener(routingQueryCompletedListener));
			}
		});
		return root;
	}

	private void initializeLocation() {
		/* Initialize Location Queue first with indoor map entrance Location */
		locationQueue.add(demoIndoorMapEntrance);
		/* Then initialize related variables */
		this.userLocation = computeCurrentLocation();
		this.outNavigationMarker = m_eegeoMap.addMarker(new MarkerOptions().position(userLocation).labelText(markerText));
		this.m_bluesphere = m_eegeoMap.getBlueSphere();
		this.m_bluesphere.setEnabled(true);
		this.m_bluesphere.setPosition(userLocation);
		this.m_bluesphere.setIndoorMap(demoIndoorMapId, 1);
		this.m_bluesphere.setBearing(180);

		/* Also now set-up Handler for periodic Map refreshing */
		this.handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				updateMapPeriodically();
				handler.postDelayed(this, mapRefreshMillis);
			}
		}, mapRefreshMillis);
	}

	/**
	 * Computes the new location of user by BLE Infrastructure in real time
	 * @return LatLng -- user's new location
	 */
	private LatLng computeCurrentLocation() {
		/* ..BLE Calculation.. */
		if (locationQueue.size() != 1) {
			/* If queue does not contain only 1 element then pop the head */
			return locationQueue.remove();
		} else {
			/* Else do not pop since user stays still */
			return locationQueue.element();
		}
	}

	/**
	 * Method that is called periodically to update user's location with its markers
	 * inside map. Utilizes computeCurrentLocation() for resolving new location
	 */
	private void updateLocation(LatLng newLocation) {
		this.userLocation = newLocation;
		this.outNavigationMarker.setPosition(newLocation);
		this.m_bluesphere.setPosition(userLocation);
		this.m_bluesphere.setBearing(180); // TODO DIRECTION
	}

	private void centerCurrentLocation() {
		CameraPosition position = new CameraPosition.Builder()
				.target(this.userLocation)
				.indoor(demoIndoorMapId, 1) // TODO maybe also store floor info?
				.zoom(19)
				.bearing(270)
				.build();
		CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder()
				.build();
		m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationOptions);
		Toast.makeText(getActivity(), "Centered Your Location", Toast.LENGTH_LONG).show();
	}

	private void displaySearchDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle("Search in Map");
		alertDialogBuilder.setMessage("You can query the system in 3 different ways!");
		alertDialogBuilder.setPositiveButton("Find Me A ...!", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog.Builder innerDialogBuilder = new AlertDialog.Builder(getActivity());
				innerDialogBuilder.setTitle("What do you want us to find for you?");
				EditText locationInput = new EditText(getContext());
				locationInput.setInputType(InputType.TYPE_CLASS_TEXT);
				innerDialogBuilder.setView(locationInput);
				innerDialogBuilder.setPositiveButton("Find!", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						locationInput.getText().toString();
					}
				});
				innerDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				innerDialogBuilder.show().show();
			}
		});
		alertDialogBuilder.setNegativeButton("Free Map Search", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialogBuilder.setNeutralButton("Report Me Nearby Information", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		AlertDialog dialog = alertDialogBuilder.show();

		/*	Push dialog buttons to the left */
		Button btn1 = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		Button btn2 = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
		Button btn3 = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btn1.getLayoutParams();
		layoutParams.gravity = Gravity.LEFT;
		btn1.setLayoutParams(layoutParams);
		btn2.setLayoutParams(layoutParams);
		btn3.setLayoutParams(layoutParams);
		dialog.show();
	}

	private void navigateToLocation() {
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

	/**
	 * Method that is called periodically to update Map Fragment
	 * updates user location etc.
	 * updates existing routes ?
	 * updates geoloc nav helper etc?
	 */
	private void updateMapPeriodically() {
		updateLocation(computeCurrentLocation());
		// TODO
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
		if (response.succeeded()) {
			Toast.makeText(getActivity(), "Found routes", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getActivity(), "Failed to find routes", Toast.LENGTH_LONG).show();
		}

		for (Route route : response.getResults()) {
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
	}

	@Override
	public void onPause() {
		super.onPause();
		m_mapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (m_eegeoMap != null) {
			for (RouteView routeView : m_routeViews) {
				routeView.removeFromMap();
			}
		}

		m_mapView.onDestroy();
	}
}