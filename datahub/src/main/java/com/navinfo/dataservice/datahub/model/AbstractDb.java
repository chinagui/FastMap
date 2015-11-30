package com.navinfo.dataservice.datahub.model;

/** 
 * @ClassName: AbstractDb 
 * @author Xiao Xiaowen 
 * @date 2015-11-26 下午3:12:50 
 * @Description: TODO
 *  
 */
public abstract class AbstractDb {
	protected int innerId;
	protected String publicIdentity;
	protected int type=DbType.TYPE_NONE;
	protected DbServer dbServer;
	
	public abstract boolean isAdminDb()throws Exception;
	public abstract AbstractDb getAdminDb()throws Exception;
	
	/**
	 * 在DbServer上创建数据库
	 * @return
	 * @throws Exception
	 */
	public abstract boolean create(AbstractDb adminDb)throws Exception;
}
