package com.knowm.kt_ram_client.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class XMLUtils {

	public static boolean saveToXML(File file, Object object) {

		boolean success = false;
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
			XMLEncoder encoder = new XMLEncoder(bos);
			encoder.writeObject(object);
			encoder.close();
			success = true;
		} catch (IOException ex) {
			ex.printStackTrace();

		}
		return success;
	}

	public static Object loadFromXML(File file) {

		Object returnObject = null;
		try (FileInputStream fis = new FileInputStream(file)) {
			XMLDecoder decoder = new XMLDecoder(fis);
			returnObject = (Object) decoder.readObject();
			decoder.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnObject;
	}
}
