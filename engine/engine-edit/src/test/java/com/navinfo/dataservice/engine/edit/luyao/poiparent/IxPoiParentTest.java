package com.navinfo.dataservice.engine.edit.luyao.poiparent;



import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class IxPoiParentTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	
	@Test
	public void testCreateParent1()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOIPARENT\",\"dbId\":42,\"objId\":3494798,\"parentPid\":4697345}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateParent2()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOIPARENT\",\"dbId\":42,\"objId\":3945327,\"parentPid\":4697345}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateParent3()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOIPARENT\",\"dbId\":42,\"objId\":3945328,\"parentPid\":4697345}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateParent1()
	{
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"IXPOIPARENT\",\"dbId\":42,\"objId\":3945327,\"parentPid\":3945330}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateParent2()
	{
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"IXPOIPARENT\",\"dbId\":42,\"objId\":3945327,\"parentPid\":4697345}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDeleteParent()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"IXPOIPARENT\",\"dbId\":42,\"objId\":3494798}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
