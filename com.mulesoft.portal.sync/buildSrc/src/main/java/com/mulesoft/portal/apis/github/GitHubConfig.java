package com.mulesoft.portal.apis.github;

import java.io.File;
import java.util.LinkedHashMap;

import com.mulesoft.portal.apis.utils.Utils;

public class GitHubConfig {
	
	public GitHubConfig(File file){
		init(file);
	}

	
	private LinkedHashMap<String,String> originsMap = new LinkedHashMap<String, String>();
	
	
	public String getRepoFullPath(String origin){
	
		return originsMap.get(origin);		
	}
	
	
	private void init(File configFile) {
	
		String content = Utils.getContents(configFile);
		int ind = content.indexOf("[remote");
		if(ind<0){
			return;
		}
		ind += "[remote".length();
		
		String[] arr = content.substring(ind).split("\\[remote");
		for(String str : arr){
			int i0 = str.indexOf('\"')+1;
			int i1 = str.indexOf('\"',i0);
			
			String key = str.substring(i0,i1);
			
			int i2 = str.indexOf("url =");
			if(i2<0){
				i2 = str.indexOf("url=");
				if(i2<0){
					throw new RuntimeException( "Unable to find repository path inside config for " + key);
				}
				else{
					i2 += "url=".length();
				}
			}
			else{
				i2 += "url =".length();
			}
			
			int i3 = str.indexOf("\n",i2);
			if(i3<0){
				i3 = str.length();
			}
			String url = str.substring(i2, i3).trim();
			if(!url.endsWith(".git")){
				if(url.endsWith("/")){
					url = url.substring(0, url.length()-1);
				}
				url += ".git";
			}
			originsMap.put(key, url);
		}
	}


	public int size() {
		return originsMap.size();
	}


	public String getRepoFullPath() {
		return originsMap.values().iterator().next();		
	}	

}
