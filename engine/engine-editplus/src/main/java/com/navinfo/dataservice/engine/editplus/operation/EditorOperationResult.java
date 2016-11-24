package com.navinfo.dataservice.engine.editplus.operation;

/** 
 * @ClassName: EditorOperationResult
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: EditorOperationResult.java
 */
public class EditorOperationResult extends OperationResult {

	protected long primaryPid;//一次操作的主要对象，前台需要用于操作结束后显示属性栏的，一次操作涉及多个Obj时，没有给定具体的原则设置为哪个Obj的pid

	public long getPrimaryPid() {
		return primaryPid;
	}

	public void setPrimaryPid(long primaryPid) {
		this.primaryPid = primaryPid;
	}
}
