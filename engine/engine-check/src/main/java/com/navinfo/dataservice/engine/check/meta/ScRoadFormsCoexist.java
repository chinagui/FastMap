package com.navinfo.dataservice.engine.check.meta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;



public class ScRoadFormsCoexist{
	
	public static Map<Integer,Map<String,List<Integer>>> formMap=new HashMap<Integer,Map<String,List<Integer>>>();

	public ScRoadFormsCoexist() {
		// TODO Auto-generated constructor stub
	}
	
	public static Map<Integer,Map<String,List<Integer>>> formsCoexistLoader() throws Exception{
		if(!formMap.isEmpty()){return formMap;}
		synchronized(ScRoadFormsCoexist.class){
			if(!formMap.isEmpty()){return formMap;}
			Connection conn = null;
			try{
				conn=DBConnector.getInstance().getMetaConnection();
				String sql = "SELECT MAIN_FORM_OF_WAY,FORM_OF_WAYS,MULTI_DIGITIZED,IMI_CODE,SPECIAL_TRAFFIC,IS_VIADUCT"
						+ " FROM SC_ROAD_FORMS_COEXIST";
				QueryRunner runner = new QueryRunner();
				formMap=runner.query(conn, sql, new ResultSetHandler<Map<Integer,Map<String,List<Integer>>>>() {
					Map<Integer,Map<String,List<Integer>>> formMap = new HashMap<Integer,Map<String,List<Integer>>>();
	
					@Override
					public Map<Integer,Map<String,List<Integer>>> handle(ResultSet rs) throws SQLException {
						while (rs.next()) {
							int mainFormOfWay=rs.getInt("MAIN_FORM_OF_WAY");
							String formOfWays=rs.getString("FORM_OF_WAYS");
							List<String> formlistTmp=java.util.Arrays.asList(formOfWays.split(","));
							List<Integer> formlist=new ArrayList<Integer>();
							for(int i=0;i<formlistTmp.size();i++){
								//System.out.println(formlistTmp.get(i));
								formlist.add(Integer.parseInt(formlistTmp.get(i)));
							   } 
							
							int multiDigitized=rs.getInt("MULTI_DIGITIZED");
							List<Integer> multiDigitizedlist=new ArrayList<Integer>();
							multiDigitizedlist.add(multiDigitized);
							
							String imiCode=rs.getString("IMI_CODE");
							List<String> imiCodeTmp=java.util.Arrays.asList(imiCode.split(","));
							List<Integer> imiCodelist=new ArrayList<Integer>();
							for(int i=0;i<imiCodeTmp.size();i++){
								imiCodelist.add(Integer.parseInt(imiCodeTmp.get(i)));
							   }
							
							int specialTraffic=rs.getInt("SPECIAL_TRAFFIC");
							List<Integer> specialTrafficlist=new ArrayList<Integer>();
							specialTrafficlist.add(specialTraffic);
							
							int isViaduct=rs.getInt("IS_VIADUCT");
							List<Integer> isViaductlist=new ArrayList<Integer>();
							isViaductlist.add(isViaduct);
							
							Map<String,List<Integer>> formMapElement = new HashMap<String,List<Integer>>();
							formMapElement.put("FORM_OF_WAYS",formlist);
							formMapElement.put("MULTI_DIGITIZED",multiDigitizedlist);
							formMapElement.put("IMI_CODE",imiCodelist);
							formMapElement.put("SPECIAL_TRAFFIC",specialTrafficlist);
							formMapElement.put("IS_VIADUCT",isViaductlist);
	
							formMap.put(mainFormOfWay, formMapElement);
							}
						return formMap;
						}
					});	
				}
			finally{
				if (conn != null) {
					try {conn.close();} catch (Exception e) {}
					}
				}
			}
		return formMap;		
	}
	
	public static void main(String[] args) throws Exception{
		String formOfWays="1,2,13,45";
		List<String> formList=java.util.Arrays.asList(formOfWays);
		
		String variables="RDLINK,RDNODE";
		List<String> variableTmp=new ArrayList<String>();
		variableTmp.addAll(java.util.Arrays.asList(variables.split(",")));
		for(int i=0;i<variableTmp.size();i++){
			System.out.println(variableTmp.get(i));}
		}
}
