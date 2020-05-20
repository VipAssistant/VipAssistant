package com.vipassistant.mobile.demo.ui.heatmap;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import com.eegeo.mapapi.geometry.WeightedLatLngAlt;
import com.eegeo.mapapi.heatmaps.Heatmap;
import com.eegeo.mapapi.heatmaps.HeatmapOptions;
import com.eegeo.mapapi.map.OnInitialStreamingCompleteListener;
import com.eegeo.mapapi.map.OnMapReadyCallback;
import com.eegeo.mapapi.markers.Marker;
import com.eegeo.mapapi.markers.MarkerOptions;
import com.eegeo.mapapi.polygons.PolygonOptions;
import com.eegeo.mapapi.precaching.OnPrecacheOperationCompletedListener;
import com.eegeo.mapapi.precaching.PrecacheOperationResult;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestOptions;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestResponse;
import com.eegeo.mapapi.services.mapscene.MapsceneService;
import com.eegeo.mapapi.services.mapscene.OnMapsceneRequestCompletedListener;
import com.vipassistant.mobile.demo.R;
import com.vipassistant.mobile.demo.ui.mapnavigation.MapNavigationViewModel;
import com.vipassistant.mobile.demo.ui.model.Location;
import com.vipassistant.mobile.demo.ui.model.StepInfo;

import java.util.LinkedList;
import java.util.Queue;

import static android.view.MotionEvent.ACTION_BUTTON_PRESS;
import static com.vipassistant.mobile.demo.ui.constants.Constants.*;
import static com.vipassistant.mobile.demo.ui.constants.Utils.buildLoadingDialog;
import static com.vipassistant.mobile.demo.ui.constants.Utils.generateRandomData;

public class HeatmapFragment extends Fragment implements OnMapsceneRequestCompletedListener, OnPrecacheOperationCompletedListener {
	private View root;
	private MapNavigationViewModel heatmapViewModel;
	private MapView m_mapView;
	private EegeoMap m_eegeoMap = null;
	private Heatmap m_heatmap0, m_heatmap1, m_heatmap2, m_heatmap3, m_heatmap4, m_heatmap5, m_heatmap6, m_heatmap7, m_heatmap8;
	private IndoorMapView m_interiorView = null;
	private Marker outNavigationMarker;
	private BlueSphere m_bluesphere = null;
	private Handler handler = new Handler();
	private Location userLocation;
	private Double userDirection = 180., finalNavBearing = 180.;
	private Queue<Location> locationQueue = new LinkedList<>(); // For demo purposes
	private Queue<StepInfo> navDirectionQueue = new LinkedList<>(); // For demo purposes
	private final OnMapsceneRequestCompletedListener mapSceneRequestCompletedListener = this;
	private final OnPrecacheOperationCompletedListener precacheOperationCompletedListener = this;
	private Integer findMePressed = 0;
	private Button findMeBtn, heatmapOpenBtn, heatmapCloseBtn;
	private LatLng preCacheLocation = null;
	private ProgressDialog mapLoading, navigationRequestLoading, recalculatingRouteLoading;
	private int cachingTimeout = 0;
	private boolean heatmapShown = false;
	private int showHeatmapClicked = 0;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		heatmapViewModel = ViewModelProviders.of(getActivity()).get(MapNavigationViewModel.class);
		root = inflater.inflate(R.layout.fragment_heatmap, container, false);
		mapLoading = buildLoadingDialog(getActivity(), "Loading Map Data...");
		mapLoading.show();


