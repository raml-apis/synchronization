package com.mulesoft.portal.apis.github;

import org.kohsuke.github.GHBranch;

public class GitHubBranch {
	
	public GitHubBranch(GHBranch branch) {
		super();
		this.branch = branch;
	}

	private GHBranch branch;

	public GHBranch getBranch() {
		return branch;
	}

}
