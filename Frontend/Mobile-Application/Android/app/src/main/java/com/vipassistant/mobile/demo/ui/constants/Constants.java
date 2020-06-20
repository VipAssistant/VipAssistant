package com.vipassistant.mobile.demo.ui.constants;

import com.eegeo.mapapi.geometry.LatLng;
import com.vipassistant.mobile.demo.ui.model.Beacon;
import com.vipassistant.mobile.demo.ui.model.Location;
import com.vipassistant.mobile.demo.ui.model.StepInfo;
import com.vipassistant.mobile.demo.ui.model.User;

import java.util.*;

public class Constants {

	/* MapNavigation Constants */
	public static final String demoIndoorMapId = "EIM-71597625-a9b6-4753-b91f-1c0e74fc966d";
	public static final String demoBuildingName = "METU-CENG Block A";
	public static final Location navigateInitialLocation = new Location("Entrance", "Building Entrance", new LatLng(39.891753, 32.783191), .000030, .000007, 1, demoIndoorMapId);
	public static final Location heatmapInitialLocation = new Location("BMB-5", "Classroom", new LatLng(39.891681, 32.783200), .000050, .000081, 6, demoIndoorMapId);
	public static final Location locationDemoInitialLocation1 = new Location("Room A-408", "Staff Room", new LatLng(39.891923, 32.783159), .000014, .000040, 8, demoIndoorMapId);
	public static final Location locationDemoInitialLocation2 = new Location("Corridor", "Corridor", new LatLng(39.891967, 32.783209), .000014, .000040, 8, demoIndoorMapId);
	public static final ArrayList<Integer> demoFirstRssi = new ArrayList<Integer>(){{add(-61); add(-63); add(-66); add(-74); add(-90);}};
	public static final String mapSceneLink = "https://wrld.mp/d5ffdc5";
	public static final String markerText = "You Are Here!";
	public static int mapRefreshMillis = 1000;
	public static int demoRefreshMillis = 2000;
	public static final double locationLatEps = 0.0001;
	public static final double locationLongEps = 0.0001;
	public static double cameraZoom = 20;
	public static double demoCameraZoom = 25;
	public static double outdoorSearchCameraZoom = 13.0826540246474;
	public static double cameraTilt = 35;
	public static double demoCameraTilt = 15;
	public static double cameraTiltHeatmap = 0;
	public static final double PERSON_WALKING_SPEED = 1.4;

	public static User userCookie = null;

	/* API-related Constants */
	public static final String BACKEND_BASE_URL = "https://vipassistant.ceng.metu.edu.tr:8081/api";
	public static final String HASH_SALT = "$2a$10$3j3gfVtuynuvE6FIVvygdu"; /* A Random BCrypt Salt value generated with BCrypt.gensalt(10) */

	/* Request Message Constants */
	public static final String CLIENT_ERROR = "Oops! Something went wrong on client";
	public static final String COOKIE_NOTFOUND = "Oops! Seems like game cookie that has your login credentials is not found.\nPlease try to logout and then login.";
	public static final String HTTP_400 = "Oops! Request failed - BAD REQUEST";
	public static final String HTTP_401 = "Invalid username or password - UNAUTHORIZED";
	public static final String HTTP_404 = "Oops! Requested data is not found on the server.";
	public static final String HTTP_CONN_ERROR = "Oops! Seems like you can't access the server.\nPlease check your internet connection and try again.";
	public static final String HTTP_500 = "Oops! Something went wrong on server";

	/* Outdoor Location (cities) Container (gets loaded on startup) */
	public static ArrayList<Location> allOutdoorLocations = new ArrayList<Location>();

