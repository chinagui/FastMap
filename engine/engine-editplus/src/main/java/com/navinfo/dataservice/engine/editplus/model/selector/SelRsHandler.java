package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
 * @ClassName: SelectorResultSetHandler
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: SelectorResultSetHandler.java
 */
public class SelRsHandler implements ResultSetHandler<BasicRow> {
	private String tableName;
	public SelRsHandler(String tableName){
		this.tableName=tableName;
	}
	@Override
	public BasicRow handle(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
