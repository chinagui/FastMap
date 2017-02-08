package com.navinfo.dataservice.engine.meta.level;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;

import net.sf.json.JSONObject;

/**
 * @ClassName: LevelSelector
 * @author: zhangpengpeng
 * @date: 2017年2月6日
 * @Desc: level赋值
 */
public class LevelSelector {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	static Connection conn;

	public LevelSelector() {

	}

	public LevelSelector(Connection conn) {
		this.conn = conn;
	}

	public JSONObject getLevel(JSONObject jsonObj) throws Exception{
    	JSONObject result = new JSONObject();
    	
    	int dbId = jsonObj.getInt("dbId");
    	Connection regionConn = DBConnector.getInstance().getConnectionById(dbId);
    	
        PreparedStatement pstmt = null;
		ResultSet resultSet = null;
    	try{
		    int pid = jsonObj.getInt("pid");
		    String poi_num = jsonObj.getString("poi_num");
		    
		    // 3Dlandmark或者3Dicon poi,level赋值为A
		    boolean is3DPoiFlag = is3DPoi(regionConn, pid);
		    // 重要车场poi,level赋值为A
		    boolean isImpCarPoiFlag = false;
		    if (StringUtils.isNotEmpty(poi_num)){
		    	isImpCarPoiFlag	= isImpCarPoi(poi_num);
		    }
		    if (is3DPoiFlag || isImpCarPoiFlag){
		    	result.put("values", "A");
		    	result.put("defaultVal", "A");
		    	return result;
		    }
		    
	        String kindCode = jsonObj.getString("kindCode");
	        String chainCode = jsonObj.getString("chainCode");
	        String name = jsonObj.getString("name");
	        int poi_rating = jsonObj.getInt("rating");
	        String poi_level = jsonObj.getString("level");
	        
	        // 查询scPointCode2level
	        String sql = "select l.category,l.old_poi_level,l.new_poi_level,l.rating,l.chain from sc_point_code2level l where l.kind_code=:1";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, kindCode);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
	            int category = resultSet.getInt("category");
                String old_level = resultSet.getString("old_poi_level");
                String new_level = resultSet.getString("new_poi_level");
                int rating = resultSet.getInt("rating");
                String chain = resultSet.getString("chain");
                
                if ("200200".equals(kindCode) && StringUtils.isNotEmpty(name)){
                	// poi.name包含“自行车租赁点”(港澳数据name包含“自行車租賃點”)则POI.level=C
            		if (name.contains("自行车租赁点") || name.contains("自行車租賃點")){
        		    	result.put("values", "C");
        		    	result.put("defaultVal", "C");
        		    	return result;
            		}
            		//poi.name不包含“自行车租赁点”(港澳数据name不包含“自行車租賃點”)则POI.level= sc_point_code2level.old_poi_level
            		if (!name.contains("自行车租赁点") && !name.contains("自行車租賃點")){
            			if (StringUtils.isNotEmpty(poi_level) && StringUtils.isNotEmpty(old_level) && old_level.contains(poi_level)){
            		    	result.put("values", old_level);
            		    	result.put("defaultVal", poi_level);
            		    	return result;
            			} else{
            		    	result.put("values", old_level);
            		    	result.put("defaultVal", new_level);
            		    	return result;
            			}
            		}
                }
                
                if (1 == category && StringUtils.isNotEmpty(old_level)){
                	if (!old_level.contains("|")){
                		if ("A".equals(old_level)){
            		    	result.put("values", "A");
            		    	result.put("defaultVal", "A");
            		    	return result;
                		} else {
                			if (StringUtils.isNotEmpty(chainCode)){
                		    	result.put("values", "B1");
                		    	result.put("defaultVal", "B1");
                		    	return result;
                			} else{
                    			if (StringUtils.isNotEmpty(poi_level) && StringUtils.isNotEmpty(old_level) && old_level.contains(poi_level)){
                    		    	result.put("values", old_level);
                    		    	result.put("defaultVal", poi_level);
                    		    	return result;
                    			} else{
                    		    	result.put("values", old_level);
                    		    	result.put("defaultVal", new_level);
                    		    	return result;
                    			}
                			}
                		}
                	} else{
                		if (!old_level.contains("A")){
                			if (StringUtils.isNotEmpty(chainCode)){
                		    	result.put("values", "B1");
                		    	result.put("defaultVal", "B1");
                		    	return result;
                			} else{
                    			if (StringUtils.isNotEmpty(poi_level) && StringUtils.isNotEmpty(old_level) && old_level.contains(poi_level)){
                    		    	result.put("values", old_level);
                    		    	result.put("defaultVal", poi_level);
                    		    	return result;
                    			} else{
                    		    	result.put("values", old_level);
                    		    	result.put("defaultVal", new_level);
                    		    	return result;
                    			}
                			}
                		}
                	}
                }
                
                if (2 == category){
                	if (StringUtils.isNotEmpty(chainCode)){
                		if (chainCode.equals(chain)){
            		    	result.put("values", "A");
            		    	result.put("defaultVal", "A");
            		    	return result;
                		} else{
            		    	result.put("values", "B1");
            		    	result.put("defaultVal", "B1");
            		    	return result;
                		}
                	} else{
                		JSONObject noChainDict = new JSONObject();
                		JSONObject ChainDict = new JSONObject();
                		while(resultSet.next()){
                            String xOldLevel = resultSet.getString("old_poi_level");
                            String xNewLevel = resultSet.getString("new_poi_level");
                            String xChain = resultSet.getString("chain");
                            if (StringUtils.isEmpty(xChain)){
                            	noChainDict.put("old_level", xOldLevel);
                            	noChainDict.put("new_level", xNewLevel);
                            } else{
                            	if (ChainDict.isEmpty()){
                            		ChainDict.put("old_level", xOldLevel);
                            		ChainDict.put("new_level", xNewLevel);
                            	}
                            }
                		}
                		
                		if (noChainDict.isEmpty()){
                			if (StringUtils.isNotEmpty(poi_level) && StringUtils.isNotEmpty(ChainDict.getString("old_level")) && ChainDict.getString("old_level").contains(poi_level)){
                		    	result.put("values", ChainDict.getString("old_level"));
                		    	result.put("defaultVal", poi_level);
                		    	return result;
                			} else {
                		    	result.put("values", ChainDict.getString("old_level"));
                		    	result.put("defaultVal", ChainDict.getString("new_level"));
                		    	return result;
                			}
                		} else{
                			if (StringUtils.isNotEmpty(poi_level) && StringUtils.isNotEmpty(noChainDict.getString("old_level")) && noChainDict.getString("old_level").contains(poi_level)){
                		    	result.put("values", noChainDict.getString("old_level"));
                		    	result.put("defaultVal", poi_level);
                		    	return result;
                			} else{
                		    	result.put("values", noChainDict.getString("old_level"));
                		    	result.put("defaultVal", noChainDict.getString("new_level"));
                		    	return result;
                			}
                		}
                	}
                }
                
                if (3 == category){
                	if (poi_rating == rating){
            			if (StringUtils.isNotEmpty(poi_level) && StringUtils.isNotEmpty(old_level) && old_level.contains(poi_level)){
            		    	result.put("values", old_level);
            		    	result.put("defaultVal", poi_level);
            		    	return result;
            			} else{
            		    	result.put("values", old_level);
            		    	result.put("defaultVal", new_level);
            		    	return result;
            			}
                	}
                }
			}
	    	
	    	return result;
    	} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(regionConn);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(resultSet);
		}
    	
    }

	public static boolean is3DPoi(Connection regionConn, int pid) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("select i.poi_pid from ix_poi_icon i where i.poi_pid = :1");
		sb.append(" union ");
		sb.append("select c.poi_pid from cmg_building_poi c where c.poi_pid = :2");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = regionConn.prepareStatement(sb.toString());

			pstmt.setInt(1, pid);

			pstmt.setInt(2, pid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	public static boolean isImpCarPoi(String poi_num) throws Exception{
		String sql = "select f.poi_num from sc_point_focus f  where f.type=1 and f.poi_num=:1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, poi_num);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
