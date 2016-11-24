package com.navinfo.dataservice.engine.editplus.log;

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
	public static List<LogOperation> generate(Collection<BasicObj> basicObjs,boolean isOneOperation,String opCmd,int opSg,long userId)throws Exception{
		String geoOpId = UuidUtils.genUuid();//先生成一个几何的统一uuid,如果有设计到几何变化,使用该uuid
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
								
							}
							
						}
					}
				}
				
//				if(row.isGeoChanged()){
//					op.setOpId(geoOpId);
//				}else{
//					op.setOpId(UuidUtils.genUuid());
//				}
			}
		}
		return logs;
	}

	
}
