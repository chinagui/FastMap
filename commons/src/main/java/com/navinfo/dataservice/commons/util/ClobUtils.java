package com.navinfo.dataservice.commons.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Date;

public class ClobUtils {
	/**
	 * 
	 * @param date
	 * @return default format:"yyyy-MM-dd HH:mm:ss"; if date == null then return
	 *         null;
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static String clobToString(Clob par) throws SQLException, IOException {
		if (par == null) {
			return "";
		}
		Reader is = par.getCharacterStream();// 得到流
		BufferedReader br = new BufferedReader(is);
		String s = br.readLine();
		StringBuffer sb = new StringBuffer();
		while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
			sb.append(s);
			s = br.readLine();
		}
		String result = sb.toString();
		return result;
	}
}
