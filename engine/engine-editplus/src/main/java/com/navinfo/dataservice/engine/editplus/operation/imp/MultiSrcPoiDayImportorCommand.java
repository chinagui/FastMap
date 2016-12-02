package com.navinfo.dataservice.engine.editplus.operation.imp;

import com.navinfo.dataservice.api.edit.upload.UploadPois;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

/** 
 * @ClassName: MultiSrcPoiDayImportorCommand
 * @author xiaoxiaowen4127
 * @date 2016年11月28日
 * @Description: MultiSrcPoiDayImportorCommand.java
 */
public class MultiSrcPoiDayImportorCommand extends AbstractCommand {
	
	protected UploadPois pois;

	public MultiSrcPoiDayImportorCommand(UploadPois pois){
		this.pois=pois;
	}
	
	public UploadPois getPois() {
		return pois;
	}
	public void setPois(UploadPois pois) {
		this.pois = pois;
	}
}
