package com.mulesoft.portal.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mulesoft.portal.apis.utils.SimpleClient;
import com.mulesoft.portal.client.APIModel.APIVersion;

public class PortalClient extends SimpleClient{

	private static final String SYNC_STATUS = "SS:";
	private static final String APIPLATFORM_SESSION = "/apiplatform/session";
	
	public PortalClient(String userName, String pass) {
		super("https://anypoint.mulesoft.com",userName,pass);		
	}

	
	@Override
	protected String innerAuthentificate(String userName, String pass) {
		String relative = "/accounts/login";
		WebTarget target = getTarget(relative);
		Builder request = target
				.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
		MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<String, String>();
		multivaluedHashMap.add("username", userName);
		multivaluedHashMap.add("password", pass);
		Entity<Form> form = Entity.form(multivaluedHashMap);
		Response invoke = request.buildPost(form).invoke();
		JSONObject obj = getJSON(invoke);
		String token = (String) obj.get("access_token");
		target = getTarget(APIPLATFORM_SESSION);
		JSONObject ss = new JSONObject();
		ss.put("token", token);
		JSONObject postJSON = postJSON(APIPLATFORM_SESSION, ss);
		return (String) postJSON.get("token");
	}
	

	public void deleteAPI(APIModel q) {
		deleteAPI(((Long) q.id).intValue());
	}

	public void deleteAPI(int id) {
		delete("/apiplatform/repository/apis/" + id);
	}

	public APIModel[] getAPIS() {
		JSONObject json = getJSON("/apiplatform/repository/apis");
		JSONArray object = (JSONArray) json.get("apis");
		APIModel[] result = new APIModel[object.size()];
		for (int a = 0; a < object.size(); a++) {
			JSONObject obj = (JSONObject) object.get(a);

			APIModel load = SimpleToJSON.load(obj, APIModel.class);
			result[a] = load;
			JSONArray va = (JSONArray) obj.get("versions");
			if (va != null) {
				for (int i = 0; i < va.size(); i++) {
					APIVersion e = load.new APIVersion();
					e.load((JSONObject) va.get(i));
					load.versionList.add(e);
					load.version = e.name;
				}
			}
		}
		return result;
	}
	public  void updateVersion(APIVersion version, String description){
		JSONObject json = getJSON("/apiplatform/repository/apis");
		JSONArray object = (JSONArray) json.get("apis");
		APIModel[] result = new APIModel[object.size()];
		for (int a = 0; a < object.size(); a++) {
			JSONObject obj = (JSONObject) object.get(a);
			APIModel load = SimpleToJSON.load(obj, APIModel.class);
			result[a] = load;
			if (load.id.longValue()==version.getAPIModel().id) {
				JSONArray va = (JSONArray) obj.get("versions");
				if (va != null) {
					for (int i = 0; i < va.size(); i++) {
						APIVersion e = load.new APIVersion();
						e.load((JSONObject) va.get(i));
						if (e.id.longValue()==version.id) {
							JSONObject vobj=(JSONObject) va.get(i);
							vobj.put("description",description);
							putJSON("/apiplatform/repository/apis/" + version.getAPIModel().id
									+ "/versions/" + version.id + "", vobj);
						}
					}
				}
			}
		}
	}
	public  APIVersion getOrCreateStagingVersion(APIVersion version){
		JSONObject json = getJSON("/apiplatform/repository/apis");
		JSONArray object = (JSONArray) json.get("apis");
		APIModel[] result = new APIModel[object.size()];
		for (int a = 0; a < object.size(); a++) {
			JSONObject obj = (JSONObject) object.get(a);
			APIModel load = SimpleToJSON.load(obj, APIModel.class);
			result[a] = load;
			if (load.id.longValue()==version.getAPIModel().id) {
				JSONArray va = (JSONArray) obj.get("versions");
				if (va != null) {
					for (int i = 0; i < va.size(); i++) {
						APIVersion e = load.new APIVersion();
						e.load((JSONObject) va.get(i));
						if (e.name.equals(version.name+"-staging")) {
							System.out.println("Found staging version for:" + version.getAPIModel().name);
							return e;
						}
					}
					for (int i = 0; i < va.size(); i++) {
						APIVersion e = load.new APIVersion();
						e.load((JSONObject) va.get(i));
						if (e.id.longValue()==version.id) {
							JSONObject vobj=(JSONObject) va.get(i);
							vobj.remove("id");
							vobj.put("name",version.name+"-staging");
							//vobj.put("description",vobj.get("de"));
							System.out.println("Creating staging version for:" + version.getAPIModel().name);
							JSONObject obj2=postJSON("/apiplatform/repository/apis/" + version.getAPIModel().id
									+ "/versions/" , vobj);
							e = load.new APIVersion();
							e.load((JSONObject) obj2);
							return e;
						}
					}
				}
			}
		}
		System.out.println("Strange (aquiring syncing info) for :" + version.getAPIModel().name);
		return null;
	}

