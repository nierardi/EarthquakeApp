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
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Manages the data of a historical earthquake data feed
 * between certain dates.
 * Returns a user-displayable data set.
 *
 * @author Nolan Ierardi
 */
public class HistoricalFeed {

	private static String USGS_API_METHOD = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson";
	private double lat;
	private double lng;
	private int radius;
	private String locationName;
	private String error;
	private ArrayList<VBox> displayObjects;
	private boolean locationEmpty;
	private Date start;
	private Date end;

	/**
	 * Constructor for LiveFeed.
	 * @param location address or search string to geocode
	 * @param radius circular radius in kilometers
	 * @param start the Date to get results starting from
	 * @param end the Date to get results ending before
	 */
	public HistoricalFeed(String location, int radius, Date start, Date end) {
		GeoCoder geoCoder;
		displayObjects = new ArrayList<>();
		this.radius = radius;
		this.start = start;
		this.end = end;
		if (Main.DEBUG) System.out.println("start date: " + start + " end date: " + end);

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
				Label dateLabel = new Label("From " + start.toString() + " to " + end.toString());
				dateLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
				VBox locLabelBox = new VBox();
				locLabelBox.getChildren().addAll(locationLabel, dateLabel);
				displayObjects.add(locLabelBox);
			} else {
				Label locationLabel = new Label("No location and radius specified. Returning data for all locations.");
				locationLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
				Label dateLabel = new Label("From " + start.toString() + " to " + end.toString());
				dateLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
				VBox locLabelBox = new VBox();
				locLabelBox.getChildren().addAll(locationLabel, dateLabel);
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

				// open url
				URLConnection connection;
				if (Main.DEBUG) System.out.println("radius: " + radius + " locationEmpty: " + locationEmpty);
				if (radius > 0 && !locationEmpty) {
					connection = new URL(USGS_API_METHOD + "&starttime=" + start.toInstant().toString() +
							"&endtime=" + end.toInstant().toString() + "&lat=" + lat + "&lon=" + lng +
							"&maxradiuskm=" + radius).openConnection();
				} else {
					connection = new URL(USGS_API_METHOD + "&starttime=" + start.toInstant().toString() +
							"&endtime=" + end.toInstant().toString()).openConnection();
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
					error = "No results found. Try changing the area, changing the dates, or increasing the radius.";
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
