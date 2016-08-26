package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: CHECK_RDELECTRONICEYE_MESHMOVE
 * @author songdongyan
 * @date 2016年8月25日
 * @Description: 不允许跨图幅移动
 */
public class CHECK_RDELECTRONICEYE_MESHMOVE extends baseRule {

	/**
	 * 
	 */
	public CHECK_RDELECTRONICEYE_MESHMOVE() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow obj:checkCommand.getGlmList()){
			//获取新建RdBranch信息
			if(obj instanceof RdElectroniceye ){
				RdElectroniceye rdElectroniceye = (RdElectroniceye)obj;
				Map<String, Object> changedFields = rdElectroniceye.changedFields();
				if(changedFields.isEmpty()){
					continue;
				}
				
				if(!changedFields.containsKey("meshId")){
					continue;
				}
				
				int meshId = rdElectroniceye.getMeshId();
				int changeMeshId = (int) changedFields.get("meshId");
				
				if (meshId != changeMeshId){
					this.setCheckResult("", "", 0);
					return;
				}

			}
		}

	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
