package com.cptingle.MCAdminConnector.web;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

public class WebInterface {

	private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

	public static void sendPost(String url, Map<Object, Object> parameters) throws Exception {
		HttpRequest request = HttpRequest.newBuilder().POST(buildFormDataFromMap(parameters)).uri(URI.create(url))
				.setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
				.header("Content-Type", "application/x-www-form-urlencoded").build();

		class SendWebRequest implements Runnable {
			HttpRequest request;

			SendWebRequest(HttpRequest request) {
				this.request = request;
			}

			public void run() {
				HttpResponse<String> response;
				try {
					response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
					
					//System.out.println(response.body());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		Executors.newCachedThreadPool().execute(new SendWebRequest(request));

	}

	private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
		var builder = new StringBuilder();
		for (Map.Entry<Object, Object> entry : data.entrySet()) {
			if (builder.length() > 0) {
				builder.append("&");
			}
			builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
			builder.append("=");
			builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
		}
		return HttpRequest.BodyPublishers.ofString(builder.toString());
	}

}
