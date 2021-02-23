package EarthquakeApp;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Manages the data of a live earthquake data feed.
 * Returns a user-displayable data set.
 *
 * @author Nolan Ierardi
 */
public class LiveFeed {

	private static String USGS_API_METHOD = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson";
	private double lat;
	private double lng;
	private int radius;
	private String locationName;
	private String error;
	private ArrayList<VBox> displayObjects;
	private boolean locationEmpty;

	/**
	 * Constructor for LiveFeed.
	 * @param location address or search string to geocode
	 * @param radius circular radius in kilometers from address
	 */
	public LiveFeed(String location, int radius) {

		GeoCoder geoCoder;
		displayObjects = new ArrayList<>();
		this.radius = radius;

		if (!location.equals("") && radius > 0) {
			geoCoder = new GeoCoder(location);
			locationEmpty = false;
		} else {
			geoCoder = new GeoCoder("Radford University");
			locationEmpty = true;
		}

		error = "none";
		if (geoCoder.getError().equals("none")) {
			// worky
			lat = geoCoder.getLat();
			lng = geoCoder.getLng();
			locationName = geoCoder.getDispName();

			if (!locationEmpty) {
				Label locationLabel = new Label(locationName);
				locationLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
				VBox locLabelBox = new VBox();
				locLabelBox.getChildren().add(locationLabel);
				displayObjects.add(locLabelBox);
			} else {
				Label locationLabel = new Label("No location and radius specified. Returning data for all locations.");
				locationLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
				VBox locLabelBox = new VBox();
				locLabelBox.getChildren().add(locationLabel);
				displayObjects.add(locLabelBox);

			}

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
				// calculate date
				Date startDate = new Date();
				startDate.setTime(Instant.now().toEpochMilli() - 86400000); // yesterday
				if (Main.DEBUG) System.out.println(startDate.toString());

				// open url
				URLConnection connection;
				if (Main.DEBUG) System.out.println("radius: " + radius + " locationEmpty: " + locationEmpty);
				if (radius > 0 && !locationEmpty) {
					connection = new URL(USGS_API_METHOD + "&starttime=" + startDate.toInstant().toString() +
							"&lat=" + lat + "&lon=" + lng + "&maxradiuskm=" + radius).openConnection();
				} else {
					connection = new URL(USGS_API_METHOD + "&starttime=" + startDate.toInstant().toString()).openConnection();
				}
				if (Main.DEBUG) System.out.println("URL: " + connection.toString());
				String response = new Scanner(connection.getInputStream()).useDelimiter("\\A").next();

				// get json data
				JSONObject outerObj = new JSONObject(response);
				JSONArray features = outerObj.getJSONArray("features");
				if (Main.DEBUG) System.out.println("Length: " + features.length());

				if (features.length() > 0) {

					// there is data here
					for (int i = 0; i < features.length(); i++) {
						JSONObject currentFeature = features.getJSONObject(i);
						JSONObject properties = currentFeature.getJSONObject("properties");
						VBox currentPane = new VBox();
						currentPane.setPadding(new Insets(2, 0, 2, 0));

						Label title = new Label(properties.getString("place"));
						title.setFont(Font.font(17));

						double magInt = 0;
						if (properties.isNull("mag")) {
							magInt = 0;
						} else {
							magInt = properties.getDouble("mag");
						}
						Label magnitude = new Label("Magntiude " + magInt + " Earthquake");
						magnitude.setFont(Font.font(13));

						Date eventTime = new Date();
						eventTime.setTime(properties.getLong("time"));
						Label time = new Label(eventTime.toString());
						time.setFont(Font.font(13));

						currentPane.getChildren().addAll(title, magnitude, time);
						displayObjects.add(currentPane);

					}
				} else {
					// there is no data
					error = "No results found. Try changing the area or increasing the radius.";
				}

			} catch (Exception e) {
				error = "Error updating data: " + e.getMessage();
				if (Main.DEBUG) e.printStackTrace();
			}

		}
	}

	/**
	 * Returns the data in an ArrayList of VBoxes. This is intended
	 * to be added to a ListView of VBoxes.
	 * @return if error = "none", ArrayList of VBoxes containing formatted data; if error contains any other string, null
	 */
	public ArrayList<VBox> getData() {
		if (error.equals("none")) {
			return displayObjects;
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
