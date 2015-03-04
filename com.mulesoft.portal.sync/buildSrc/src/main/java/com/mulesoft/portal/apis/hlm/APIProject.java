package com.mulesoft.portal.apis.hlm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class APIProject {

	protected APIFolder[] folders;
	
	public APIProject(APIFolder[] array) {
		this.folders=array;
	}

	public APIFolder[] getFolders() {
		return folders.clone();
	}

	public API[] getAllApis() {
		ArrayList<API>result=new ArrayList<API>();
		for (APIFolder f:folders){
			result.addAll(Arrays.asList(f.apis));
		}
		return result.toArray(new API[result.size()]);
	}

	public API[] getAllApisToPortal() {
		ArrayList<API>result=new ArrayList<API>();
		for (APIFolder f:folders){
			List<API> c = Arrays.asList(f.apis);
			for (API q:c) {
				if (q.publishToPortal) {
					result.add(q);
				}
			}
		}
		return result.toArray(new API[result.size()]);
	}
}
