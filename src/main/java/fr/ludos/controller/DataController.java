package fr.ludos.controller;


import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

import java.util.Properties;


public class DataController {

	private static final File propsFile = new File("manhunt.dat");
	private static final String propertiesPath = "prop.propeties";

	private static final Properties props = new Properties();

	public static Properties getProperties() {
		return DataController.props;
	}

	public static void loadProperties() throws IOException {
		FileInputStream fileStream = new FileInputStream( DataController.propsFile );
		props.load( fileStream );
		fileStream.close();
	}

	public static void saveProperties() throws IOException {
		FileOutputStream fileStream = new FileOutputStream( DataController.propsFile );
		props.store(fileStream , DataController.propertiesPath);
		fileStream.close();
	}

}