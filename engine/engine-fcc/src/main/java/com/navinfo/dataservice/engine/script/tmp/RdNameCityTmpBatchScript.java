package com.navinfo.dataservice.engine.script.tmp;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @ClassName: RdNameCityTmpBatchScript.java
 * @author y
 * @date 2017-8-28 下午1:50:13
 * @Description：道路名city字段批处理脚本
 * 脚本背景：道路名 rd_name.city字段，原来的tips上传道路名导入 和差分，对city字段未赋值，需要批处理
 * 数据并不多，所以用这种方式
 */
public class RdNameCityTmpBatchScript {

	private static final Logger log = Logger.getLogger(RdNameCityTmpBatchScript.class);

	protected static int total = 0;
	protected static VMThreadPoolExecutor poolExecutor;

	private static String tableName=HBaseConstant.tipTab;
	private static TipsIndexOracleOperator op;
	
	
	private  static Map<String,Long>  name2batch=new HashMap<String,Long>();
	
	private  static Map<String,String>  meshCityMap=new HashMap<String,String>();
	
	
	/**
	 * @Description:查询tips导入的rdName，且city字段为空的namgeGroupId和tips rowkey
	 * @return
	 * @throws SQLException
	 * @author: y
	 * @time:2017-9-13 上午11:51:13
	 */
	private static void getNoCityNames() throws SQLException{
		  
		  java.sql.Connection metaConn=null;
		  PreparedStatement pst = null;
	      ResultSet rs = null;
	      try {
	        	metaConn = DBConnector.getInstance().getMetaConnection();
	        	String sql="SELECT  DISTINCT n.name_groupid,n.src_resume FROM rd_name n WHERE n.src_resume LIKE '\"tips\":%' AND city IS NULL";
	            pst = metaConn.prepareStatement(sql);
	            rs = pst.executeQuery();
	            rs.setFetchSize(5000);
	            while (rs.next()) {
	                Long nameGroupId = rs.getLong("name_groupid");
	                String rowkeyString=rs.getString("src_resume");
	                log.info("src_resume:"+rowkeyString);
	                rowkeyString=rowkeyString.substring("\"tips\":\"".length(),rowkeyString.length()-1);
	               log.info("rowkey:"+rowkeyString);
	                name2batch.put(rowkeyString,nameGroupId);
	            }
	            log.debug("has found  rd_name.city is null nameGroupId count   " + name2batch.size() );
	        }catch (SQLException e) {
	            log.error("query rd_Name to batch erro..." + e.getMessage(), e);
	            throw new SQLException("query rd_Name to batch erro..."  + e.getMessage(),
	                    e);
	        } finally {
	        	DbUtils.closeQuietly(rs);
	            DbUtils.closeQuietly(pst);
	            DbUtils.closeQuietly(metaConn);
	        }
	}
	
	
	/**
	 * @Description:查询tips的wkt Location
	 * @throws SQLException
	 * @author: y
	 * @time:2017-9-13 上午11:54:16
	 */
	private static void queryWktLocationAndUpdate() throws SQLException{
		
		java.sql.Connection tipsConn=null;
		PreparedStatement pst = null;
		
		java.sql.Connection metaConn=null;
		PreparedStatement metaPst = null;
	    ResultSet rs = null;
		try{
			log.debug("start query wktLocation and update ...............");
			StringBuffer rowkeys = getTipsRowkeys();
			log.debug("rowkeys:"+rowkeys+" ...............");
			String sql="SELECT id,wktlocation FROM tips_index  WHERE ID IN (select column_value from table(clob_to_table(?)))";
			String updateSql="UPDATE RD_NAME N SET N.CITY = ?  WHERE N.NAME_GROUPID = ?";
			
			tipsConn=DBConnector.getInstance().getTipsIdxConnection();
			metaConn = DBConnector.getInstance().getMetaConnection();
			
			log.debug("metaConn:==============="+metaConn);
			metaConn.setAutoCommit(false);
			
			pst=tipsConn.prepareStatement(sql);
			metaPst=metaConn.prepareStatement(updateSql);
			
			Clob clob=ConnectionUtil.createClob(tipsConn, rowkeys.toString());
			
			pst.setClob(1, clob);
			rs=pst.executeQuery();
			rs.setFetchSize(5000);
			
			//meta
			int count=0;
			int updateCount=0;
			while(rs.next()){
				count++;
				String id=rs.getString("id");
				STRUCT wktLocation = (STRUCT) rs.getObject("wktlocation");
				Geometry geoLocation=(GeoTranslator.struct2Jts(wktLocation));
				
				String city=getCityByGLocation(metaConn,geoLocation);
				
				log.debug("id:"+id+"--------------city: " + city );
				
				if(StringUtils.isEmpty(city)){
					log.warn("name_groupid:"+name2batch.get(id)+" city is null ,will not be updated.......");
					continue;
				}
				
				metaPst.setString(1, city);
				metaPst.setLong(2, name2batch.get(id));
				metaPst.addBatch();
				updateCount++;
				if(count%1000==0){
					metaPst.executeBatch();
					metaConn.commit();
					metaPst.clearBatch();
					
				}
				
				
			}
			metaPst.executeBatch();
			metaConn.commit();
			metaPst.clearBatch();
			
			
			log.debug("total find count:"+count+"--------------total update count: " + updateCount );
			
		}catch (Exception e) {
				DbUtils.rollback(metaConn);
		     log.error("update rd_Name  erro..." + e.getMessage(), e);
	         throw new SQLException("update rd_Name  erro......"  + e.getMessage(),
	                    e);
        } finally {
        	DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(pst);
            DbUtils.closeQuietly(tipsConn);
            DbUtils.closeQuietly(metaPst);
            DbUtils.commitAndCloseQuietly(metaConn);
        }
	}


