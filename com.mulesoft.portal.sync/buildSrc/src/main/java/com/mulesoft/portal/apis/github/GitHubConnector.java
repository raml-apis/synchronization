package com.mulesoft.portal.apis.github;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class GitHubConnector {
	

	public void cloneRepository(String repoPath, String branch, File workdir){
		
		CloneCommand cloneRepository = Git.cloneRepository();
		cloneRepository.setCredentialsProvider(credentialsProvider);

		cloneRepository.setBranch(branch);

		cloneRepository.setDirectory(workdir);
//		cloneRepository.setProgressMonitor(gitMonitor);
		cloneRepository.setRemote("origin");
		cloneRepository.setURI(repoPath);
		cloneRepository.setTimeout(600);
		cloneRepository.setCloneAllBranches(false);
		cloneRepository.setCloneSubmodules(true);

		Git git;
		try {
			git = cloneRepository.call();
			Repository repo = git.getRepository();
		} catch (InvalidRemoteException e) {
			e.printStackTrace();
		} catch (TransportException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
	
	private UsernamePasswordCredentialsProvider credentialsProvider;
	
	private GitHub gitHub;

	public GitHubConnector(GitHubCredentials credentials){
		
		credentialsProvider = new UsernamePasswordCredentialsProvider(
				credentials.getLogin(), credentials.getPassword());
		
		try {
			gitHub = GitHub.connectUsingPassword(credentials.getLogin(), credentials.getPassword());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public GitHubRepository getRepository(GitApiLocation location){
		
		String repoPath = location.getRepoPath();
		String repoFullPath = location.getRepoFullPath();
		try {
			GHRepository repository = gitHub.getRepository(repoPath);
			GitHubRepository result = new GitHubRepository(repository, repoFullPath, repoPath);
			return result;
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<GitHubRepository> listRepositories(String organization){
		
		ArrayList<GitHubRepository> result = new ArrayList<GitHubRepository>();
		try {
			GHOrganization org = gitHub.getOrganization(organization);
			Map<String, GHRepository> repoMap = org.getRepositories();
			for(GHRepository repository : repoMap.values()){
				String repoPath = repository.getFullName();
				String repoFullPath = "https://github.com/" + repoPath + ".git";
				GitHubRepository repo = new GitHubRepository(repository, repoFullPath, repoPath);
				result.add(repo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void createHook(String repoName, String organization){
		GHOrganization org = null;
		try {
			org = gitHub.getOrganization(organization);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HashMap<String,String> map = new HashMap<String, String>();
		map.put("url", "https://muleion.ci.cloudbees.com/github-webhook/");
		map.put("content_type", "form");
		try {
			GHRepository repo = org.getRepository(repoName);
			List<GHHook> hooks = repo.getHooks();
			for(GHHook h : hooks){
				if("https://muleion.ci.cloudbees.com/github-webhook/".equals(h.getConfig().get("url"))){
					return;
				}
			}
			repo.createHook("web", map, new ArrayList<GHEvent>(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

		
	
}