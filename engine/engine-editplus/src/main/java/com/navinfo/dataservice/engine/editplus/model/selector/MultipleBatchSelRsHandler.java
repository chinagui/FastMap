package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.utils.ResultSetGetter;

/** 
 * @ClassName: MultipleBatchSelRsHandler
 * @author songdongyan
 * @date 2016年11月17日
 * @Description: MultipleBatchSelRsHandler.java
 */
public class MultipleBatchSelRsHandler implements ResultSetHandler<Map<Long,List<BasicRow>>> {

	private GlmTable glmTable;
	private long objPid;
	public MultipleBatchSelRsHandler(GlmTable glmTable,long objPid){
		this.glmTable=glmTable;
		this.objPid=objPid;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
	 */
	@Override
	public Map<Long, List<BasicRow>> handle(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		Map<Long,List<BasicRow>> map = new HashMap<Long,List<BasicRow>>();
		List<BasicRow> basicRowList = new ArrayList<BasicRow>();
		try{
			while(rs.next()){
				BasicRow row = (BasicRow)Class.forName(glmTable.getModelClassName()).getConstructor(new Class[]{long.class}).newInstance(objPid);
				for(Map.Entry<String, String> entry:glmTable.getColumns().entrySet()){
					row.setAttrByCol(entry.getKey(), ResultSetGetter.getValue(rs, entry.getValue()));
				}
				basicRowList.add(row);
			}
			return basicRowList;
		}catch(Exception e){
			throw new SQLException(e.getMessage(),e);
		}
	}

}
