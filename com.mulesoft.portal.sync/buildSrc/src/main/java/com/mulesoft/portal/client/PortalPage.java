package com.mulesoft.portal.client;

import org.json.simple.JSONObject;

public class PortalPage {

	String name;
	String content;
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON(){
		JSONObject result=new JSONObject();
		result.put("type", "markdown");
		result.put("name", name);
		return  result;
	}
}
