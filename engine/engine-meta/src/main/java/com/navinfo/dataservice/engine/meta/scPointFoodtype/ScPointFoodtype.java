package com.navinfo.dataservice.engine.meta.scPointFoodtype;

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

public class ScPointFoodtype {
	
	private List<String> kinds= new ArrayList<String>();
	private List<String> foodType110302s= new ArrayList<String>();
	private Map<String, String> drinkMap=new HashMap<String, String>();

	private static class SingletonHolder {
		private static final ScPointFoodtype INSTANCE = new ScPointFoodtype();
	}

	public static final ScPointFoodtype getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * select poiKind from SC_POINT_FOODTYP
	 * @return List<String> SC_POINT_FOODTYP的poikind列表
	 * @throws Exception
	 */
	public List<String> scPointFoodtypeKindList() throws Exception{
		if (kinds==null||kinds.isEmpty()) {
				synchronized (this) {
					if (kinds==null||kinds.isEmpty()) {
						try {
							String sql = "select poiKind from SC_POINT_FOODTYPE";								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									kinds.add(rs.getString("poiKind"));			
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
								DbUtils.close(rs);
								DbUtils.close(pstmt);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_POINT_FOODTYPE失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return kinds;
	}
	
	/**
	 * SELECT foodtype FROM SC_POINT_FOODTYPE WHERE POIKIND='110302'
	 * @return List<String> foodtype列表,即SC_POINT_FOODTYP的poikind=110302的foodtype列表
	 * @throws Exception
	 */
	public List<String> scPointFoodtype110302FoodTypes() throws Exception{
		if (foodType110302s==null||foodType110302s.isEmpty()) {
				synchronized (this) {
					if (foodType110302s==null||foodType110302s.isEmpty()) {
						try {
							String sql = "SELECT foodtype FROM SC_POINT_FOODTYPE WHERE POIKIND='110302'";								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									foodType110302s.add(rs.getString("foodtype"));			
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
								DbUtils.close(rs);
								DbUtils.close(pstmt);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_POINT_FOODTYPE失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return foodType110302s;
	}
	
	/**
	 * SELECT poikind,foodtype FROM SC_POINT_FOODTYPE WHERE MEMO='饮品'
	 * @return  Map<String, String> SC_POINT_FOODTYP的饮品的对应表：key：foodtype value:kind
	 * @throws Exception
	 */
	public Map<String, String> scPointFoodtypeDrinkMap() throws Exception{
		if (drinkMap==null||drinkMap.isEmpty()) {
				synchronized (this) {
					if (drinkMap==null||drinkMap.isEmpty()) {
						try {
							String sql = "SELECT poikind,foodtype FROM SC_POINT_FOODTYPE WHERE MEMO='饮品'";								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									drinkMap.put(rs.getString("foodtype"),rs.getString("poikind"));
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
								DbUtils.close(rs);
								DbUtils.close(pstmt);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_POINT_FOODTYPE失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return drinkMap;
	}
}
