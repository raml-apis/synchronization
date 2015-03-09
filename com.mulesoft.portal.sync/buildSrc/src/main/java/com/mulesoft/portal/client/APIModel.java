package com.mulesoft.portal.client;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import com.mulesoft.portal.client.APIModel.PortalAPIVersion;

public class APIModel extends SimpleToJSON {

	Long id;

	protected String name;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	protected String description;

	public String getName() {
		return name;
	}

	public String getVersion() {
		if (version==null){
			return getLastVersion().getName();
		}
		return version;
	}
	
	public PortalAPIVersion getVersion(String name) {
		for(PortalAPIVersion ver : versionList){
			if(ver.getName().equals(name)){
				return ver;
			}
		}
		return null;
	}

	protected ArrayList<PortalAPIVersion> versionList = new ArrayList<PortalAPIVersion>();

	protected String version;

	public APIModel() {
	}

	public class PortalAPIVersion extends SimpleToJSON{
		Long id;
		private String name;
		String description;

		public APIModel getAPIModel() {
			return APIModel.this;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}


	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public APIModel(String title, String version,String description) {
		super();
		this.name = title;
		this.version = version;
		this.description=description;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		JSONObject version = new JSONObject();
		version.put("name", this.version);
		json.put("version", version);
		return json;
	}

	public PortalAPIVersion getLastVersion() {
		for (PortalAPIVersion v:versionList){
			if (!v.getName().endsWith("-staging")){
				return v;
			}
		}
		return versionList.get(versionList.size()-1);
	}

	public void addVersion(PortalAPIVersion e) {
		this.versionList.add(e);		
	}
}