package com.mulesoft.portal.apis.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

public class Utils {

	public static String getContents(File description) {
		
		try {
			FileInputStream fis = new FileInputStream(description);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int l = 0 ;
			byte[] buf = new byte[1024];
			while((l=bis.read(buf))>=0){
				baos.write(buf,0,l);
			}
			byte[] bytes = baos.toByteArray();
			String result = new String(bytes,"UTF-8");
			return result;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
