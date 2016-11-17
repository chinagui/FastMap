package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.utils.ResultSetGetter;

/** 
 * @ClassName: SingleBatchSelRsHandler
 * @author songdongyan
 * @date 2016年11月17日
 * @Description: SingleBatchSelRsHandler.java
 */
public class SingleBatchSelRsHandler implements ResultSetHandler<Map<Long,BasicRow>>{

	private GlmTable glmTable;
	private long objPid;
	public SingleBatchSelRsHandler(GlmTable glmTable,long objPid){
		this.glmTable=glmTable;
		this.objPid=objPid;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
	 */
	@Override
	public Map<Long, BasicRow> handle(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		Map<Long,BasicRow> map = new HashMap<Long,BasicRow>();
		try{
			while(rs.next()){
				BasicRow row = (BasicRow)Class.forName(glmTable.getModelClassName()).getConstructor(new Class[]{long.class}).newInstance(objPid);
				for(Map.Entry<String, String> entry:glmTable.getColumns().entrySet()){
					row.setAttrByCol(entry.getKey(), ResultSetGetter.getValue(rs, entry.getValue()));
				}
				map.put(objPid,row);
			}
			return map;
		}catch(Exception e){
			throw new SQLException(e.getMessage(),e);
		}
	}

}
