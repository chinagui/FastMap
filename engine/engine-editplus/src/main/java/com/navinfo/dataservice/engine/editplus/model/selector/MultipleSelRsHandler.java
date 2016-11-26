package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.glm.GlmColumn;
import com.navinfo.dataservice.dao.plus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.dataservice.engine.editplus.utils.ResultSetGetter;

import oracle.sql.STRUCT;

import org.apache.commons.dbutils.ResultSetHandler;

/** 
 * selector出来的row为UPDATE状态
 * @ClassName: MultipleSelRsHandler
 * @author songdongyan
 * @date 2016年11月17日
 * @Description: MultipleSelRsHandler.java
 */
public class MultipleSelRsHandler implements ResultSetHandler<List<BasicRow>> {
	private GlmTable glmTable;
	private long objPid;
	public MultipleSelRsHandler(GlmTable glmTable,long objPid){
		this.glmTable=glmTable;
		this.objPid=objPid;
	}

	@Override
	public List<BasicRow> handle(ResultSet rs) throws SQLException {
		List<BasicRow> basicRowList = new ArrayList<BasicRow>();
		try{
			while(rs.next()){
				BasicRow row = (BasicRow)Class.forName(glmTable.getModelClassName()).getConstructor(new Class[]{long.class}).newInstance(objPid);
				for(Map.Entry<String, GlmColumn> entry:glmTable.getColumns().entrySet()){
					ResultSetGetter.setAttrByCol(rs, row, entry.getValue());
				}
				//selector出来的row为UPDATE状态
				row.setOpType(OperationType.UPDATE);
				basicRowList.add(row);
			}
			return basicRowList;
		}catch(Exception e){
			throw new SQLException(e.getMessage(),e);
		}
	}

}
