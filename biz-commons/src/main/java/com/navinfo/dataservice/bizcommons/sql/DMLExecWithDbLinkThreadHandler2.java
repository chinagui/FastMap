package com.navinfo.dataservice.bizcommons.sql;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

/**
 * 执行DML语句，支持关闭SQL语句中使用到的DbLink，暂时只支持关闭一个DbLink
 * 
 * @author liuqing 。
 */
public class DMLExecWithDbLinkThreadHandler2 extends DMLExecThreadHandler2 {

	public DMLExecWithDbLinkThreadHandler2(CountDownLatch doneSignal,
			List<String> sqlList,
			DataSource ds, ThreadLocalContext ctx) {
		super(doneSignal, sqlList, ds, ctx);

	}
	
	/**
	 * 从sqlList中取其中用到的DbLink，暂时只支持取一个DbLink
	 */
	@Override
	public String getConnDbLink() {
		for(String sql:sqlList){
			int index=sql.indexOf("@");
			if(index>-1){
				return sql.substring(index+1).split(" ")[0];
			}
		}
		return null;
	}

}
