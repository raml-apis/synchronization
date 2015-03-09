package com.mulesoft.portal.apis.hlm;

import java.io.File;
import java.util.ArrayList;

public class APIFolder {

	public APIFolder(File f, ArrayList<API> apiList) {
		this.rootDirectory = f;
		this.apis = apiList.toArray(new API[apiList.size()]);
	}
	
	private File rootDirectory;

	protected API[] apis;
	
	public API[] getAPIs(){
		if (apis==null){
			return new API[0];
		}
		return apis.clone();
	}
}
