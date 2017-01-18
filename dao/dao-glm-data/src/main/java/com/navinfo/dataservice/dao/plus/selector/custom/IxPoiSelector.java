package com.navinfo.dataservice.dao.plus.selector.custom;

import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.glm.GlmFactory;
import com.navinfo.dataservice.dao.plus.glm.GlmObject;
import com.navinfo.dataservice.dao.plus.glm.GlmTable;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.SingleBatchSelRsHandler;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * 
 * @ClassName IxPoiSelector
 * @author Han Shaoming
 * @date 2016年11月21日 下午5:36:44
 * @Description TODO
 */
public class IxPoiSelector {
	protected static Logger log = LoggerRepos.getLogger(IxPoiSelector.class);

	public static Map<String,Long> getPidByFids(Connection conn,Collection<String> fids)throws Exception{
		if(fids==null|fids.size()==0)return null;

		if(fids.size()>1000){
			String sql= "SELECT PID,POI_NUM FROM IX_POI WHERE POI_NUM IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(fids, ","));
			return new QueryRunner().query(conn, sql, new FidPidSelHandler(),clob);
		}else{
			String sql= "SELECT PID,POI_NUM FROM IX_POI WHERE POI_NUM IN ('"+StringUtils.join(fids, "','")+"')";
			return new QueryRunner().query(conn,sql,new FidPidSelHandler());
		}
	}
	public static Map<Long,Long> getAdminIdByPids(Connection conn,Collection<Long> pids)throws Exception{
		if(pids!=null&&pids.size()>0){
			if(pids.size()>1000){
				String sql= "SELECT T.PID,P.ADMIN_ID FROM IX_POI T,AD_ADMIN P WHERE T.REGION_ID=P.REGION_ID AND T.PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				return new QueryRunner().query(conn, sql, new PoiAdminIdSelHandler(),clob);
			}else{
				String sql= "SELECT T.PID,P.ADMIN_ID FROM IX_POI T,AD_ADMIN P WHERE T.REGION_ID=P.REGION_ID AND T.PID IN ("+StringUtils.join(pids, ",")+")";
				return new QueryRunner().query(conn,sql,new PoiAdminIdSelHandler());
			}
		}
		return null;
	}
	
	/**
	 * 查询父POI的Fid
	 * @author Han Shaoming
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public static Map<Long,String> getParentFidByPids(Connection conn,Collection<Long> pids)throws Exception{
		if(pids!=null&&pids.size()>0){
			if(pids.size()>1000){
				String sql= "SELECT C.GROUP_ID,C.CHILD_POI_PID,P.PARENT_POI_PID,I.PID,I.POI_NUM "
						+ "FROM IX_POI_CHILDREN C,IX_POI_PARENT P,IX_POI I "
						+ "WHERE C.GROUP_ID=P.GROUP_ID AND P.PARENT_POI_PID=I.PID AND C.CHILD_POI_PID IN  "
						+ "(SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				return new QueryRunner().query(conn, sql, new PoiParentFidSelHandler(),clob);
			}else{
				String sql= "SELECT C.GROUP_ID,C.CHILD_POI_PID,P.PARENT_POI_PID,I.PID,I.POI_NUM "
						+ "FROM IX_POI_CHILDREN C,IX_POI_PARENT P,IX_POI I WHERE C.GROUP_ID=P.GROUP_ID "
						+ "AND P.PARENT_POI_PID=I.PID AND C.CHILD_POI_PID IN ("+StringUtils.join(pids, ",")+")";
				return new QueryRunner().query(conn,sql,new PoiParentFidSelHandler());
			}
		}
		return null;
	}
	public static List<Long> getChildrenPidsByParentPid(Connection conn,Set<Long> pidList) throws ServiceException{
		List<Long> childPids = new ArrayList<Long>();
		if(pidList.isEmpty()){
			return childPids;
		}
		try{
			String sql = "SELECT DISTINCT IPC.CHILD_POI_PID"
					+ " FROM IX_POI_PARENT IPP,IX_POI_CHILDREN IPC"
					+ " WHERE IPC.GROUP_ID = IPP.GROUP_ID"
					+ " AND IPP.PARENT_POI_PID IN (" + StringUtils.join(pidList.toArray(),",") + ")";
			
			ResultSetHandler<List<Long>> rsHandler = new ResultSetHandler<List<Long>>() {
				public List<Long> handle(ResultSet rs) throws SQLException {
					List<Long> result = new ArrayList<Long>();
					while (rs.next()) {
						long childPid = rs.getLong("CHILD_POI_PID");
						result.add(childPid);
					}
					return result;
				}
			};
			
			log.info("getIxPoiParentMapByChildrenPidList查询主表："+sql);
			childPids = new QueryRunner().query(conn,sql, rsHandler);
			return childPids;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}

	}
	public static Map<Long,Long> getParentPidsByChildrenPids(Connection conn,Set<Long> pidList) throws ServiceException{
		Map<Long,Long> childPidParentPid = new HashMap<Long,Long>();
		if(pidList.isEmpty()){
			return childPidParentPid;
		}
		try{
			String sql = "SELECT DISTINCT IPP.PARENT_POI_PID,IPC.CHILD_POI_PID"
					+ " FROM IX_POI_PARENT IPP,IX_POI_CHILDREN IPC"
					+ " WHERE IPC.GROUP_ID = IPP.GROUP_ID"
					+ " AND IPC.CHILD_POI_PID IN (" + StringUtils.join(pidList.toArray(),",") + ")";
			
			ResultSetHandler<Map<Long,Long>> rsHandler = new ResultSetHandler<Map<Long,Long>>() {
				public Map<Long,Long> handle(ResultSet rs) throws SQLException {
					Map<Long,Long> result = new HashMap<Long,Long>();
					while (rs.next()) {
						long parentPid = rs.getLong("PARENT_POI_PID");
						long childPid = rs.getLong("CHILD_POI_PID");
						result.put(childPid, parentPid);
					}
					return result;
				}
			};
			
			log.info("getIxPoiParentMapByChildrenPidList查询主表："+sql);
			childPidParentPid = new QueryRunner().query(conn,sql, rsHandler);
			return childPidParentPid;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}

	}
	
	/**
	 * @Title: getSamePoiPidsByThisPids
	 * @Description: 获取同组的另一个poi 的 pid
	 * @param conn
	 * @param pidList
	 * @return
	 * @throws ServiceException  Map<Long,Long>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月9日 下午12:06:21 
	 */
	public static Map<Long,Long> getSamePoiPidsByThisPids(Connection conn,Set<Long> pidList) throws ServiceException{
		Map<Long,Long> childPidParentPid = new HashMap<Long,Long>();
		if(pidList.isEmpty()){
			return childPidParentPid;
		}
		for(final Long pid : pidList){
			try{
				String sql = "select nvl(p.poi_pid,0) otherpid from ix_samepoi_part p where p.group_id =(select t.group_id from ix_samepoi_part t where t.poi_pid= "+pid+" and t.u_record != 2 ) and p.poi_pid != "+pid+"  and p.u_record != 2 ";

				
				ResultSetHandler<Map<Long,Long>> rsHandler = new ResultSetHandler<Map<Long,Long>>() {
					public Map<Long,Long> handle(ResultSet rs) throws SQLException {
						Map<Long,Long> result = new HashMap<Long,Long>();
						while (rs.next()) {
							long samepoiPid = rs.getLong("otherpid");
							if(samepoiPid != 0){
								result.put(pid, samepoiPid);
							}
						}
						return result;
					}
				};
				
				log.info("getIxPoiParentMapByChildrenPidList查询主表："+sql);
				 childPidParentPid.putAll(new QueryRunner().query(conn,sql, rsHandler));
			}catch(Exception e){
				DbUtils.rollbackAndCloseQuietly(conn);
				log.error(e.getMessage(), e);
				throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
			}
		}
		return childPidParentPid;

	}
	
