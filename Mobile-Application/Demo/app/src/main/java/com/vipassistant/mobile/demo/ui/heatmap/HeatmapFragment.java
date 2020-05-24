package com.vipassistant.mobile.demo.ui.heatmap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.fragment.app.FragmentTransaction;
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
import com.vipassistant.mobile.demo.MainActivity;
import com.vipassistant.mobile.demo.R;
import com.vipassistant.mobile.demo.VIPMainActivity;
import com.vipassistant.mobile.demo.ui.home.HomeFragment;
import com.vipassistant.mobile.demo.ui.mapnavigation.MapNavigationViewModel;
import com.vipassistant.mobile.demo.ui.model.Location;
import com.vipassistant.mobile.demo.ui.model.StepInfo;

import java.util.*;

import static android.view.MotionEvent.ACTION_BUTTON_PRESS;
import static com.vipassistant.mobile.demo.ui.constants.Constants.*;
import static com.vipassistant.mobile.demo.ui.utils.Utils.buildLoadingDialog;
import static com.vipassistant.mobile.demo.ui.utils.Utils.generateRandomData;

public class HeatmapFragment extends Fragment implements OnMapsceneRequestCompletedListener, OnPrecacheOperationCompletedListener {
	private View root;
	private MapNavigationViewModel heatmapViewModel;
	private MapView m_mapView;
	private EegeoMap m_eegeoMap = null;
	private List<Heatmap> heatmaps = new ArrayList<>();
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
	private ProgressDialog mapLoading, creatingHeatmapLoading, refreshingHeatmapLoading;
	private int cachingTimeout = 0;
	private boolean heatmapShown = false;
	private int showHeatmapClicked = 0, heatmapRefreshCounter = 0;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		heatmapViewModel = ViewModelProviders.of(getActivity()).get(MapNavigationViewModel.class);
		root = inflater.inflate(R.layout.fragment_heatmap, container, false);
		mapLoading = buildLoadingDialog(getActivity(), "Loading Map Data...");
		mapLoading.show();
		creatingHeatmapLoading = buildLoadingDialog(getActivity(), "Creating Great Heatmaps for you...");
		refreshingHeatmapLoading = buildLoadingDialog(getActivity(), "Loading Map Data...");

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
						if (!m_eegeoMap.getCameraPosition().targetIndoorMapId.equals("")) {
							creatingHeatmapLoading.show();
							heatmapOpenBtn.setVisibility(View.INVISIBLE);
							heatmapCloseBtn.setVisibility(View.VISIBLE);

							heatmapShown = true;
							showHeatmapClicked = 1;
							prepareHeatmaps(m_eegeoMap.getCameraPosition().targetIndoorMapFloorId);
							heatmapRefreshCounter = 0;

							CameraPosition currentCamPos = m_eegeoMap.getCameraPosition(), newCamPos;
							CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder().build();
							if (!currentCamPos.targetIndoorMapId.equals("")) {
								newCamPos = new CameraPosition.Builder()
										.target(currentCamPos.target)
										.indoor(currentCamPos.targetIndoorMapId, currentCamPos.targetIndoorMapFloorId)
										.zoom(19.7)
										.tilt(cameraTiltHeatmap)
										.bearing(currentCamPos.bearing)
										.build();
							} else {
								newCamPos = new CameraPosition.Builder()
										.target(currentCamPos.target)
										.zoom(19.7)
										.tilt(cameraTiltHeatmap)
										.bearing(currentCamPos.bearing)
										.build();
							}
							m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos), animationOptions);
							creatingHeatmapLoading.dismiss();
						} else {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
							alertDialogBuilder.setIcon(R.drawable.heatmap_closed);
							alertDialogBuilder.setTitle("Heatmap Warning");
							alertDialogBuilder.setMessage("Please Enter an Indoor Map to Display its Heatmap");
							alertDialogBuilder.setPositiveButton("Ok...", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
							alertDialogBuilder.show().show();
						}
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
						for (Heatmap heatmap : heatmaps) {
							m_eegeoMap.removeHeatmap(heatmap);
						}
						heatmaps = new ArrayList<>();

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

		// TODO: INTERNET OLMAZSA DO NOT PERMIT
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

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		dialogBuilder.setIcon(R.drawable.nav_heat);
		dialogBuilder.setTitle("Welcome to Heatmap Module");
		dialogBuilder.setMessage("To be able to display heatmap of the building you are currently in," +
				" you need to accept sharing your heatmap data with VipAssistant community and make sure your Internet connection is on.");
		dialogBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialogBuilder.setNeutralButton("Reject", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				mapLoading.dismiss();
				((MainActivity) getActivity()).redirectToHome();
			}
		});
		dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				mapLoading.dismiss();
				((MainActivity) getActivity()).redirectToHome();
			}
		});
		dialogBuilder.show().show();

		return root;
	}

	private void initializeLocation() {
		/* Initialize Location Queue first with indoor map entrance Location */
		locationQueue.add(heatmapInitialLocation);
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

		if (heatmapShown && heatmapRefreshCounter >= 15) {
			refreshingHeatmapLoading.show();
			prepareHeatmaps(m_eegeoMap.getCameraPosition().targetIndoorMapFloorId);
			heatmapRefreshCounter = 0;
			refreshingHeatmapLoading.dismiss();
		}
		heatmapRefreshCounter++;

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
		Double tiltValue = heatmapShown ? cameraTiltHeatmap : cameraTilt;
		CameraPosition position = new CameraPosition.Builder()
				.target(userLocation.getLocation())
				.indoor(userLocation.getIndoorMapId(), userLocation.getFloor())
				.zoom(zoomValue)
				.tilt(tiltValue)
				.bearing(userDirection - 180)
				.build();
		CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder()
				.build();
		m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationOptions);
		Toast.makeText(getActivity(), "Centered Your Location", Toast.LENGTH_LONG).show();
	}

	/**
	 * Begin an operation to precache a spherical area of the map. This allows that area to load faster in future.
	 *
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
//			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity()); TODO
//			alertDialogBuilder.setIcon(android.R.drawable.ic_menu_search);
//			alertDialogBuilder.setTitle("Welcome non-VIP Navigation Module!");
//			alertDialogBuilder.setPositiveButton("Find Me A ...!", new DialogInterface.OnClickListener() {
		} else {
			Toast.makeText(getActivity(), "Failed to load mapscene", Toast.LENGTH_LONG).show();
		}
	}

	private void prepareHeatmaps(int floor) {
		for (Heatmap heatmap : heatmaps) {
			m_eegeoMap.removeHeatmap(heatmap);
		}
		heatmaps = new ArrayList<>();
		switch (floor) {
			case 0:
				createBasementHeatmapData();
				break;
			case 1:
				createEntranceHeatmapData();
				break;
			case 2:
				createFirstFloorHeatmapData();
				break;
			case 3:
				createFirstHalfFloorHeatmapData();
				break;
			case 4:
				createSecondFloorHeatmapData();
				break;
			case 5:
				createSecondHalfFloorHeatmapData();
				break;
			case 6:
				createThirdFloorHeatmapData();
				break;
			case 7:
				createThirdHalfFloorHeatmapData();
				break;
			case 8:
				createFourthFloorHeatmapData();
				break;
			default:
				System.err.println("INVALID FLOOR FOR THE DEMO MAP");
				break;
		}
	}

	private void createBasementHeatmapData() {
		List<WeightedLatLngAlt> points = new ArrayList<>();
		/* Per room generate random data */
		// Server room
		points.addAll(generateRandomData(3, new LatLng(39.892078, 32.783100), new LatLng(39.892058, 32.783206)));
		// Digital lab
		points.addAll(generateRandomData(45, new LatLng(39.892043, 32.783106), new LatLng(39.891884, 32.783199)));
		// Hallway
		points.addAll(generateRandomData(1, new LatLng(39.892051, 32.783188), new LatLng(39.891844, 32.783232)));
		// Back stairs
		points.addAll(generateRandomData(1, new LatLng(39.892029, 32.783214), new LatLng(39.892020, 32.783264)));
		// z103
		points.addAll(generateRandomData(4, new LatLng(39.891965, 32.783227), new LatLng(39.891938, 32.783304)));
		// z102
		points.addAll(generateRandomData(2, new LatLng(39.891923, 32.783233), new LatLng(39.891903, 32.783307)));
		// man wc
		points.addAll(generateRandomData(1, new LatLng(39.891896, 32.783302), new LatLng(39.891856, 32.783313)));
		// woman wc
		points.addAll(generateRandomData(1, new LatLng(39.891883, 32.783286), new LatLng(39.891854, 32.783293)));
		// disabled wc
		points.addAll(generateRandomData(1, new LatLng(39.891879, 32.783236), new LatLng(39.891847, 32.783282)));
		// stationary
		points.addAll(generateRandomData(3, new LatLng(39.891872, 32.783124), new LatLng(39.891849, 32.783204)));
		// hall
		points.addAll(generateRandomData(2, new LatLng(39.891836, 32.783127), new LatLng(39.891822, 32.783317)));
		// study room
		points.addAll(generateRandomData(10, new LatLng(39.891809, 32.783245), new LatLng(39.891756, 32.783322)));
		// front stairs
		points.addAll(generateRandomData(1, new LatLng(39.891797, 32.783156), new LatLng(39.891761, 32.783185)));
		createHeatmap(points, 0);
	}

	private void createEntranceHeatmapData() {
		List<WeightedLatLngAlt> points = new ArrayList<>();
		/* Per room generate random data */
		// Back stairs
		points.addAll(generateRandomData(1, new LatLng(39.892044, 32.783216), new LatLng(39.892020, 32.783274)));
		// front stairs
		points.addAll(generateRandomData(2, new LatLng(39.891801, 32.783157), new LatLng(39.891767, 32.783220)));
		// entrance hall
		points.addAll(generateRandomData(2, new LatLng(39.891760, 32.783136), new LatLng(39.891741, 32.783246)));
		createHeatmap(points, 1);
	}

	private void createFirstFloorHeatmapData() {
		List<WeightedLatLngAlt> points = new ArrayList<>();
		/* Per room generate random data */
		// a105
		points.addAll(generateRandomData(3, new LatLng(39.892078, 32.783100), new LatLng(39.892058, 32.783206)));
		// a106
		points.addAll(generateRandomData(1, new LatLng(39.892037, 32.783107), new LatLng(39.892012, 32.783183)));
		// a107
		points.addAll(generateRandomData(1, new LatLng(39.891998, 32.783111), new LatLng(39.891972, 32.783187)));
		// a108
		points.addAll(generateRandomData(3, new LatLng(39.891958, 32.783118), new LatLng(39.891851, 32.783201)));
		// Hallway
		points.addAll(generateRandomData(1, new LatLng(39.892051, 32.783188), new LatLng(39.891844, 32.783232)));
		// Back stairs
		points.addAll(generateRandomData(1, new LatLng(39.892044, 32.783216), new LatLng(39.892020, 32.783274)));
		// a104
		points.addAll(generateRandomData(1, new LatLng(39.892011, 32.783225), new LatLng(39.891979, 32.783298)));
		// a103
		points.addAll(generateRandomData(1, new LatLng(39.891962, 32.783229), new LatLng(39.891938, 32.783303)));
		// a102
		points.addAll(generateRandomData(1, new LatLng(39.891923, 32.783232), new LatLng(39.891903, 32.783306)));
		// wc
		points.addAll(generateRandomData(1, new LatLng(39.891888, 32.783236), new LatLng(39.891847, 32.783283)));
		// tea room
		points.addAll(generateRandomData(1, new LatLng(39.891892, 32.783290), new LatLng(39.891857, 32.783311)));
		// hall
		points.addAll(generateRandomData(2, new LatLng(39.891836, 32.783127), new LatLng(39.891822, 32.783317)));
		// a101
		points.addAll(generateRandomData(8, new LatLng(39.891809, 32.783245), new LatLng(39.891756, 32.783322)));
		// front stairs
		points.addAll(generateRandomData(2, new LatLng(39.891801, 32.783157), new LatLng(39.891767, 32.783220)));
		// B block crossover
		points.addAll(generateRandomData(3, new LatLng(39.891851, 32.783321), new LatLng(39.891835, 32.783520)));
		createHeatmap(points, 2);
	}

	private void createFirstHalfFloorHeatmapData() {
		List<WeightedLatLngAlt> points = new ArrayList<>();
		/* Per room generate random data */
		// Back stairs
		points.addAll(generateRandomData(1, new LatLng(39.892044, 32.783216), new LatLng(39.892020, 32.783274)));
		// front stairs
		points.addAll(generateRandomData(2, new LatLng(39.891801, 32.783157), new LatLng(39.891767, 32.783220)));
		// hall
		points.addAll(generateRandomData(2, new LatLng(39.891796, 32.783131), new LatLng(39.891741, 32.783246)));
		// bmb4
		points.addAll(generateRandomData(30, new LatLng(39.891727, 32.783142), new LatLng(39.891641, 32.783260)));
		createHeatmap(points, 3);
	}

	private void createSecondFloorHeatmapData() {
		List<WeightedLatLngAlt> points = new ArrayList<>();
		/* Per room generate random data */
		// a205
		points.addAll(generateRandomData(3, new LatLng(39.892078, 32.783100), new LatLng(39.892058, 32.783206)));
		// a206
		points.addAll(generateRandomData(1, new LatLng(39.892037, 32.783107), new LatLng(39.892012, 32.783183)));
		// a207
		points.addAll(generateRandomData(1, new LatLng(39.891998, 32.783111), new LatLng(39.891972, 32.783187)));
		// a208
		points.addAll(generateRandomData(3, new LatLng(39.891958, 32.783118), new LatLng(39.891851, 32.783201)));
		// Hallway
		points.addAll(generateRandomData(1, new LatLng(39.892051, 32.783188), new LatLng(39.891844, 32.783232)));
		// Back stairs
		points.addAll(generateRandomData(1, new LatLng(39.892044, 32.783216), new LatLng(39.892020, 32.783274)));
		// a204
		points.addAll(generateRandomData(1, new LatLng(39.892011, 32.783225), new LatLng(39.891979, 32.783298)));
		// a203
		points.addAll(generateRandomData(1, new LatLng(39.891962, 32.783229), new LatLng(39.891938, 32.783303)));
		// a202
		points.addAll(generateRandomData(1, new LatLng(39.891923, 32.783232), new LatLng(39.891903, 32.783306)));
		// wc
		points.addAll(generateRandomData(1, new LatLng(39.891888, 32.783236), new LatLng(39.891847, 32.783283)));
		// tea room
		points.addAll(generateRandomData(1, new LatLng(39.891892, 32.783290), new LatLng(39.891857, 32.783311)));
		// hall
		points.addAll(generateRandomData(2, new LatLng(39.891836, 32.783127), new LatLng(39.891822, 32.783317)));
		// resting room
		points.addAll(generateRandomData(8, new LatLng(39.891809, 32.783245), new LatLng(39.891756, 32.783322)));
		// front stairs
		points.addAll(generateRandomData(2, new LatLng(39.891801, 32.783157), new LatLng(39.891767, 32.783220)));
		// B block crossover
		points.addAll(generateRandomData(3, new LatLng(39.891851, 32.783321), new LatLng(39.891835, 32.783520)));
		createHeatmap(points, 4);
	}

	private void createSecondHalfFloorHeatmapData() {
		List<WeightedLatLngAlt> points = new ArrayList<>();
		/* Per room generate random data */
		// Back stairs
		points.addAll(generateRandomData(1, new LatLng(39.892044, 32.783216), new LatLng(39.892020, 32.783274)));
		// front stairs
		points.addAll(generateRandomData(2, new LatLng(39.891801, 32.783157), new LatLng(39.891767, 32.783220)));
		// hall
		points.addAll(generateRandomData(1, new LatLng(39.891763, 32.783162), new LatLng(39.891737, 32.783218)));
		createHeatmap(points, 5);
	}

	private void createThirdFloorHeatmapData() {
		List<WeightedLatLngAlt> points = new ArrayList<>();
		/* Per room generate random data */
		// a305
		points.addAll(generateRandomData(3, new LatLng(39.892078, 32.783100), new LatLng(39.892058, 32.783206)));
		// a306
		points.addAll(generateRandomData(1, new LatLng(39.892037, 32.783107), new LatLng(39.892012, 32.783183)));
		// a307
		points.addAll(generateRandomData(1, new LatLng(39.891998, 32.783111), new LatLng(39.891972, 32.783187)));
		// a308
		points.addAll(generateRandomData(3, new LatLng(39.891958, 32.783118), new LatLng(39.891851, 32.783201)));
		// Hallway
		points.addAll(generateRandomData(1, new LatLng(39.892051, 32.783188), new LatLng(39.891844, 32.783232)));
		// Back stairs
		points.addAll(generateRandomData(1, new LatLng(39.892044, 32.783216), new LatLng(39.892020, 32.783274)));
		// a304
		points.addAll(generateRandomData(1, new LatLng(39.892011, 32.783225), new LatLng(39.891979, 32.783298)));
		// a303
		points.addAll(generateRandomData(1, new LatLng(39.891962, 32.783229), new LatLng(39.891938, 32.783303)));
		// a302
		points.addAll(generateRandomData(1, new LatLng(39.891923, 32.783232), new LatLng(39.891903, 32.783306)));
		// wc
		points.addAll(generateRandomData(1, new LatLng(39.891888, 32.783236), new LatLng(39.891847, 32.783283)));
		// tea room
		points.addAll(generateRandomData(1, new LatLng(39.891892, 32.783290), new LatLng(39.891857, 32.783311)));
		// hall
		points.addAll(generateRandomData(2, new LatLng(39.891836, 32.783127), new LatLng(39.891822, 32.783317)));
		// resting room
		points.addAll(generateRandomData(8, new LatLng(39.891809, 32.783245), new LatLng(39.891756, 32.783322)));
		// front stairs
		points.addAll(generateRandomData(2, new LatLng(39.891801, 32.783157), new LatLng(39.891767, 32.783220)));
		// B block crossover
		points.addAll(generateRandomData(3, new LatLng(39.891851, 32.783321), new LatLng(39.891835, 32.783520)));
		// hall addition
		points.addAll(generateRandomData(2, new LatLng(39.891795, 32.783111), new LatLng(39.891745, 32.783155)));
		// bmb5
		points.addAll(generateRandomData(30, new LatLng(39.891727, 32.783142), new LatLng(39.891641, 32.783260)));
		createHeatmap(points, 6);
	}

	private void createThirdHalfFloorHeatmapData() {
		List<WeightedLatLngAlt> points = new ArrayList<>();
		/* Per room generate random data */
		// Back stairs
		points.addAll(generateRandomData(1, new LatLng(39.892044, 32.783216), new LatLng(39.892020, 32.783274)));
		// front stairs
		points.addAll(generateRandomData(2, new LatLng(39.891801, 32.783157), new LatLng(39.891767, 32.783220)));
		// hall
		points.addAll(generateRandomData(1, new LatLng(39.891763, 32.783162), new LatLng(39.891737, 32.783218)));
		createHeatmap(points, 7);
	}

	private void createFourthFloorHeatmapData() {
		List<WeightedLatLngAlt> points = new ArrayList<>();
		/* Per room generate random data */
		// a405
		points.addAll(generateRandomData(3, new LatLng(39.892078, 32.783100), new LatLng(39.892058, 32.783206)));
		// a406
		points.addAll(generateRandomData(1, new LatLng(39.892037, 32.783107), new LatLng(39.892012, 32.783183)));
		// a407
		points.addAll(generateRandomData(1, new LatLng(39.891998, 32.783111), new LatLng(39.891972, 32.783187)));
		// a408
		points.addAll(generateRandomData(3, new LatLng(39.891958, 32.783118), new LatLng(39.891851, 32.783201)));
		// Hallway
		points.addAll(generateRandomData(1, new LatLng(39.892051, 32.783188), new LatLng(39.891844, 32.783232)));
		// Back stairs
		points.addAll(generateRandomData(1, new LatLng(39.892044, 32.783216), new LatLng(39.892020, 32.783274)));
		// a404
		points.addAll(generateRandomData(1, new LatLng(39.892011, 32.783225), new LatLng(39.891979, 32.783298)));
		// a403
		points.addAll(generateRandomData(1, new LatLng(39.891962, 32.783229), new LatLng(39.891938, 32.783303)));
		// a402
		points.addAll(generateRandomData(1, new LatLng(39.891923, 32.783232), new LatLng(39.891903, 32.783306)));
		// wc
		points.addAll(generateRandomData(2, new LatLng(39.891888, 32.783236), new LatLng(39.891856, 32.783312)));
		// hall
		points.addAll(generateRandomData(2, new LatLng(39.891843, 32.783210), new LatLng(39.891820, 32.783283)));
		// front stairs
		points.addAll(generateRandomData(2, new LatLng(39.891800, 32.783189), new LatLng(39.891765, 32.783218)));
		// hall study area
		points.addAll(generateRandomData(2, new LatLng(39.891837, 32.783126), new LatLng(39.891804, 32.783189)));
		// a401
		points.addAll(generateRandomData(3, new LatLng(39.891810, 32.783246), new LatLng(39.891756, 32.783322)));
		createHeatmap(points, 8);
	}

	private void createHeatmap(List<WeightedLatLngAlt> pointsPerPolygon, int floor) {
		pointsPerPolygon.add(new WeightedLatLngAlt(userLocation.getLocation().latitude, userLocation.getLocation().longitude, 1));
		heatmaps.add(
				m_eegeoMap.addHeatmap(
					new HeatmapOptions()
							.polygon(new PolygonOptions().indoor(demoIndoorMapId, floor))
							.add(pointsPerPolygon.toArray(new WeightedLatLngAlt[0]))
							.weightMin(2.0)
							.weightMax(8.0)
//						 sets normative value to be mid-point between weightMin and weightMax,
//						 to align with transparent mid-point of color gradient
							.intensityBias(0.5f)
							.interpolateDensityByZoom(18.0, 21.5)
							.addDensityStop(0.0f, 0.6, 1.0)
							.addDensityStop(0.5f, 1.3, 0.75)
							.addDensityStop(1.0f, 2.1, 0.5)
							.gradient(
									// transparent at mid-point, with differing hues either side,
									// suitable for diverging data set. Similar to:
									// http://colorbrewer2.org/#type=diverging&scheme=RdYlBu&n=6
									new float[]{0.f, 0.1f, 0.4f, 0.49f, 0.51f, 0.6f, 0.9f, 1.f},
									new int[]{0x4575b4ff, 0x91bfdbff, 0xe0f3f8ff, 0xffffff00, 0xffffff00, 0xfee090ff, 0xfc8d59ff, 0xd73027ff})
							.opacity(0.8f)
				)
		);
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
			for (Heatmap heatmap : heatmaps) {
				m_eegeoMap.removeHeatmap(heatmap);
			}
		}
		m_mapView.onDestroy();
	}
}