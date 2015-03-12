package com.mulesoft.portal.apis.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mulesoft.portal.apis.github.GitApiLocation;
import com.mulesoft.portal.apis.github.GitHubBranch;
import com.mulesoft.portal.apis.github.GitHubConnector;
import com.mulesoft.portal.apis.github.GitHubCredentials;
import com.mulesoft.portal.apis.github.GitHubRepository;

public class CodeRetriever {
	
	public CodeRetriever(File targetDir, List<GitApiLocation> apiLocations, List<String> branches, GitHubCredentials credentials)
	{
		super();
		this.targetDir = targetDir;
		this.apiLocations = apiLocations;
		this.branches = branches;
		this.connector = new GitHubConnector(credentials);
	}

	private File targetDir;
	
	private List<GitApiLocation> apiLocations;
	
	private List<String> branches;
	
	private GitHubConnector connector;
	
	public List<File> cloneRepos(){
		
		createTargetDirectory();
		
		ArrayList<File> list = new ArrayList<File>();  
		for(GitApiLocation gal: apiLocations){
			File apiDir = cloneRepo(gal);
			list.add(apiDir);
		}
		return list;
	}

	private File cloneRepo(GitApiLocation apiLocation) {
		
		File apiDir = new File(targetDir, apiLocation.getApiName());
		
		GitHubRepository repo = connector.getRepository(apiLocation);
		
		Map<String, GitHubBranch> gitHubBranches = repo.getBranches();
		
		for(String br : branches){
			if(!gitHubBranches.containsKey(br)){
				continue;
			}
			cloneBranch(br,apiDir,apiLocation);
		}
		return apiDir;
	}

	private void cloneBranch(String branch, File apiDir, GitApiLocation apiLocation) {
		
		File branchDir = new File(apiDir,branch);
		branchDir.mkdirs();
		
		connector.cloneRepository(apiLocation.getRepoFullPath(), branch, branchDir);
	}

	private void createTargetDirectory() {
		
//		if(targetDir.exists()){
//			if(targetDir.isDirectory()){
//				throw new RuntimeException("File " + targetDir.getAbsolutePath() + "already exists");
//			}
//			else{
//				File[] listFiles = targetDir.listFiles();
//				if(listFiles!=null&&listFiles.length!=0){
//					throw new RuntimeException("Directory " + targetDir.getAbsolutePath() + "already exists and is not empty");
//				}
//			}
//		}
		
		targetDir.mkdirs();
	}

	public GitHubConnector getConnector() {
		return connector;
	}

}
