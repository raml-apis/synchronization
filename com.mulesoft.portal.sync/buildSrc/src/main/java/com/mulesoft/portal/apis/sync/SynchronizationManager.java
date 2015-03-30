package com.mulesoft.portal.apis.sync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.mulesoft.portal.apis.github.GitApiLocation;
import com.mulesoft.portal.apis.github.GitHubBranch;
import com.mulesoft.portal.apis.github.GitHubConnector;
import com.mulesoft.portal.apis.github.GitHubCredentials;
import com.mulesoft.portal.apis.github.GitHubRepository;
import com.mulesoft.portal.apis.github.RepoUrlExtractor;
import com.mulesoft.portal.apis.hlm.API;
import com.mulesoft.portal.apis.hlm.APIVersion;
import com.mulesoft.portal.apis.hlm.APIProject;
import com.mulesoft.portal.apis.hlm.Notebook;
import com.mulesoft.portal.apis.hlm.ProjectBuilder;
import com.mulesoft.portal.apis.utils.CodeRetriever;
import com.mulesoft.portal.client.APIFile;
import com.mulesoft.portal.client.APIModel;
import com.mulesoft.portal.client.APIModel.PortalAPIVersion;
import com.mulesoft.portal.client.PortalClient;

public class SynchronizationManager {

	private static final String STAGING_SUFFIX = "-staging";

	protected PortalClient client;

	protected APIProject project;

	private GitHubConnector connector;
	
	private GitHubCredentials ghCredentials;
	
	private HashMap<String,APIModel> createdPortals = new HashMap<String, APIModel>();

	public SynchronizationManager(String portalLogin, String portalPassword,
			String gitHubLogin, String gitHubPassword) throws IOException {
		
		client = new PortalClient(portalLogin, portalPassword);		
		this.ghCredentials = new GitHubCredentials(gitHubLogin, gitHubPassword);
	}

	private boolean cleanNotExistingApis = true;

	public boolean isCleanNotExistingApis() {
		return cleanNotExistingApis;
	}

	public void setCleanNotExistingApis(boolean cleanNotExistingApis) {
		this.cleanNotExistingApis = cleanNotExistingApis;
	}
	
	public void removeAllApisFromPortal(){
		APIModel[] apis = client.getAPIs();
		for (APIModel m:apis){
			System.out.println("Deleteting: "+m.getName());
			client.deleteAPI(m);
		}
		System.out.println("Api deletion completed");
	}
	

	boolean verbose=true;
        public void setVerbose(boolean v){
	this.verbose=v;
	}

	public void setStaging(boolean staging) {
		this.staging = staging;
	}

	boolean staging;

	public void syncAllApis(File workingDir, String organization, List<String> branches) {
		
		File targetDir = new File(workingDir, "_SYNC_WOKING_DIR_");
		if(targetDir.exists()){
			deleteDirectory(targetDir);
		}
		
		List<GitHubRepository> repos = new GitHubConnector(ghCredentials).listRepositories(organization);
		
		for(GitHubRepository repo : repos){
			if(repo.getName().equals("synchronization")){
				continue;
			}
			System.out.println(repo.getRepoFullPath());
			GitApiLocation al = new GitApiLocation(repo.getName(), repo.getRepoFullPath(), branches);
			ArrayList<GitApiLocation> apiLocations = new ArrayList<GitApiLocation>();
			apiLocations.add(al);		
			
			CodeRetriever cr = new CodeRetriever(targetDir, apiLocations, ghCredentials);
			cr.cloneRepos();
		}
			
		ProjectBuilder pb = new ProjectBuilder();
		APIProject apiProject = pb.build(targetDir);

		API[] allApis = apiProject.getAllApis();
		Arrays.sort(allApis, new Comparator<API>() {

			@Override
			public int compare(API a1, API a2) {
				int compareName = a1.getName().compareTo(a2.getName());
				return compareName;

			}
		});
		
		APIModel[] apis = client.getAPIs();

		HashMap<String, API> newApisp = getNewApis(allApis, apis);
		for (API a : newApisp.values()) {
			if (verbose)
				System.out.println("Creating:" + a.getName());
			createAPI(a);
		}

		HashMap<String, APIModel> allAPIModels = new HashMap<String, APIModel>();
		for (APIModel q : apis) {
			allAPIModels.put(q.getName(), q);
		}
		for (API a : allApis) {
			if (newApisp.containsKey(a.getName())) {
				continue;
			}
			updateAPIIfNeeded(allAPIModels.get(a.getName()), a);
		}

		deleteDirectory(targetDir);
	}

