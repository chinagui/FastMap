package com.navinfo.dataservice.diff.job;

import java.util.Map;

import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

/** 
 * @ClassName: DiffOperationCommand
 * @author xiaoxiaowen4127
 * @date 2017年8月31日
 * @Description: DiffOperationCommand.java
 */
public class DiffOperationCommand extends AbstractCommand {
	protected Map<Long,BasicObj> leftObjs;
	protected Map<Long,BasicObj> rightObjs;
	public DiffOperationCommand(Map<Long,BasicObj> leftObjs,Map<Long,BasicObj> rightObjs){
		this.leftObjs=leftObjs;
		this.rightObjs=rightObjs;
	}
	public Map<Long, BasicObj> getLeftObjs() {
		return leftObjs;
	}
	public void setLeftObjs(Map<Long, BasicObj> leftObjs) {
		this.leftObjs = leftObjs;
	}
	public Map<Long, BasicObj> getRightObjs() {
		return rightObjs;
	}
	public void setRightObjs(Map<Long, BasicObj> rightObjs) {
		this.rightObjs = rightObjs;
	}
}
