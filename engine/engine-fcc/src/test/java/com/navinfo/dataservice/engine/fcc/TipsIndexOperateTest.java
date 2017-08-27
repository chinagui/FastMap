package com.navinfo.dataservice.engine.fcc;
 
import java.sql.Connection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.nirobot.common.utils.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: TipsIndexOperateTest.java
 * @author y
 * @date 2017-8-2 上午9:42:01
 * @Description: TODO
 *  
 */
public class TipsIndexOperateTest extends InitApplication{
	
	
	@Override
	@Before
	public void init() {
		initContext();
	}


    
    @Test
    public void testUpdate() throws Exception {

      	Connection conn = null;
    	
    	String rowkey="022001A166B7F75D7B495C9D667A0BB706B355";
		
 		try 
 		{ 
    		conn = DBConnector.getInstance().getTipsIdxConnection();
    		
    		TipsIndexOracleOperator r=new TipsIndexOracleOperator(conn);
    		
    		
        	TipsDao dao=r.getById(rowkey);
        	
        	
        	System.out.println(dao.getDeep());
        	
        	System.out.println(dao.getRelate_links());
        	
        	System.out.println(dao.getWkt());
        	
        	System.out.println(dao.getWktLocation());
        	
 /*       	dao.setStage(2);
        	
        	r.updateOne(dao);*/
        	
        	System.out.println("修改成功");
 		}catch (Exception e) {
			e.printStackTrace();
			DbUtils.rollback(conn);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
    }
    
    
    
    
    @Test
    public void testGetByRowkey() throws Exception {

      	Connection conn = null;
    	
    	String rowkey="021116bd25ecc165f84a0fa5506674d1c7716f";
		
 		try 
 		{ 
    		conn = DBConnector.getInstance().getTipsIdxConnection();
    		
    		TipsIndexOracleOperator r=new TipsIndexOracleOperator(conn);
    		
    		
        	TipsDao dao=r.getById(rowkey);
        	
        	
        	System.out.println(dao.getDeep());
        	
        	JSONObject deep=JSONObject.fromObject(dao.getDeep());
        	
        	JSONArray f_array = deep.getJSONArray("f_array");
        	for (Object object : f_array) {
				JSONObject fInfo = JSONObject.fromObject(object); // 是个对象
				// 关联link是测线的
	            if(fInfo != null && fInfo.containsKey("type")) {
	                int type = fInfo.getInt("type");
	                String id = fInfo.getString("id");
	                JSONObject  geo=fInfo.getJSONObject("geo");
	                
	                Geometry lineGeomtry = (Geometry) GeoTranslator.geojson2Jts(geo);
	                
	                double len =GeometryUtils.getLinkLength(lineGeomtry);
	                
	                System.out.println("长度："+len);
	                
	            }
			}
        	

        	
        	System.out.println("修改成功");
 		}catch (Exception e) {
			e.printStackTrace();
			DbUtils.rollback(conn);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
    }
    
    
    
	
	

}