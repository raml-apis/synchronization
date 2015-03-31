package com.mulesoft.portal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PortalMap {
	
	private HashMap<String,PortalPageContent> map = new HashMap<String, PortalPageContent>();
	
	public PortalPageContent getPage(String pageName){
		return map.get(pageName);
	}
	
	public void registerPage(String pageName, PortalPageContent jsonObject){
		map.put(pageName, jsonObject);
	}
	
	public List<PortalPageContent> getNotebooks(){
		
		ArrayList<PortalPageContent> result = new ArrayList<PortalPageContent>();
		for(PortalPageContent p : map.values()){
			if(p.getType()!=null&&p.getType().equals("notebook")){
				result.add(p);
			}
		}
		return result;		
	}

}
