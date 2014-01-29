package big.marketing;

import org.apache.log4j.Logger;

import big.marketing.controller.DataController;
import big.marketing.controller.MongoExecutor;
import big.marketing.view.ControlsJPanel;
import big.marketing.view.GraphJPanel;
import big.marketing.view.PCoordinatesJPanel;
import big.marketing.view.WindowFrame;

public class Application {
	static Logger logger = Logger.getLogger(Application.class);

	/**
	 * Application class implements main method and initializes the main parts of
	 * the application
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("Starting eyeNet application: initializing controller and views");

		Settings.loadConfig();

		DataController controller = DataController.getInstance();

		// All panels have a reference to controller so changes in selections and data can be passed to other views
		GraphJPanel graphPanel = new GraphJPanel(controller);
		PCoordinatesJPanel pCoordinatesPanel = new PCoordinatesJPanel(controller);

		ControlsJPanel controlsPanel = new ControlsJPanel(controller);

		// DataController implements observer pattern and pushes changes in data and selections to JPanels
		controller.getGephiController().addObserver(graphPanel);
		controller.addObserver(pCoordinatesPanel);
		controller.addObserver(graphPanel);
		controller.addObserver(controlsPanel);

		@SuppressWarnings("unused")
		WindowFrame frame = new WindowFrame(controller, graphPanel, pCoordinatesPanel, controlsPanel);
	}

	public static void quit() {
		logger.info("Received quit, closing the application");

		MongoExecutor.killMongoProcess();
		logger.info("Goodbye!");
		System.exit(0);
	}
}
