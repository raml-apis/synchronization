package com.mulesoft.portal.apis.library;

import com.mulesoft.portal.apis.utils.INamed;

public class LibraryNode implements INamed{

	protected Integer id;
	
	protected String title;
	
	protected String ramlUrl;
	
	protected String tags;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((ramlUrl == null) ? 0 : ramlUrl.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LibraryNode other = (LibraryNode) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (ramlUrl == null) {
			if (other.ramlUrl != null)
				return false;
		} else if (!ramlUrl.equals(other.ramlUrl))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	protected String description;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRamlUrl() {
		return ramlUrl;
	}

	public void setRamlUrl(String ramlUrl) {
		this.ramlUrl = ramlUrl;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String object) {
		this.description = object;
	}

	public String getName() {
		return title;
	}

	
}
