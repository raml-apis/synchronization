package com.mulesoft.portal.apis.sync;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import com.mulesoft.portal.apis.github.GitHubConnector;
import com.mulesoft.portal.apis.hlm.API;
import com.mulesoft.portal.apis.hlm.APIProject;
import com.mulesoft.portal.apis.hlm.Notebook;
import com.mulesoft.portal.client.APIFile;
import com.mulesoft.portal.client.APIModel;
import com.mulesoft.portal.client.APIModel.APIVersion;
import com.mulesoft.portal.client.PortalClient;

public class SynchronizationManager {

	protected PortalClient client;

	protected APIProject project;

	private String latestVersion;

	private GitHubConnector connector;

	public SynchronizationManager(PortalClient client, APIProject project,
			GitHubConnector connector) throws IOException {
		super();
		this.client = client;
		this.project = project;
		this.latestVersion = connector.getCurrentSHA();
		this.connector = connector;
	}

	public SynchronizationManager(String portalLogin, String portalPassword,
			String gitHubLogin, String girHubPassword,
			APIProject project) throws IOException {
		connector = new GitHubConnector("restful-api-modeling-lang/100apis",
				gitHubLogin, girHubPassword);
		client = new PortalClient(portalLogin, portalPassword);
		this.latestVersion = connector.getCurrentSHA();
		this.project = project;
	}

	private boolean cleanNotExistingApis = true;

	public boolean isCleanNotExistingApis() {
		return cleanNotExistingApis;
	}

	public void setCleanNotExistingApis(boolean cleanNotExistingApis) {
		this.cleanNotExistingApis = cleanNotExistingApis;
	}
	
	public void removeAllApisFromPortal(){
		APIModel[] apis = client.getAPIS();
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

	public boolean isStaging() {
		return staging;
	}

	public void setStaging(boolean staging) {
		this.staging = staging;
	}

	boolean staging;

	public void syncAllApis() {
		API[] allApis = project.getAllApisToPortal();

		APIModel[] apis = client.getAPIS();
		//allApis = project.getAllApisToPortal();
		HashMap<String, API> newApisp = getNewApis(allApis, apis);
		for (API a : newApisp.values()) {
		    if (verbose) System.out.println("Creating:"+a.getName());
		    createAPI(a);
		}
		if (cleanNotExistingApis) {
			HashMap<String, APIModel> removed = getDeletedApis(allApis, apis);
			for (APIModel m : removed.values()) {
	 			if (verbose) System.out.println("Deleting:"+m.getName());
				client.deleteAPI(m);
			}
		}
		HashMap<String, APIModel> allAPIModels = new HashMap<String, APIModel>();
		for (APIModel q : apis) {
			allAPIModels.put(q.getName(), q);
		}
		for (API a : allApis) {
			if (!newApisp.containsKey(a.getName())) {
	 			if (verbose) System.out.println("Checking:"+a.getName());
				updateAPIIfNeeded(allAPIModels, a);
			}
		}
	}

	private void updateAPIIfNeeded(HashMap<String, APIModel> allAPIModels, API a) {
		APIModel apiModel = allAPIModels.get(a.getName());
		APIVersion lastVersion = apiModel.getLastVersion();
		if (isStaging()){
			lastVersion=client.getOrCreateStagingVersion(lastVersion);
		}
		String syncInfo = client.getSyncInfo(lastVersion);
		if (syncInfo == null || !syncInfo.equals(latestVersion)) {
			// we should check if this api is changed;
			if (syncInfo == null || syncInfo.length() == 0) {
				syncAPI(apiModel, a);
			} else {
				try {
					HashSet<String> calculateChangedPaths = connector
							.calculateChangedPaths(syncInfo);
					for (String s : calculateChangedPaths) {
						if (s.startsWith("/apis/")) {
							String string = s = s.substring("/apis/".length());
							int indexOf = string.indexOf('/');
							if (indexOf > -1) {
								String apiFolderName = string = string
										.substring(0, indexOf);
								if (a.getAPIFolder().getName()
										.equals(apiFolderName)) {
									syncAPI(apiModel, a);
									break;
								}
							}
						}
					}
				} catch (IOException e) {
					throw new IllegalStateException();
				}
			}
		}
	}

	private void syncAPI(APIModel apiModel, API a) {
		System.out.println("Syncing API: " + a.getName());
		APIVersion lastVersion = apiModel.getLastVersion();
		if (isStaging()){
			lastVersion=client.getOrCreateStagingVersion(lastVersion);
		}
		client.updateVersion(lastVersion,a.getHeadLine());
		client.deleteAllPages(lastVersion);
		createPortalContent(apiModel, a, lastVersion);
		APIFile[] files = null;
		APIFile main = null;
		do {
			files = client.getFiles(lastVersion);
			for (APIFile f : files) {
				if (f.getName().equals("api.raml")) {
					main = f;
				}
			}
			for (APIFile z : files) {
				if (z != main) {
					client.deleteFile(lastVersion, z);
				}
			}
		} while (files != null && files.length > 1);
		if (main != null) {
			client.setFileRAML(lastVersion, main, a.getMainRAMLContent());
		}

		uploadFiles(a.getAPIFolder(), lastVersion);
		client.createAPIPortalSyncStatus(lastVersion, latestVersion);
	}

	private void createPortalContent(APIModel apiModel, API a,
			APIVersion lastVersion) {
		createPortalPages(lastVersion, a.getName(), a.getPortalDescription(),
				a.getNotebooksDescription());
		for (Notebook q : a.getNotebooks()) {
			client.createAPIPortalNotebook(apiModel, apiModel.getLastVersion(),
					q.getTitle(), q.getContent());
		}

	}

	protected void clean(APIModel[] apis) {
		for (APIModel a : apis) {
			client.deleteAPI(a);
		}
	}

	private void createAPI(API a) {
		APIModel createNewAPI = client.createNewAPI(new APIModel(a.getName(), a
				.getVersion(),a.getDescription()));
		APIVersion lastVersion = createNewAPI.getLastVersion();
		if (isStaging()){
			lastVersion=client.getOrCreateStagingVersion(lastVersion);
		}
		client.updateVersion(lastVersion,a.getHeadLine());
		client.createAPIPortal(lastVersion);
		client.addRootRAML(lastVersion, a.getMainRAMLContent());
		createPortalContent(createNewAPI, a, lastVersion);
		uploadFiles(a.getAPIFolder(), lastVersion);
		client.createAPIPortalSyncStatus(lastVersion, latestVersion);
	}

	protected void createPortalPages(APIVersion version, String apiName,
			String description, String notebooks) {
		if (!apiName.endsWith(" API")) {
			apiName += " API";
		}
		client.createAPIPortalPage(version, apiName, description);
		client.createAPIPortalReference(version, "API reference");
		client.createAPIPortalSection(version, "NOTEBOOKS");
		client.createAPIPortalPage(version, "About", notebooks);
	}

	void uploadFiles(File folder, APIVersion version) {
		HashSet<String> skip = getSkipSet(folder);
		client.writeFolder(version, folder, skip);
	}

	private HashSet<String> getSkipSet(File folder) {
		HashSet<String> skip = new HashSet<String>();
		skip.add("portal");
		skip.add("notebooks");
		skip.add("validation.ignore");
		skip.add("apiPack.json");
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