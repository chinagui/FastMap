package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import oracle.sql.STRUCT;

public class SubtaskQualityOperation {
	private static Logger log = LoggerRepos.getLogger(SubtaskQualityOperation.class);
	/**
	 * @param conn
	 * @param Map<Integer,List<SubtaskQuality>> Interger:子任务id，value:List<SubtaskQuality>对应的质检圈
	 * @throws Exception  void
	 */
	public static Map<Integer,List<SubtaskQuality>> queryBySubtaskIds(Connection conn,Set<Integer> subtaskIds) throws Exception{
		try{
			//持久化
			QueryRunner run = new QueryRunner();			
			String updateSql = "select q.quality_id,q.subtask_id,q.geometry from subtask_quality q "
					+ "where q.subtask_id in "+subtaskIds.toString().replace("[", "(").replace("]", ")")
					+ " order by q.subtask_id ";
			return run.query(conn, updateSql, new ResultSetHandler<Map<Integer,List<SubtaskQuality>>>(){

				@Override
				public Map<Integer,List<SubtaskQuality>> handle(ResultSet rs) throws SQLException {
					Map<Integer,List<SubtaskQuality>> returns=new HashMap<Integer,List<SubtaskQuality>>();
					List<SubtaskQuality> qualitys=new ArrayList<SubtaskQuality>();
					int beforeId=0;
					while(rs.next()){
						SubtaskQuality bean=new SubtaskQuality();
						bean.setQualityId(rs.getInt("QUALITY_ID"));
						bean.setSubtaskId(rs.getInt("SUBTASK_ID"));
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							bean.setGeometry(GeoTranslator.struct2Jts(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						if(beforeId==0){
							beforeId=bean.getSubtaskId();
						}
						if(beforeId!=bean.getSubtaskId()){
							returns.put(beforeId, qualitys);
							qualitys=new ArrayList<SubtaskQuality>();
							beforeId=bean.getSubtaskId();
						}
						qualitys.add(bean);
					}
					if(qualitys.size()>0){
						returns.put(beforeId, qualitys);
					}
					return returns;
				}});
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}
	}
}
