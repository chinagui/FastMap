package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.engine.editplus.glm.GlmColumn;
import com.navinfo.dataservice.engine.editplus.glm.GlmObject;
import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.utils.ResultSetGetter;

/** 
 * @ClassName: SelectorResultSetHandler
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: SelectorResultSetHandler.java
 */
public class SelRsHandler implements ResultSetHandler<BasicRow> {
	private GlmTable glmTable;
	private long objPid;
	public SelRsHandler(GlmTable glmTable,long objPid){
		this.glmTable=glmTable;
		this.objPid=objPid;
	}
	@Override
	public BasicRow handle(ResultSet rs) throws SQLException {
		BasicRow row = null;
		try{
			row = (BasicRow)Class.forName(glmTable.getModelClassName()).getConstructor(new Class[]{long.class}).newInstance(objPid);
			while(rs.next()){
				for(Map.Entry<String, GlmColumn> entry:glmTable.getColumns().entrySet()){
					
					if(GlmColumn.TYPE_NUMBER.equals(entry.getValue().getType())){
						if(entry.getValue().getDataScale()>0){
							row.setAttrByCol(entry.getKey(), rs.getDouble(entry.getKey()));
						}
						if(entry.getValue().getDataPrecision()>8){
							
						}
					}
					row.setAttrByCol(entry.getKey(), ResultSetGetter.getValue(rs, entry.getValue()));
				}
			}
			return row;
		}catch(Exception e){
			throw new SQLException(e.getMessage(),e);
		}
	}

}