		/* Initialize Map */
		EegeoApi.init(getActivity(), getString(R.string.eegeo_api_key));
		m_mapView = (MapView) root.findViewById(R.id.mapView);
		m_mapView.onCreate(savedInstanceState);

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
						mapLoading.dismiss();
						initializeLocation();
					}
				});

				m_eegeoMap.addOnCameraMoveListener(new EegeoMap.OnCameraMoveListener() {
					@Override
					public void onCameraMove() {
						CameraPosition currCamePosition = m_eegeoMap.getCameraPosition();
						if (currCamePosition.tilt > 10 && heatmapShown && showHeatmapClicked >= 3) {
							heatmapCloseBtn.performClick();
						}
					}
				});

				RelativeLayout uiContainer = (RelativeLayout) root.findViewById(R.id.eegeo_ui_container);
				m_interiorView = new IndoorMapView(m_mapView, uiContainer, m_eegeoMap);

				findMeBtn = (Button) root.findViewById(R.id.findMeButton);
				findMeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						centerCurrentLocation();
					}
				});

				heatmapOpenBtn = (Button) root.findViewById(R.id.heatmapOpenButton);
				heatmapOpenBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						heatmapOpenBtn.setVisibility(View.INVISIBLE);
						heatmapCloseBtn.setVisibility(View.VISIBLE);
						heatmapShown = true;
						showHeatmapClicked = 1;
						CameraPosition currentCamPos = m_eegeoMap.getCameraPosition(), newCamPos;
						CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder().build();
						if (!currentCamPos.targetIndoorMapId.equals("")) {
							newCamPos = new CameraPosition.Builder()
									.target(currentCamPos.target)
									.indoor(currentCamPos.targetIndoorMapId, currentCamPos.targetIndoorMapFloorId)
									.zoom(cameraZoom)
									.tilt(cameraTiltHeatmap)
									.bearing(currentCamPos.bearing)
									.build();
						} else {
							newCamPos = new CameraPosition.Builder()
									.target(currentCamPos.target)
									.zoom(cameraZoom)
									.tilt(cameraTiltHeatmap)
									.bearing(currentCamPos.bearing)
									.build();
						}
						m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos), animationOptions);
					}
				});

				heatmapCloseBtn = (Button) root.findViewById(R.id.heatmapCloseButton);
				heatmapCloseBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						heatmapCloseBtn.setVisibility(View.INVISIBLE);
						heatmapOpenBtn.setVisibility(View.VISIBLE);
						heatmapShown = false;
						showHeatmapClicked = 0;
						CameraPosition currentCamPos = m_eegeoMap.getCameraPosition(), newCamPos;
						CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder().build();
						if (!currentCamPos.targetIndoorMapId.equals("")) {
							newCamPos = new CameraPosition.Builder()
									.target(currentCamPos.target)
									.indoor(currentCamPos.targetIndoorMapId, currentCamPos.targetIndoorMapFloorId)
									.zoom(currentCamPos.zoom)
									.tilt(cameraTilt)
									.bearing(currentCamPos.bearing)
									.build();
						} else {
							newCamPos = new CameraPosition.Builder()
									.target(currentCamPos.target)
									.zoom(currentCamPos.zoom)
									.tilt(cameraTilt)
									.bearing(currentCamPos.bearing)
									.build();
						}
						m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos), animationOptions);
					}
				});
			}
		});

		RelativeLayout uiContainer = (RelativeLayout) root.findViewById(R.id.eegeo_ui_container);
		uiContainer.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() != ACTION_BUTTON_PRESS) {
					findMeBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.nav_find_me_1));
					findMePressed = 0;
				}
				return false;
			}
		});
		return root;
	}

	private void initializeLocation() {
		/* Initialize Location Queue first with indoor map entrance Location */
		locationQueue.add(demoIndoorMapEntrance);
		/* Then initialize related variables */
		this.userLocation = computeCurrentLocation();
		this.outNavigationMarker = m_eegeoMap.addMarker(
				new MarkerOptions()
						.position(userLocation.getLocation())
						.iconKey("my_location")
						.labelText(markerText));
		this.m_bluesphere = m_eegeoMap.getBlueSphere();
		this.m_bluesphere.setEnabled(true);
		this.m_bluesphere.setPosition(userLocation.getLocation());
		this.m_bluesphere.setIndoorMap(userLocation.getIndoorMapId(), userLocation.getFloor());
		this.m_bluesphere.setBearing(userDirection);

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
	 *
	 * @return LatLng -- user's new location
	 */
	private Location computeCurrentLocation() {
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
	private void updateLocation(Location newLocation) {
		this.userLocation = newLocation;
		this.outNavigationMarker.setPosition(newLocation.getLocation());
		this.m_bluesphere.setPosition(userLocation.getLocation());
		this.m_bluesphere.setIndoorMap(userLocation.getIndoorMapId(), userLocation.getFloor());
		this.m_bluesphere.setBearing(userDirection);
	}

	/**
	 * Method that is called periodically to update Map Fragment
	 * updates user location etc.
	 * updates existing routes ? !!!!!!
	 * updates geoloc nav helper etc?
	 */
	private void updateMapPeriodically() {
		updateLocation(computeCurrentLocation());

		if (heatmapViewModel.getCachingActivated() && cachingTimeout >= 3) {
			cacheCurrentCameraLocation(m_eegeoMap.getCameraPosition().target);
			cachingTimeout = 0;
		}
		cachingTimeout++;

		if (showHeatmapClicked > 0 && showHeatmapClicked <= 3) {
			showHeatmapClicked++;
		}

		if (this.findMePressed == 3) {
			Double tiltValue = heatmapShown ? cameraTiltHeatmap : cameraTilt;
			CameraPosition position = new CameraPosition.Builder()
					.target(userLocation.getLocation())
					.indoor(userLocation.getIndoorMapId(), userLocation.getFloor())
					.zoom(cameraZoom)
					.tilt(tiltValue)
					.bearing(userDirection - 180)
					.build();
			CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder()
					.build();
			m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationOptions);
		} else if (this.findMePressed != 0) {
			this.findMePressed++;
		}
	}

	private void centerCurrentLocation() {
		findMePressed = 1;
		heatmapCloseBtn.performClick();
		findMeBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.nav_find_me_2));
		CameraPosition currentCamPosition = m_eegeoMap.getCameraPosition();
		Double zoomValue = currentCamPosition.targetIndoorMapId.equals("") ? 19 : cameraZoom;
		CameraPosition position = new CameraPosition.Builder()
				.target(userLocation.getLocation())
				.indoor(userLocation.getIndoorMapId(), userLocation.getFloor())
				.zoom(zoomValue)
				.tilt(cameraTilt)
				.bearing(userDirection - 180)
				.build();
		CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder()
				.build();
		m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationOptions);
		Toast.makeText(getActivity(), "Centered Your Location", Toast.LENGTH_LONG).show();
	}

	/**
	 * Begin an operation to precache a spherical area of the map. This allows that area to load faster in future.
	 * @param location
	 */
	public void cacheCurrentCameraLocation(LatLng location) {
		this.preCacheLocation = location;
		// Precache a 3000 meter radius around this point
		m_eegeoMap.precache(
				location,
				3000.0,
				precacheOperationCompletedListener);
	}

	@Override
	public void onPrecacheOperationCompleted(PrecacheOperationResult precacheOperationResult) {
		if (heatmapViewModel.getCachingToastActivated()) {
			String toastMessage;
			if (precacheOperationResult.succeeded()) {
				toastMessage = String.format("Successfully cached a radius of 3km around (%.2f, %.2f).\n" +
								"Now this area will load faster than ever!",
						preCacheLocation.latitude, preCacheLocation.longitude);
			} else {
				toastMessage = String.format("Could not cache 3km radius around (%.2f, %.2f)!",
						preCacheLocation.latitude, preCacheLocation.longitude);
			}
			Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onMapsceneRequestCompleted(MapsceneRequestResponse response) {
		if (response.succeeded()) {
//			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
//			alertDialogBuilder.setIcon(android.R.drawable.ic_menu_search);
//			alertDialogBuilder.setTitle("Welcome non-VIP Navigation Module!");
//			alertDialogBuilder.setPositiveButton("Find Me A ...!", new DialogInterface.OnClickListener() {
			for (int i = 0; i < 9; i++) {
				prepareHeatmap(i);
			}
		} else {
			Toast.makeText(getActivity(), "Failed to load mapscene", Toast.LENGTH_LONG).show();
		}
	}

	private void prepareHeatmap(int floor) {
		Heatmap tempHeatmap = m_eegeoMap.addHeatmap(
				new HeatmapOptions()
						.polygon(new PolygonOptions().indoor(demoIndoorMapId, floor))
						.add(generateRandomData(50, new LatLng(39.892078, 32.783103), new LatLng(39.891757, 32.783322)))
						.weightMin(2.0)
						.weightMax(8.0)
//						 sets normative value to be mid-point between weightMin and weightMax,
//						 to align with transparent mid-point of color gradient
						.intensityBias(0.5f)
						.interpolateDensityByZoom(18.0, 21.5)
						.addDensityStop(0.0f, 0.6, 1.0)
						.addDensityStop(0.5f, 1.3, 0.75) // TODO STYLE
						.addDensityStop(1.0f, 2.1, 0.5)
						.gradient(
								// transparent at mid-point, with differing hues either side,
								// suitable for diverging data set. Similar to:
								// http://colorbrewer2.org/#type=diverging&scheme=RdYlBu&n=6
								new float[]{0.f, 0.1f, 0.4f, 0.49f, 0.51f, 0.6f, 0.9f, 1.f},
								new int[]{0x4575b4ff, 0x91bfdbff, 0xe0f3f8ff, 0xffffff00, 0xffffff00, 0xfee090ff, 0xfc8d59ff, 0xd73027ff})
						.opacity(0.8f)
		);

		switch (floor) {
			case 0:
				m_heatmap0 = tempHeatmap;
				break;
			case 1:
				m_heatmap1 = tempHeatmap;
				break;
			case 2:
				m_heatmap2 = tempHeatmap;
				break;
			case 3:
				m_heatmap3 = tempHeatmap;
				break;
			case 4:
				m_heatmap4 = tempHeatmap;
				break;
			case 5:
				m_heatmap5 = tempHeatmap;
				break;
			case 6:
				m_heatmap6 = tempHeatmap;
				break;
			case 7:
				m_heatmap7 = tempHeatmap;
				break;
			case 8:
				m_heatmap8 = tempHeatmap;
				break;
			default:
				System.out.println("INVALID FLOOR FOR DEMO MAP!");
				break;
		}
	}

	private WeightedLatLngAlt[] getHeatmapData(int floor) {
		switch (floor) {
			case 0:
				return heatmapPoints0;
			case 1:
				return heatmapPoints1;
			case 2:
				return heatmapPoints2;
			case 3:
				return heatmapPoints3;
			case 4:
				return heatmapPoints4;
			case 5:
				return heatmapPoints5;
			case 6:
				return heatmapPoints6;
			case 7:
				return heatmapPoints7;
			case 8:
				return heatmapPoints8;
			default:
				System.out.println("INVALID FLOOR FOR DEMO MAP!");
				return null;
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
//		m_eegeoMap.removeHeatmap(m_heatmap); todo
//		m_eegeoMap.removeHeatmap(m_heatmap);
//		m_eegeoMap.removeHeatmap(m_heatmap);
//		m_eegeoMap.removeHeatmap(m_heatmap);
//		m_eegeoMap.removeHeatmap(m_heatmap);
//		m_eegeoMap.removeHeatmap(m_heatmap);
//		m_eegeoMap.removeHeatmap(m_heatmap);
	}
}