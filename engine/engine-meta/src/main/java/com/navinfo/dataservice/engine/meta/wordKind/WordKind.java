package com.navinfo.dataservice.engine.meta.wordKind;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class WordKind {
	
	private static class SingletonHolder {
		private static final WordKind INSTANCE = new WordKind();
	}

	public static final WordKind getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public String getWordKind(String kindCode,String chain) throws Exception {
		String sql = "select word,chain,flag,kind_code,type from word_kind";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			JSONObject resut = new JSONObject();
			while (rs.next()) {
				JSONObject temp = new JSONObject();
				temp.put("type", rs.getInt("type"));
				temp.put("chain", rs.getString("chain"));
				temp.put("flag", rs.getString("flag"));
				temp.put("word", rs.getString("word"));
				String kind = rs.getString("kind_code");
				JSONArray kindListEach = new JSONArray();
				if (resut.containsKey(kind)) {
					kindListEach = resut.getJSONArray(kind);
				}
				kindListEach.add(temp);
				resut.put(kind, kindListEach);
			}
			if (!resut.containsKey(kindCode)) {
				return null;
			} else {
				if (chain == null || chain.isEmpty()) {
					chain = null;
				}
				JSONArray typeChainList = resut.getJSONArray(kindCode);
				int type = typeChainList.getJSONObject(0).getInt("type");
				if (type == 1 || type == 2 || type == 5 || type == 6) {
					return typeChainList.getJSONObject(0).getString("word");
				} else if (type == 3 || type == 7) {
					JSONObject noneChain = new JSONObject();
					for (int i=0;i<typeChainList.size();i++) {
						if (typeChainList.getJSONObject(i).getString("chain") == null || typeChainList.getJSONObject(i).getString("chain").isEmpty()) {
							noneChain = typeChainList.getJSONObject(i);
						}
						if (chain != null && typeChainList.getJSONObject(i).getString("chain").equals(chain)) {
							return typeChainList.getJSONObject(i).getString("word"); 
						}
					}
					if (noneChain != null && noneChain.containsKey("word")) {
						return noneChain.getString("word");
					}
				}
			}
			
			return null;
		} catch (Exception e) {
			throw e;
		}
	}
	
}
