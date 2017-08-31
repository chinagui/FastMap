package com.navinfo.dataservice.dao.plus.operation;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

/** 
 * @ClassName: AbstractCommand
 * @author xiaoxiaowen4127
 * @date 2016年11月28日
 * @Description: AbstractCommand.java
 */
public class AbstractCommand {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected long userId=0;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
}
