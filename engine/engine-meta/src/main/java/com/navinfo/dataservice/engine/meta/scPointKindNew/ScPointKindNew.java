package com.navinfo.dataservice.engine.meta.scPointKindNew;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointKindNew {

	private Map<String, List<String>> chainKind8Map = new HashMap<String, List<String>>();

	private List<Map<String, String>> chainKind5Map = new ArrayList<Map<String, String>>();

	private List<Map<String, String>> chainKind6Map = new ArrayList<Map<String, String>>();

	private static class SingletonHolder {
		private static final ScPointKindNew INSTANCE = new ScPointKindNew();
	}

	public static final ScPointKindNew getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * SELECT R_KIND, POIKIND FROM SC_POINT_KIND_NEW WHERE TYPE=8
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, List<String>> scPointKindNewChainKind8Map() throws Exception {
		if (chainKind8Map == null || chainKind8Map.isEmpty()) {
			synchronized (this) {
				if (chainKind8Map == null || chainKind8Map.isEmpty()) {
					try {
						String sql = "SELECT R_KIND, POIKIND FROM SC_POINT_KIND_NEW WHERE TYPE=8";

						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								String chain = rs.getString("R_KIND");
								String kind = rs.getString("POIKIND");
								if (!chainKind8Map.containsKey(chain)) {
									chainKind8Map.put(chain, new ArrayList<String>());
								}
								chainKind8Map.get(chain).add(kind);
							}
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.close(conn);
						}
					} catch (Exception e) {
						throw new SQLException("加载SC_POINT_KIND_NEW失败：" + e.getMessage(), e);
					}
				}
			}
		}
		return chainKind8Map;
	}

	/**
	 * SELECT R_KIND, POIKIND FROM SC_POINT_KIND_NEW WHERE TYPE=5
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> scPointKindNewChainKind5Map() throws Exception {
		if (chainKind5Map == null || chainKind5Map.isEmpty()) {
			synchronized (this) {
				if (chainKind5Map == null || chainKind5Map.isEmpty()) {
					try {
						String sql = "SELECT R_KIND, POIKIND FROM SC_POINT_KIND_NEW WHERE TYPE=5";

						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								Map<String,String> map = new HashMap<String,String>();
								String chain = rs.getString("R_KIND");
								String kind = rs.getString("POIKIND");
								map.put(kind, chain);
								chainKind5Map.add(map);
							}
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.close(conn);
						}
					} catch (Exception e) {
						throw new SQLException("加载SC_POINT_KIND_NEW失败：" + e.getMessage(), e);
					}
				}
			}
		}
		return chainKind5Map;
	}
	
	/**
	 * SELECT R_KIND, POIKIND FROM SC_POINT_KIND_NEW WHERE TYPE=6
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> scPointKindNewChainKind6Map() throws Exception {
		if (chainKind6Map == null || chainKind6Map.isEmpty()) {
			synchronized (this) {
				if (chainKind6Map == null || chainKind6Map.isEmpty()) {
					try {
						String sql = "SELECT R_KIND, POIKIND FROM SC_POINT_KIND_NEW WHERE TYPE=6";

						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								Map<String,String> map = new HashMap<String,String>();
								String chain = rs.getString("R_KIND");
								String kind = rs.getString("POIKIND");
								map.put(kind, chain);
								chainKind6Map.add(map);
							}
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.close(conn);
						}
					} catch (Exception e) {
						throw new SQLException("加载SC_POINT_KIND_NEW失败：" + e.getMessage(), e);
					}
				}
			}
		}
		return chainKind6Map;
	}

}
