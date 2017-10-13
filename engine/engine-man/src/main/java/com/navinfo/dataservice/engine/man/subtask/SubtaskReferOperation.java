package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import oracle.sql.STRUCT;

public class SubtaskReferOperation {
	private static Logger log = LoggerRepos.getLogger(SubtaskReferOperation.class);
	
	/**
	 * @param conn
	 * @param bean
	 * @throws Exception  void
	 */
	public static void create(Connection conn,SubtaskRefer bean) throws Exception{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String createSql = "insert into SUBTASK_REFER ";			
			List<String> columns = new ArrayList<String>();
			List<String> placeHolder = new ArrayList<String>();
			List<Object> values = new ArrayList<Object>();
			
			columns.add(" ID ");
			placeHolder.add("SUBTASK_REFER_SEQ.NEXTVAL");
				
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("BLOCK_ID")){
				columns.add(" BLOCK_ID ");
				placeHolder.add("?");
				values.add(bean.getBlockId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("GEOMETRY")){
				columns.add(" GEOMETRY ");
				placeHolder.add("?");
				STRUCT struct = GeoTranslator.wkt2Struct(conn,  GeoTranslator.jts2Wkt(bean.getGeometry()));
				values.add(struct);
			};
			if(!columns.isEmpty()){
				String columsStr = "(" + StringUtils.join(columns.toArray(),",") + ")";
				String placeHolderStr = "(" + StringUtils.join(placeHolder.toArray(),",") + ")";
				createSql = createSql + columsStr + " values " + placeHolderStr;
			}
			run.update(conn, 
					   createSql, 
					   values.toArray() );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @param conn
	 * @param bean
	 * @throws Exception  void
	 */
	public static void updateGeo(Connection conn,SubtaskRefer bean) throws Exception{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String createSql = "update SUBTASK_REFER set GEOMETRY=? where id=?";
			List<Object> values = new ArrayList<Object>();
			STRUCT struct = GeoTranslator.wkt2Struct(conn,  GeoTranslator.jts2Wkt(bean.getGeometry()));
			values.add(struct);
			values.add(bean.getId());
			run.update(conn, 
					   createSql, 
					   values.toArray() );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @param conn
	 * @param bean
	 * @throws Exception  void
	 */
	public static void delete(Connection conn,Set<Integer> ids) throws Exception{
		try{
			//持久化
			QueryRunner run = new QueryRunner();			
			String updateSql = "delete from subtask_refer where id in "+ids.toString().replace("[", "(").replace("]", ")");
			run.update(conn, updateSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @param conn
	 * @param bean
	 * @throws Exception  void
	 */
	public static void deleteDetail(Connection conn,Set<Integer> ids) throws Exception{
		try{
			//持久化
			QueryRunner run = new QueryRunner();			
			String updateSql = "delete from subtask_refer_detail where refer_id in "+ids.toString().replace("[", "(").replace("]", ")");
			run.update(conn, updateSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}
	}
}
