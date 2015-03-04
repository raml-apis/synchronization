package com.mulesoft.portal.test;

import java.io.File;

public class DirGrabber {
	
	public String listFiles(){
		
		String userDir = System.getProperty("user.dir");
		File root = new File(userDir);
		File folder = root;

		StringBuilder bld = new StringBuilder();
		for(int i = 0 ; i < 4 ; i++){
			if(folder==null){
				break;
			}
			String fileList = listFiles(folder);
			bld.append("\n").append(fileList);
			folder = folder.getParentFile();
		}
		String result = bld.toString();
		return result;
	}

	private String listFiles(File root) {
		File[] rootFiles = root.listFiles();
		
		StringBuilder bld = new StringBuilder("Files for " + root.getAbsolutePath() + ":\n");
		for(File f : rootFiles ){
			bld.append(f.getName()).append("\n");
		}
		String result = bld.toString();
		return result;
	}

}
