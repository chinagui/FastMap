package com.navinfo.dataservice.scripts;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import com.navinfo.dataservice.scripts.model.VectorTabSuspect;
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if(args==null||args.length!=1){
				System.out.println("ERROR:need args:shapeFile");
				return;
			}

			String shapeFile = args[0];
//			String shapeFile = "F:\\shapefile\\leshan\\road_out.shp";

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

			WKTReader r = new WKTReader();
			DbInfo manInfo = DbService.getInstance().getOnlyDbByBizType("fmRender");

			OracleSchema manSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(manInfo.getConnectParam()));
			conn = manSchema.getPoolDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			List<VectorTabSuspect> vtsList = parseShape(shapeFile);
			/*List<VectorTabSuspect> vtsList = new ArrayList<VectorTabSuspect>();
			VectorTabSuspect  vts1 = new VectorTabSuspect();
				vts1.setId(1);
				vts1.setGeometry("MULTILINESTRING ((108.00903921127319 35.038537865448, 108.00880317687988 35.038387661743165, 108.00777320861816 35.03810871200562, 108.00755863189697 35.0379799659729, 108.00727968215942 35.03795850830078, 108.00584201812744 35.03755081253052, 108.00436143875122 35.03703582839966, 108.0024302482605 35.03656375961304, 107.9953706741333 35.034375077056886, 107.99217348098755 35.03347385482788, 107.99137954711914 35.033173447418214, 107.99028520584106 35.032915955352784, 107.98833255767822 35.03229368286133, 107.98753862380981 35.03197181777954, 107.98732404708862 35.03195036010742, 107.9861867904663 35.03145683364868, 107.98453454971313 35.03111351089478, 107.98157339096069 35.03074873046875, 107.97977094650268 35.030426865386964, 107.97882680892944 35.03034103469849, 107.97781829833984 35.030147915649415, 107.97734622955322 35.030147915649415, 107.97623043060302 35.02995479660034, 107.97623043060302 35.02995479660034))");
			vtsList.add(vts1);*/
			int count = 0;
			if(vtsList != null && vtsList.size() >0){
				//write vtsList
				String insCitySql = "INSERT INTO vector_tab_suspect (ID,GEOMETRY) VALUES (?,SDO_GEOMETRY(?,8307))";
				String deleteSql = " delete vector_tab_suspect ";
				run.execute(conn, deleteSql);
				for(VectorTabSuspect vts:vtsList){
					int id = vts.getId();
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
	
	public static List<VectorTabSuspect> parseShape(String shapeFile)throws Exception{
		List<VectorTabSuspect> vtsList =null;
		 
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();  
        try {  
        	vtsList = new ArrayList<VectorTabSuspect>();
        	
            ShapefileDataStore sds = (ShapefileDataStore)dataStoreFactory.createDataStore(new File(shapeFile).toURI().toURL());  
            sds.setCharset(Charset.forName("GBK"));  
            SimpleFeatureSource featureSource = sds.getFeatureSource();  
            SimpleFeatureIterator itertor = featureSource.getFeatures().features();  
  
            while(itertor.hasNext()) {  
            	VectorTabSuspect vts = new VectorTabSuspect();
                SimpleFeature feature = itertor.next();  
//                System.out.println("geometry: "+feature.getDefaultGeometryProperty().getValue());
//                System.out.println(feature.getAttribute("road_ID"));  
                long idl = (long) feature.getAttribute("road_ID");
                vts.setId( Integer.parseInt(String.valueOf(idl)) );
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
