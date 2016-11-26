package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.editplus.glm.GlmColumn;
import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.dataservice.engine.editplus.utils.ResultSetGetter;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/** 
 * @ClassName: SingleSelRsHandler
 * @author songdongyan
 * @date 2016年11月17日
 * @Description: SingleSelRsHandler.java
 */
public class SingleSelRsHandler implements ResultSetHandler<BasicRow> {

	private GlmTable glmTable;
	private long objPid;
	public SingleSelRsHandler(GlmTable glmTable,long objPid){
		this.glmTable=glmTable;
		this.objPid=objPid;
	}

	@Override
	public BasicRow handle(ResultSet rs) throws SQLException {
		BasicRow row = null;
		try{
			row = (BasicRow)Class.forName(glmTable.getModelClassName()).getConstructor(new Class[]{long.class}).newInstance(objPid);
			if(rs.next()){
				for(Map.Entry<String, GlmColumn> entry:glmTable.getColumns().entrySet()){
					ResultSetGetter.setAttrByCol(rs, row, entry.getValue());
				}
				if(rs.getInt("U_RECORD")==2){
					row.setOpType(OperationType.PRE_DELETED);
				}else{
					row.setOpType(OperationType.UPDATE);
				}
			}
			return row;
		}catch(Exception e){
			throw new SQLException(e.getMessage(),e);
		}
	}
	
	

}
