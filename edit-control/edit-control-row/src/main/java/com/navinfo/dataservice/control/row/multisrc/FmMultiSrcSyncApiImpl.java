package com.navinfo.dataservice.control.row.multisrc;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.edit.iface.FmMultiSrcSyncApi;
import com.navinfo.dataservice.api.edit.model.FmMultiSrcSync;
import com.navinfo.dataservice.api.edit.model.MultiSrcFmSync;
import com.navinfo.dataservice.commons.util.DateUtils;

/**
 * POI数据同步
 * @ClassName FmMultiSrcSyncApiImpl
 * @author Han Shaoming
 * @date 2016年11月18日 下午4:55:15
 * @Description TODO
 */
@Service("fmMultiSrcSyncApi")
public class FmMultiSrcSyncApiImpl implements FmMultiSrcSyncApi {

	//创建FM-POI增量包同步到多源的管理记录
	@Override
	public String insertFmMultiSrcSync(long jobId,String syncTime) throws Exception {
		
		FmMultiSrcSync obj = new FmMultiSrcSync();
		obj.setJobId(jobId);
		Date date = DateUtils.stringToDate(syncTime, DateUtils.DATE_COMPACTED_FORMAT);
		obj.setSyncTime(date);
		String msg = FmMultiSrcSyncService.getInstance().insert(obj);
		return msg;
	}

	//查询POI增量包同步到多源最新成功的数据
	@Override
	public FmMultiSrcSync queryLastSuccessSync() throws Exception {
		
		FmMultiSrcSync fmMultiSrcSync = FmMultiSrcSyncService.getInstance().queryLastSuccessSync();
		return fmMultiSrcSync;
	}

	//更新FmMultiSrcSync管理表的同步状态
	@Override
	public void updateFmMultiSrcSyncStatus(int syncStatus,long jobId) throws Exception {
		
		FmMultiSrcSync obj = new FmMultiSrcSync();
		obj.setSyncStatus(syncStatus);
		FmMultiSrcSyncService.getInstance().updateSync(obj);
	}

	//更新FmMultiSrcSync管理表中增量包文件和同步状态
	@Override
	public void updateFmMultiSrcSync(int syncStatus, String zipFile,long jobId) throws Exception {
		
		FmMultiSrcSync obj = new FmMultiSrcSync();
		obj.setSyncStatus(syncStatus);
		obj.setZipFile(zipFile);
		FmMultiSrcSyncService.getInstance().updateSync(obj);
	}

	//创建MS-POI增量包同步到FM的管理记录
	@Override
	public String insertMultiSrcFmSync(long jobId, int dbType,String zipFile) throws Exception {
		MultiSrcFmSync obj = new MultiSrcFmSync();
		obj.setJobId(jobId);
		obj.setDbType(dbType);
		obj.setZipFile(zipFile);
		String msg = MultiSrcFmSyncService.getInstance().insert(obj);
		return msg;
	}

	//更新MultiSrcFmSync管理表的同步状态
	@Override
	public void updateMultiSrcFmSyncStatus(int syncStatus,long jobId) throws Exception {
		MultiSrcFmSync obj = new MultiSrcFmSync();
		obj.setSyncStatus(syncStatus);
		MultiSrcFmSyncService.getInstance().updateSync(obj);
	}

	//更新FmMultiSrcSync管理表中增量包文件和同步状态
	/*@Override
	public void updateMultiSrcFmSync(long syncStatus, String zipFile) throws Exception {
		MultiSrcFmSync obj = new MultiSrcFmSync();
		obj.setSyncStatus(syncStatus);
		obj.setZipFile(zipFile);
		MultiSrcFmSyncService.getInstance().updateSync(obj);
	}*/

}
