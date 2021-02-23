package EarthquakeApp;

import com.sun.javafx.binding.StringFormatter;
import com.sun.source.tree.BinaryTree;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import sun.tools.tree.ThisExpression;

import java.time.Instant;
import java.util.*;

/**
 * Controller class. Manages GUI controls and inputs.
 *
 * @author Nolan Ierardi
 */
public class Controller {

	@FXML
	private Tab feedTab;
	@FXML
	private ListView<VBox> feedListView;
	@FXML
	private Button feedClearButton;
	@FXML
	private CheckBox feedAutoRefreshCheckbox;
	@FXML
	private Button feedGoRefreshButton;
	@FXML
	private TextField feedAddressTextField;
	@FXML
	private TextField feedRadiusTextField;
	@FXML
	private Label feedUpdatedLabel;
	@FXML
	private Tab historyTab;
	@FXML
	private ListView<VBox> historyListView;
	@FXML
	private Button historyGoButton;
	@FXML
	private TextField historyFromTextField;
	@FXML
	private TextField historyToTextField;
	@FXML
	private TextField historyAddressTextField;
	@FXML
	private TextField historyRadiusTextField;
	@FXML
	private Tab seismographTab;
	@FXML
	private TextField seismoAddressTextField;
	@FXML
	private Button seismoGoButton;
	@FXML
	private LineChart<String, Double> seismoGraph;
	@FXML
	private TextField seismoRadiusTextField;
	@FXML
	private TextField seismoFromTextField;
	@FXML
	private TextField seismoToTextField;

	private Timeline updateTimeline;

	public Controller() {

	}

	/**
	 * Initializes GUI components and creates timeline for updates.
	 * Called after all @FXML-annotated objects are initialized.
	 */
	@FXML
	public void initialize() {
		feedUpdatedLabel.setOpacity(0);

		KeyFrame update = new KeyFrame(Duration.millis(10000), event -> {
			// auto-refresh if checkbox clicked
			if (feedAutoRefreshCheckbox.isSelected()) {
				feedGoRefreshButtonClicked();
				feedUpdatedLabel.setOpacity(1);
				KeyFrame labelAnim = new KeyFrame(Duration.millis(70), event2 -> {
					feedUpdatedLabel.setOpacity(feedUpdatedLabel.getOpacity() - 0.05);
				});
				Timeline animTimeline = new Timeline();
				animTimeline.getKeyFrames().add(labelAnim);
				animTimeline.setCycleCount(20);
				animTimeline.playFromStart();
			}

		});

		updateTimeline = new Timeline();
		updateTimeline.getKeyFrames().add(update);
		updateTimeline.setCycleCount(Timeline.INDEFINITE);
	}

	public static void doStuffWithClass() {
		String file = Controller.class.getClassLoader().getResource("file").toExternalForm();
	}

	/**
	 * Plays or stops the update timeline based on the state of the
	 * Auto-refresh checkbox in the Feed tab.
	 * Called when Auto-refresh checkbox state changes.
	 */
	@FXML
	public void feedAutoRefreshCheckboxChanged() {

		if (feedAutoRefreshCheckbox.isSelected()) {
			updateTimeline.playFromStart();
		} else {
			updateTimeline.stop();
		}

	}

	/**
	 * Clears the feed of all data.
	 * Called when the Clear Feed button is clicked.
	 */
	@FXML
	public void feedClearButtonClicked() {

		feedListView.getItems().clear();

	}

	/**
	 * Creates a LiveFeed and displays it to the screen, or if an
	 * error is encountered, displays the error to the user via
	 * an Alert.
	 * Called when the Go/Refresh button on the Feed tab is clicked.
	 */
	@FXML
	public void feedGoRefreshButtonClicked() {

		// create the live feed
		try {

			int radius = 0;
			if (feedRadiusTextField.getText().equals("")) {
				radius = 0;
			} else {
				radius = Integer.parseInt(feedRadiusTextField.getText());
			}

			LiveFeed feed = new LiveFeed(feedAddressTextField.getText(), radius);
			feed.updateData();
			if (feed.getError().equals("none")) {
				feedListView.getItems().clear();
				for (VBox box : feed.getData()) {   // <------------ THE EXCEPTION IS OCCURRING HERE
					feedListView.getItems().add(box);
				}
			} else {
				new Alert(Alert.AlertType.ERROR, feed.getError()).show();
			}

		} catch (Exception e) {
			new Alert(Alert.AlertType.ERROR, "Formatting error: " + e.getMessage()).show();
			if (Main.DEBUG) e.printStackTrace();
		}

	}

