package com.vipassistant.mobile.demo.ui.service;

import android.util.Log;
import android.util.Pair;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipassistant.mobile.demo.ui.constants.Constants;
import com.vipassistant.mobile.demo.ui.model.Beacon;
import com.vipassistant.mobile.demo.ui.model.RestApiResponse;
import com.vipassistant.mobile.demo.ui.model.User;
import okhttp3.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;

import static com.vipassistant.mobile.demo.ui.constants.Constants.*;
import static com.vipassistant.mobile.demo.ui.utils.Utils.resolveHttpCodeResponse;

public class RequestService {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final OkHttpClient client = new OkHttpClient();

	/**
	 * Static login request method. Can be called from any UI code.
	 * Creates initial User credentials with the cookie-provided values of the user
	 * (username, password) and makes the request using HTTPS and HTTP Basic Auth to the backend.
	 * Responses with logged in user and response message to the caller code.
	 *
	 * @param user
	 * @return Pair<User, String>
	 */
	public static Pair<User, String> login(User user) {
		/* Construct the password first */
		String encodedPassword = BCrypt.hashpw(user.getPassword(), HASH_SALT);
		MediaType mediaType = MediaType.parse("text/plain");
		RequestBody body = RequestBody.create(mediaType, "");
		String authorizationHeader = Credentials.basic(user.getUsername(), encodedPassword);
		Request request = new Request.Builder()
				.url(BACKEND_BASE_URL + "/users/login")
				.method("POST", body)
				.addHeader("Authorization", authorizationHeader)
				.build();
		try {
			Response response = client.newCall(request).execute();
			if (response.code() == 200) {
				String responseBody = response.body().string();
				RestApiResponse mappedResponse = objectMapper.readValue(responseBody, RestApiResponse.class);
				if (mappedResponse.getResult().equals("success")) {
					User loggedInUser = objectMapper.convertValue(mappedResponse.getData(), User.class);
					return Pair.create(loggedInUser, mappedResponse.getMessage());
				} else {
					Log.w(String.valueOf(Log.WARN),"Request result is 'fail' on login request");
					return Pair.create(null, mappedResponse.getMessage());
				}
			} else {
				Log.w(String.valueOf(Log.WARN), String.format("Got HTTP %s from login request", response.code()));
				String responseString = resolveHttpCodeResponse(response.code());
				return Pair.create(null, responseString);
			}
		} catch (ConnectException e) {
			Log.w(String.valueOf(Log.WARN), "HTTP Connection Error on request");
			return Pair.create(null, HTTP_CONN_ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			return Pair.create(null, CLIENT_ERROR);
		}
	}

	/**
	 * Static register request method. Can be called from any UI code.
	 * Creates initial User credentials with the user-provided values of the user
	 * (username, password) and makes the request using HTTPS to the backend.
	 * Responses with created User and response message to the caller code.
	 * Beware, no authorization header is provided, since this endpoint is not authorized.
	 *
	 * @param user
	 * @return Pair<User, String>
	 */
	public static Pair<User, String> register(User user) {
		/* Construct the password first */
		String encodedPassword = BCrypt.hashpw(user.getPassword(), HASH_SALT);
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType,
				"{\n\t\"username\": \"" + user.getUsername() + "\"," +
						"\n\t\"password\": \"" + encodedPassword + "\"\n}");
		Request request = new Request.Builder()
				.url(BACKEND_BASE_URL + "/users/register")
				.method("POST", body)
				.addHeader("Content-Type", "application/json")
				.build();

		try {
			Response response = client.newCall(request).execute();
			if (response.code() == 200) {
				String responseBody = response.body().string();
				RestApiResponse mappedResponse = objectMapper.readValue(responseBody, RestApiResponse.class);
				if (mappedResponse.getResult().equals("success")) {
					User createdUser = objectMapper.convertValue(mappedResponse.getData(), User.class);
					return Pair.create(createdUser, mappedResponse.getMessage());
				} else {
					Log.w(String.valueOf(Log.WARN),"Request result is 'fail' on register request");
					return Pair.create(null, mappedResponse.getMessage());
				}
			} else {
				Log.w(String.valueOf(Log.WARN), String.format("Got HTTP %s from register request", response.code()));
				String responseString = resolveHttpCodeResponse(response.code());
				return Pair.create(null, responseString);
			}
		} catch (ConnectException e) {
			Log.w(String.valueOf(Log.WARN), "HTTP Connection Error on register request");
			return Pair.create(null, HTTP_CONN_ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			return Pair.create(null, CLIENT_ERROR);
		}
	}

	/**
	 * Static addSocialDistancingPeople request method. Can be called from any UI code.
	 * Creates initial User credentials with the cookie-provided values of the user
	 * (username, password) and makes the request using HTTPS and HTTP Basic Auth to the backend.
	 * Responses with the result of the operation as flag and a message.
	 *
	 * @param user
	 * @return Pair<Boolean, String>
	 */
	public static Pair<Boolean, String> addSocialDistancingPeople(String macAddressOfPerson) {
		if (userCookie != null) {
			String authorizationHeader = Credentials.basic(userCookie.getUsername(),
					userCookie.getPassword());
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType,
					"{\n\t\"macAddressOfPerson\":" + macAddressOfPerson + "\n}");
			Request request = new Request.Builder()
					.url(BACKEND_BASE_URL + "/users/addToSocialDistancingList/" + userCookie.getId())
					.method("PUT", body)
					.addHeader("Authorization", authorizationHeader)
					.addHeader("Content-Type", "application/json")
					.build();
			try {
				Response response = client.newCall(request).execute();
				if (response.code() == 200) {
					String responseBody = response.body().string();
					RestApiResponse mappedResponse = objectMapper.readValue(responseBody, RestApiResponse.class);
					if (mappedResponse.getResult().equals("success")) {
						return Pair.create(true, mappedResponse.getMessage());
					} else {
						Log.w(String.valueOf(Log.WARN),"Request result is 'fail' on addSocialDistancingPeople request");
						return Pair.create(null, mappedResponse.getMessage());
					}
				} else {
					Log.w(String.valueOf(Log.WARN), String.format("Got HTTP %s from addSocialDistancingPeople request", response.code()));
					String responseString = resolveHttpCodeResponse(response.code());
					return Pair.create(null, responseString);
				}
			} catch (ConnectException e) {
				Log.w(String.valueOf(Log.WARN), "HTTP Connection Error on addSocialDistancingPeople request");
				return Pair.create(null, Constants.HTTP_CONN_ERROR);
			} catch (IOException e) {
				e.printStackTrace();
				return Pair.create(null, Constants.CLIENT_ERROR);
			}
		} else {
			Log.w(String.valueOf(Log.WARN), "User cookie not found");
			return Pair.create(null, Constants.COOKIE_NOTFOUND);
		}
	}

