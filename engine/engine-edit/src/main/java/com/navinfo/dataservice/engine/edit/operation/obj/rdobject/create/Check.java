package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.create;

import java.sql.Connection;

public class Check {
	
	private Command command;
	
	public Check(Command command)
	{
		this.command = command;
	}
	
	/**
	 * @param conn
	 * @throws Exception 
	 */
	public void hasRdObject(Connection conn) throws Exception {
	}
}
