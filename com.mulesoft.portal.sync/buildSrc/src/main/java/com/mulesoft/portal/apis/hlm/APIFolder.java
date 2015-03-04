package com.mulesoft.portal.apis.hlm;

import java.io.File;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class APIFolder {

	public APIFolder(JSONObject parse,File fl, ArrayList<File> rootRamls) {
		JSONArray apiList=(JSONArray) parse.get("apis");
		ArrayList<API>apList=new ArrayList<API>();
		for (int a=0;a<apiList.size();a++){
			JSONObject apiObj=(JSONObject) apiList.get(a);
			API api=new API(fl,apiObj);
			apList.add(api);
		}
		this.apis=apList.toArray(new API[apList.size()]);
	}

	public APIFolder(File f, ArrayList<File> rootRamls) {
		ArrayList<API>apiList=new ArrayList<API>();
		for (File rr:rootRamls){
			apiList.add(new API(rr));
		}
		this.apis=apiList.toArray(new API[apiList.size()]);
	}

	protected API[] apis;
	
	public API[] getAPIs(){
		if (apis==null){
			return new API[0];
		}
		return apis.clone();
	}
}
