package com.navinfo.dataservice.engine.editplus.log;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObjGrid;
import com.navinfo.dataservice.engine.editplus.model.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: LogGenerator
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: LogGenerator.java
 */
public class LogGenerator {
	
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
	public static List<PreparedStatement> generate(Connection conn,Collection<BasicObj> basicObjs,String opCmd,int opSg,long userId)throws Exception{
		List<PreparedStatement> perstmtList = new ArrayList<PreparedStatement>();
		String insertLogOperationSql = "INSERT INTO LOG_OPERATION (OP_ID,US_ID,OP_CMD,OP_DT,OP_SG) VALUES (?,?,?,TO_DATE(?,'yyyy-MM-dd HH24:MI:ss'),?)";
		String insertLogDetailSql = "INSERT INTO LOG_DETAIL (OP_ID,ROW_ID,OB_NM,OB_PID,TB_NM,OLD,NEW,FD_LST,OP_TP,TB_ROW_ID) VALUES (?,?,?,?,?,?,?,?,?,?)";
		String insertLogDetailGridSql = "INSERT INTO LOG_DETAIL_GRID (LOG_ROW_ID,GRID_ID,GRID_TYPE) VALUES (?,?,?)";
		PreparedStatement perstmtLogOperation = conn.prepareStatement(insertLogOperationSql);
		PreparedStatement perstmtLogDetail = conn.prepareStatement(insertLogDetailSql);
		PreparedStatement perstmtLogDetailGrid = conn.prepareStatement(insertLogDetailGridSql);
		perstmtList.add(perstmtLogOperation);
		perstmtList.add(perstmtLogDetail);
		perstmtList.add(perstmtLogDetailGrid);

		if(basicObjs!=null&&basicObjs.size()>0){
			Date opDt = new Date();
			for(BasicObj basicObj:basicObjs){
				//主表
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				//UUID
				String opId = UuidUtils.genUuid();
				perstmtLogOperation.setString(1, opId);
				perstmtLogOperation.setLong(2, userId);
				perstmtLogOperation.setString(3, opCmd);
				perstmtLogOperation.setString(4, format.format(opDt));
				perstmtLogOperation.setInt(5, opSg);
				perstmtLogOperation.addBatch();

				//子表
				for(Entry<String, List<BasicRow>> entry:basicObj.getSubrows().entrySet()){
					List<BasicRow> subrows = entry.getValue();
					for(BasicRow subrow:subrows){
						//新增
						if(subrow.getOpType().equals(OperationType.INSERT)){
							//UUID
							String logDetailRowId = UuidUtils.genUuid();
							assembleLogDetail(conn,basicObj,subrow,null,logDetailRowId,opId,perstmtLogDetail);
							//log_detail_grid
							assembleLogDetailGrid(conn,basicObj,subrow,logDetailRowId,perstmtLogDetailGrid);
						}
						//修改
						else if(subrow.getOpType().equals(OperationType.UPDATE)){
							Map<String,Object> oldValues = subrow.getOldValues();
							if(oldValues!=null&&!oldValues.isEmpty()){
								for(Entry<String, Object> oldValue:oldValues.entrySet()){
									//UUID
									String logDetailRowId = UuidUtils.genUuid();
									assembleLogDetail(conn,basicObj,subrow,oldValue,logDetailRowId,opId,perstmtLogDetail);
									//log_detail_grid
									assembleLogDetailGrid(conn,basicObj,subrow,logDetailRowId,perstmtLogDetailGrid);
								}
							}
						}
						//删除
						else if(subrow.getOpType().equals(OperationType.DELETE)){
							//UUID
							String logDetailRowId = UuidUtils.genUuid();
							assembleLogDetail(conn,basicObj,subrow,null,logDetailRowId,opId,perstmtLogDetail);
							//log_detail_grid
							assembleLogDetailGrid(conn,basicObj,subrow,logDetailRowId,perstmtLogDetailGrid);
						}
					}
				}
			}
		}
		
		return perstmtList;
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
	 * @throws SQLException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	private static void assembleLogDetail(Connection conn, BasicObj basicObj, BasicRow subrow,
			Entry<String, Object> oldValue, String logDetailRowId,String opId,PreparedStatement perstmtLogDetail) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		perstmtLogDetail.setString(1, opId);
		perstmtLogDetail.setString(2, logDetailRowId);
		perstmtLogDetail.setString(3, basicObj.objType());
		perstmtLogDetail.setLong(4, basicObj.objPid());
		perstmtLogDetail.setString(5, subrow.tableName());

		if(subrow.getOpType().equals(OperationType.DELETE)){
			perstmtLogDetail.setString(6,null);
			perstmtLogDetail.setString(7,null);
			perstmtLogDetail.setString(8, null);
			perstmtLogDetail.setInt(9, 2);
		}else if(subrow.getOpType().equals(OperationType.INSERT)){
			perstmtLogDetail.setString(6,null);
			perstmtLogDetail.setString(7,null);
			perstmtLogDetail.setString(8, null);
			perstmtLogDetail.setInt(9, 1);
		}else if(subrow.getOpType().equals(OperationType.UPDATE)){
			if(oldValue.getValue() instanceof String||oldValue.getValue() instanceof Integer){
				perstmtLogDetail.setString(6, oldValue.getValue().toString());
				perstmtLogDetail.setString(7, subrow.getAttrByColName(oldValue.getKey()).toString());
			}else if(oldValue.getValue() instanceof Date){
				perstmtLogDetail.setString(6, format.format(oldValue.getValue()));
				perstmtLogDetail.setString(7, format.format(subrow.getAttrByColName(oldValue.getKey())));
			}else if(oldValue.getValue() instanceof Geometry){
				perstmtLogDetail.setString(6, GeoTranslator.jts2Wkt((Geometry) oldValue.getValue()));
				perstmtLogDetail.setString(7, GeoTranslator.jts2Wkt((Geometry) subrow.getAttrByColName(oldValue.getKey())));
			}
			perstmtLogDetail.setString(8, oldValue.getKey());
			perstmtLogDetail.setInt(9, 3);
		}else{
			perstmtLogDetail.setString(6,null);
			perstmtLogDetail.setString(7,null);
			perstmtLogDetail.setString(8, null);
			perstmtLogDetail.setInt(9, 0);
		}
		
		perstmtLogDetail.setString(10, subrow.getRowId());
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
	private static void assembleLogDetailGrid(Connection conn, BasicObj basicObj, BasicRow subrow, String logDetailRowId,
			PreparedStatement perstmtLogDetailGrid) throws Exception {
		BasicObjGrid grid = basicObj.getGrid();
		long geoPid = subrow.getGeoPid();
		if(subrow.getGeoPid()!=basicObj.objPid()){
			BasicObj referObj = ObjSelector.selectByPid(conn, basicObj.objType(), null, subrow.getGeoPid(), true);
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
	
	
	public static void writeLog(Connection conn,Collection<BasicObj> basicObjs,String opCmd,int opSg,long userId)throws Exception{
		//获得log PreparedStatement list
		List<PreparedStatement> preparedStatementList = LogGenerator.generate(conn, basicObjs, opCmd, opSg, userId);
		//执行log preparedStatement List
		for(PreparedStatement preparedStatement:preparedStatementList){
			preparedStatement.executeBatch();
		}
	}
}
