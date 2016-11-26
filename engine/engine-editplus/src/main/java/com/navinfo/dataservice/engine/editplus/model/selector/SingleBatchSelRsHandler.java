package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.editplus.glm.GlmColumn;
import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.dataservice.engine.editplus.utils.ResultSetGetter;

import oracle.sql.STRUCT;

/** 
 * selector出来的row为UPDATE状态
 * @ClassName: SingleBatchSelRsHandler
 * @author songdongyan
 * @date 2016年11月17日
 * @Description: SingleBatchSelRsHandler.java
 */
public class SingleBatchSelRsHandler implements ResultSetHandler<List<BasicRow>>{

	private GlmTable glmTable;
	public SingleBatchSelRsHandler(GlmTable glmTable){
		this.glmTable=glmTable;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
	 */
	@Override
	public List<BasicRow> handle(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		List<BasicRow> list = new ArrayList<BasicRow>();
		try{
			while(rs.next()){
				long objPid = rs.getLong("OBJ_PID");
				BasicRow row = (BasicRow)Class.forName(glmTable.getModelClassName()).getConstructor(new Class[]{long.class}).newInstance(objPid);
				for(Map.Entry<String, GlmColumn> entry:glmTable.getColumns().entrySet()){
					ResultSetGetter.setAttrByCol(rs, row, entry.getValue());
				}
				if(rs.getInt("U_RECORD")==2){
					row.setOpType(OperationType.PRE_DELETED);
				}else{
					row.setOpType(OperationType.UPDATE);
				}
				list.add(row);
			}
			return list;
		}catch(Exception e){
			throw new SQLException(e.getMessage(),e);
		}
	}

}
