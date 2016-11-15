package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
 * @ClassName: SelectorResultSetHandler
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: SelectorResultSetHandler.java
 */
public class BatchSelRsHandler implements ResultSetHandler<Map<Long,List<BasicRow>>> {

	@Override
	public Map<Long,List<BasicRow>> handle(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
