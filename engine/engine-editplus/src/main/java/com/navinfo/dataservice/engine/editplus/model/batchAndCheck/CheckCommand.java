package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

public class CheckCommand extends BasicCommand {
	//isErrorReturn为ture，表示有错误log，则直接停止后续检查；false则继续执行，最后检查结果统一返回
	//isSaveResult=true，则检查结果保存；否则不保存检查结果
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
