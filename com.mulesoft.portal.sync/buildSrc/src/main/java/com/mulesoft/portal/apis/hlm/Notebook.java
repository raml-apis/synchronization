package com.mulesoft.portal.apis.hlm;

public class Notebook {

	public Notebook(String title, String contents) {
		this.title = title;
		this.content = contents;
	}

	protected String title;

	protected String content;

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}
}