	private void deleteDirectory(File dir) {
		cleanDirectory(dir);
		dir.delete();
	}

	private void cleanDirectory(File dir) {
		
		File[] listFiles = dir.listFiles();
		for(File f : listFiles){
			if(f.isDirectory()){
				deleteDirectory(f);
			}
			else{
				f.delete();
			}
		}
		
	}

	public void syncAPI(File apiDir, GitApiLocation al) {
		
		ArrayList<GitApiLocation> apiLocations = new ArrayList<GitApiLocation>();
		apiLocations.add(al);		
		
		File targetDir = new File(apiDir, "_SYNC_WOKING_DIR_");
		if(targetDir.exists()){
			deleteDirectory(targetDir);
		}
		
		CodeRetriever cr = new CodeRetriever(targetDir, apiLocations, ghCredentials);
		cr.cloneRepos();
		
		ProjectBuilder pb = new ProjectBuilder();
		APIProject apiProject = pb.build(targetDir);
		
		API[] allApis = apiProject.getAllApis();		
		APIModel[] apis = client.getAPIs();
		
		HashMap<String, API> newApisp = getNewApis(allApis, apis);
		for (API a : newApisp.values()) {
		    if (verbose) System.out.println("Creating:"+a.getName());
		    createAPI(a);
		}
		
		HashMap<String, APIModel> allAPIModels = new HashMap<String, APIModel>();
		for (APIModel q : apis) {
			allAPIModels.put(q.getName(), q);
		}
		for (API a : allApis) {
			if(newApisp.containsKey(a.getName())){
				continue;
			}
			updateAPIIfNeeded(allAPIModels.get(a.getName()), a);
		}
	}


	private void updateAPIIfNeeded(APIModel apiModel, API a)
	{
		ArrayList<APIVersion> versions = a.getVersions();
		for(APIVersion ver : versions){
			Notebook[] notebooks = ver.getNotebooks();
			if(notebooks==null||notebooks.length==0){
				continue;
			}
			String branch = ver.getBranch();
			
			PortalAPIVersion lastVersion = findLastVersion(apiModel, branch);
			
			if(lastVersion == null){
				createVersion(ver, apiModel);
				continue;
			}
	
			GitHubRepository repo = new GitHubConnector(this.ghCredentials).getRepository(ver.getApiLocation());
			GitHubBranch ghBranch = repo.getBranch(branch);
			String latestSHA1 = ghBranch.getBranch().getSHA1();
	
			String syncInfo = client.getSyncInfo(lastVersion);
//			if (syncInfo == null || !syncInfo.equals(latestSHA1)) {
				syncAPI(apiModel, ver, lastVersion, latestSHA1);
//			}
		}
	}

	private PortalAPIVersion findLastVersion(APIModel apiModel, String branch) {
		PortalAPIVersion lastVersion = apiModel.getLastVersion();
			
		String lastVersionName = lastVersion.getName();
		if(branch.equals("staging")&&!lastVersionName.endsWith(STAGING_SUFFIX)){
			lastVersionName = lastVersionName+STAGING_SUFFIX;
		}
		else if(branch.equals("production")&&lastVersionName.endsWith(STAGING_SUFFIX)){
			lastVersionName = lastVersionName.substring(0, lastVersionName.length()-STAGING_SUFFIX.length());
		}
		else{
			return lastVersion;
		}
		lastVersion = apiModel.getVersion(lastVersionName);
		return lastVersion;
	}

	private void syncAPI(APIModel apiModel, APIVersion ver, PortalAPIVersion lastVersion, String latestSHA1) {
		System.out.println("Syncing Version: " + ver.getName());
		client.checkPortal(apiModel, lastVersion);
		client.updateVersion(lastVersion,ver.getHeadLine());
		client.deleteAllPages(lastVersion);
		createPortalContent(apiModel, ver, lastVersion);
		APIFile[] files = null;
		APIFile main = null;
		do {
			files = client.getFiles(lastVersion);
			for (APIFile f : files) {
				String fName = f.getName();
				String fPath = f.getPath();
				if (fName.endsWith(".raml") && fPath.substring(1).equals(fName)) {
					main = f;
					break;
				}
			}
			for (APIFile z : files) {
				if (z != main) {
					client.deleteFile(lastVersion, z);
				}
			}
		} while (files != null && files.length > 1);
		if (main != null) {
			client.setFileRAML(lastVersion, main, ver.getMainRAMLContent());
		}

		uploadFiles(ver.getAPIFolder(), lastVersion);
		client.createAPIPortalSyncStatus(lastVersion, latestSHA1);
	}

