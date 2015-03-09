package com.mulesoft.portal.apis.hlm;

import java.util.ArrayList;

public class API {

	protected String name;

	public API(String name) {
		this.name = name;
	}
	
	private ArrayList<APIVersion> versions = new ArrayList<APIVersion>();
	
	public void addVersion(APIVersion version){
		this.versions.add(version);
	}

	public String getName() {
		return name;
	}

	public ArrayList<APIVersion> getVersions() {
		return versions;
	}

	public boolean publishToLibrary() {
		
		for(APIVersion ver : versions){
			if(ver.getBranch().equals("production")&&ver.isPublishToLibrary()){
				return true;
			}
		}
		return false;
	}
}