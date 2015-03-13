package com.mulesoft.portal.apis.hlm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.mulesoft.portal.apis.github.GitApiLocation;
import com.mulesoft.portal.apis.github.RepoUrlExtractor;

public class ProjectBuilder {

	public APIProject build(File rootFolder) {
		System.out.println(rootFolder.toString());
		File[] listFiles = rootFolder.listFiles();
		ArrayList<APIFolder> list = new ArrayList<APIFolder>();
		for (File f : listFiles) {
			List<APIFolder> folders = tryCreateAPI(f);
			if(folders==null){
				continue;
			}
			list.addAll(folders);
		}
		return new APIProject(list.toArray(new APIFolder[list.size()]));
	}
	
	public APIProject buildForOneApiFolder(File rootFolder) {
		List<APIFolder> folders = tryCreateAPI(rootFolder);		
		return new APIProject( folders.toArray(new APIFolder[folders.size()]));
	}

	private List<APIFolder> tryCreateAPI(File f) {
		
		List<APIFolder> list = new ArrayList<APIFolder>();
		
		File[] listFiles = f.listFiles();
		
		
		ArrayList<APIVersion> versions = new ArrayList<APIVersion>();
		
		for (File branchFolder : listFiles) {
			
			if(!branchFolder.isDirectory()){
				continue;
			}
			String branch = branchFolder.getName();
			List<String> branches = Arrays.asList(branch);
			
			ArrayList<File> rootRamls = new ArrayList<File>();
			
			GitApiLocation apiLocation = new RepoUrlExtractor(branches).createLocation(null,branchFolder);
			

			for (File fl : branchFolder.listFiles()) {
				if (fl.getName().endsWith(".raml")) {
					rootRamls.add(fl);
				}
			}
			if (rootRamls.size() == 0) {
				throw new IllegalStateException(branchFolder.getAbsolutePath()
						+ " do not have any apis");
			}

			if(rootRamls.isEmpty()){
				return null;
			}
			for(File rootRaml : rootRamls){
				APIVersion ver = new APIVersion(rootRaml, branchFolder);
				ver.setBranch(branch);
				versions.add(ver);
				ver.setApiLocation(apiLocation);
			}
		}
		
		HashMap<String,API> apis = new HashMap<String, API>();
		for(APIVersion ver : versions){
			String name = ver.getName();
			API api = apis.get(name);
			if(api==null){
				api = new API(name);
				apis.put(name, api);
			}
			api.addVersion(ver);
		}
		
		
		ArrayList<API> apiList = new ArrayList<API>(apis.values());
		APIFolder fld = new APIFolder(f,apiList);
		list.add(fld);

		
		return list;
	}
}
