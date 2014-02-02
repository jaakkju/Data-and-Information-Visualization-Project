package big.marketing;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import big.marketing.controller.DataController;
import big.marketing.controller.MongoExecutor;
import big.marketing.view.ControlsJPanel;
import big.marketing.view.GraphJPanel;
import big.marketing.view.PCoordinatesJPanel;
import big.marketing.view.WindowFrame;

public class Application {
	static Logger logger = Logger.getLogger(Application.class);
	private static String logConfigFile = "config/log4j.properties";
	private static String defaultLogConfigFile = logConfigFile + ".template";

	/**
	 * Application class implements main method and initializes the main parts of
	 * the application
	 * @param args
	 */
	public static void main(String[] args) {

		PropertyConfigurator.configure(getLogSettings());

		logger.info("Starting eyeNet application: initializing controller and views");

		Settings.loadConfig();

		DataController controller = new DataController();

		// All panels have a reference to controller so changes in selections and data can be passed to other views
		GraphJPanel graphPanel = new GraphJPanel(controller);
		PCoordinatesJPanel pCoordinatesPanel = new PCoordinatesJPanel(controller);

		ControlsJPanel controlsPanel = new ControlsJPanel(controller);

		// DataController implements observer pattern and pushes changes in data and selections to JPanels
		controller.getGephiController().addObserver(graphPanel);
		controller.addObserver(pCoordinatesPanel);
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

	private static Properties getLogSettings() {
		Properties defaultProps = new Properties();
		try {
			defaultProps.load(new FileReader(defaultLogConfigFile));
			if (new File(logConfigFile).exists()) {
				Properties userProps = new Properties(defaultProps);
				userProps.load(new FileReader(logConfigFile));
				System.out.println("Read user log settings");
				return userProps;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return defaultProps;
	}
}
