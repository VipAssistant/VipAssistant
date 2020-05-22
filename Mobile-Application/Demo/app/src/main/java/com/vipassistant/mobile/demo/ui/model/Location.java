package com.vipassistant.mobile.demo.ui.model;

import androidx.annotation.Nullable;
import com.eegeo.mapapi.geometry.LatLng;

public class Location {
	private String name;
	private String type;
	private LatLng location;
	private Double locEpsLat;
	private Double locEpsLong;
	private Integer floor; // May be null if location is in outdoor
	private String indoorMapId; // May be null if location is in outdoor

	public Location(String name, String type, LatLng location) {
		this.name = name;
		this.type = type;
		this.location = location;
	}

	public Location(String name, String type, LatLng location, Double locEpsLat, Double locEpsLong, Integer floor, String indoorMapId) {
		this.name = name;
		this.type = type;
		this.location = location;
		this.locEpsLat = locEpsLat;
		this.locEpsLong = locEpsLong;
		this.floor = floor;
		this.indoorMapId = indoorMapId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LatLng getLocation() {
		return location;
	}

	public void setLocation(LatLng location) {
		this.location = location;
	}

	public Double getLocEpsLat() {
		return locEpsLat;
	}

	public void setLocEpsLat(Double locEpsLat) {
		this.locEpsLat = locEpsLat;
	}

	public Double getLocEpsLong() {
		return locEpsLong;
	}

	public void setLocEpsLong(Double locEpsLong) {
		this.locEpsLong = locEpsLong;
	}

	public Integer getFloor() {
		return floor;
	}

	public void setFloor(Integer floor) {
		this.floor = floor;
	}

	public String getIndoorMapId() {
		return indoorMapId;
	}

	public void setIndoorMapId(String indoorMapId) {
		this.indoorMapId = indoorMapId;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		Location op2 = (Location) obj;
		return  this.location.latitude == op2.getLocation().latitude &&
				this.location.longitude == op2.getLocation().longitude &&
				this.indoorMapId.equals(op2.getIndoorMapId()) &&
				this.getFloor() == op2.getFloor();

	}
}
