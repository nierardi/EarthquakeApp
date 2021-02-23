package EarthquakeApp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Manages the data of an earthquake data graph.
 * Returns displayable magnitudes paired with timestamps.
 * The timestamps should be converted to user-facing data.
 *
 * @author Nolan Ierardi
 */
public class GraphFeed {

	private static String USGS_API_METHOD = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson";
	private double lat;
	private double lng;
	private int radius;
	private String locationName;
	private String error;
	private ArrayList<Long> timestamps;
	private ArrayList<Double> mags;
	private String displayLabel;
	private Date start;
	private Date end;

	/**
	 * Constructor for GraphFeed.
	 * @param location address or search string to geocode
	 * @param radius circular radius in kilometers
	 * @param start the Date to get results starting from
	 * @param end the Date to get results ending before
	 */
	public GraphFeed(String location, int radius, Date start, Date end) {
		GeoCoder geoCoder;
		mags = new ArrayList<>();
		timestamps = new ArrayList<>();
		this.radius = radius;
		this.start = start;
		this.end = end;
		if (Main.DEBUG) System.out.println("start date: " + start + " end date: " + end);

		if (!location.equals("")) {
			geoCoder = new GeoCoder(location);
		} else {
			geoCoder = new GeoCoder("Radford University");
		}

		error = "none";
		if (geoCoder.getError().equals("none")) {
			// worky
			lat = geoCoder.getLat();
			lng = geoCoder.getLng();
			locationName = geoCoder.getDispName();

			displayLabel += locationName + " from " + start.toString() + " to " + end.toString();

		} else {
			// an error happened
			error = geoCoder.getError();
		}

	}

	/**
	 * Queries the database for new data using the already-
	 * established parameters.
	 */
	public void updateData() {
		if (error.equals("none")) {

			try {

				// open url
				URLConnection connection = new URL(USGS_API_METHOD + "&starttime=" + start.toInstant().toString() +
						"&endtime=" + end.toInstant().toString() + "&lat=" + lat + "&lon=" + lng +
						"&maxradiuskm=" + radius).openConnection();
				if (Main.DEBUG) System.out.println("URL: " + connection.toString());
				String response = new Scanner(connection.getInputStream()).useDelimiter("\\A").next();

				// get json data
				JSONObject outerObj = new JSONObject(response);
				JSONArray features = outerObj.getJSONArray("features");
				if (Main.DEBUG) System.out.println("length: " + features.length());

				if (features.length() > 0) {

					// there is data here
					for (int i = 0; i < features.length(); i++) {
						JSONObject currentFeature = features.getJSONObject(i);
						JSONObject properties = currentFeature.getJSONObject("properties");

						if (!properties.isNull("mag")) {

							mags.add(properties.getDouble("mag"));
							timestamps.add(properties.getLong("time"));

						}
						// otherwise do nothing (bogus data point)
					}
				} else {
					// there is no data
					error = "No results found. Try changing the area, changing the dates, or increasing the radius.";
				}

			} catch (Exception e) {
				error = "Error updating data: " + e.getMessage();
				if (Main.DEBUG) e.printStackTrace();
			}

		}
	}

	/**
	 * Returns an ArrayList of magnitudes, corresponding to timestamps
	 * at each index.
	 * This is intended to be displayed on the y-axis of a LineChart.
	 * @return ArrayList of magnitudes
	 */
	public ArrayList<Double> getMags() {
		if (error.equals("none")) {
			return mags;
		} else {
			return null;
		}
	}

	/**
	 * Returns an ArrayList of timestamps, in epoch milliseconds,
	 * corresponding to magnitudes at each index.
	 * This is intended to be processed into a user-facing Date.
	 * @return ArrayList of timestamps
	 */
	public ArrayList<Long> getTimestamps() {
		if (error.equals("none")) {
			return timestamps;
		} else {
			return null;
		}
	}

	/**
	 * Returns an error encountered by the execution of this class.
	 * This error is formatted for user-facing display, and can be a result of
	 * an invalid address, an error during geocoding, absence of results, or an
	 * exception that occurred due to a bad network connection.
	 * @return if no error, "none"; if an error, string explaining the error to the user
	 */
	public String getError() {
		return error;
	}



}
