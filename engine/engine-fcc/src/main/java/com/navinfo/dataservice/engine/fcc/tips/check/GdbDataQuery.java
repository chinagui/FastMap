package com.navinfo.dataservice.engine.fcc.tips.check;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: GdbDataQuery.java
 * @author y
 * @date 2017-8-14 下午5:13:23
 * @Description: TODO
 *  
 */
public class GdbDataQuery {
	
	private  Connection conn=null;

	/**
	 * @param conn
	 */
	public GdbDataQuery(Connection conn) {
		super();
		this.conn = conn;
	}
	
	

	/**
	 * @Description:查询gdb中的几何
	 * @param tableName：要查询的表名
	 * @param pidList：pid列表
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-8-14 下午5:17:23
	 */
    public  List<Geometry> queryLineGeometry(String tableName,String pids) throws Exception{
    	List<Geometry> geoList=new ArrayList<Geometry>();
        String sqlLink = "SELECT K.GEOMETRY GEOMETRY  FROM "+tableName+" K WHERE   K.LINK_PID IN(?)";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sqlLink);
            
            Clob  pidClob=ConnectionUtil.createClob(conn,pids);
            pstmt.setClob(1, pidClob);
            rs = pstmt.executeQuery();
            if (rs.next()){//有记录
                STRUCT geomtry = (STRUCT) rs.getObject("GEOMETRY");
                Geometry geo=GeoTranslator.struct2Jts(geomtry);
                geoList.add(geo);
            }
        } catch (SQLException e) {
           throw e;
        }finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(pstmt);
        }
        return geoList;
    }
	
	
	
	
	
	
	
	
	

}
