package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectName;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: GLM28059
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: RD_OBJECT_NAME表中不能存在名称内容为空的记录，否则报log
 */
public class GLM28059 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//RdObjectName新增修改都会触发
			if (obj instanceof RdObjectName){
				RdObjectName rdObjectName = (RdObjectName)obj;
				checkRdObjectName(rdObjectName);
			}
		}
		
	}

	/**
	 * @param rdObjectName
	 */
	private void checkRdObjectName(RdObjectName rdObjectName) {
		String target = "[RD_ROAD," + rdObjectName.getPid() + "]";
		//新增RdObjectName
		if(rdObjectName.status().equals(ObjStatus.INSERT)){
			if(rdObjectName.getName()==null||rdObjectName.getName().isEmpty()){
				this.setCheckResult("", target, 0);
			}
		}
		if(rdObjectName.status().equals(ObjStatus.UPDATE)){
			if(rdObjectName.changedFields().containsKey("name")){
				if(rdObjectName.changedFields().get("name")==null||rdObjectName.changedFields().get("name").toString().isEmpty()){
					this.setCheckResult("", target, 0);
				}
			}
		}
	}

}
