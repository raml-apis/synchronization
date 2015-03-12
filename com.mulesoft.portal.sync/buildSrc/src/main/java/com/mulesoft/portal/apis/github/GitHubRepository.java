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
import org.kohsuke.github.GHCommit.ShortInfo;
import org.kohsuke.github.GHCommitComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.GHCommit.File;

public class GitHubRepository {
	
	public GitHubRepository(GHRepository repository, String repoFullPath, String repoPath) {
		super();
		this.repository = repository;
		this.repoFullPath = repoFullPath;
		this.repoPath = repoPath;
	}

	private GHRepository repository;
	
	private String repoFullPath;
	
	private String repoPath;
	
	protected HashMap<String,HashSet<String>>files=new HashMap<String,HashSet<String>>();
	
	public HashSet<String>calculateChangedPaths(String lastSyncedSHA) throws IOException{
		PagedIterable<GHCommit> listCommits = repository.listCommits();
		ArrayList<GHCommit> changed = new ArrayList<GHCommit>();
		String requiredSHA1 = lastSyncedSHA;
		for (GHCommit c : listCommits) {			
			if (c.getSHA1().equals(requiredSHA1)) {
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
	

	public String getRepoFullPath() {
		return repoFullPath;
	}

	public String getRepoPath() {
		return repoPath;
	}

	public Map<String,GitHubBranch> getBranches() {
		
		try {
			HashMap<String,GitHubBranch> map = new HashMap<String, GitHubBranch>();
			Map<String, GHBranch> branches = repository.getBranches();			
			for(GHBranch br : branches.values() ){
				String name = br.getName();
				GitHubBranch branch = new GitHubBranch(br);
				map.put(name, branch);
			}
			return map;
		} catch (IOException e) {
			e.printStackTrace();			
		}
		return null;
	}
	
	public GitHubBranch getBranch(String name){
		GHBranch br;
		try {
			br = this.repository.getBranches().get(name);
			return new GitHubBranch(br);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;		
	}
	
	public String getLatestSHA(String branch){
		PagedIterable<GHCommit> commits = this.repository.listCommits();
		List<GHCommit> cList = commits.asList();
		for(GHCommit commit : cList){
			ShortInfo commitShortInfo = commit.getCommitShortInfo();
			PagedIterable<GHCommitComment> listComments = commit.listComments();
			System.out.println("");
		}
		return cList.get(0).getSHA1();
	}

	public String getName() {
		return repository.getName();
	}
	
	


}
