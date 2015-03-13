package com.mulesoft.portal.apis.github;

import java.io.File;

import com.mulesoft.portal.apis.utils.Utils;

public class RepoUrlExtractor {
	
	public static String extractRepoUrl(File repoDir) {
		
		File gitMetaFolder = new File(repoDir, ".git");
		if(!gitMetaFolder.exists() || !gitMetaFolder.isDirectory()){
			throw new RuntimeException( repoDir.getAbsolutePath() + " is not a git repository");
		}
		
		File configFile = new File(gitMetaFolder, "config");
		if(!configFile.exists() || configFile.isDirectory()){
			throw new RuntimeException( "Unable to find repository config for " + repoDir.getAbsolutePath());
		}
		
		String config = Utils.getContents(configFile);
		int ind0 = config.indexOf("[remote");
		int ind1 = config.indexOf("[",ind0+1);
		if(ind1<0){
			ind1 = config.length();
		}
		
		String remote = config.substring(ind0, ind1);
		int ind2 = remote.indexOf("url =");
		if(ind2<0){
			ind2 = remote.indexOf("url=");
			if(ind2<0){
				throw new RuntimeException( "Unable to find repository path inside config for " + repoDir.getAbsolutePath());
			}
			else{
				ind2 += "url=".length();
			}
		}
		else{
			ind2 += "url =".length();
		}
		
		int ind3 = remote.indexOf("\n",ind2);
		if(ind3<0){
			ind3 = remote.length();
		}
		String url = remote.substring(ind2, ind3).trim();
		if(!url.endsWith(".git")){
			if(url.endsWith("/")){
				url = url.substring(0, url.length()-1);
			}
			url += ".git";
		}
		return url;
	}

}
