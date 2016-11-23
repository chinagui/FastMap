package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

public class CheckCommand extends BasicCommand {
	
	private boolean isSaveResult=true;
	private boolean isErrorReturn=false;

	public CheckCommand() {
		// TODO Auto-generated constructor stub
	}

	public boolean isSaveResult() {
		return isSaveResult;
	}

	public void setSaveResult(boolean isSaveResult) {
		this.isSaveResult = isSaveResult;
	}

	public boolean isErrorReturn() {
		return isErrorReturn;
	}

	public void setErrorReturn(boolean isErrorReturn) {
		this.isErrorReturn = isErrorReturn;
	}

}
