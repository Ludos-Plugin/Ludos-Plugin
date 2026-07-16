package fr.ludos.core.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


public class DataController {
	private static final File PROPS_FILE = new File("manhunt.dat");
	private static final String PROPS_PATH = "prop.propeties";

	private static final Properties PROPS = new Properties();

	public static Properties getProperties() {
		return DataController.PROPS;
	}

	public static void loadProperties() throws IOException {
		FileInputStream fileStream = new FileInputStream( DataController.PROPS_FILE );
		PROPS.load( fileStream );
		fileStream.close();
	}

	public static void saveProperties() throws IOException {
		FileOutputStream fileStream = new FileOutputStream( DataController.PROPS_FILE );
		PROPS.store(fileStream , DataController.PROPS_PATH);
		fileStream.close();
	}

}