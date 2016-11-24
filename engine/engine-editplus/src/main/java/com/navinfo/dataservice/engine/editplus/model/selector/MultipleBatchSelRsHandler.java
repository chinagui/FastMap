package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.engine.editplus.glm.GlmColumn;
import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.dataservice.engine.editplus.utils.ResultSetGetter;

/** 
 * selector出来的row为UPDATE状态
 * @ClassName: MultipleBatchSelRsHandler
 * @author songdongyan
 * @date 2016年11月17日
 * @Description: MultipleBatchSelRsHandler.java
 */
public class MultipleBatchSelRsHandler implements ResultSetHandler<Map<Long,List<BasicRow>>> {

	private GlmTable glmTable;
	public MultipleBatchSelRsHandler(GlmTable glmTable){
		this.glmTable=glmTable;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
	 */
	@Override
	public Map<Long, List<BasicRow>> handle(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		Map<Long,List<BasicRow>> map = new HashMap<Long,List<BasicRow>>();
		try{
			while(rs.next()){
				long objPid = rs.getLong("OBJ_PID");
				BasicRow row = (BasicRow)Class.forName(glmTable.getModelClassName()).getConstructor(new Class[]{long.class}).newInstance(objPid);
				for(Map.Entry<String, GlmColumn> entry:glmTable.getColumns().entrySet()){
					ResultSetGetter.setAttrByCol(rs, row, entry.getValue());
				}
				
				List<BasicRow> basicRowList;
				if(map.containsKey(objPid)){
					basicRowList = map.get(objPid);		
				}else{
					basicRowList = new ArrayList<BasicRow>();
					map.put(objPid, basicRowList);
				}
				//selector出来的row为UPDATE状态
				row.setOpType(OperationType.UPDATE);
				basicRowList.add(row);
			}
			return map;
		}catch(Exception e){
			throw new SQLException(e.getMessage(),e);
		}
	}

}
