package com.navinfo.dataservice.commons.util;

public class ByteUtils {

	public static byte[][] toBytes(String[] strings) {
		byte[][] bytes = new byte[strings.length][];

		for (int i = 0; i < strings.length; i++) {
			bytes[i] = strings[i].getBytes();
		}

		return bytes;
	}

}
