package com.navinfo.dataservice.dao.plus.log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.handlers.MapHandler;

import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.BasicObjGrid;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: LogGenerator
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: LogGenerator.java
 */
public class LogGenerator {
	String insertLogActionSql = "INSERT INTO LOG_ACTION (ACT_ID,US_ID,OP_CMD,SRC_DB,STK_ID) VALUES (?,?,?,?,?)";
	String insertLogOperationSql = "INSERT INTO LOG_OPERATION (OP_ID,ACT_ID,OP_DT,OP_SEQ) VALUES (?,?,SYSDATE,LOG_OP_SEQ.NEXTVAL)";
	String insertLogDetailSql = "INSERT INTO LOG_DETAIL (OP_ID,ROW_ID,OB_NM,OB_PID,GEO_NM,GEO_PID,TB_NM,OLD,NEW,FD_LST,OP_TP,TB_ROW_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
	String insertLogDetailGridSql = "INSERT INTO LOG_DETAIL_GRID (LOG_ROW_ID,GRID_ID,GRID_TYPE) VALUES (?,?,?)";
	PreparedStatement perstmtLogAction = null;
	PreparedStatement perstmtLogOperation = null;
	PreparedStatement perstmtLogDetail = null;
	PreparedStatement perstmtLogDetailGrid = null;

	public void writeLog(Connection conn,boolean isUnionOperation,OperationResult result,String opCmd,int srcDb,long userId,int subtaskId) throws Exception{
		Collection<BasicObj> allObjs = result.getAllObjs();
		try{
			writeLog( conn, isUnionOperation, allObjs, opCmd, srcDb, userId,subtaskId);
		}finally{
			DbUtils.closeQuietly(perstmtLogAction);
			DbUtils.closeQuietly(perstmtLogOperation);
			DbUtils.closeQuietly(perstmtLogDetail);
			DbUtils.closeQuietly(perstmtLogDetailGrid);
		}
	}
	/**
	 * 生成履历
	 * @param conn
	 * @param isUnionOperation 是否融合
	 * @param allObjs 
	 * @param opCmd 数据修改的指令信息
	 * @param opSg 指令执行时间
	 * @param userId 操作员ID
	 * @throws Exception
	 */
	public void writeLog(Connection conn,boolean isUnionOperation,Collection<BasicObj> allObjs
			,String opCmd,int opSg,long userId,int subtaskId)throws Exception{
		if(allObjs==null||allObjs.size()==0)return;
		
		//获得log pstm
		generate(conn, isUnionOperation,allObjs, opCmd, opSg, userId,subtaskId);

		if(perstmtLogAction!=null){
			perstmtLogAction.executeBatch();
		}
		if(perstmtLogOperation!=null){
			perstmtLogOperation.executeBatch();
		}
		if(perstmtLogDetail!=null){
			perstmtLogDetail.executeBatch();
		}
		if(perstmtLogDetailGrid!=null){
			perstmtLogDetailGrid.executeBatch();
		}
	}
	
	/**
	 * 根据编辑结果生成履历模型对象
	 * @param basicObjs
	 * @param isOneOperation
	 * @param opCmd
	 * @param opSg
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	private void generate(Connection conn,boolean isUnionOperation,Collection<BasicObj> basicObjs,String opCmd
			,int opSg,long userId,int subtaskId)throws Exception{
		if(basicObjs!=null&&basicObjs.size()>0){
			//融合operation id
			String geoChangeOpId=null;
			//log_action act_id
			String actId = UuidUtils.genUuid();
			
			//log_operation
			for(BasicObj basicObj:basicObjs){
				//对象未修改则不记log
				if(!basicObj.isChanged()){
					continue;
				}
				//operation
				String opId = null;
				if(isUnionOperation&&basicObj.isGeoChanged()){
					if(geoChangeOpId==null){
						if(perstmtLogOperation==null){
							perstmtLogOperation = conn.prepareStatement(insertLogOperationSql);
						}
						geoChangeOpId=UuidUtils.genUuid();
					}
					perstmtLogOperation.setString(1, geoChangeOpId);
					perstmtLogOperation.setString(2, actId);
					perstmtLogOperation.addBatch();
					opId = geoChangeOpId;
				}else{
					if(perstmtLogOperation==null){
						perstmtLogOperation = conn.prepareStatement(insertLogOperationSql);
					}
					opId = UuidUtils.genUuid();
					perstmtLogOperation.setString(1, opId);
					perstmtLogOperation.setString(2, actId);
					perstmtLogOperation.addBatch();
				}

				//主表，更新log_detail,log_detail_grid
				goThroughOneBasicRow(conn,basicObj,basicObj.getMainrow(),opId);
				//子表，更新log_detail,log_detail_grid
				for(Entry<String, List<BasicRow>> entry:basicObj.getSubrows().entrySet()){
					List<BasicRow> subrows = entry.getValue();
					if(subrows!=null){
						for(BasicRow subrow:subrows){
							//遍历每个子表，更新log_detail,log_detail_grid
							goThroughOneBasicRow(conn,basicObj,subrow,opId);
						}
					}
				}
			}
			
			//log_action。此次操作确实产生了履历
			if(perstmtLogOperation!=null){
				if(perstmtLogAction==null){
					perstmtLogAction = conn.prepareStatement(insertLogActionSql);
				}
				perstmtLogAction.setString(1, actId);
				perstmtLogAction.setLong(2, userId);
				perstmtLogAction.setString(3, opCmd);
				perstmtLogAction.setInt(4, opSg);//SRC_DB
				perstmtLogAction.setInt(5, subtaskId);
				perstmtLogAction.addBatch();
			}
		}
	}
	
	/**
	 * @param conn
	 * @param perstmtLogDetail
	 * @param perstmtLogDetailGrid
	 * @param basicObj
	 * @param subrow
	 * @param opId
	 * @throws Exception 
	 */
	private void goThroughOneBasicRow(Connection conn, BasicObj basicObj, BasicRow subrow, String opId) throws Exception {
		//UUID
		String logDetailRowId = UuidUtils.genUuid();
		assembleLogDetail(conn,basicObj,subrow,logDetailRowId,opId);
		//log_detail_grid
		assembleLogDetailGrid(conn,basicObj,subrow,logDetailRowId);
	}

