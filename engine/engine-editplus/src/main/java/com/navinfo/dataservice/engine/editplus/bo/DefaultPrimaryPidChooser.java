package com.navinfo.dataservice.engine.editplus.bo;

import com.navinfo.dataservice.engine.editplus.operation.OperationResult;

/** 
 * @ClassName: DefaultPrimaryPidChooser
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: DefaultPrimaryPidChooser.java
 */
public class DefaultPrimaryPidChooser {
	
	public long choose(OperationResult result){
		if(result.getAddObjs().size()>0){
			return result.getAddObjs().get(0).objPid();
		}
		if(result.getUpdateObjs().size()>0){
			return result.getUpdateObjs().get(0).objPid();
		}
		if(result.getDelObjs().size()>0){
			return result.getDelObjs().get(0).objPid();
		}
		return 0L;
	}
}