	/**
	 * Static checkUserSocialDistanceSet request method. Can be called from any UI code.
	 * Creates initial User credentials with the cookie-provided values of the user
	 * (username, password) and makes the request using HTTPS and HTTP Basic Auth to the backend.
	 * Responses with the result of the operation with a bool indicating whether any
	 * of the users that was nearby this cookie-specified user has tested positive in the close past
	 *
	 * @param user
	 * @return Pair<Boolean, String>
	 */
	public static Pair<Boolean, String> checkUserSocialDistanceSet() {
		if (userCookie != null) {
			String authorizationHeader = Credentials.basic(userCookie.getUsername(),
					userCookie.getPassword());
			Request request = new Request.Builder()
					.url(Constants.BACKEND_BASE_URL + "/users/checkSocialDistancingList/" + userCookie.getId())
					.method("GET", null)
					.addHeader("Authorization", authorizationHeader)
					.build();
			try {
				Response response = client.newCall(request).execute();
				if (response.code() == 200) {
					String responseBody = response.body().string();
					RestApiResponse mappedResponse = objectMapper.readValue(responseBody, RestApiResponse.class);
					if (mappedResponse.getResult().equals("success")) {
						return Pair.create(true, mappedResponse.getMessage());
					} else {
						Log.w(String.valueOf(Log.WARN),"Request result is 'fail' on checkUserSocialDistanceSet request");
						return Pair.create(false, mappedResponse.getMessage());
					}
				} else {
					Log.w(String.valueOf(Log.WARN), String.format("Got HTTP %s from checkUserSocialDistanceSet request", response.code()));
					String responseString = resolveHttpCodeResponse(response.code());
					return Pair.create(false, responseString);
				}
			} catch (ConnectException e) {
				Log.w(String.valueOf(Log.WARN), "HTTP Connection Error on checkUserSocialDistanceSet request");
				return Pair.create(false, Constants.HTTP_CONN_ERROR);
			} catch (IOException e) {
				e.printStackTrace();
				return Pair.create(false, Constants.CLIENT_ERROR);
			}
		} else {
			Log.w(String.valueOf(Log.WARN), "User cookie not found");
			return Pair.create(false, Constants.COOKIE_NOTFOUND);
		}
	}

	/**
	 * Static getBeaconTable request method. Can be called from any UI code.
	 * Creates initial User credentials with the cookie-provided values of the user
	 * (username, password) and makes the request using HTTPS and HTTP Basic Auth to the backend.
	 * Responses with beacon table if exists and operation of the result with a message from backend.
	 *
	 * @param user
	 * @return Pair<Boolean, String>
	 */
	public static Pair<List<Beacon>, String> getBeaconTable() {
		if (userCookie != null) {
			String authorizationHeader = Credentials.basic(userCookie.getUsername(),
					userCookie.getPassword());
			MediaType mediaType = MediaType.parse("application/json");
			Request request = new Request.Builder()
					.url(BACKEND_BASE_URL + "/beacons")
					.method("GET", null)
					.addHeader("Authorization", authorizationHeader)
					.addHeader("Content-Type", "application/json")
					.build();
			try {
				Response response = client.newCall(request).execute();
				if (response.code() == 200) {
					String responseBody = response.body().string();
					RestApiResponse mappedResponse = objectMapper.readValue(responseBody, RestApiResponse.class);
					if (mappedResponse.getResult().equals("success")) {
						List<Beacon> beaconTable = objectMapper.convertValue(mappedResponse.getData(), List.class);
						return Pair.create(beaconTable, mappedResponse.getMessage());
					} else {
						Log.w(String.valueOf(Log.WARN),"Request result is 'fail' on getBeaconTable request");
						return Pair.create(null, mappedResponse.getMessage());
					}
				} else {
					Log.w(String.valueOf(Log.WARN), String.format("Got HTTP %s from getBeaconTable request", response.code()));
					String responseString = resolveHttpCodeResponse(response.code());
					return Pair.create(null, responseString);
				}
			} catch (ConnectException e) {
				Log.w(String.valueOf(Log.WARN), "HTTP Connection Error on getBeaconTable request");
				return Pair.create(null, Constants.HTTP_CONN_ERROR);
			} catch (IOException e) {
				e.printStackTrace();
				return Pair.create(null, Constants.CLIENT_ERROR);
			}
		} else {
			Log.w(String.valueOf(Log.WARN), "User cookie not found");
			return Pair.create(null, Constants.COOKIE_NOTFOUND);
		}
	}
}
