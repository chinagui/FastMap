package com.navinfo.dataservice.commons.util;

import java.io.UnsupportedEncodingException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/*
 * @author MaYunFei
 * 2016年6月14日
 * 描述：commonsBase64Utils.java
 */
public class Base64Utils {
	// 加密
	public static String encrypt(String str) {
		byte[] b = null;
		String s = null;
		try {
			b = str.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (b != null) {
			s = new BASE64Encoder().encode(b);
		}
		return s;
	}

	// 解密
	public static String decrypt(String s) {
		byte[] b = null;
		String result = null;
		if (s != null) {
			BASE64Decoder decoder = new BASE64Decoder();
			try {
				b = decoder.decodeBuffer(s);
				result = new String(b, "utf-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	public static void main(String[] args){
		String pwd="123456";
		String encrypt = Base64Utils.encrypt(pwd);
		System.out.println(encrypt);
		System.out.println(Base64Utils.decrypt(encrypt));
	}
}
