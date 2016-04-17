package com.navinfo.dataservice.diff.scanner;


import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: FillLeftDeleteLogDetail 
 * @author Xiao Xiaowen 
 * @date 2016-1-14 下午2:54:40 
 * @Description: TODO
 */
public class FillLeftDeleteLogDetail implements ResultSetHandler<String> {
	protected Logger log = Logger.getLogger(this.getClass());
	private GlmTable table;
	private DataSource dataSource;
	private QueryRunner runner = new QueryRunner();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public FillLeftDeleteLogDetail(GlmTable table,DataSource dataSource){
		this.table=table;
		this.dataSource=dataSource;
	}

	@Override
	public String handle(ResultSet rs) throws SQLException {

		return null;
	}

}