	/**
	 * Creates a HistoricalFeed and displays it to the screen, or if an
	 * error is encountered, displays the error to the user via
	 * an Alert.
	 * Called when the Go button on the History tab is clicked.
	 */
	@FXML
	public void historyGoButtonClicked() {

		// create the historical feed
		try {

			int radius = 0;
			if (historyRadiusTextField.getText().equals("")) {
				radius = 0;
			} else {
				radius = Integer.parseInt(historyRadiusTextField.getText());
			}

			Date startDate = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli() - 86400000)); // yesterday by default
			Date endDate = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli()));
			try {

				Scanner startDateScanner = new Scanner(historyFromTextField.getText()).useDelimiter("/");
				int sm = startDateScanner.nextInt();
				int sd = startDateScanner.nextInt();
				int sy = startDateScanner.nextInt();
				if (sy < 1000) throw new NumberFormatException();
				Calendar startCal = Calendar.getInstance();
				startCal.set(sy, sm - 1, sd, 0, 0); // sm - 1 because months in Calendar are zero-indexed for whatever reason
				startDate.setTime(startCal.getTimeInMillis());

				Scanner endDateScanner = new Scanner(historyToTextField.getText()).useDelimiter("/");
				int em = endDateScanner.nextInt();
				int ed = endDateScanner.nextInt();
				int ey = endDateScanner.nextInt();
				if (ey < 1000) throw new NumberFormatException();
				Calendar endCal = Calendar.getInstance();
				endCal.set(ey, em - 1, ed, 0, 0); // sm - 1 because months in Calendar are zero-indexed for whatever reason
				endDate.setTime(endCal.getTimeInMillis());

			} catch (Exception e) {
				new Alert(Alert.AlertType.ERROR, "Formatting error: Dates must be in M/D/YYYY format. " +
						"Using the date range from yesterday to now.").show();
			}

			HistoricalFeed feed = new HistoricalFeed(historyAddressTextField.getText(), radius, startDate, endDate);
			feed.updateData();
			if (feed.getError().equals("none")) {
				historyListView.getItems().clear();
				for (VBox box : feed.getData()) {
					historyListView.getItems().add(box);
				}
			} else {
				new Alert(Alert.AlertType.ERROR, feed.getError()).show();
			}

		} catch (Exception e) {
			new Alert(Alert.AlertType.ERROR, "Formatting error: " + e.getMessage()).show();
			if (Main.DEBUG) e.printStackTrace();
		}

	}

	/**
	 * Creates a GraphFeed and displays the information to the graph,
	 * or if an error is encountered, displays the error to the user
	 * via an Alert.
	 * Called when the Go button on the Graph tab is clicked.
	 */
	@FXML
	public void seismoGoButtonClicked() {

		try {

			int radius = 0;
			if (seismoRadiusTextField.getText().equals("")) {
				radius = 1;
			} else {
				radius = Integer.parseInt(seismoRadiusTextField.getText());
			}

			Date startDate = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli() - 86400000));  // yesterday by default
			Date endDate = Date.from(Instant.ofEpochMilli(Instant.now().toEpochMilli()));
			try {

				Scanner startDateScanner = new Scanner(seismoFromTextField.getText()).useDelimiter("/");
				int sm = startDateScanner.nextInt();
				int sd = startDateScanner.nextInt();
				int sy = startDateScanner.nextInt();
				if (sy < 1000) throw new NumberFormatException();
				Calendar startCal = Calendar.getInstance();
				startCal.set(sy, sm - 1, sd, 0, 0); // sm - 1 because months in Calendar are zero-indexed for whatever reason
				startDate.setTime(startCal.getTimeInMillis());

				Scanner endDateScanner = new Scanner(seismoToTextField.getText()).useDelimiter("/");
				int em = endDateScanner.nextInt();
				int ed = endDateScanner.nextInt();
				int ey = endDateScanner.nextInt();
				if (ey < 1000) throw new NumberFormatException();
				Calendar endCal = Calendar.getInstance();
				endCal.set(ey, em - 1, ed, 0, 0); // sm - 1 because months in Calendar are zero-indexed for whatever reason
				endDate.setTime(endCal.getTimeInMillis());

			} catch (Exception e) {
				new Alert(Alert.AlertType.ERROR, "Formatting error: Dates must be in M/D/YYYY format. " +
						"Using the date range from yesterday to now.").show();
			}

			GraphFeed feed = new GraphFeed(seismoAddressTextField.getText(), radius, startDate, endDate);
			feed.updateData();
			if (feed.getError().equals("none")) {

				ArrayList<Double> mags = feed.getMags();
				ArrayList<Long> timestamps = feed.getTimestamps();
				XYChart.Series<String, Double> chartSeries = new XYChart.Series<>();

				for (int i = 0 ; i < mags.size() ; i++) {
					XYChart.Data<String, Double> point = new XYChart.Data<>();

					Date tsDate = new Date();
					tsDate.setTime(timestamps.get(i));
					Calendar tsCal = Calendar.getInstance();
					tsCal.setTimeInMillis(tsDate.toInstant().toEpochMilli());
					String dateStr = "" + tsCal.get(Calendar.MONTH) + "/" + tsCal.get(Calendar.DATE) + "/" + tsCal.get(Calendar.YEAR);

					point.setXValue(dateStr);
					point.setYValue(mags.get(i));
					chartSeries.getData().add(point);
				}

				seismoGraph.getXAxis().setAutoRanging(true);
				seismoGraph.getXAxis().setTickLabelsVisible(true);
				seismoGraph.getYAxis().setAutoRanging(true);
				seismoGraph.getData().clear();
				seismoGraph.getData().add(chartSeries);

			} else {
				new Alert(Alert.AlertType.ERROR, feed.getError()).show();
			}

		} catch (Exception e) {
			new Alert(Alert.AlertType.ERROR, "Formatting error: " + e.getMessage()).show();
			if (Main.DEBUG) e.printStackTrace();
		}

	}

}
