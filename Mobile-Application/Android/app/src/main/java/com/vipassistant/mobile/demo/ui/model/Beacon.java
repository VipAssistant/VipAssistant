package com.vipassistant.mobile.demo.ui.model;

public class Beacon {
	private Integer rssiValue;
	private String uuid;
	private Location location;

	public Beacon() {
	}

	public Beacon(Integer rssiValue, String uuid, Location location) {
		this.rssiValue = rssiValue;
		this.uuid = uuid;
		this.location = location;
	}

	public Integer getRssiValue() {
		return rssiValue;
	}

	public void setRssiValue(Integer rssiValue) {
		this.rssiValue = rssiValue;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}
