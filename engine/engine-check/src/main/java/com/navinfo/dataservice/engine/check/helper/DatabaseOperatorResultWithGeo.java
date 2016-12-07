package com.navinfo.dataservice.engine.check.helper;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/** 
 * @ClassName: DatabaseOperatorResultWithGeo
 * @author songdongyan
 * @date 2016年12月6日
 * @Description: 继承自DatabaseOperator，重写settleResultSet
 * 使exeSelect返回的内容包含[geometry,targets,meshid]
 * 只返回一条记录
 */
public class DatabaseOperatorResultWithGeo extends DatabaseOperator{

	/**
	 * 
	 */
	public DatabaseOperatorResultWithGeo() {
		// TODO Auto-generated constructor stub
	}

	//pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId()
	public List<Object> settleResultSet(ResultSet resultSet) throws Exception{
		List<Object> resultList=new ArrayList<Object>();
		if (resultSet.next()){
			String pointWkt ="";
			try{
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);			
				Geometry pointGeo=GeoHelper.getPointFromGeo(geometry);
				pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
			}catch(Exception e){}
			
			String targets=resultSet.getString(2);
			int meshId=resultSet.getInt(3);
			
			resultList.add(pointWkt);
			resultList.add(targets);
			resultList.add(meshId);
		} 
		return resultList;
	}
}