	private void createPortalContent(APIModel apiModel, APIVersion a,
			PortalAPIVersion lastVersion) {
		createPortalPages(lastVersion, a.getName(), a.getPortalDescription(),
				a.getNotebooksDescription());
		for (Notebook q : a.getNotebooks()) {
			client.createAPIPortalNotebook(apiModel, lastVersion,
					q.getTitle(), q.getContent());
		}

	}

	protected void clean(APIModel[] apis) {
		for (APIModel a : apis) {
			client.deleteAPI(a);
		}
	}

	private void createAPI(API a) {
		
		ArrayList<APIVersion> versions = a.getVersions();
		
		APIVersion ver0 = versions.get(0);
		Notebook[] notebooks = ver0.getNotebooks();
		if(notebooks==null||notebooks.length==0){
			return;
		}
		APIModel newAPI = client.createNewAPI(new APIModel(a.getName(), ver0.getVersion(),ver0.getDescription()));
		PortalAPIVersion lastVersion = newAPI.getLastVersion();
		
		uploadVersion(ver0, newAPI, lastVersion);		
		
		for(int i = 1 ; i < versions.size(); i++){
			APIVersion ver = versions.get(i);
			createVersion(ver, newAPI);
		}
	}

	private void createVersion(APIVersion ver, APIModel apiModel) {
		
		PortalAPIVersion lastVersion = client.createNewVersion(apiModel, ver.getVersion(), ver.getDescription());
		uploadVersion(ver, apiModel, lastVersion);
	}

	private void uploadVersion(APIVersion ver, APIModel apiModel, PortalAPIVersion portalVersion)
	{
		client.checkPortal(apiModel, portalVersion);
		client.updateVersion(portalVersion,ver.getHeadLine());
		client.deleteAPIPortal(portalVersion);
		client.addRootRAML(portalVersion, ver.getMainRAMLContent());
		createPortalContent(apiModel, ver, portalVersion);
		uploadFiles(ver.getAPIFolder(), portalVersion);
		
		GitHubRepository repo = new GitHubConnector(this.ghCredentials).getRepository(ver.getApiLocation());
		String latestSHA1 = repo.getBranch(ver.getBranch()).getBranch().getSHA1();
		client.createAPIPortalSyncStatus(portalVersion, latestSHA1);
	}

	protected void createPortalPages(PortalAPIVersion version, String apiName,
			String description, String notebooks) {
		if (!apiName.endsWith(" API")) {
			apiName += " API";
		}
		client.createAPIPortalPage(version, apiName, description);
		client.createAPIPortalReference(version, "API reference");
		client.createAPIPortalSection(version, "NOTEBOOKS");
		client.createAPIPortalPage(version, "About", notebooks);
	}

	void uploadFiles(File folder, PortalAPIVersion version) {
		HashSet<String> skip = getSkipSet(folder);
		client.writeFolder(version, folder, skip);
	}

	private HashSet<String> getSkipSet(File folder) {
		HashSet<String> skip = new HashSet<String>();
		skip.add("portal");
		skip.add("notebooks");
		skip.add("validation.ignore");
		skip.add("apiPack.json");
		skip.add("connectors");
		skip.add(".git");
		skip.add(".settings");
		File[] listFiles = folder.listFiles();
		for (File f : listFiles) {
			if (!f.isDirectory()) {
				skip.add(f.getName());
			}
		}
		return skip;
	}

	private HashMap<String, API> getNewApis(API[] allApis, APIModel[] apis) {
		HashMap<String, API> newApisp = new HashMap<String, API>();
		for (API q : allApis) {
			newApisp.put(q.getName(), q);
		}
		for (APIModel m : apis) {
			newApisp.remove(m.getName());
		}
		return newApisp;
	}

	private HashMap<String, APIModel> getDeletedApis(API[] allApis,
			APIModel[] apis) {
		HashMap<String, APIModel> newApisp = new HashMap<String, APIModel>();
		for (APIModel q : apis) {
			newApisp.put(q.getName(), q);
		}
		for (API m : allApis) {
			newApisp.remove(m.getName());
		}
		return newApisp;
	}
}