	/* Demo Indoor Map's Location Container */
	public static ArrayList<Location> allLocations = new ArrayList<Location>() {{
		/* Basement */
		add(new Location("Server Room", "Server Room", new LatLng(39.892068, 32.783152), .000014, .000058, 0, demoIndoorMapId));
		add(new Location("Digital Lab", "Lab", new LatLng(39.891962, 32.783152), .000088, .000038, 0, demoIndoorMapId));
		add(new Location("Stationary", "Stationary", new LatLng(39.891855, 32.783169), .000018, .000035, 0, demoIndoorMapId));
		add(new Location("Basement Back Upstairs", "Stairs", new LatLng(39.892025, 32.783237), .000005, .000023, 0, demoIndoorMapId));
		add(new Location("Basement Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 0, demoIndoorMapId));
		add(new Location("Z-103", "Staff Room", new LatLng(39.891951, 32.783267), .000019, .000040, 0, demoIndoorMapId));
		add(new Location("Z-102", "Staff Room", new LatLng(39.891911, 32.783274), .000017, .000040, 0, demoIndoorMapId));
		add(new Location("Basement Disabled Restroom/Wc", "Disabled Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 0, demoIndoorMapId));
		add(new Location("Basement Woman Restroom/Wc", "Woman Restroom/Wc", new LatLng(39.891868, 32.783292), .000016, .000006, 0, demoIndoorMapId));
		add(new Location("Basement Man Restroom/Wc", "Man Restroom/Wc", new LatLng(39.891876, 32.783309), .000019, .000006, 0, demoIndoorMapId));
		add(new Location("Basement Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 0, demoIndoorMapId));
		add(new Location("Basement Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 0, demoIndoorMapId));
		add(new Location("Study Room", "Study Room", new LatLng(39.891780, 32.783290), .000033, .000038, 0, demoIndoorMapId));
		add(new Location("Vending Machine", "Vending Machine/Automat", new LatLng(39.891797, 32.783194), .000003, .000003, 0, demoIndoorMapId));
		add(new Location("Basement Front Upstairs", "Stairs", new LatLng(39.891780, 32.783169), .000008, .000015, 0, demoIndoorMapId));

		/* Entrance */
		add(navigateInitialLocation);
		add(new Location("Entrance Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 1, demoIndoorMapId));
		add(new Location("Entrance Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 1, demoIndoorMapId));
		add(new Location("Entrance Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 1, demoIndoorMapId));
		add(new Location("Entrance Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 1, demoIndoorMapId));

		/* 1st Floor */
		add(new Location("Room A-105", "Staff Room", new LatLng(39.892068, 32.783152), .000014, .000058, 2, demoIndoorMapId));
		add(new Location("Room A-106", "Staff Room", new LatLng(39.892027, 32.783144), .000015, .000038, 2, demoIndoorMapId));
		add(new Location("Room A-107", "Staff Room", new LatLng(39.891984, 32.783150), .000015, .000038, 2, demoIndoorMapId));
		add(new Location("Room A-108", "Staff Room", new LatLng(39.891902, 32.783159), .000060, .000038, 2, demoIndoorMapId));
		add(new Location("First Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 2, demoIndoorMapId));
		add(new Location("First Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 2, demoIndoorMapId));
		add(new Location("Room A-104", "Staff Room", new LatLng(39.891997, 32.783264), .000018, .000040, 2, demoIndoorMapId));
		add(new Location("Room A-103", "Staff Room", new LatLng(39.891949, 32.783269), .000018, .000040, 2, demoIndoorMapId));
		add(new Location("Room A-102", "Staff Room", new LatLng(39.891909, 32.783274), .000014, .000040, 2, demoIndoorMapId));
		add(new Location("First Floor Woman Restroom/Wc", "Woman Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 2, demoIndoorMapId));
		add(new Location("First Floor Tea Room", "Tea Room", new LatLng(39.891875, 32.783303), .000025, .000010, 2, demoIndoorMapId));
		add(new Location("First Floor Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 2, demoIndoorMapId));
		add(new Location("First Floor Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 2, demoIndoorMapId));
		add(new Location("First Floor Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 2, demoIndoorMapId));
		add(new Location("First Floor Crossover to B Block", "Crossover", new LatLng(39.891843, 32.783413), .000015, .000077, 2, demoIndoorMapId));
		add(new Location("First Floor B Block Entrance", "B Block Entrance", new LatLng(39.891823, 32.783558), .000053, .000062, 2, demoIndoorMapId));
		add(new Location("Room A-101", "Classroom", new LatLng(39.891780, 32.783290), .000033, .000038, 2, demoIndoorMapId));
		add(new Location("First Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 2, demoIndoorMapId));
		add(new Location("First Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 2, demoIndoorMapId));

		/* 1.5 Floor */
		add(new Location("First Half Floor Hall", "Hall", new LatLng(39.891753, 32.783191), .000030, .000007, 3, demoIndoorMapId));
		add(new Location("BMB-4", "Classroom", new LatLng(39.891681, 32.783200), .000050, .000081, 3, demoIndoorMapId));
		add(new Location("First Half Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 3, demoIndoorMapId));
		add(new Location("First Half Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 3, demoIndoorMapId));
		add(new Location("First Half Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 3, demoIndoorMapId));
		add(new Location("First Half Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 3, demoIndoorMapId));

		/* 2nd Floor */
		add(new Location("Room A-205", "Staff Room", new LatLng(39.892068, 32.783152), .000014, .000058, 4, demoIndoorMapId));
		add(new Location("Room A-206", "Staff Room", new LatLng(39.892027, 32.783144), .000015, .000038, 4, demoIndoorMapId));
		add(new Location("Room A-207", "Staff Room", new LatLng(39.891984, 32.783150), .000015, .000038, 4, demoIndoorMapId));
		add(new Location("Room A-208", "Staff Room", new LatLng(39.891902, 32.783159), .000060, .000038, 4, demoIndoorMapId));
		add(new Location("Second Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 4, demoIndoorMapId));
		add(new Location("Second Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 4, demoIndoorMapId));
		add(new Location("Room A-204", "Staff Room", new LatLng(39.891997, 32.783264), .000018, .000040, 4, demoIndoorMapId));
		add(new Location("Room A-203", "Staff Room", new LatLng(39.891949, 32.783269), .000018, .000040, 4, demoIndoorMapId));
		add(new Location("Room A-202", "Staff Room", new LatLng(39.891909, 32.783274), .000014, .000040, 4, demoIndoorMapId));
		add(new Location("Second Floor Man Restroom/Wc", "Man Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 4, demoIndoorMapId));
		add(new Location("Second Floor Tea Room", "Tea Room", new LatLng(39.891875, 32.783303), .000025, .000010, 4, demoIndoorMapId));
		add(new Location("Second Floor Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 4, demoIndoorMapId));
		add(new Location("Second Floor Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 4, demoIndoorMapId));
		add(new Location("Second Floor Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 4, demoIndoorMapId));
		add(new Location("Second Floor Crossover to B Block", "Crossover", new LatLng(39.891843, 32.783413), .000015, .000077, 4, demoIndoorMapId));
		add(new Location("Second Floor B Block Entrance", "B Block Entrance", new LatLng(39.891823, 32.783558), .000053, .000062, 4, demoIndoorMapId));
		add(new Location("Resting Room", "Resting Room", new LatLng(39.891780, 32.783290), .000033, .000038, 4, demoIndoorMapId));
		add(new Location("Second Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 4, demoIndoorMapId));
		add(new Location("Second Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 4, demoIndoorMapId));

		/* 2.5 Floor */
		add(new Location("Second Half Floor Hall", "Hall", new LatLng(39.891753, 32.783191), .000030, .000007, 5, demoIndoorMapId));
		add(new Location("Second Half Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 5, demoIndoorMapId));
		add(new Location("Second Half Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 5, demoIndoorMapId));
		add(new Location("Second Half Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 5, demoIndoorMapId));
		add(new Location("Second Half Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 5, demoIndoorMapId));

		/* 3rd Floor */
		add(new Location("BMB-5", "Classroom", new LatLng(39.891681, 32.783200), .000050, .000081, 6, demoIndoorMapId));
		add(new Location("Room A-305", "Staff Room", new LatLng(39.892068, 32.783152), .000014, .000058, 6, demoIndoorMapId));
		add(new Location("Room A-306", "Staff Room", new LatLng(39.892027, 32.783144), .000015, .000038, 6, demoIndoorMapId));
		add(new Location("Room A-307", "Staff Room", new LatLng(39.891984, 32.783150), .000015, .000038, 6, demoIndoorMapId));
		add(new Location("Room A-308", "Staff Room", new LatLng(39.891902, 32.783159), .000060, .000038, 6, demoIndoorMapId));
		add(new Location("Third Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 6, demoIndoorMapId));
		add(new Location("Third Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 6, demoIndoorMapId));
		add(new Location("Room A-304", "Staff Room", new LatLng(39.891997, 32.783264), .000018, .000040, 6, demoIndoorMapId));
		add(new Location("Room A-303", "Staff Room", new LatLng(39.891949, 32.783269), .000018, .000040, 6, demoIndoorMapId));
		add(new Location("Room A-302", "Staff Room", new LatLng(39.891909, 32.783274), .000014, .000040, 6, demoIndoorMapId));
		add(new Location("Third Floor Man Restroom/Wc", "Man Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 6, demoIndoorMapId));
		add(new Location("Third Floor Tea Room", "Tea Room", new LatLng(39.891875, 32.783303), .000025, .000010, 6, demoIndoorMapId));
		add(new Location("Third Floor Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 6, demoIndoorMapId));
		add(new Location("Third Floor Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 6, demoIndoorMapId));
		add(new Location("Third Floor Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 6, demoIndoorMapId));
		add(new Location("Third Floor Crossover to B Block", "Crossover", new LatLng(39.891843, 32.783413), .000015, .000077, 6, demoIndoorMapId));
		add(new Location("Third Floor B Block Entrance", "B Block Entrance", new LatLng(39.891823, 32.783558), .000053, .000062, 6, demoIndoorMapId));
		add(new Location("Room A-301", "Staff Room", new LatLng(39.891780, 32.783290), .000033, .000038, 6, demoIndoorMapId));
		add(new Location("Third Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 6, demoIndoorMapId));
		add(new Location("Third Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 6, demoIndoorMapId));

		/* 3.5 Floor */
		add(new Location("Third Half Floor Hall", "Hall", new LatLng(39.891753, 32.783191), .000030, .000007, 7, demoIndoorMapId));
		add(new Location("Third Half Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 7, demoIndoorMapId));
		add(new Location("Third Half Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 7, demoIndoorMapId));
		add(new Location("Third Half Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 7, demoIndoorMapId));
		add(new Location("Third Half Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 7, demoIndoorMapId));

		/* 4th Floor */
		add(new Location("Room A-405", "Staff Room", new LatLng(39.892068, 32.783152), .000014, .000058, 8, demoIndoorMapId));
		add(new Location("Room A-406", "Staff Room", new LatLng(39.892027, 32.783144), .000015, .000038, 8, demoIndoorMapId));
		add(new Location("Room A-407", "Staff Room", new LatLng(39.891984, 32.783150), .000015, .000038, 8, demoIndoorMapId));
		add(new Location("Room A-408", "Staff Room", new LatLng(39.891902, 32.783159), .000060, .000038, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 8, demoIndoorMapId));
		add(new Location("Room A-404", "Staff Room", new LatLng(39.891997, 32.783264), .000018, .000040, 8, demoIndoorMapId));
		add(new Location("Room A-403", "Staff Room", new LatLng(39.891949, 32.783269), .000018, .000040, 8, demoIndoorMapId));
		add(new Location("Room A-402", "Staff Room", new LatLng(39.891909, 32.783274), .000014, .000040, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Man Restroom/Wc", "Man Restroom/Wc", new LatLng(39.891873, 32.783274), .000020, .000039, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 8, demoIndoorMapId));
		add(new Location("Room A-401", "Staff Room", new LatLng(39.891780, 32.783296), .000035, .000047, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 8, demoIndoorMapId));
	}};

	/* Beacon Container */
	public static List<Beacon> beaconList = new ArrayList<Beacon>() {{
		add(new Beacon(null, "fcfb2778-8b74-4ff9-81b2-615a9001ee15", new Location("Server Room", "Server Room", new LatLng(39.892068, 32.783152), .000014, .000058, 0, demoIndoorMapId)));
		add(new Beacon(null, "f2f63962-8bdc-4e9f-a37a-fe323e9930ae", new Location("Digital Lab", "Lab", new LatLng(39.891962, 32.783152), .000088, .000038, 0, demoIndoorMapId)));
		add(new Beacon(null, "e2a9b361-29cb-4b05-a7f8-9dc275150a60", new Location("Stationary", "Stationary", new LatLng(39.891855, 32.783169), .000018, .000035, 0, demoIndoorMapId)));
		add(new Beacon(null, "3b455670-698c-4f0b-9d7a-128af96c4d68", new Location("Study Room", "Study Room", new LatLng(39.891780, 32.783290), .000033, .000038, 0, demoIndoorMapId)));
		add(new Beacon(null, "43978a80-79e4-44ef-8804-71a14dad4a6b", new Location("Basement Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 0, demoIndoorMapId)));
	}};

	public static final Set<LatLng> criticalLatLngs = new HashSet<>(
			Arrays.asList(
					new LatLng(39.891772, 32.783201999999996),
					new LatLng(39.891802, 32.78319900000001),
					new LatLng(39.89186, 32.783269)
			)
	);
}