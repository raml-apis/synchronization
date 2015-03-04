package com.mulesoft.portal.client;

import java.util.ArrayList;
import org.json.simple.JSONObject;

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
			return getLastVersion().name;
		}
		return version;
	}

	protected ArrayList<APIVersion> versionList = new ArrayList<APIVersion>();

	protected String version;

	public APIModel() {
	}

	public class APIVersion extends SimpleToJSON{
		Long id;
		String name;
		String description;

		public APIModel getAPIModel() {
			return APIModel.this;
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

	public APIVersion getLastVersion() {
		for (APIVersion v:versionList){
			if (!v.name.endsWith("-staging")){
				return v;
			}
		}
		return versionList.get(versionList.size()-1);
	}
}