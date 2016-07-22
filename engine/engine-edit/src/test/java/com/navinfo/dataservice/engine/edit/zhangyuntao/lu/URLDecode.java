package com.navinfo.dataservice.engine.edit.zhangyuntao.lu;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Scanner;

public class URLDecode {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("请粘贴URL地址并按下Enter:");
		String url = sc.nextLine();
		if (null != url) {
			String regex = "parameter=";
			url = url.substring(url.indexOf(regex) + regex.length());
			try {
				url = URLDecoder.decode(url, "UTF-8");
				url = url.replaceAll("\"", "\'").replaceAll("'dbId':42", "'dbId':43").replaceAll("RDNODE", "LUNODE").replaceAll("RDLINK", "LULINK").replaceAll("RDFACE", "LUFACE");
				System.out.println(url);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} finally {
				sc.close();
			}
		}
	}

}
