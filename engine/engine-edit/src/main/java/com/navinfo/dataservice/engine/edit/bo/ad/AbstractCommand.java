package com.navinfo.dataservice.engine.edit.bo.ad;

import net.sf.json.JSONObject;

/** 
 * @ClassName: AbstractCommand
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: AbstractCommand.java
 */
public abstract class AbstractCommand {
	public abstract void validate()throws Exception;
	public void parse(JSONObject data){
		//...
	}
}
