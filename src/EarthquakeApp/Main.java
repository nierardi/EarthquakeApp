package EarthquakeApp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main/Driver class. Executes the application and
 * instantiates the controller.
 *
 * @author Nolan Ierardi
 */
public class Main extends Application {

	// Set to false to turn off all console output.
    public static final boolean DEBUG = true;

	/**
	 * Called on application start. Initializes the FXML document
	 * and attached stylesheet.
	 * @param primaryStage the Stage to use
	 */
	@Override
    public void start(Stage primaryStage) {

		try {
			Controller ctrl = new Controller();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
			loader.setController(ctrl);


			Parent base = loader.load();        // This is when ctrl.initialize() runs.
			primaryStage.setTitle("Earthquake Data");
			Scene mainScene = new Scene(base);
			mainScene.getStylesheets().add(getClass().getResource("MainCSS.css").toExternalForm());
			primaryStage.setScene(mainScene);
			primaryStage.show();
		} catch (Exception e) {
			System.err.println("Exception loading FXML document: " + e.getMessage());
		}

    }

	/**
	 * Main method. Launches the application.
	 * @param args the command-line arguments
	 */
    public static void main(String[] args) {
        launch(args);
    }
}
