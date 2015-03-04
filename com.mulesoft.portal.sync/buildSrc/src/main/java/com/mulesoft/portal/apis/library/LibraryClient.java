package com.mulesoft.portal.apis.library;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.mulesoft.portal.apis.utils.SimpleClient;

public class LibraryClient extends SimpleClient implements ILibraryClient {

	
	public LibraryClient(String userName,String password){
		super("https://qa.anypoint.mulesoft.com/",userName,password);
		
	}
	/* (non-Javadoc)
	 * @see com.mulesoft.portal.apis.library.ILibraryClient#getCurrentlyPublishedToLibrary()
	 */
	public LibraryNode[] getCurrentlyPublishedToLibrary() {
		JSONArray json = getJSONArray("/library/api/1/objects");
		LibraryNode[] ls=new LibraryNode[json.size()];
		for (int a=0;a<json.size();a++){
			JSONObject object = (JSONObject) json.get(a);
			System.out.println(object);
			ls[a]=toNode(object);
		}
		return ls;
	}
	
	private LibraryNode toNode(JSONObject object) {
		LibraryNode node=new LibraryNode();
		node.setId(((Long)object.get("id")).intValue());
		node.setTitle((String) object.get("name"));
		node.setDescription((String) object.get("description"));
		boolean contains = object.toString().contains(".raml");
		if (contains){
			System.out.println("a");
		}
		return node;
	}
	/* (non-Javadoc)
	 * @see com.mulesoft.portal.apis.library.ILibraryClient#updateOrCreateNode(com.mulesoft.portal.apis.library.LibraryNode)
	 */
	public void updateOrCreateNode(LibraryNode node){
		String object="{\r\n" + 
				"  \"owner\":\"anypointadmin\",\r\n" + 
				"  \"type_id\":1,\r\n" + 
				"  \"versions\":[\r\n" + 
				"    {\r\n" + 
				"      \"isSuggested\":true,\r\n" + 
				"      \"object_version\":\"1.2\",\r\n" + 
				"      \"mule_version_id\":\"3.5\",\r\n" + 
				"      \"download_url\":\"http://somevalidurl.com\",\r\n" + 
				"      \"doc_url\":\"http://anotherValidUrl.com\"\r\n" + 
				"    }\r\n" + 
				"  ],\r\n" + 
				"  \"name\":\"Name123\",\r\n" + 
				"  \"name_url\":\"name-ur1232l\",\r\n" + 
				"  \"icon_url\":\"http://someurl.com\",\r\n" + 
				"  \"summary\":\"summary\",\r\n" + 
				"  \"youtube_video_url\":\"http://another.com\",\r\n" + 
				"  \"video_caption\":\"some video caption\",\r\n" + 
				"  \"description\":\"some description\",\r\n" + 
				"  \"metadata\":[\r\n" + 
				"    {\r\n" + 
				"      \"label\":\"Some label group\",\r\n" + 
				"      \"type\":\"tag\",\r\n" + 
				"      \"values\":[\r\n" + 
				"        {\r\n" + 
				"          \"text\":\"Good-tag\",\r\n" + 
				"          \"value\":\"Good-tag\"\r\n" + 
				"        },\r\n" + 
				"        {\r\n" + 
				"          \"text\":\"Another-tag\",\r\n" + 
				"          \"value\":\"Another-tag\"\r\n" + 
				"        }\r\n" + 
				"      ]\r\n" + 
				"    }\r\n" + 
				"  ]" + 
				"}";
		JSONObject parse = (JSONObject) JSONValue.parse(object);
		JSONObject postJSON = postJSON("/library/api/1/objects", parse);
		LibraryNode nm=toNode(postJSON);
		deleteNode(nm);
	}
	
	/* (non-Javadoc)
	 * @see com.mulesoft.portal.apis.library.ILibraryClient#deleteNode(com.mulesoft.portal.apis.library.LibraryNode)
	 */
	public void deleteNode(LibraryNode node){
		JSONObject putJSON = putJSON("/library/api/1/objects/"+node.getId()+"/delete",new JSONObject());
		System.out.println(putJSON);
	}
	@Override
	protected String authentificationHeader() {
		return "x-token";
	}
	@Override
	protected String innerAuthentificate(String userName,String pass) {
		String value="{\"grant_type\" : \"password\",\r\n"
				+ "\"username\" : \"\",\r\n"
				+ "\"password\" : \"\",\r\n"
				+ "\"client_id\" : \"\",\r\n"
				+ "\"client_secret\" : \"\"}";
		JSONObject postJSON = postJSON("accounts/oauth2/token", (JSONObject)JSONValue.parse(value));
		postJSON = postJSON("/library/api/exchangeToken",
		(JSONObject) JSONValue.parse("{\"access_token\":\""+postJSON.get("access_token") + "\"}"));
		return (String) postJSON.get("token");
	}
}
