package com.mulesoft.portal.apis.library;

import java.util.ArrayList;
import java.util.HashMap;

import com.mulesoft.portal.apis.hlm.API;
import com.mulesoft.portal.apis.hlm.APIProject;
import com.mulesoft.portal.apis.utils.Utils;

public class LibrarySync {

	protected ILibraryClient client;
	
	protected IRAMLUrlProvider urlProvider;

	public LibrarySync(ILibraryClient client, IRAMLUrlProvider urlProvider) {
		super();
		this.client = client;
		this.urlProvider = urlProvider;
	}

	public void doSync(APIProject project) {
		API[] allApis = getAPIsToSync(project);
		LibraryNode[] currentlyPublishedToLibrary = client
				.getCurrentlyPublishedToLibrary();
		/*HashMap<String, API> new1 = Utils.getNew(allApis,
				currentlyPublishedToLibrary);
		HashMap<String, LibraryNode> deleted = Utils.getDeleted(allApis,
				currentlyPublishedToLibrary);*/
		/*for (LibraryNode n : deleted.values()) {
			client.deleteNode(n);
		}
		for (API qa : new1.values()) {
			client.updateOrCreateNode(toLibrary(qa));
		}
		HashMap<String, LibraryNode> allLibraryModels = new HashMap<String, LibraryNode>();
		for (LibraryNode q : currentlyPublishedToLibrary) {
			allLibraryModels.put(q.getName(), q);
		}
		for (API a : allApis) {
			if (!new1.containsKey(a.getName())) {
				LibraryNode apiModel = allLibraryModels.get(a.getName());
				doSync(a,apiModel);
			}
		}*/
	}

	private API[] getAPIsToSync(APIProject project) {
		API[] allApis = project.getAllApis();
		ArrayList<API>toSync=new ArrayList<API>();
		for (API a:allApis){
			if (a.publishToLibrary()){
				toSync.add(a);
			}
		}
		return toSync.toArray(new API[toSync.size()]);
	}

	private void doSync(API a, LibraryNode apiModel) {
		LibraryNode library = toLibrary(a);
		if (library.equals(apiModel)){
			return;
		}
		library.setId(apiModel.getId());
		client.updateOrCreateNode(library);
	}

	private LibraryNode toLibrary(API qa) {
		LibraryNode libraryNode = new LibraryNode();
		libraryNode.setTitle(qa.getName());
		libraryNode.setDescription(qa.getDescription());
		libraryNode.setRamlUrl(this.urlProvider.getRAWUrl(qa));
		return libraryNode;
	}
}
