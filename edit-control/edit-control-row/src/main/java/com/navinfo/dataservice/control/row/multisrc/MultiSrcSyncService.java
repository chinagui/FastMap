package com.navinfo.dataservice.control.row.multisrc;

import org.apache.log4j.Logger;

/** 
 * @ClassName: MultiSrcSyncService
 * @author xiaoxiaowen4127
 * @date 2016年11月15日
 * @Description: MultiSrcSyncService.java
 */
public class MultiSrcSyncService {
	protected Logger log = Logger.getLogger(this.getClass());
	
	private volatile static MultiSrcSyncService instance=null;
	public MultiSrcSyncService getInstance(){
		if(instance==null){
			synchronized(MultiSrcSyncService.class){
				if(instance==null){
					instance=new MultiSrcSyncService();
				}
			}
		}
		return instance;
	}
	
	public void insert(MultiSrcFmSync obj)throws Exception{
		//
	}
	
	/**
	 * @param zipUrl
	 * @return:jobId
	 * @throws Exception
	 */
	public int applyUploadDay(String zipUrl)throws Exception{
		//创建ms->fm day的job,获取jobId
		//insert
		return 0;
	}
	
}
