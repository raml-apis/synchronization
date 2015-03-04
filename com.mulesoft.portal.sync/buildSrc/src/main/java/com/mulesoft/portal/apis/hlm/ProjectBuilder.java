package com.mulesoft.portal.apis.hlm;

import java.io.File;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.mulesoft.portal.apis.utils.Utils;

public class ProjectBuilder {

	public APIProject build(File rootFolder) {
		File[] listFiles = rootFolder.listFiles();
		ArrayList<APIFolder> folders = new ArrayList<APIFolder>();
		for (File f : listFiles) {
			if (!f.getName().endsWith("tools") && f.isDirectory()) {
				APIFolder folder = tryCreateAPI(f);
				folders.add(folder);
			}
		}
		return new APIProject(folders.toArray(new APIFolder[folders.size()]));
	}
	
	public APIProject buildForOneApiFolder(File rootFolder) {
		APIFolder folder = tryCreateAPI(rootFolder);		
		return new APIProject(new APIFolder[]{folder});
	}

	private APIFolder tryCreateAPI(File f) {
		File[] listFiles = f.listFiles();
		ArrayList<File> rootRamls = new ArrayList<File>();
		File metaFile = null;
		for (File fl : listFiles) {
			if (fl.getName().endsWith(".raml")) {
				rootRamls.add(fl);
			}
			if (fl.getName().equals("apiPack.json")) {
				metaFile = fl;
			}
		}
		if (rootRamls.size() == 0) {
			throw new IllegalStateException(f.getAbsolutePath()
					+ " do not have any apis");
		}
		/*if (rootRamls.size() > 1 && metaFile == null) {
			throw new IllegalStateException(
					f.getAbsolutePath()
							+ " have more then one api and no predefined mapping is defined");
		}*/
		if (metaFile != null) {
			try {
				JSONObject parse = (JSONObject) JSONValue.parse(Utils
						.getContents(metaFile));
				APIFolder fl = new APIFolder(parse, f,rootRamls);
				return fl;
			} catch (Exception e) {
				throw new IllegalStateException("Invalid meta file at "
						+ metaFile.getAbsolutePath());
			}
		}
		APIFolder fld = new APIFolder(f,rootRamls);
		return fld;
	}
}