	/**
	 * 通过子poi查询父，祖父等，一直到一级父（即父没有父了，只有子）
	 * @param conn
	 * @param pidList
	 * @return
	 * @throws ServiceException
	 */
	public static Map<Long,Long> getAllParentPidsByChildrenPids(Connection conn,Set<Long> pidList) throws ServiceException{
		Map<Long,Long> childPidParentPid = new HashMap<Long,Long>();
		if(pidList.isEmpty()){
			return childPidParentPid;
		}
		childPidParentPid=getParentPidsByChildrenPids(conn,pidList);
		Set<Long> poiPids=new HashSet<Long>();
		poiPids.addAll(childPidParentPid.keySet());
		poiPids.addAll(childPidParentPid.values());
		poiPids.addAll(pidList);
		//循环查询直到没有新父被查出来为止
		while(poiPids.size()!=pidList.size()){
			pidList.addAll(poiPids);
			childPidParentPid=getParentPidsByChildrenPids(conn,pidList);
			poiPids.addAll(childPidParentPid.values());
		}
		return childPidParentPid;
	}
	
	
	/**
	 * 查询子POI的fid
	 * @author Han Shaoming
	 * @param conn
	 * @param pids
	 * @return
	 * @throws ServiceException
	 */
	public static Map<Long,List<Map<Long,Object>>> getChildFidByPids(Connection conn,Map<Long,List<Long>> objMap) throws ServiceException{
		try {
			Map<Long,List<Map<Long,Object>>> msgs = new HashMap<Long, List<Map<Long,Object>>>();
			//获取父对象
			for(Map.Entry<Long,List<Long>> entry :objMap.entrySet()){
				long pid = entry.getKey();
				List<Long> childPids = entry.getValue();
				List<Map<Long,Object>> childFids = new ArrayList<Map<Long,Object>>();
				if(childPids!=null && childPids.size()>0){
					Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI,null,true,childPids,true,true);
					for(BasicObj obj:objs.values()){
						Map<Long,Object> childFid = new HashMap<Long, Object>();
						IxPoiObj poi = (IxPoiObj) obj;
						IxPoi ixPoi = (IxPoi)poi.getMainrow();
						long childPid = obj.objPid();
						childFid.put(childPid, ixPoi.getPoiNum());
						childFids.add(childFid);
					}
				}
				msgs.put(pid, childFids);
			}
			return msgs;
		} catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	/**
	 * 如果多条只返回第一条,仅支持主表数值或字符类型字段
	 * @param conn
	 * @param objType
	 * @param tabNames
	 * @param colName
	 * @param colValues
	 * @param isLock
	 * @param isWait//是否等待，true:等待；false：不等待
	 * @return
	 * @throws SQLException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 */
	public static Map<String,BasicObj> selectByFids(Connection conn,Set<String> tabNames
			,Collection<String> fids,boolean isLock,boolean isWait) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
		if(fids==null||fids.isEmpty()){
			log.info("fids为空");
			return null;
		}
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(ObjectName.IX_POI);
		GlmTable mainTable = glmObj.getMainTable();

