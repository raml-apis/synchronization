package com.mulesoft.portal.apis.hlm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.mulesoft.portal.apis.github.GitApiLocation;
import com.mulesoft.portal.apis.utils.Utils;

public class APIVersion {

	protected String name;
	protected String version;

	protected Notebook[] notebooks;

	protected String description = "";

	protected String notebooksDescription = "";

	protected String headLine;

	protected String[] tags;
	
	protected File mainFile;
	
	protected String repoName;
	
	protected String branch;
	
	public APIVersion(File rootRaml, File apiFolder) {
		
		this.mainFile = rootRaml;
		this.repoName = apiFolder.getName();
		
		this.name = getTitle(mainFile);
		this.version = getVersion(mainFile);		
		
		File[] notebookFiles = getFiles(rootRaml,apiFolder,"notebooks");
		this.notebooks = new Notebook[notebookFiles.length];
		for (int a=0;a<this.notebooks.length;a++){
			File notebookFile = notebookFiles[a];
			Notebook n = parseNotebook(notebookFile);
			notebooks[a]=n;
		}
		
		File[] docsFiles = getFiles(rootRaml,apiFolder,"docs");
		for(File f : docsFiles){
			if(f.getName().equals("headline.md")){
				try{
					this.headLine = Utils.getContents(f);
				}catch (IllegalStateException e) {
					System.err.println("Unable to read headline " + f.getAbsolutePath());
					this.headLine = "";
				}
				break;
			}
		}
		
		File[] portalFiles = getFiles(rootRaml,apiFolder,"portal");
		for(File f : portalFiles){
			String fName = f.getName();
			if(fName.equals("api.md")){
				try{
					this.description = Utils.getContents(f);
				}catch (IllegalStateException e) {
					System.err.println("Unable to read portal description " + f.getAbsolutePath());
					this.description = "";
				}
			}
			else if(fName.equals("notebook.md")){
				try{
					this.notebooksDescription = Utils.getContents(f);
				}catch (IllegalStateException e) {
					System.err.println("Unable to read notebooks description " + f.getAbsolutePath());
					this.notebooksDescription = "";
				}
			}
		}
	}

	private File[] getFiles(File rr, File f, String subfolder) {
		
		String ramlFileName = rr.getName();
		if(ramlFileName.endsWith(".raml")){
			ramlFileName = ramlFileName.substring(0, ramlFileName.length() - ".raml".length());
		}
		File noteBookFolder = new File(f, subfolder);		
		File noteBookSubFolder = new File(noteBookFolder, ramlFileName);
		if(noteBookSubFolder.exists() && noteBookSubFolder.isDirectory()){
			noteBookFolder = noteBookSubFolder;
		}
		File[] files = noteBookFolder.listFiles();		
		return files != null ? files : new File[0];
	}

	private Notebook parseNotebook(File nbFile) {
		String title = getTitle(nbFile);
		if (title == null) {
			System.err.println("Notebook without title:"
					+ nbFile.getAbsolutePath());
		}
		String content = Utils.getContents(nbFile);
		if(content.startsWith("---")){
			int ind0= content.indexOf("title:");
			int ind1 = content.indexOf("\n",ind0);
			int ind2 = content.indexOf("---",ind1);
			ind2 +=3;
			while(Character.isWhitespace(content.charAt(ind2))){ind2++;}
			content = content.substring(ind2);
		}
		Notebook n = new Notebook(title, content);
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
	
	private GitApiLocation apiLocation;

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
		return publishToLibrary;
	}
	public boolean publishToPortal() {
		return publishToPortal;
	}
	public File getAPIFolder(){
		return mainFile.getParentFile();
	}

	public String getMainRAMLContent(){
		return Utils.getContents(mainFile);
	}

	public String getHeadLine() {
		return headLine;
	}

	public void setHeadLine(String headLine) {
		this.headLine = headLine;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setBranch(String branch) {

		if("staging".equals(branch)){
			String suff = "-" + this.branch;
			if(this.branch!=null&&this.version.endsWith(suff)){
				this.version = this.version.substring(0, this.version.length() - suff.length());
			}
			this.version += "-staging";
		}
		this.branch = branch;
		
	}

	public String getBranch() {
		return branch;
	}

	public void setApiLocation(GitApiLocation apiLocation) {
		this.apiLocation = apiLocation;		
	}

	public GitApiLocation getApiLocation() {
		return apiLocation;
	}
}