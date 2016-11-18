package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.utils.ResultSetGetter;

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
			while(rs.next()){
				for(Map.Entry<String, String> entry:glmTable.getColumns().entrySet()){
//					row.setAttrByCol(entry.getKey(), ResultSetGetter.getValue(rs, entry.getValue()));
					row.setAttrByCol(entry.getKey(), ResultSetGetter.getValue(rs, entry.getKey(), entry.getValue()));
				}
			}
			return row;
		}catch(Exception e){
			throw new SQLException(e.getMessage(),e);
		}
	}
	
	

}
