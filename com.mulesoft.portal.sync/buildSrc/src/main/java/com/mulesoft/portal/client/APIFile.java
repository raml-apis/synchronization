package com.mulesoft.portal.client;

public class APIFile extends SimpleToJSON {

	protected Long apiVersionId;
	protected Long id;
	protected boolean isDirectory;
	protected String name;
	protected Long parentId;
	protected String path;

	public Long getApiVersionId() {
		return apiVersionId;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Long getParentId() {
		return parentId;
	}

	public String getPath() {
		return path;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setApiVersionId(Long apiVersionId) {
		this.apiVersionId = apiVersionId;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