	@SuppressWarnings("unchecked")
	public void addRootRAML(APIVersion version, String content) {
		JSONObject object = new JSONObject();
		APIModel mdl = version.getAPIModel();
		object.put("apiId", mdl.id);
		object.put("apiVersionId", version.id);
		object.put("apiName", mdl.name);
		object.put("isDirectory", false);
		object.put("name", "api.raml");
		object.put("data", content);
		postJSON("/apiplatform/repository/apis/" + mdl.id
				+ "/versions/" + version.id + "/addRootRaml", object);
	}

	public void writeFolder(APIVersion version, File fld, HashSet<String> skip) {
		writeFolder(version, fld, 0, skip);
	}

	public void writeFolder(APIVersion version, File fld, long parentId,
			HashSet<String> skip) {
		File[] listFiles = fld.listFiles();
		for (File f : listFiles) {
			if (skip.contains(f.getName())) {
				continue;
			}
			if (f.isDirectory()) {
				long addFolder = addFolder(version, f.getName(), (int) parentId);
				writeFolder(version, f, addFolder, skip);
			} else {
				try {
					addFileRAML(version, f.getName(),
							new String(
									Files.readAllBytes(Paths.get(f.toURI())),
									"UTF-8"), parentId);
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException(e);
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void addFileRAML(APIVersion version, String title, String content,
			long folderId) {
		APIModel mdl = version.getAPIModel();
		JSONObject object = new JSONObject();
		object.put("apiId", mdl.id);
		object.put("apiVersion", version.id);
		object.put("apiName", mdl.name);
		object.put("isDirectory", false);
		object.put("name", title);
		if (folderId != 0) {
			object.put("parentId", folderId);
		}
		object.put("data", content);
		JSONObject postJSON = postJSON("/apiplatform/repository/apis/" + mdl.id
				+ "/versions/" + version.id + "/files", object);
		
		// https://127.0.0.1:8443/apiplatform/repository/apis/12/versions/31/addRootRaml
	}

	public APIFile[] getFiles(APIVersion version) {
		JSONArray postJSON = getJSONArray("/apiplatform/repository/apis/"
				+ version.getAPIModel().id + "/versions/" + version.id
				+ "/files");
		APIFile[] files = new APIFile[postJSON.size()];
		for (int a = 0; a < postJSON.size(); a++) {
			files[a] = SimpleToJSON.load((JSONObject) postJSON.get(a),
					APIFile.class);
		}
		return files;
	}

	public String getFileContent(APIVersion version, APIFile file) {
		JSONObject postJSON = getJSON("/apiplatform/repository/apis/"
				+ version.getAPIModel().id + "/versions/" + version.id
				+ "/files/" + file.getId());
		return (String) postJSON.get("data");
	}

	@SuppressWarnings("unchecked")
	public void setFileRAML(APIVersion version, APIFile fl, String content) {
		APIModel mdl = version.getAPIModel();
		JSONObject object = new JSONObject();
		// {"apiId":12,"apiVersionId":31,"apiName":"dummy","isDirectory":false,"name":"api.raml","data":"#%RAML 0.8\ntitle: test\nversion: dummy"}
		object.put("apiId", mdl.id);
		object.put("apiVersion", version.id);
		object.put("apiName", mdl.name);
		object.put("isDirectory", fl.isDirectory);
		object.put("name", fl.name);
		object.put("id", fl.id);
		if (fl.parentId != null) {
			object.put("parentId", fl.parentId);
		}
		object.put("data", content);
		JSONObject postJSON = putJSON("/apiplatform/repository/apis/" + mdl.id
				+ "/versions/" + version.id + "/files/" + fl.getId(), object);

		// https://127.0.0.1:8443/apiplatform/repository/apis/12/versions/31/addRootRaml
	}

	@SuppressWarnings("unchecked")
	public long addFolder(APIVersion version, String title, long folderId) {
		JSONObject object = new JSONObject();
		// {"apiId":12,"apiVersionId":31,"apiName":"dummy","isDirectory":false,"name":"api.raml","data":"#%RAML 0.8\ntitle: test\nversion: dummy"}
		object.put("apiId", version.getAPIModel().id);
		object.put("apiVersion", version.id);
		if (folderId != 0) {
			object.put("parentId", folderId);
		}
		object.put("apiName", version.getAPIModel().name);
		object.put("isDirectory", true);
		object.put("name", title);
		JSONObject postJSON = postJSON("/apiplatform/repository/apis/"
				+ version.getAPIModel().id + "/versions/" + version.id
				+ "/files", object);
		Object object2 = postJSON.get("id");
		if (object2==null){
			APIFile[] files = getFiles(version);
			for (APIFile f:files){
				if (f.getName().equals(title)){
					return f.id;
				}
			}
		}
		return (Long) object2;
		// https://127.0.0.1:8443/apiplatform/repository/apis/12/versions/31/addRootRaml
	}

	public void createAPIPortal(APIVersion version) {
		APIModel mdl = version.getAPIModel();
		JSONObject postJSON = postJSON("/apiplatform/repository/apis/" + mdl.id
				+ "/versions/" + version.id + "/portal", null);		
		JSONArray object = (JSONArray) postJSON.get("pages");
		for (int a=0;a<object.size();a++){
			JSONObject page = (JSONObject) object.get(a);
			Long pageId = (Long) page.get("id");
			deleteAPIPortalPage(mdl.getLastVersion(), pageId);
		}
	}

	@SuppressWarnings("unchecked")
	public void createAPIPortalPage(APIVersion version,String title,
			String content) {
		JSONObject object = new JSONObject();
		object.put("type", "markdown");
		object.put("name", title);
		object.put("draftName", title);
		object.put("data", content);
		JSONObject postJSON = postJSON("/apiplatform/repository/apis/" + version.getAPIModel().id
				+ "/versions/" + version.id + "/portal/pages", object);
		//System.out.println(postJSON);
	}
	
	@SuppressWarnings("unchecked")
	public void updateAPIPortalPage(APIVersion version,String title,
			String content,Long pageId) {
		JSONObject object = new JSONObject();
		object.put("type", "markdown");
		object.put("name", title);
		object.put("draftName", title);
		object.put("data", content);
		JSONObject postJSON = putJSON("/apiplatform/repository/apis/" + version.getAPIModel().id
				+ "/versions/" + version.id + "/portal/pages/"+pageId, object);
		//System.out.println(postJSON);
	}
	public void deleteAPIPortalPage(APIVersion version,Long pageId) {
		delete("/apiplatform/repository/apis/" + version.getAPIModel().id
				+ "/versions/" + version.id + "/portal/pages/"+pageId);
	}

	@SuppressWarnings("unchecked")
	public void createAPIPortalSection(APIVersion version, 
			String description) {
		JSONObject object = new JSONObject();
		object.put("type", "header");
		object.put("name", description);
		object.put("draftName", description);
		object.put("data", "");
		postJSON("/apiplatform/repository/apis/" + version.getAPIModel().getId()
				+ "/versions/" + version.id + "/portal/pages", object);		
	}

	@SuppressWarnings("unchecked")
	public void createAPIPortalNotebook(APIModel mdl, APIVersion version,
			String title, String content) {
		JSONObject object = new JSONObject();
		object.put("type", "notebook");
		object.put("name", title);
		object.put("draftName", title);
		object.put("data", content);
		postJSON("/apiplatform/repository/apis/" + mdl.id + "/versions/"
				+ version.id + "/portal/pages", object);
	}

	public APIModel createNewAPI(APIModel mdl) {
		JSONObject postJSON = postJSON("/apiplatform/repository/apis",
				mdl.toJSON());
		JSONArray arr = (JSONArray) postJSON.get("versions");
		if (arr == null) {
			return null;
		}
		for (int a = 0; a < arr.size(); a++) {
			JSONObject object = (JSONObject) arr.get(a);
			APIVersion version = mdl.new APIVersion();
			version.id = (Long) object.get("id");
			version.name = (String) object.get("name");
			mdl.versionList.add(version);
		}
		mdl.id = (Long) postJSON.get("id");
		return mdl;
	}

	public void deleteFile(APIVersion version, APIFile fl) {
		APIModel mdl = version.getAPIModel();
		delete("/apiplatform/repository/apis/" + mdl.id
				+ "/versions/" + version.id + "/files/" + fl.getId());		
	}

	@SuppressWarnings("unchecked")
	public void createAPIPortalReference(APIVersion version, String string) {
		JSONObject object = new JSONObject();
		object.put("type", "console");
		object.put("name", string);
		postJSON("/apiplatform/repository/apis/" + version.getAPIModel().getId()
				+ "/versions/" + version.id + "/portal/pages", object);
	}

	@SuppressWarnings("unchecked")
	public void createAPIPortalSyncStatus(APIVersion version, 
			String sync) {
		JSONObject object = new JSONObject();
		object.put("type", "markdown");
		object.put("name", SYNC_STATUS+sync);
		object.put("draftName", SYNC_STATUS+sync);
		object.put("draftData", "");
		postJSON("/apiplatform/repository/apis/" + version.getAPIModel().getId()
				+ "/versions/" + version.id + "/portal/pages", object);		
	}
	public String getSyncInfo(APIVersion lastVersion) {
		try{
		JSONObject json = getJSON("/apiplatform/repository/apis/" + lastVersion.getAPIModel().getId()
				+ "/versions/" + lastVersion.id + "/portal");
		JSONArray arr=(JSONArray) json.get("pages");
		for (int a=0;a<arr.size();a++){
			JSONObject object = (JSONObject) arr.get(a);
			object.get("id");
			String name = (String) object.get("name");
			String dname = (String) object.get("draftName");
			String prefix = SYNC_STATUS;
			if (name.startsWith(prefix)||dname.startsWith(prefix)){
				String string = name=name.substring(prefix.length());
				if (string!=null){
					return string;
				}
			}				
		}
		return "";
		}catch (ProcessingException e) {
			return "";
		}
	}
	
	public void deleteAllPages(APIVersion lastVersion) {
		try{
		JSONObject json = getJSON("/apiplatform/repository/apis/" + lastVersion.getAPIModel().getId()
				+ "/versions/" + lastVersion.id + "/portal");
		JSONArray arr=(JSONArray) json.get("pages");
		for (int a=0;a<arr.size();a++){
			JSONObject object = (JSONObject) arr.get(a);
			Long pageId = (Long) object.get("id");
			deleteAPIPortalPage(lastVersion, pageId);
		}
		}catch (ProcessingException e) {
			e.printStackTrace();
		}
	}

	
}