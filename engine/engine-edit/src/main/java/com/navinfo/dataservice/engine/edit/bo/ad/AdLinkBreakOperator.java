package com.navinfo.dataservice.engine.edit.bo.ad;

import com.navinfo.dataservice.dao.glm.iface.Result;

import net.sf.json.JSONObject;

/** 
 * @ClassName: AdLinkBreakOperator
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: AdLinkBreakOperator.java
 */
public class AdLinkBreakOperator extends AbstractOperator {
	
	@Override
	public void createCmd(JSONObject data){
		this.cmd=new AdLinkBreakCommand();
		this.cmd.parse(data);
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.bo.ad.AbstractOperator#loadData()
	 */
	@Override
	public void loadData() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.bo.ad.AbstractOperator#execute()
	 */
	@Override
	public Result execute() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