	/**
	 * 生成log_detail插入preparedStatement
	 * @param conn
	 * @param basicObj
	 * @param subrow
	 * @param oldValue
	 * @param logDetailRowId
	 * @param opId
	 * @param perstmtLogDetail
	 * @throws Exception 
	 */
	private void assembleLogDetail(Connection conn, BasicObj basicObj, BasicRow subrow,String logDetailRowId,String opId) throws Exception {
		if(!subrow.isChanged()){
			return;
		}
		if(perstmtLogDetail==null){
			perstmtLogDetail = conn.prepareStatement(insertLogDetailSql);
		}
		perstmtLogDetail.setString(1, opId);
		perstmtLogDetail.setString(2, logDetailRowId);
		perstmtLogDetail.setString(3, basicObj.objName());
		perstmtLogDetail.setLong(4, basicObj.objPid());
		perstmtLogDetail.setString(5, subrow.getGeoType());//几何参考对象名
		perstmtLogDetail.setLong(6, subrow.getGeoPid());//几何参考对象pid
		perstmtLogDetail.setString(7, subrow.tableName());
		if(subrow.getOpType().equals(OperationType.UPDATE)){
			perstmtLogDetail.setString(8,(subrow.getOldValueJson()==null?null:subrow.getOldValueJson().toString()));
			perstmtLogDetail.setString(9,(subrow.getNewValueJson()==null?null:subrow.getNewValueJson().toString()));
			perstmtLogDetail.setString(10,(subrow.getChangedColumns()==null?null:subrow.getChangedColumns().toString()));
		}else{
			perstmtLogDetail.setString(8,null);
			perstmtLogDetail.setString(9,null);
			perstmtLogDetail.setString(10,null);
		}

		if(subrow.getOpType().equals(OperationType.DELETE)){
			perstmtLogDetail.setInt(11, 2);
		}else if(subrow.getOpType().equals(OperationType.INSERT)){
			perstmtLogDetail.setInt(11, 1);
		}else if(subrow.getOpType().equals(OperationType.UPDATE)){
			perstmtLogDetail.setInt(11, 3);
		}
		
		perstmtLogDetail.setString(12, subrow.getRowId());
		perstmtLogDetail.addBatch();
		
	}
	/**
	 * 生成log_detail_grid插入preparedStatement
	 * @param conn
	 * @param basicObj
	 * @param subrow
	 * @param logDetailRowId
	 * @param perstmtLogDetailGrid
	 * @throws Exception 
	 */
	private void assembleLogDetailGrid(Connection conn, BasicObj basicObj, BasicRow subrow, String logDetailRowId) throws Exception {
		if(!subrow.isChanged()){
			return;
		}
		if(perstmtLogDetailGrid==null){
			perstmtLogDetailGrid = conn.prepareStatement(insertLogDetailGridSql);
		}
		BasicObjGrid grid = basicObj.getGrid();
		if(subrow.getGeoPid()!=basicObj.objPid()){
			BasicObj referObj = ObjSelector.selectByPid(conn, basicObj.objName(), null,true, subrow.getGeoPid(), true);
			grid = referObj.getGrid();
		}
		for(String gridId:grid.getGridListBefore()){
			perstmtLogDetailGrid.setString(1, logDetailRowId);
			perstmtLogDetailGrid.setLong(2, Long.parseLong(gridId));
			perstmtLogDetailGrid.setInt(3, 0);
			perstmtLogDetailGrid.addBatch();
		}
		for(String gridId:grid.getGridListAfter()){
			perstmtLogDetailGrid.setString(1, logDetailRowId);
			perstmtLogDetailGrid.setLong(2, Long.parseLong(gridId));
			perstmtLogDetailGrid.setInt(3, 1);
			perstmtLogDetailGrid.addBatch();
		}
		
	}
}
