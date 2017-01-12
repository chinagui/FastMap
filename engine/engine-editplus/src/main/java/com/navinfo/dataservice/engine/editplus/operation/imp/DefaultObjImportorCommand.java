package com.navinfo.dataservice.engine.editplus.operation.imp;

import com.navinfo.dataservice.api.edit.upload.EditJson;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

public class DefaultObjImportorCommand extends AbstractCommand {

	protected EditJson editJson;

	public DefaultObjImportorCommand(EditJson editJson) {
		this.editJson = editJson;
	}

	public EditJson getEditJson() {
		return editJson;
	}

	public void setEditJson(EditJson editJson) {
		this.editJson = editJson;
	}
	
	

}
