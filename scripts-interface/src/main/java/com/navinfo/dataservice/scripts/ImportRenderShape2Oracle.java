package com.navinfo.dataservice.scripts;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.scripts.model.ShapeModel;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.io.WKTReader;

/** 
 * @ClassName: ImportRenderShape2Oracle
 * @author zl
 * @date 2017年6月28日
 * @Description: ImportCityBlockByJson.java
 */
public class ImportRenderShape2Oracle {
	private static QueryRunner runner = new QueryRunner();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("args.length : "+args.length);
		try {
			Long beginTime1 = System.currentTimeMillis(); 
			
			if(args==null||args.length < 2){
				System.out.println("ERROR:need args:shapeFile");
				return;
			}

			String shapeFilePath = args[0];
			//VECTOR_TAB
			//VECTOR_TAB_SUSPECT
			//MISS_ROAD_DIDI
			//MISS_ROAD_TENGXUN
			String tableName = args[1];
			int deleteFlag = 1;
			if(args.length == 3 && args[2] != null){
				deleteFlag = Integer.parseInt(args[2]);
			}
			/*int deleteFlag = 1;
			String shapeFilePath = "F:\\shapefile\\shpdir";
			String tableName = "VECTOR_TAB_SUSPECT";*/
			
			//先判断是否清除数据
			if(deleteFlag > 0){//当 deleteFlag > 0 则清除数据库表
				Long beginTime = System.currentTimeMillis(); 
				deleteData(tableName,deleteFlag);
				Long endTime = System.currentTimeMillis(); 
				System.out.println(tableName +" 表数据一清空!"+" 耗时："+(endTime-beginTime)/1000+" 秒.");
			}
			
			File shapeFile = new File(shapeFilePath);
			//判断是不是文件夹,如果是遍历文件夹下所有的文件
			if(shapeFile.isDirectory()){
				System.out.println("文件夹:" + shapeFile.getAbsolutePath());
				File[] files = shapeFile.listFiles(); // 该文件目录下文件全部放入数组
				if (files != null) {
		            for (int i = 0; i < files.length; i++) {
		                String fileName = files[i].getName();
		                System.out.println("当前文件: "+fileName);
		                if (files[i].isDirectory()) { // 判断是文件还是文件夹
		                	File[] filefiles = files[i].listFiles(); // 该文件目录下文件全部放入数组
		                	if (filefiles != null) {
		                        for (int j = 0; j < filefiles.length; j++) {
		                            String filefileName = filefiles[j].getName();
		                            if (filefiles[j].isDirectory()) { // 判断是文件还是文件夹
		                            	System.out.println("只支持到二级目录,请重新检查此文件夹: "+filefiles[j].getAbsolutePath());
		                            	continue;
		                            } else if (filefileName.endsWith(".shp")) { // 判断文件名是否以.shp结尾
		                               System.out.println("正在读取文件: "+filefiles[j].getAbsolutePath());
//		                               imp(filefiles[j],tableName);
		                               impBatch(filefiles[j],tableName);
		                            } else {
		                                continue;
		                            }
		                        }
		                    }
		                } else if (fileName.endsWith(".shp")) { // 判断文件名是否以.shp结尾
		                	 System.out.println("正在读取文件: "+files[i].getAbsolutePath());
//                           imp(files[i],tableName);
		                	 impBatch(files[i],tableName);
		                } else {
		                    continue;
		                }
		            }
		        }
			}else{
				System.out.println("正在读取文件: "+shapeFile.getAbsolutePath());
//				imp(shapeFile,tableName);
				impBatch(shapeFile,tableName);
			}
			Long endTime1 = System.currentTimeMillis(); 
			System.out.println(" 总共耗时："+(endTime1-beginTime1)/1000+" 秒. "+"Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}
	//*****************************
	public static void deleteData(String tableName, int deleteFlag) throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		try{

//			WKTReader r = new WKTReader();
			DbInfo manInfo = DbService.getInstance().getOnlyDbByBizType("fmRender");

			OracleSchema manSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(manInfo.getConnectParam()));
			conn = manSchema.getPoolDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			
				String deleteSql = " delete "+tableName+" ";
				run.execute(conn, deleteSql);
				conn.commit();
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	//***** 批处理 插入***********
	public static void impBatch(File shapeFile, String tableName)throws Exception{
		Connection conn = null;
		PreparedStatement pst = null;
		try{

			WKTReader r = new WKTReader();
			DbInfo manInfo = DbService.getInstance().getOnlyDbByBizType("fmRender");

			OracleSchema manSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(manInfo.getConnectParam()));
			conn = manSchema.getPoolDataSource().getConnection();
			
			List<ShapeModel> vtsList = parseShape(shapeFile);
			
			int count = 0;
			int commitCount = 1000;
			if(vtsList != null && vtsList.size() >0 ){
				//判断上传文件的数量级
				if(vtsList.size() < 50000){
					commitCount = 500;
				}
				
			   String insCitySql = "INSERT INTO "+tableName+" (RID,GEOMETRY) VALUES ("+tableName+"_SEQ.NEXTVAL,SDO_GEOMETRY(?,8307))";
			   conn.setAutoCommit(false);      
			   Long beginTime = System.currentTimeMillis(); 
			   //构造预处理statement     
			   pst = conn.prepareStatement(insCitySql);    
			   
			   
				for(ShapeModel vts:vtsList){
					count++;
					String geo = vts.getGeometry();
					if(geo != null ){
						Geometry geo_mls = r.read(geo);
//				        System.out.println(geo_mls.getGeometryType());
				        if(geo_mls instanceof MultiLineString){
//				        	LineString[] lineStrings = new LineString[geo_mls.getNumGeometries()];	
				        	for (int k = 0; k < geo_mls.getNumGeometries(); k++) {
				    			LineString ls = (LineString) geo_mls.getGeometryN(k);
//				    			System.out.println(ls);
				    			Clob clob = ConnectionUtil.createClob(conn);
								clob.setString(1, ls.toString());
								
								pst.setClob(1, clob); 
				    		}
				        }else if(geo_mls instanceof LineString){
				        	Clob clob = ConnectionUtil.createClob(conn);
							clob.setString(1, geo_mls.toString());
							
							pst.setClob(1, clob); 
				        }
//						System.out.println(count);
					}
					//添加到批处理
				    pst.addBatch();
				    
					if(count%commitCount == 0){//每1000次提交一次     
						pst.executeBatch();      
					    conn.commit();      
					    pst.clearBatch(); 
						System.out.println(shapeFile.getAbsolutePath() +"已导入数据数量为  count: "+count);
					}
				}
				  pst.executeBatch();
				  conn.commit(); 
				  Long endTime = System.currentTimeMillis();      
				  
				  pst.close();      
				  conn.close();     
				System.out.println(shapeFile.getAbsolutePath() +" 总共导入数据: "+count+" 条.");
				System.out.println(" 耗时："+(endTime-beginTime)/1000+" 秒."); 
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.closeQuietly(pst);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	
	public static void imp(File shapeFile, String tableName)throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		try{

			WKTReader r = new WKTReader();
			DbInfo manInfo = DbService.getInstance().getOnlyDbByBizType("fmRender");

			OracleSchema manSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(manInfo.getConnectParam()));
			conn = manSchema.getPoolDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			List<ShapeModel> vtsList = parseShape(shapeFile);
			
			int count = 0;
			if(vtsList != null && vtsList.size() >0 ){
				String insCitySql = "INSERT INTO "+tableName+" (RID,GEOMETRY) VALUES ("+tableName+"_SEQ.NEXTVAL,SDO_GEOMETRY(?,8307))";
				
				for(ShapeModel vts:vtsList){
					
					String geo = vts.getGeometry();
					if(geo != null ){
						Geometry geo_mls = r.read(geo);
//				        System.out.println(geo_mls.getGeometryType());
				        if(geo_mls instanceof MultiLineString){
//				        	LineString[] lineStrings = new LineString[geo_mls.getNumGeometries()];	
				        	for (int k = 0; k < geo_mls.getNumGeometries(); k++) {
				    			LineString ls = (LineString) geo_mls.getGeometryN(k);
//				    			System.out.println(ls);
				    			Clob clob = ConnectionUtil.createClob(conn);
								clob.setString(1, ls.toString());
								run.update(conn, insCitySql,clob);
								conn.commit();
								count++;
				    		}
				        }else if(geo_mls instanceof LineString){
				        	Clob clob = ConnectionUtil.createClob(conn);
							clob.setString(1, geo_mls.toString());
							run.update(conn, insCitySql,clob);
							conn.commit();
							count++;
				        }
//						System.out.println(count);
					}
					/*if(count > 1000){
						System.out.println(shapeFile.getAbsolutePath() +"  count: "+count);
						break;
					}*/
				}
				System.out.println(shapeFile.getAbsolutePath() +"  count: "+count);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	public static List<ShapeModel> parseShape(File shapeFile)throws Exception{
		List<ShapeModel> vtsList =null;
		 
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();  
        try {  
        	vtsList = new ArrayList<ShapeModel>();
        	
            ShapefileDataStore sds = (ShapefileDataStore)dataStoreFactory.createDataStore(shapeFile.toURI().toURL());  
            sds.setCharset(Charset.forName("GBK"));  
            SimpleFeatureSource featureSource = sds.getFeatureSource();  
            SimpleFeatureIterator itertor = featureSource.getFeatures().features();  
  
            while(itertor.hasNext()) {  
            	ShapeModel vts = new ShapeModel();
                SimpleFeature feature = itertor.next();  
//                System.out.println("geometry: "+feature.getDefaultGeometryProperty().getValue());
//                System.out.println(feature.getAttribute("road_ID"));  
                /*long idl = (long) feature.getAttribute("road_ID");
                vts.setId( Integer.parseInt(String.valueOf(idl)) );*/
                String geo = feature.getDefaultGeometryProperty().getValue().toString();
                vts.setGeometry(geo);
                vtsList.add(vts);
                
            }    
                 
            itertor.close(); 
            sds.dispose();
                
        } catch (Exception e) {  
            e.printStackTrace();  
        }finally {
        	 
		}
        System.out.println("shp 文件条数: "+vtsList.size());
        return vtsList;
	}
	
	//******************************
	private static int getId(Connection conn,String tableName)throws SQLException{
		String sql = "SELECT "+tableName+"_SEQ.NEXTVAL FROM DUAL";
		return runner.queryForInt(conn, sql);
	}
	
	
	public static void imp(String shapeFile, String tableName, int deleteFlag)throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		try{

			WKTReader r = new WKTReader();
			DbInfo manInfo = DbService.getInstance().getOnlyDbByBizType("fmRender");

			OracleSchema manSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(manInfo.getConnectParam()));
			conn = manSchema.getPoolDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			List<ShapeModel> vtsList = parseShape(shapeFile);
			/*List<VectorTabSuspect> vtsList = new ArrayList<VectorTabSuspect>();
			VectorTabSuspect  vts1 = new VectorTabSuspect();
				vts1.setId(1);
				vts1.setGeometry("MULTILINESTRING ((108.00903921127319 35.038537865448, 108.00880317687988 35.038387661743165, 108.00777320861816 35.03810871200562, 108.00755863189697 35.0379799659729, 108.00727968215942 35.03795850830078, 108.00584201812744 35.03755081253052, 108.00436143875122 35.03703582839966, 108.0024302482605 35.03656375961304, 107.9953706741333 35.034375077056886, 107.99217348098755 35.03347385482788, 107.99137954711914 35.033173447418214, 107.99028520584106 35.032915955352784, 107.98833255767822 35.03229368286133, 107.98753862380981 35.03197181777954, 107.98732404708862 35.03195036010742, 107.9861867904663 35.03145683364868, 107.98453454971313 35.03111351089478, 107.98157339096069 35.03074873046875, 107.97977094650268 35.030426865386964, 107.97882680892944 35.03034103469849, 107.97781829833984 35.030147915649415, 107.97734622955322 35.030147915649415, 107.97623043060302 35.02995479660034, 107.97623043060302 35.02995479660034))");
			vtsList.add(vts1);*/
			int count = 0;
			if(vtsList != null && vtsList.size() >0 ){
				//write vtsList
				String insCitySql = "INSERT INTO "+tableName+" (RID,GEOMETRY) VALUES (?,SDO_GEOMETRY(?,8307))";
				
				if(deleteFlag > 0){//当 deleteFlag > 0 则清除数据库表
					String deleteSql = " delete "+tableName+" ";
					run.execute(conn, deleteSql);
					conn.commit();
				}
				
				for(ShapeModel vts:vtsList){
//					int id = vts.getId();
					int id = getId(conn,tableName);
					
					String geo = vts.getGeometry();
					if(geo != null ){
						Geometry geo_mls = r.read(geo);
//				        System.out.println(geo_mls.getGeometryType());
				        if(geo_mls instanceof MultiLineString){
//				        	LineString[] lineStrings = new LineString[geo_mls.getNumGeometries()];	
				        	for (int k = 0; k < geo_mls.getNumGeometries(); k++) {
				    			LineString ls = (LineString) geo_mls.getGeometryN(k);
//				    			System.out.println(ls);
				    			Clob clob = ConnectionUtil.createClob(conn);
								clob.setString(1, ls.toString());
								run.update(conn, insCitySql,id,clob);
								conn.commit();
								count++;
				    		}
				        }else if(geo_mls instanceof LineString){
				        	Clob clob = ConnectionUtil.createClob(conn);
							clob.setString(1, geo_mls.toString());
							run.update(conn, insCitySql,id,clob);
							conn.commit();
							count++;
				        }
						
//						System.out.println(count);
					}
					
					/*if(count > 1000){
						break;
					}*/
				}
				System.out.println("count: "+count);
			}
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public static List<ShapeModel> parseShape(String shapeFile)throws Exception{
		List<ShapeModel> vtsList =null;
		 
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();  
        try {  
        	vtsList = new ArrayList<ShapeModel>();
        	
            ShapefileDataStore sds = (ShapefileDataStore)dataStoreFactory.createDataStore(new File(shapeFile).toURI().toURL());  
            sds.setCharset(Charset.forName("GBK"));  
            SimpleFeatureSource featureSource = sds.getFeatureSource();  
            SimpleFeatureIterator itertor = featureSource.getFeatures().features();  
  
            while(itertor.hasNext()) {  
            	ShapeModel vts = new ShapeModel();
                SimpleFeature feature = itertor.next();  
//                System.out.println("geometry: "+feature.getDefaultGeometryProperty().getValue());
//                System.out.println(feature.getAttribute("road_ID"));  
                /*long idl = (long) feature.getAttribute("road_ID");
                vts.setId( Integer.parseInt(String.valueOf(idl)) );*/
                String geo = feature.getDefaultGeometryProperty().getValue().toString();
                vts.setGeometry(geo);
                vtsList.add(vts);
                
            }    
                 
            itertor.close(); 
            sds.dispose();
                
        } catch (Exception e) {  
            e.printStackTrace();  
        }finally {
        	 
		}
        System.out.println("shp 文件条数: "+vtsList.size());
        return vtsList;
	}

}
