package com.mulesoft.portal.apis.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public abstract class SimpleClient {

	
	private String baseUrl ;
	private Client client=ClientBuilder.newClient();
	private Object actualToken;

	public SimpleClient(String baseUrl,String userName, String pass) {
		super();
		this.baseUrl=baseUrl;
		authentificate(userName,pass);
	}

	private void authentificate(String userName, String pass) {
		Object object = innerAuthentificate(userName,pass);
		actualToken = object;
	}

	protected abstract String innerAuthentificate(String userName, String pass);

	protected JSONObject execute(JSONObject object, WebTarget target,
			String method) {
		Entity<String> entity2 = object == null ? null : Entity.entity(
				object.toJSONString(), MediaType.APPLICATION_JSON);
		Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
		if (actualToken != null) {
			request = request.header(authentificationHeader(), "Bearer " + actualToken);
		}
		Response post = request.method(method, entity2);
		String asString = post.readEntity(String.class);
		Object parse = JSONValue.parse(asString);
		if (post.getStatus() < 200 || post.getStatus() > 300) {
			System.err.println(target.getUri().toASCIIString() + ":"
					+ post.getStatus());
			System.err.println(asString);
			// throw new IllegalStateException();
		}
		if (parse instanceof JSONObject) {
			return (JSONObject) parse;
		}

		return null;
	}

	protected String authentificationHeader() {
		return "Authorization";
	}

	protected WebTarget getTarget(String relative) {
		WebTarget target = client.target(baseUrl + relative);
		return target;
	}

	protected JSONObject postJSON(String relative, JSONObject object) {
		WebTarget target = getTarget(relative);
		String method = "POST";
		return execute(object, target, method);
	}

	protected JSONObject putJSON(String relative, JSONObject object) {
		WebTarget target = getTarget(relative);
		String method = "PUT";
		return execute(object, target, method);
	}

	protected JSONObject delete(String relative) {
		WebTarget target = getTarget(relative);
		String method = "DELETE";
		return execute(null, target, method);
	}

	protected JSONObject getJSON(String relative) {
		WebTarget target = getTarget(relative);
		String method = "GET";
		return execute(null, target, method);
	}

	protected JSONArray getJSONArray(String relative) {
		WebTarget target = getTarget(relative);
		String method = "GET";
		return executeAndReturnArray(null, target, method);
	}

	protected JSONArray executeAndReturnArray(JSONObject object,
			WebTarget target, String method) {
		Entity<String> entity2 = object == null ? null : Entity.entity(
				object.toJSONString(), MediaType.APPLICATION_JSON);
		Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
		if (actualToken != null) {
			request = request.header(authentificationHeader(), "Bearer " + actualToken);
		}
		Response post = request.method(method, entity2);
		String asString = post.readEntity(String.class);
		Object parse = JSONValue.parse(asString);
		if (parse instanceof JSONArray) {
			return (JSONArray) parse;
		}

		return null;
	}

	protected JSONObject getJSON(Response invoke) {
		String entity = invoke.readEntity(String.class);
		JSONObject obj = (JSONObject) JSONValue.parse(entity);
		return obj;
	}
}