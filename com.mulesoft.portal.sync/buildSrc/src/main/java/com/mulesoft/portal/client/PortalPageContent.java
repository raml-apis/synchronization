package com.mulesoft.portal.client;

import org.json.simple.JSONObject;

public class PortalPageContent {
	
	public PortalPageContent(String name, String portalUrl) {
		super();
		this.name = name;
		this.portalUrl = portalUrl;
	}

	public PortalPageContent(String portalPagesUrl, String displayedPagesUrl, JSONObject content) {
		super();		
		this.content = content;
		this.id = (Long) content.get("id");
		this.name = (String) content.get("name");
		this.type = (String) content.get("type");
		
		if(!portalPagesUrl.endsWith("/")){
			portalPagesUrl += "/";
		}
		if(!displayedPagesUrl.endsWith("/")){
			displayedPagesUrl += "/";
		}
		this.portalUrl = displayedPagesUrl + id;
		this.url = portalPagesUrl + id;
	}

	private long id;
	
	private String name;
	
	private JSONObject content;
	
	private String portalUrl;
	
	private String url;
	
	private String type;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public JSONObject getContent() {
		return content;
	}

	public String getPortalUrl() {
		return portalUrl;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

}
