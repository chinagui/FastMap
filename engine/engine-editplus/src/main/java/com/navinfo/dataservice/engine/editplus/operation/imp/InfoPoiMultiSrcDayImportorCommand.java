package com.navinfo.dataservice.engine.editplus.operation.imp;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

/** 
 * @ClassName: MultiSrcPoiDayImportorCommand
 * @author xiaoxiaowen4127
 * @date 2016年11月28日
 * @Description: MultiSrcPoiDayImportorCommand.java
 */
public class InfoPoiMultiSrcDayImportorCommand extends AbstractCommand {
	
	private int dbId;
	
	protected MultiSrcUploadPois pois;

	public InfoPoiMultiSrcDayImportorCommand(int dbId,MultiSrcUploadPois pois){
		this.dbId=dbId;
		this.pois=pois;
	}
	
	public MultiSrcUploadPois getPois() {
		return pois;
	}
	public void setPois(MultiSrcUploadPois pois) {
		this.pois = pois;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}
}
