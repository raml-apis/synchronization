package com.mulesoft.portal.apis.github;

import java.util.List;


public class GitApiLocation {
	
	private static final String DOT_GIT_STRING = ".git";

	private static final String GITHUB_COM_SLASH_STRING = "github.com/";

	public GitApiLocation(String apiName, String repoFullPath, List<String> branches) {
		super();
		this.apiName = apiName.trim();
		this.repoFullPath = repoFullPath.trim();
		this.branches = branches;
		extractOwnerAndRepoNames();
	}

	private String apiName;
	
	private String repoFullPath;
	
	private String ownerId;
	
	private String repoId;
	
	private String repoPath;
	
	private List<String> branches;
	

	private void extractOwnerAndRepoNames() {
		
		int ind0 = this.repoFullPath.indexOf(GITHUB_COM_SLASH_STRING);
		if(ind0 < 0){
			throw new RuntimeException(constructErrorMessage());
		}
		ind0 += GITHUB_COM_SLASH_STRING.length();
		
		if(!this.repoFullPath.endsWith(DOT_GIT_STRING)){
			throw new RuntimeException(constructErrorMessage());
		}
		int ind1 = this.repoFullPath.length() - DOT_GIT_STRING.length();
		
		repoPath = this.repoFullPath.substring(ind0, ind1);
		int ind = repoPath.indexOf('/');
		this.repoId = repoPath.substring(ind+1);
		this.ownerId = repoPath.substring(0, ind);
		
	}

	public String getApiName() {
		return apiName;
	}

	public String getRepoFullPath() {
		return repoFullPath;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public String getRepoId() {
		return repoId;
	}
	
	public String getRepoPath() {
		return repoPath;
	}
	
	private String constructErrorMessage() {
		return "Repository path '" + this.repoFullPath +"' does not belong to GitHub.\nExpecting path as 'https://github.com/{ownerId}/{repoId}.git'";
	}

	public List<String> getBranches() {
		return branches;
	}

}
