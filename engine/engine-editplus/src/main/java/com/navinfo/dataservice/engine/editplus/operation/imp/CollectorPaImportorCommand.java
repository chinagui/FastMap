package com.navinfo.dataservice.engine.editplus.operation.imp;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

/**
 * Collector Pa导入类的请求参数类，Pa唯一标识使用fid
 * @ClassName: CollectorPaImportorCommand
 * @author zl
 * @date 2017年4月24日
 * @Description: CollectorPaImportorCommand.java
 */
public class CollectorPaImportorCommand extends AbstractCommand {
	
	private int dbId;
	
	protected CollectorUploadPas pas;

	public CollectorPaImportorCommand(int dbId,CollectorUploadPas pas){
		this.dbId=dbId;
		this.pas=pas;
	}
	
	public CollectorUploadPas getPas() {
		return pas;
	}
	public void setPas(CollectorUploadPas pas) {
		this.pas = pas;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	
}
