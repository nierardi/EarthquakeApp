package EarthquakeApp;

import com.sun.jndi.cosnaming.CNNameParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;

/**
 * Translates an address or search string into geographic
 * coordinates, using an online API. Returns these coordinates
 * and a user-displayable place name.
 *
 * @author Nolan Ierardi
 */
public class GeoCoder {

	private String GEOCODE_API_METHOD = "https://us1.locationiq.org/v1/search.php?key=902a0943d207c2&format=json&q=";
	private String searchString;
	private String dispName;
	private String error;
	private double lat;
	private double lng;
	private boolean success;

	/**
	 * Constructor for GeoCoder.
	 * @param address the address or search string to translate to coordinates
	 */
	public GeoCoder(String address) {
		lat = 0;
		lng = 0;
		success = false;
		dispName = "";
		error = "";
		try {
			searchString = URLEncoder.encode(address, "ASCII");
		} catch (UnsupportedEncodingException e) {
			// This will never happen, as we are hard-coding the encoding
		}
		doGeoCode();
	}

	private void doGeoCode() {

		try {

			// the ultimate spaghetti code. (DON'T INCLUDE THIS IN PRODUCTION)
			//String response = new Scanner(new URL(GEOCODE_API_METHOD + URLEncoder.encode(searchString, "ASCII"))
			// .openConnection().getInputStream()).useDelimiter("\\A").next();

			URLConnection connection = new URL(GEOCODE_API_METHOD + searchString).openConnection();
			String response = new Scanner(connection.getInputStream()).useDelimiter("\\A").next();
			JSONArray locationList = new JSONArray(response);
			if (locationList.length() == 0) {
				// response failed: no search results found
				success = false;
				error = "No search results found for address.";
			} else {
				// We are only returning the first result found. In the future, we could
				// possibly implement selection of results.
				JSONObject firstLocation = locationList.getJSONObject(0);
				lat = firstLocation.getDouble("lat");
				lng = firstLocation.getDouble("lon");
				dispName = firstLocation.getString("display_name");
				success = true;
				if (Main.DEBUG) {
					if (Main.DEBUG) System.out.println("lat: " + lat + " long: " + lng + " success: " + success + " displayname: " + dispName);
				}
			}

		} catch (Exception e) {
			success = false;
			if (e.getClass().toString().contains("FileNotFoundException")) {
				error = "Address not found.";
			} else if (e.getMessage().contains("429")) {
				error = "Too many requests. Slow down a little!";
			} else {
				error = e.getMessage();
				if (Main.DEBUG) e.printStackTrace();
			}
		}

	}

	/**
	 * Returns the latitude.
	 * @return latitude of first found location, -1 if error
	 */
	public double getLat() {
		if (success) {
			return lat;
		} else {
			return -1;
		}
	}

	/**
	 * Returns the longitude.
	 * @return longitude of first found location, -1 if error
	 */
	public double getLng() {
		if (success) {
			return lng;
		} else {
			return -1;
		}
	}

	/**
	 * Returns the location display (place) name.
	 * @return display name of first found location, null if error
	 */
	public String getDispName() {
		if (success) {
			return dispName;
		} else {
			return null;
		}
	}

	/**
	 * Returns an error encountered by excecution of this class.
	 * This error is formatted for user-facing display, and can be a
	 * result of an invalid address returning no data, a rate limit exception,
	 * or some other error while connecting to the URL.
	 * @return if no error, "none"; if an error, string explaining the error to the user
	 */
	public String getError() {
		if (success) {
			return "none";
		} else {
			return error;
		}
	}


}
