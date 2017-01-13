package com.navinfo.dataservice.engine.check.helper;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * @ClassName DatabaseOperatorResult
 * @author Han Shaoming
 * @date 2016年12月21日 下午2:40:51
 * @Description TODO
 * 继承自DatabaseOperator，重写settleResultSet
 * 使exeSelect返回的内容包含[geometry,target,meshId,log]
 * 只返回一条记录
 */
public class DatabaseOperatorResult extends DatabaseOperator {

	public DatabaseOperatorResult() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public List<Object> settleResultSet(ResultSet resultSet) throws Exception{
		List<Object> resultList=new ArrayList<Object>();
		while (resultSet.next()){
			String pointWkt ="";
			try{
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);			
				Geometry pointGeo=GeoHelper.getPointFromGeo(geometry);
				pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
			}catch(Exception e){}
			
			String target=resultSet.getString(2);
			int meshId=resultSet.getInt(3);
			String log = resultSet.getString(4);
			
			resultList.add(pointWkt);
			resultList.add(target);
			resultList.add(meshId);
			resultList.add(log);
		} 
		return resultList;
	}
}