		String sql = ObjBatchSelector.assembleSql(mainTable,mainTable,IxPoi.POI_NUM,fids);
		if(isLock){
			sql +=" FOR UPDATE";
			if(!isWait){
				sql +=" NOWAIT";
			}
		}
		log.info("selectByFids查询主表："+sql);
		List<BasicRow> mainrowList = new ArrayList<BasicRow>();
		
		if(fids.size()>1000){
			Clob clobPids=ConnectionUtil.createClob(conn);
			clobPids.setString(1, StringUtils.join(fids, ","));
			mainrowList = new QueryRunner().query(conn, sql, new SingleBatchSelRsHandler(mainTable),clobPids);
		}else{
			mainrowList = new QueryRunner().query(conn, sql, new SingleBatchSelRsHandler(mainTable));
		}

		
		Map<String,BasicObj> objs = new HashMap<String,BasicObj>();
		List<Long> pids = new ArrayList<Long>();
		for(BasicRow mainrow:mainrowList){
			BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
			String poiNum = ((IxPoi)mainrow).getPoiNum();
			objs.put(poiNum, obj);
			pids.add(obj.objPid());
		}
		
		if(tabNames!=null&&!tabNames.isEmpty()){
			log.info("selectByFids开始加载子表");
			ObjBatchSelector.selectChildren(conn,objs.values(),tabNames,pids);
			log.info("selectByFids开始加载子表");
		}
		return objs;
	}
	/**
	 * @Title: getGroupIdByPids
	 * @Description: 获取每个pid 在 ix_samepoi_part 中的group_id
	 * @param conn
	 * @param pidOriginSamePoiPidNeedToBeLoad
	 * @return  Map<Long,Long>
	 * @throws ServiceException 
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月10日 上午9:42:00 
	 */
	public static List<Map<String,Long>> getIxSamePoiGroupIdsByPids(Connection conn, Set<Long> pidSet) throws ServiceException {
		List<Map<String,Long>> groupIdList = new ArrayList<Map<String,Long>>();
		if(pidSet.isEmpty()){
			return groupIdList;
		}
		try{
			String sql = " select distinct  p.group_id ,p.poi_pid   from ix_samepoi_part p "
					+ " where p.poi_pid IN (" + StringUtils.join(pidSet.toArray(),",") + ")" 
					+ " and p.u_record != 2 " ;
			ResultSetHandler<List<Map<String,Long>>> rsHandler = new ResultSetHandler<List<Map<String,Long>>>() {
				public List<Map<String,Long>> handle(ResultSet rs) throws SQLException {
					List<Map<String,Long>> result = new ArrayList<Map<String,Long>>();
					while (rs.next()) {
						Map<String,Long> map= new HashMap<String,Long>();
						long groupId = rs.getLong("group_id");
						long poiPid = rs.getLong("poi_pid");
						map.put("group_id", groupId);
						map.put("poi_pid", poiPid);
						
						result.add(map);
					}
					return result;
				}
			};
			
			log.info("getIxSamePoiGroupIdsByPids查询主表(返回 List<Map<>>)："+sql);
			groupIdList = new QueryRunner().query(conn,sql, rsHandler);
			return groupIdList;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	public static List<Long> getIxSamePoiGroupIdsByPids(Connection conn, List<Long> pidList) throws ServiceException {
		List<Long> groupIdList = new ArrayList<Long>();
		if(pidList.isEmpty()){
			return groupIdList;
		}
		try{
			String sql = " select distinct  p.group_id    from ix_samepoi_part p "
					+ " where p.poi_pid IN (" + StringUtils.join(pidList.toArray(),",") + ")" 
					+ " and p.u_record != 2 " ;
			ResultSetHandler<List<Long>> rsHandler = new ResultSetHandler<List<Long>>() {
				public List<Long> handle(ResultSet rs) throws SQLException {
					List<Long> result = new ArrayList<Long>();
					while (rs.next()) {
						long groupId = rs.getLong("group_id");
						//long poiPid = rs.getLong("poi_pid");
						result.add(groupId);
					}
					return result;
				}
			};
			
			log.info("getIxSamePoiGroupIdsByPids查询主表(返回 List<>)："+sql);
			groupIdList = new QueryRunner().query(conn,sql, rsHandler);
			return groupIdList;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}
}
