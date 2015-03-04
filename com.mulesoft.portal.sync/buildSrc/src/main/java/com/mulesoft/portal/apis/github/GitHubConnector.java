package com.mulesoft.portal.apis.github;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

public class GitHubConnector {

	private GHRepository repository;

	public GitHubConnector(String repo,String userName,String password) throws IOException{
		GitHub connectUsingPassword = GitHub.connectUsingPassword(
				userName, password);
		repository = connectUsingPassword
				.getRepository(repo);
	}
	
	public String getCurrentSHA() throws IOException{
		Map<String, GHBranch> branches = repository.getBranches();
		GHBranch ghBranch = branches.get("master");
		return ghBranch.getSHA1();				
	}
	protected HashMap<String,HashSet<String>>files=new HashMap<String,HashSet<String>>(); 
	
	public HashSet<String>calculateChangedPaths(String lastSyncedSHA) throws IOException{
		PagedIterable<GHCommit> listCommits = repository.listCommits();
		ArrayList<GHCommit> changed = new ArrayList<GHCommit>();
		String requiredSHA1 = lastSyncedSHA;
		for (GHCommit c : listCommits) {			
			if (c.getSHA1() .equals(requiredSHA1)) {
				break;
			}
			changed.add(c);
		}
		HashSet<String> changedPaths = new HashSet<String>();
		for (GHCommit c : changed) {
			String sha1 = c.getSHA1();
			HashSet<String> lcp = getPaths(sha1);
			changedPaths.addAll(lcp);
		}
		return changedPaths;
	}

	private HashSet<String> getPaths(String sha1) throws IOException {
		if (files.containsKey(sha1)){
			return files.get(sha1);
		}
		HashSet<String> lcp = new HashSet<String>();
		
		GHCommit commit = repository.getCommit(sha1);
		List<File> files = commit.getFiles();
		for (File q : files) {
			URL rawUrl = q.getRawUrl();
			String path = rawUrl.getPath();
			path = path.substring(path.indexOf("/raw/") + 5);
			path = path.substring(path.indexOf('/'));
			lcp.add(path);
		}
		this.files.put(sha1, lcp);
		return lcp;
	}
	
	
}