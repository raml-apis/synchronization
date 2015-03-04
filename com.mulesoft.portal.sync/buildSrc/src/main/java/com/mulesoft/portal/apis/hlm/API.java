package com.mulesoft.portal.apis.hlm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mulesoft.portal.apis.utils.Utils;

public class API {

	protected String name;
	protected String version;

	protected Notebook[] notebooks;

	protected String description;

	protected String notebooksDescription;

	protected String headLine;

	protected String[] tags;
	
	protected File mainFile;
	
	public File getAPIFolder(){
		return mainFile.getParentFile();
	}

	public API(File apiFile) {
		File fl = apiFile.getParentFile();
		File noteBookFolder = new File(fl, "notebooks");
		readNotebooks(noteBookFolder);
		name = getTitle(apiFile);
		version = getVersion(apiFile);
		mainFile=apiFile;
		String apiDesc = "api.md";
		String apiNote = "notebook.md";
		initDescriptions(fl, apiDesc, apiNote);
	}
	public String getMainRAMLContent(){
		return Utils.getContents(mainFile);
	}

	private void initDescriptions(File fl, String apiDesc, String apiNote) {
		try{
		description=Utils.getContents(new File(fl,"portal/"+apiDesc));
		notebooksDescription=Utils.getContents(new File(fl,"portal/"+apiNote));
		}catch (IllegalStateException e) {
			System.err.println("Not able to read descriptions "+fl.getAbsolutePath());
			description="";
			notebooksDescription="";
		}
	}

	public String getHeadLine() {
		return headLine;
	}

	public void setHeadLine(String headLine) {
		this.headLine = headLine;
	}

	public API(File fl, JSONObject apiObj) {
		mainFile=new File(fl,(String) apiObj.get("apiFile"));
		JSONArray object = (JSONArray) apiObj.get("notebooks");
		Notebook[] notebooks=new Notebook[object.size()];
		File noteBookFolder = new File(fl, "notebooks");
		for (int a=0;a<object.size();a++){
			File nbFile=new File(noteBookFolder,(String) object.get(a));
			Notebook n = parseNotebook(nbFile);
			notebooks[a]=n;
		}
		name = getTitle(mainFile);
		version = getVersion(mainFile);
		publishToPortal=(Boolean) apiObj.get("publishToPortal");
		this.notebooks=notebooks;
		this.headLine=(String) apiObj.get("description");
		String apiDesc = (String) apiObj.get("apiDescription");
		String apiNote = (String) apiObj.get("notebooksDescription");
		initDescriptions(fl, apiDesc, apiNote);
	}

	private Notebook parseNotebook(File nbFile) {
		String title = getTitle(nbFile);
		if (title == null) {
			System.err.println("Notebook without title:"
					+ nbFile.getAbsolutePath());
		}
		Notebook n = new Notebook(title, Utils.getContents(nbFile));
		return n;
	}

	@Override
	public String toString() {
		return name + " v:" + version + "(" + notebooks.length + ")";
	}

	private void readNotebooks(File noteBookFolder) {
		ArrayList<Notebook> nb = new ArrayList<Notebook>();
		if (noteBookFolder.exists()) {
			for (File pn : noteBookFolder.listFiles()) {
				if (pn.getName().endsWith(".md")) {
					Notebook n = parseNotebook(pn);
					nb.add(n);
				}
			}
		}
		notebooks = nb.toArray(new Notebook[nb.size()]);
	}

	private String getTitle(File pn) {
		String str = "title:";
		return getValue(pn, str);
	}

	private String getVersion(File pn) {
		String str = "version:";
		return getValue(pn, str);
	}

	private String getValue(File pn, String str) {
		try {
			List<String> readAllLines = Files.readAllLines(pn.toPath(),
					Charset.forName("UTF-8"));
			String title = null;
			for (String line : readAllLines) {

				int indexOf = line.indexOf(str);
				if (indexOf != -1) {
					title = line.substring(indexOf + str.length()).trim();
					break;
				}
			}
			if (title!=null&&title.startsWith("\"")){
				title=title.substring(1,title.length()-1);
			}
			return title;
		} catch (IOException e) {
			System.err.println(pn.getAbsolutePath());
			throw new IllegalStateException(e);
		}
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		if (version==null){
			return "v1";
		}
		return version;
	}

	public String getPortalDescription() {
		return description;
	}

	public String getNotebooksDescription() {
		return notebooksDescription;
	}

	public Notebook[] getNotebooks() {
		return notebooks;
	}

	public String getDescription() {
		return description;
	}
	boolean publishToPortal;
	boolean publishToLibrary;

	public boolean isPublishToPortal() {
		return publishToPortal;
	}

	public void setPublishToPortal(boolean publishToPortal) {
		this.publishToPortal = publishToPortal;
	}

	public boolean isPublishToLibrary() {
		return publishToLibrary;
	}

	public void setPublishToLibrary(boolean publishToLibrary) {
		this.publishToLibrary = publishToLibrary;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean publishToLibrary() {
		return publishToPortal;
	}
	public boolean publishToPortal() {
		return publishToLibrary;
	}
}