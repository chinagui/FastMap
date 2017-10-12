package com.navinfo.dataservice.dao.plus.selector.custom;

import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.glm.GlmFactory;
import com.navinfo.dataservice.dao.plus.glm.GlmObject;
import com.navinfo.dataservice.dao.plus.glm.GlmTable;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.SingleBatchSelRsHandler;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * 
 * @ClassName IxPoiSelector
 * @author Han Shaoming
 * @date 2016年11月21日 下午5:36:44
 * @Description TODO
 */
public class IxPointaddressSelector {
	protected static Logger log = LoggerRepos.getLogger(IxPointaddressSelector.class);

	public static Map<String,Long> getPidByFids(Connection conn,Collection<String> fids)throws Exception{
		if(fids==null|fids.size()==0)return new HashMap<String,Long>();

		if(fids.size()>1000){
			String sql= "SELECT PID,idcode FROM IX_POINTADDRESS WHERE idcode IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?))) AND U_RECORD <>2";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(fids, ","));
			return new QueryRunner().query(conn, sql, new FidPidSelHandler(),clob);
		}else{
			String sql= "SELECT PID,idcode FROM IX_POINTADDRESS WHERE idcode IN ('"+StringUtils.join(fids, "','")+"') AND U_RECORD <>2";
			return new QueryRunner().query(conn,sql,new FidPidSelHandler());
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
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(ObjectName.IX_POINTADDRESS);
		GlmTable mainTable = glmObj.getMainTable();

		String sql = ObjBatchSelector.assembleSql(mainTable,mainTable,IxPointaddress.IDCODE,fids);
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
			String idcode = ((IxPointaddress)mainrow).getIdcode();
			objs.put(idcode, obj);
			pids.add(obj.objPid());
		}
		
		if(tabNames!=null&&!tabNames.isEmpty()){
			log.info("selectByFids开始加载子表");
			//ObjBatchSelector.selectChildren(conn,objs.values(),tabNames,pids);
			log.info("selectByFids开始加载子表");
		}
		return objs;
	}
}