	/**
	 * @Description:根据gLocation查询city
	 * @param geoLocation
	 * @return
	 * @author: y
	 * @param metaConn 
	 * @throws ServiceException 
	 * @time:2017-9-13 下午3:56:50
	 */
	private static String getCityByGLocation(Connection metaConn, Geometry geoLocation) throws ServiceException {
		StringBuffer cityResult=new StringBuffer();
		String meshes[] = CompGeometryUtil.geo2MeshesWithoutBreak(geoLocation);
		
		if(meshes==null||meshes.length==0){
			log.debug(" sorry  mesh is null ");
			return null;
		}
		int index=0;
		for (String meshId : meshes) {
			log.debug(" meshId: "+meshId);
			String city=null;
			if(!meshCityMap.containsKey(meshId)){
				city = getCityByMesh(metaConn, meshId);
                meshCityMap.put(meshId, city);
			}else{
				city=meshCityMap.get(meshId);
			}
			if(index==0){
				cityResult.append(city);
			}else{
				cityResult.append("|"+city);
			}
			
			index++;
		}
		
		if(StringUtils.isNotEmpty(cityResult.toString())){
			return cityResult.toString();
		}
		
		return null;
	}


	/**
	 * @Description:TOOD
	 * @param metaConn
	 * @param meshId
	 * @return
	 * @throws ServiceException
	 * @author: y
	 * @time:2017-9-13 下午3:46:42
	 */
	private static String getCityByMesh(java.sql.Connection metaConn,
			String meshId) throws ServiceException {
		StringBuffer city=new StringBuffer("");
		List<String> cityList  =getCityListByMesh(metaConn,meshId);
		int index=0;
		if(cityList!=null&&cityList.size()!=0){
			for (String cityStr : cityList) {
				if(index!=0){
					city.append("|"+cityStr);
				}else{
					city.append(cityStr);
				}
				index++;
			}
			
			return city.toString();
		}else{
			return "";
		}
		
	}
	
	
	/**
	 * @Description:通过图幅号获取地级市
	 * @param meshId
	 * @return
	 * @throws ServiceException
	 * @author: y
	 * @time:2016-6-28 下午1:53:19
	 */
	public static List<String> getCityListByMesh(java.sql.Connection metaConn,String meshId)
			throws ServiceException {
		
		try{
			String selectSql = "SELECT city FROM sc_partition_meshlist WHERE mesh = :1";

			QueryRunner run = new QueryRunner();

			metaConn = DBConnector.getInstance().getMetaConnection();
			
			ResultSetHandler<List<String>> rsHandler = new ResultSetHandler<List<String>>() {
				public List<String> handle(ResultSet rs) throws SQLException {
                    List<String> rsList = new ArrayList<String>();
					while (rs.next()) {
                        rsList.add(rs.getString("city"));
					}
                    return rsList;
				}
			};

            List<String> rsList = run.query(metaConn, selectSql, rsHandler, meshId);

			return rsList;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询city失败，原因为:" + e.getMessage(), e);
		} finally {
			
		}

	}


	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-9-13 下午3:26:14
	 */
	private static StringBuffer getTipsRowkeys() {
		StringBuffer rowkeys=new StringBuffer();
		
		Set<String> rowkeySet=name2batch.keySet();//所有的tips
		
		int index =0;
		for (String rowkey : rowkeySet) {
			
			if(index==0){
				rowkeys.append(rowkey);
			}else{
				rowkeys.append(","+rowkey);
			}
			index++;
		}
		return rowkeys;
	}
	
	

	/**
	 * @Description:TOOD
	 * @author: y
	 * @throws SQLException 
	 * @time:2017-9-13 下午4:12:10
	 */
	private static void dobatch() throws SQLException {
		//1.查询需要批处理的tips
		getNoCityNames();
		queryWktLocationAndUpdate();
	}
	

	public static void initContext(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-scripts.xml","dubbo-scripts.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			log.info("......................start batch rd_name.city......................");
			initContext();
			dobatch();
			log.info("......................all rd_name.city. update Over......................");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(" excute  error "+e.getMessage(), e);
		} finally {
			log.info("......................all rd_name.city. update Over......................");
			System.exit(0);
		}

	}



}
