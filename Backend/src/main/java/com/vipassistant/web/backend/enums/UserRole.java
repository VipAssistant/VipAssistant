package com.vipassistant.web.backend.enums;

public enum UserRole {
	USER("USER"),
	ADMIN("ADMIN");

	private final String displayName;

	UserRole(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return this.displayName;
	}
}
