package com.mulesoft.portal.apis.hlm;

import java.util.ArrayList;
import java.util.Arrays;

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

}
