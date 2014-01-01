package big.marketing;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Settings {
	static Logger logger = Logger.getLogger(Settings.class);

	private static Properties properties=new Properties();
	private static final String configFile="eyeNet.properties";

	public static String get(String key) {
		return properties.getProperty(key);
	}
	public static int getInt(String key){
		return Integer.parseInt(get(key));
	}
	
	public static void loadConfig(){
		try {
			properties.load(new FileReader(configFile));
			logger.info("Successfully read config from file "+configFile);
		} catch (IOException e) {
			logger.error("Could not load Settings from "+configFile+"! Exiting...");
			System.exit(1);
		}
	}
	
	

}
