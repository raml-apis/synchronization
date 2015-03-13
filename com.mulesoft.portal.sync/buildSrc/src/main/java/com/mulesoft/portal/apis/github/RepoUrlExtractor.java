package com.mulesoft.portal.apis.github;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.mulesoft.portal.apis.utils.Utils;

public class RepoUrlExtractor {
	
	public RepoUrlExtractor(List<String> branches) {
		super();
		this.branches = branches;
	}


	private List<String> branches;
	
	public GitApiLocation createLocation(String fullBranch, File repoDir){
		
		File gitMetaFolder = new File(repoDir, ".git");
		if(!gitMetaFolder.exists() || !gitMetaFolder.isDirectory()){
			throw new RuntimeException( repoDir.getAbsolutePath() + " is not a git repository");
		}
		
		File configFile = new File(gitMetaFolder, "config");
		if(!configFile.exists() || configFile.isDirectory()){
			throw new RuntimeException( "Unable to find repository config for " + repoDir.getAbsolutePath());
		}
		
		GitHubConfig cfg = new GitHubConfig(configFile);
		GitApiLocation loc = null;
		
		if(fullBranch!= null){
			int ind = fullBranch.indexOf('/');
			String origin = fullBranch.substring(0, ind);
			String branch = fullBranch.substring(ind+1);
			String repoFullPath = cfg.getRepoFullPath(origin);
			String apiName = extractName(repoFullPath);
			if(repoFullPath!=null){
				loc = new GitApiLocation(apiName, repoFullPath, Arrays.asList(branch));
			}
		}
		if(loc==null){
			if(cfg.size()>1){
				throw new RuntimeException( "Unable to find repository for" + repoDir.getAbsolutePath());
			}
			else if(cfg.size()>1){
				throw new RuntimeException( "Unable to determine exact repository for" + repoDir.getAbsolutePath());
			}
			String repoFullPath = cfg.getRepoFullPath();
			String apiName = extractName(repoFullPath);
			loc = new GitApiLocation(apiName, repoFullPath, this.branches);
		}
		
		return loc;
	}
	

	private String extractName(String repoFullPath) {
		
		int ind0 = repoFullPath.lastIndexOf('/');
		ind0++;
		int ind1 = repoFullPath.lastIndexOf(".git");
		String name = repoFullPath.substring(ind0, ind1);
		return name;
	}

}
