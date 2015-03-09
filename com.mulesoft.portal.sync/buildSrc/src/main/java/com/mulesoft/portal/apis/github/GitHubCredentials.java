package com.mulesoft.portal.apis.github;

public class GitHubCredentials {
	
	public GitHubCredentials(String login, String password) {
		super();
		this.login = login;
		this.password = password;
	}

	private String login;
	
	private String password;

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

}
