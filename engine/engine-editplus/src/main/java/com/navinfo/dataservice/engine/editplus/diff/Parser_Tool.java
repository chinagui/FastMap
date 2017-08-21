package com.navinfo.dataservice.engine.editplus.diff;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * @author jicaihua
 * BaiduGeocoding API 
 */
public class Parser_Tool {

	public static DefaultHttpClient httpclient = new DefaultHttpClient();

	// /////////////////////////////http 请求/////////////////////////////////////
	/**
	 * post 获取 rest 资源
	 * 
	 * @param url
	 * @param name_value_pair
	 * @return
	 * @throws IOException
	 */
	public static String do_post(String url, List<NameValuePair> name_value_pair) throws IOException {
		String body = "{UTF-8}";
		HttpPost httpost = new HttpPost(url);
		UrlEncodedFormEntity i = new UrlEncodedFormEntity(name_value_pair, body);
		httpost.setEntity(i);
		HttpResponse response = httpclient.execute(httpost);
		HttpEntity entity = response.getEntity();
		body = EntityUtils.toString(entity);
		return body;
	}

	/**
	 * get 获取 rest 资源
	 * 
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String do_get(String url) throws ClientProtocolException, IOException {

		String body = "{}";
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpget);

		HttpEntity entity = response.getEntity();

		body = EntityUtils.toString(entity);
		return body;
	}

	public static String doGet(String strUrl) throws Exception {

		URL url = new URL(strUrl);
		URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
		HttpClient client = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(uri);
		HttpResponse response = client.execute(httpget);
		HttpEntity entity = response.getEntity();
		String body = EntityUtils.toString(entity);
		return body;
	}

	/**
	 * get 获取 rest 资源
	 * 
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String do_get2(String url) throws ClientProtocolException, IOException {
		HttpClient httpclient2 = new DefaultHttpClient();

		String body = "{}";
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpclient2.execute(httpget);

		HttpEntity entity = response.getEntity();

		body = EntityUtils.toString(entity);
		httpclient2.getConnectionManager().shutdown();
		return body;
	}
}
