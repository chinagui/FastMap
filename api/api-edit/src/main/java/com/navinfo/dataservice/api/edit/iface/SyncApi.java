package com.navinfo.dataservice.api.edit.iface;

import com.navinfo.dataservice.api.edit.model.FmMultiSrcSync;

/**
 * POI数据同步
 * @ClassName SyncApi
 * @author Han Shaoming
 * @date 2016年11月18日 下午3:16:48
 * @Description TODO
 */
public interface SyncApi {
	
	/**
	 * 创建FM-POI增量包同步到多源的管理记录
	 * @author Han Shaoming
	 * @param jobId
	 * @param syncTime 
	 * @return
	 * @throws Exception 
	 */
	public String insertFmMultiSrcSync(long jobId, String syncTime) throws Exception;
	
	/**
	 * 查询POI增量包同步到多源最新成功的数据
	 * @author Han Shaoming
	 * @return
	 * @throws Exception 
	 */
	public FmMultiSrcSync queryLastSuccessSync() throws Exception;
	
	/**
	 * 更新FmMultiSrcSync管理表的同步状态
	 * @author Han Shaoming
	 * @param syncStatus
	 * 同步状态:开始创建1，创建中2，已创建8，创建失败9，多源同步成功18，多源同步失败19
	 * @throws Exception 
	 */
	public void updateFmMultiSrcSyncStatus(long syncStatus) throws Exception;
	
	/**
	 * 更新FmMultiSrcSync管理表中增量包文件和同步状态
	 * @author Han Shaoming
	 * @param syncStatus
	 * 同步状态:开始创建1，创建中2，已创建8，创建失败9，多源同步成功18，多源同步失败19
	 * @param zipFile
	 * @throws Exception 
	 */
	public void updateFmMultiSrcSync(long syncStatus,String zipFile) throws Exception;
	
	/**
	 * 创建MS-POI增量包同步到FM的管理记录
	 * @author Han Shaoming
	 * @param jobId
	 * @param dbType
	 * @return
	 * @throws Exception 
	 */
	public String insertMultiSrcFmSync(long jobId,long dbType) throws Exception;
	
	/**
	 * 更新MultiSrcFmSync管理表的同步状态
	 * @author Han Shaoming
	 * @param syncStatus
	 * 同步状态:已接收1，导入中2 下载成功3，下载失败4，,导入成功5,导入失败6,反馈多源成功11，反馈多源失败12
	 * @throws Exception 
	 */
	public void updateMultiSrcFmSyncStatus(long syncStatus) throws Exception;
	
	/**
	 * 更新FmMultiSrcSync管理表中增量包文件和同步状态
	 * @author Han Shaoming
	 * @param syncStatus
	 * 同步状态:已接收1，导入中2 下载成功3，下载失败4，,导入成功5,导入失败6,反馈多源成功11，反馈多源失败12
	 * @param zipFile
	 * @throws Exception 
	 */
	public void updateMultiSrcFmSync(long syncStatus,String zipFile) throws Exception;
	
}
