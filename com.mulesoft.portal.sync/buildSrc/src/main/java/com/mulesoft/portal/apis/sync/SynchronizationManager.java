package com.mulesoft.portal.apis.sync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mulesoft.portal.apis.github.GitApiLocation;
import com.mulesoft.portal.apis.github.GitHubBranch;
import com.mulesoft.portal.apis.github.GitHubConnector;
import com.mulesoft.portal.apis.github.GitHubCredentials;
import com.mulesoft.portal.apis.github.GitHubRepository;
import com.mulesoft.portal.apis.hlm.API;
import com.mulesoft.portal.apis.hlm.APIVersion;
import com.mulesoft.portal.apis.hlm.APIProject;
import com.mulesoft.portal.apis.hlm.Notebook;
import com.mulesoft.portal.apis.hlm.ProjectBuilder;
import com.mulesoft.portal.apis.utils.CodeRetriever;
import com.mulesoft.portal.client.APIFile;
import com.mulesoft.portal.client.APIModel;
import com.mulesoft.portal.client.PortalMap;
import com.mulesoft.portal.client.APIModel.PortalAPIVersion;
import com.mulesoft.portal.client.PortalClient;
import com.mulesoft.portal.client.PortalPageContent;

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
//		client.test(apis,allApis);
		
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
		
		APIModel[] newApiSet = client.getAPIs();
		allAPIModels.clear();
		for (APIModel q : newApiSet) {
			allAPIModels.put(q.getName(), q);
		}
		HashMap<String,APIModel> portalApiMap = getAllPresentAPIs();
		for(API a : allApis){
			updatePortalReferences(allAPIModels.get(a.getName()),a,portalApiMap);
		}
	}

	private HashMap<String, APIModel> getAllPresentAPIs() {
		
		APIModel[] apis = client.getAPIs();
		HashMap<String,APIModel> map = new HashMap<String, APIModel>();
		for(APIModel am : apis){
			map.put(am.getName(), am);
		}
		return map;
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
	
//			String syncInfo = client.getSyncInfo(lastVersion);
//			if (syncInfo == null || !syncInfo.equals(latestSHA1)) {
				syncAPI(apiModel, ver, lastVersion, latestSHA1);
//			}
		}
	}

	private void updatePortalReferences(APIModel apiModel, API a, HashMap<String, APIModel> portalApiMap) {
		ArrayList<APIVersion> versions = a.getVersions();
		for(APIVersion ver : versions){
			String branch = ver.getBranch();
			PortalAPIVersion lastVersion = findLastVersion(apiModel, branch);
			updatePortalReferences(apiModel, ver,  lastVersion,portalApiMap);
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
		createPortalPages(lastVersion, a);
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
		
//		GitHubRepository repo = new GitHubConnector(this.ghCredentials).getRepository(ver.getApiLocation());
//		String latestSHA1 = repo.getBranch(ver.getBranch()).getBranch().getSHA1();
//		client.createAPIPortalSyncStatus(portalVersion, latestSHA1);
	}

	protected void createPortalPages(PortalAPIVersion version, APIVersion a) {
		
		String apiName = composeAboutApiPageName(a);
		
		String description = a.getPortalDescription();
		String notebooks = a.getNotebooksDescription();
		
		client.createAPIPortalPage(version, apiName, description);
		client.createAPIPortalReference(version, "API reference");
		client.createAPIPortalSection(version, "NOTEBOOKS");
		client.createAPIPortalPage(version, "About", notebooks);
	}

	private String composeAboutApiPageName(APIVersion a) {
		String apiName = a.getName();
		if (!apiName.endsWith(" API")) {
			apiName += " API";
		}
		return apiName;
	}
	
	private void updatePortalReferences(
			APIModel apiModel,
			APIVersion ver,
			PortalAPIVersion lastVersion,
			HashMap<String, APIModel> portalApiMap) {
		

		PortalMap portalMap = lastVersion.getPortalMap();
		if(portalMap == null){
			portalMap = client.getPortalMap(lastVersion);
			lastVersion.setPortalMap(portalMap);
		}
		
		String apiName = composeAboutApiPageName(ver);
		String[] pagesToUpdate = new String[]{ apiName, "About" };
		
		for(String pageName : pagesToUpdate){
			PortalPageContent page = portalMap.getPage(pageName);
			updatePortalReferences(page, lastVersion, portalApiMap);
		}
		
		for(PortalPageContent page : portalMap.getNotebooks()){
			updatePortalReferences(page, lastVersion, portalApiMap);
		}
		
	}
	
	public static final String DEFINITION_PAGE_NAME = "definition";

	public static final String ROOT_RAML_PAGE_NAME = "root RAML";
	
	private static HashMap<String,String> referenceTagMap = new HashMap<String, String>();
	static{
		referenceTagMap.put("#REF_TAG_API_REFERENCE", "API reference");
		referenceTagMap.put("#REF_TAG_DEFENITION", DEFINITION_PAGE_NAME);
		referenceTagMap.put("#REF_TAG_ROOT_RAML", ROOT_RAML_PAGE_NAME);
		referenceTagMap.put("#REF_TAG_ABOUT_NOTEBOOKS", "About");
	}

	private void updatePortalReferences(PortalPageContent page, PortalAPIVersion lastVersion, HashMap<String, APIModel> portalApiMap)
	{
		boolean gotChange = false;
		gotChange |= updateReferences(page, lastVersion, portalApiMap, "data");
		gotChange |= updateReferences(page, lastVersion, portalApiMap, "draftData");
		
		if(gotChange){			
			client.updateAPIPortalPage(page);
		}
	}

	private boolean updateReferences(PortalPageContent page, PortalAPIVersion version, HashMap<String, APIModel> portalApiMap, String fName) {
		
		String content = (String)page.getContent().get(fName);
		String currentAPIName = version.getAPIModel().getName();
		List<RefTagOccurence> refTagOccurences = getRefTagOccurences(content, currentAPIName);
		if(refTagOccurences.isEmpty()){
			return false;
		}
		StringBuilder bld = new StringBuilder();
		
		int prev = 0;
		for(RefTagOccurence occurence : refTagOccurences){
			bld.append( content.substring(prev, occurence.getStart()) );
			String apiName = occurence.getApiName();
			APIModel apiModel = portalApiMap.get(apiName);
			
			PortalAPIVersion latestVersion = getLatestVersion(version,apiModel);
			
			
			PortalMap portalMap = latestVersion.getPortalMap();
			if(portalMap==null){
				portalMap = client.getPortalMap(latestVersion);
				latestVersion.setPortalMap(portalMap);
			}
			
			String pageName = occurence.getPageName();
			PortalPageContent pageContent = portalMap.getPage(pageName);
			String url = pageContent.getPortalUrl();
			bld.append(url);
			prev = occurence.getEnd();
		}
		RefTagOccurence lastOccurence = refTagOccurences.get(refTagOccurences.size()-1);
		bld.append(content.substring(lastOccurence.getEnd()));
		String updatedContent = bld.toString();
		page.getContent().put(fName, updatedContent);
		return true;
	}

	private PortalAPIVersion getLatestVersion(PortalAPIVersion version,	APIModel apiModel)
	{
		String vName = version.getName();
		PortalAPIVersion lv = apiModel.getLastVersion();
		String lvName = lv.getName();
		if(vName.endsWith(STAGING_SUFFIX)){
			if(lvName.endsWith(STAGING_SUFFIX)){
				return lv;
			}
			else{
				PortalAPIVersion lvStaging = apiModel.getVersion(lvName+STAGING_SUFFIX);
				return lvStaging;
			}
		}
		else{
			if(lvName.endsWith(STAGING_SUFFIX)){
				String pvName = lvName.substring(0, lvName.length()-STAGING_SUFFIX.length());
				PortalAPIVersion lvProduction = apiModel.getVersion(pvName);
				return lvProduction;
			}
			else{
				return lv;
			}
		}
	}

	//tag_apiname:
	private List<RefTagOccurence> getRefTagOccurences(String str, String currentAPIName) {
		
		ArrayList<RefTagOccurence> list = new ArrayList<RefTagOccurence>(); 
		
		for(Map.Entry<String,String> entry: referenceTagMap.entrySet() ){
			String tag = entry.getKey();
			String pageName = entry.getValue();
			int l = tag.length();
			int prev = 0 ;
			for(int ind = str.indexOf(tag); ind >=0 ; ind = str.indexOf(tag, prev) ){
				int tagEnd = ind + l;
				String apiName = currentAPIName;
				if(str.charAt(tagEnd)=='_'){
					int apiNameEnd = str.indexOf(":", tagEnd);
					apiName = str.substring(tagEnd+1, apiNameEnd);
					prev = apiNameEnd+1;
				}
				else{
					prev = tagEnd;
				}
				RefTagOccurence occurence = new RefTagOccurence(apiName, pageName, ind, prev);
				list.add(occurence);
			}
		}
		return list;
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
	
	public static class RefTagOccurence{
		
		public RefTagOccurence(String apiName, String pageName, int start, int end) {
			super();
			this.apiName = apiName;
			this.pageName = pageName;
			this.start = start;
			this.end = end;
		}

		private String apiName;
		
		private String pageName;
		
		private int start;
		
		private int end;

		public String getApiName() {
			return apiName;
		}

		public String getPageName() {
			return pageName;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}
	}
}