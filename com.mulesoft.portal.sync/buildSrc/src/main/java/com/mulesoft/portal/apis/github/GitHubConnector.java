package com.mulesoft.portal.apis.github;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
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

		
	
}