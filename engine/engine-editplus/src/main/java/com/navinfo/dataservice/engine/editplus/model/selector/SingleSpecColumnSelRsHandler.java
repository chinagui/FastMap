package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.plus.glm.GlmColumn;
import com.navinfo.dataservice.dao.plus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.dataservice.engine.editplus.utils.ResultSetGetter;

/** 
 * @ClassName: SingleSpecColumnSelRsHandler
 * @author songdongyan
 * @date 2016年11月21日
 * @Description: SingleSpecColumnSelRsHandler.java
 */
public class SingleSpecColumnSelRsHandler implements ResultSetHandler<BasicRow>{

	private GlmTable glmTable;
	/**
	 * 
	 */
	public SingleSpecColumnSelRsHandler(GlmTable glmTable) {
		this.glmTable=glmTable;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
	 */
	@Override
	public BasicRow handle(ResultSet rs) throws SQLException {
		BasicRow row = null;
		try{
			if(rs.next()){
				long objPid = rs.getLong("OBJ_PID");
				row = (BasicRow)Class.forName(glmTable.getModelClassName()).getConstructor(new Class[]{long.class}).newInstance(objPid);
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
