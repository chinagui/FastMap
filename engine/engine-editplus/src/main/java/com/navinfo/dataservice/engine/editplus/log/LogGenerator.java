package com.navinfo.dataservice.engine.editplus.log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObjGrid;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjFactory;
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
	public static List<LogOperation> generate(Connection conn,Collection<BasicObj> basicObjs,boolean isOneOperation,String opCmd,int opSg,long userId)throws Exception{
//		String geoOpId = UuidUtils.genUuid();//先生成一个几何的统一uuid,如果有设计到几何变化,使用该uuid
		List<LogOperation> logs = new ArrayList<LogOperation>();
		if(basicObjs!=null&&basicObjs.size()>0){
			Date opDt = new Date();
			for(BasicObj basicObj:basicObjs){
				LogOperation op = new LogOperation();
				//主表
				op.setOpCmd(opCmd);
				op.setOpDt(opDt);
				op.setOpSg(opSg);
				op.setUsId(userId);
				//UUID
				String opId = UuidUtils.genUuid();
				op.setOpId(opId);
				
				List<LogDetail> details = new ArrayList<LogDetail>();
				//子表
				for(Entry<String, List<BasicRow>> entry:basicObj.getSubrows().entrySet()){
					List<BasicRow> subrows = entry.getValue();
					for(BasicRow subrow:subrows){
						Map<String,Object> oldValues = subrow.getOldValues();
						if(!oldValues.isEmpty()){
							for(Entry<String, Object> oldValue:oldValues.entrySet()){
								LogDetail logDetail = new LogDetail();
								logDetail.setOpId(opId);
								logDetail.setRowId(UuidUtils.genUuid());
								logDetail.setObNm(basicObj.objType());
								logDetail.setObPid(basicObj.objPid());
								logDetail.setTbNm(subrow.tableName());
								if(oldValue.getValue() instanceof String||oldValue.getValue() instanceof Integer){
									logDetail.setOld(oldValue.getValue().toString());
									logDetail.setNew(subrow.getAttrByColName(oldValue.getKey()).toString());
								}else if(oldValue.getValue() instanceof Date){
									logDetail.setOld(oldValue.getValue().toString().substring(0,10));
									logDetail.setNew(subrow.getAttrByColName(oldValue.getKey()).toString().substring(0,10));
								}else if(oldValue.getValue() instanceof Geometry){
									logDetail.setOld(GeoTranslator.jts2Wkt((Geometry) oldValue.getValue()));
									logDetail.setNew(GeoTranslator.jts2Wkt((Geometry) subrow.getAttrByColName(oldValue.getKey())));
								}
								logDetail.setFdLst(oldValue.getKey());
								logDetail.setOpTp(subrow.getOpType());
								logDetail.setTbRowId(subrow.getRowId());
								
								//log_detail_grid
								List<LogDetailGrid> grids = new ArrayList<LogDetailGrid>();
								if(subrow.getGeoPid()==basicObj.objPid()){
									BasicObjGrid grid = basicObj.getGrid();
									for(String gridId:grid.getGridListBefore()){
										LogDetailGrid logDetailGrid = new LogDetailGrid();
										logDetailGrid.setLogRowId(logDetail.getRowId());
										logDetailGrid.setGridId(Long.parseLong(gridId));
										logDetailGrid.setGridType(0);
										grids.add(logDetailGrid);
									}
									for(String gridId:grid.getGridListAfter()){
										LogDetailGrid logDetailGrid = new LogDetailGrid();
										logDetailGrid.setLogRowId(logDetail.getRowId());
										logDetailGrid.setGridId(Long.parseLong(gridId));
										logDetailGrid.setGridType(1);
										grids.add(logDetailGrid);
									}
								}else{
									BasicObj referObj = ObjSelector.selectByPid(conn, basicObj.objType(), null, subrow.getGeoPid(), true, false);
									BasicObjGrid grid = referObj.getGrid();
									for(String gridId:grid.getGridListBefore()){
										LogDetailGrid logDetailGrid = new LogDetailGrid();
										logDetailGrid.setLogRowId(logDetail.getRowId());
										logDetailGrid.setGridId(Long.parseLong(gridId));
										logDetailGrid.setGridType(0);
										grids.add(logDetailGrid);
									}
									for(String gridId:grid.getGridListAfter()){
										LogDetailGrid logDetailGrid = new LogDetailGrid();
										logDetailGrid.setLogRowId(logDetail.getRowId());
										logDetailGrid.setGridId(Long.parseLong(gridId));
										logDetailGrid.setGridType(1);
										grids.add(logDetailGrid);
									}
								}
								logDetail.setGrids(grids);
								details.add(logDetail);
							}
						}
					}
				}
				op.setDetails(details);
//				if(row.isGeoChanged()){
//					op.setOpId(geoOpId);
//				}else{
//					op.setOpId(UuidUtils.genUuid());
//				}
			}
		}
		return logs;
	}
	public static List<PreparedStatement> generate(Connection conn,Collection<BasicObj> basicObjs,String opCmd,int opSg,long userId)throws Exception{
		List<PreparedStatement> perstmtList = new ArrayList<PreparedStatement>();
		String insertLogOperationSql = "INSERT INTO LOG_OPERATION (OP_ID,US_ID,OP_CMD,OP_DT,OP_SG) VALUES (?,?,?,TO_DATE(?,'yyyy-MM-dd HH24:MI:ss'),?)";
		String insertLogDetailSql = "INSERT INTO LOG_DETAIL (OP_ID,ROW_ID,OB_NM,OB_PID,TB_NM,OLD,NEW,FD_LST,OP_TP,TB_ROW_ID) VALUES (?,?,?,?,?,?,?,?,?,?)";
		String insertLogDetailGridSql = "INSERT INTO LOG_DETAIL_GRID (LOG_ROE_ID,GRID_ID,GRID_TYPE) VALUES (?,?,?)";
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
						Map<String,Object> oldValues = subrow.getOldValues();
						if(!oldValues.isEmpty()){
							for(Entry<String, Object> oldValue:oldValues.entrySet()){
								//UUID
								String logDetailRowId = UuidUtils.genUuid();
								perstmtLogDetail.setString(1, opId);
								perstmtLogDetail.setString(2, logDetailRowId);
								perstmtLogDetail.setString(3, basicObj.objType());
								perstmtLogDetail.setLong(4, basicObj.objPid());
								perstmtLogDetail.setString(5, subrow.tableName());

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
								if(subrow.getOpType().equals(OperationType.DELETE)){
									perstmtLogDetail.setInt(9, 2);
								}else if(subrow.getOpType().equals(OperationType.INSERT)){
									perstmtLogDetail.setInt(9, 1);
								}else if(subrow.getOpType().equals(OperationType.UPDATE)){
									perstmtLogDetail.setInt(9, 3);
								}else{
									perstmtLogDetail.setInt(9, 0);
								}
								perstmtLogDetail.setString(10, subrow.getRowId());
								perstmtLogDetail.addBatch();
								
								//log_detail_grid
								BasicObjGrid grid = basicObj.getGrid();
								if(subrow.getGeoPid()!=basicObj.objPid()){
									BasicObj referObj = ObjSelector.selectByPid(conn, basicObj.objType(), null, subrow.getGeoPid(), true, false);
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
					}
				}
			}
		}
		return perstmtList;
	}
	
}
