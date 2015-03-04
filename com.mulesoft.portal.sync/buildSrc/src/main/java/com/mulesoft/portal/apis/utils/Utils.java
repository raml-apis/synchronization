package com.mulesoft.portal.apis.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

public class Utils {

	public static String getContents(File description) {
		try {
			return new String(Files.readAllBytes(description.toPath()),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);			
		}
	}
}
