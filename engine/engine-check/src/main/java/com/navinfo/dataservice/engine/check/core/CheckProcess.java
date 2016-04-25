package com.navinfo.dataservice.engine.check.core;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IProcess;

/** 
 * @ClassName: CheckProcess.java 
 * @author MaYunFei
 * @date 2016年4月19日
 * @Description: 
 */
public class CheckProcess {
	private IProcess process;

	public CheckProcess(IProcess process) {
		super();
		this.process = process;
	};
	public ICommand getCommand(){
		return this.process.getCommand();
	}
	
	
}

