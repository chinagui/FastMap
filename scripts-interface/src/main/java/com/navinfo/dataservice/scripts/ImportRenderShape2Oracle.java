package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.scripts.model.Block4Imp;
import com.navinfo.dataservice.scripts.model.City4Imp;
import com.navinfo.dataservice.scripts.model.VectorTabSuspect;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.navicommons.database.sql.SqlExec;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

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

		try {
			/*if(args==null||args.length!=1){
				System.out.println("ERROR:need args:shapeFile");
				return;
			}*/

//			String shapeFile = args[0];
			String shapeFile = "F:\\shapefile\\road_out.shp";

			imp(shapeFile);
			
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}
	
	public static void imp(String shapeFile)throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		try{

			DbInfo manInfo = DbService.getInstance().getOnlyDbByBizType("fmRender");

			OracleSchema manSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(manInfo.getConnectParam()));
			conn = manSchema.getPoolDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			List<VectorTabSuspect> vtsList = parseShape(shapeFile);
			if(vtsList != null && vtsList.size() >0){
				//write vtsList
				String insCitySql = "INSERT INTO vector_tab_suspect (ID,GEOMETRY) VALUES (?,SDO_GEOMETRY(?,8307))";
				String deleteSql = " delete vector_tab_suspect ";
				run.execute(conn, deleteSql);
				for(VectorTabSuspect vts:vtsList){
					int id = vts.getId();
					String geo = vts.getGeometry();
					Clob clob = ConnectionUtil.createClob(conn);
					clob.setString(1, geo);
					run.update(conn, insCitySql,id,clob);
				}
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
	
	public static List<VectorTabSuspect> parseShape(String shapeFile)throws Exception{
		List<VectorTabSuspect> vtsList =null;
		 
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();  
        try {  
        	vtsList = new ArrayList<VectorTabSuspect>();
        	
            ShapefileDataStore sds = (ShapefileDataStore)dataStoreFactory.createDataStore(new File("F:\\shapefile\\road_out.shp").toURI().toURL());  
            sds.setCharset(Charset.forName("GBK"));  
            SimpleFeatureSource featureSource = sds.getFeatureSource();  
            SimpleFeatureIterator itertor = featureSource.getFeatures().features();  
  
            while(itertor.hasNext()) {  
            	VectorTabSuspect vts = new VectorTabSuspect();
                SimpleFeature feature = itertor.next();  
                System.out.println("geometry: "+feature.getDefaultGeometryProperty().getValue());
                System.out.println(feature.getAttribute("road_ID"));  
                long idl = (long) feature.getAttribute("road_ID");
                vts.setId( Integer.parseInt(String.valueOf(idl)) );
                vtsList.add(vts);
                
            }    
                 
            itertor.close(); 
                
        } catch (Exception e) {  
            e.printStackTrace();  
        }finally {
        	 
		}
        System.out.println("shp 文件条数: "+vtsList.size());
        return vtsList;
	}

}
