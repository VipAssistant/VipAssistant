package com.vipassistant.mobile.demo.ui.service;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.eegeo.mapapi.geometry.LatLng;
import com.vipassistant.mobile.demo.ui.constants.Constants;
import com.vipassistant.mobile.demo.ui.model.Location;

import java.util.*;
import java.util.stream.Collectors;

import static com.vipassistant.mobile.demo.ui.constants.Constants.locationLatEps;
import static com.vipassistant.mobile.demo.ui.constants.Constants.locationLongEps;

public class LocationService {
	/* We keep Location Information in our custom Indoor Map in 1 container
	 * namely in an ArrayList and perform all our queries with stream operations on that list */
	private final List<Location> allLocations;

	public LocationService(ArrayList<Location> allLocations) {
		this.allLocations = allLocations;
	}

	public List<Location> getAllLocations() {
		return allLocations;
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public Optional<Location> getLocation(LatLng location) {
		return allLocations.stream()
				.filter(loc -> loc.getLocation() == location)
				.findAny();
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public Optional<List<Location>> findByName(String name) {
		return Optional.of(allLocations.stream()
								.filter(loc -> loc.getName() == name)
								.collect(Collectors.toList()));
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public Optional<List<Location>> findByType(String type) {
		return Optional.of(allLocations.stream()
								.filter(loc -> loc.getType() == type)
								.collect(Collectors.toList()));
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public Optional<List<Location>> findByFloorAndLocation(Integer floor, LatLng location) {
		return Optional.of(allLocations.stream()
								.filter(loc -> loc.getFloor() == floor)
								.filter(loc -> Math.abs(loc.getLocation().latitude + loc.getLocEpsLat() - location.latitude) <= locationLatEps &&
										       Math.abs(loc.getLocation().longitude + loc.getLocEpsLong() - location.longitude) <= locationLongEps)
								.collect(Collectors.toList()));
	}
}
