package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class RdLinkTest {

	public static void testAddLinkIn2Mesh() {
		String parameter = "{\"command\":\"CREATE\",\"projectId\":11,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.70675,40.08349,40.01382753543055],[116.70690,40.08319],[116.70699,40.08353],[116.70707,40.08318]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testAddLinkByCatchLinks() {
		String parameter = "{\"command\":\"CREATE\",\"projectId\":11,\"data\":{\"eNodePid\":100020158,\"sNodePid\":100020231,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.47767305374146,40.01311264392862],[116.47776961326599,40.01241417948214],[116.47735177103769,40.01218816003256],[116.47686839103697,40.01253743843326]]},\"catchLinks\":[{\"nodePid\":100020181,\"lon\":116.47767305374146,\"lat\":40.01311264392862},{\"nodePid\":100020158,\"lon\":116.47735177103769,\"lat\":40.01218816003256}]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testUpdateAttr() {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDLINK\",\"projectId\":11,\"data\":{\"forms\":[{\"linkPid\":100003809,\"rowId\":\"\",\"formOfWay\":13,\"extendedForm\":0,\"auxiFlag\":0,\"kgFlag\":0,\"objStatus\":\"INSERT\"},{\"linkPid\":100003809},{\"linkPid\":100003809},{\"linkPid\":100003809}],\"pid\":100003809}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testCreateMultiMesh() {
		String parameter = "{\"command\":\"CREATE\",\"projectId\":11,\"data\":{\"eNodePid\":100020885,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.5003752708435,39.996844784553524],[116.49999976158142,39.996692730201616],[116.49999976158142,39.99628177079969],[116.50030016899109,39.99605163245438],[116.499924659729,39.99593245336362],[116.499924659729,39.99593245336362]]},\"catchLinks\":[{\"nodePid\":100020882,\"lon\":116.49999976158142,\"lat\":39.996692730201616},{\"nodePid\":100020885,\"lon\":116.49999976158142,\"lat\":39.99628177079969}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testCreate() {
		String parameter = "{\"command\":\"CREATE\",\"projectId\":11,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49824291467667,40.00019811338064],[116.49796396493912,39.99991045682577],[116.49796396493912,39.99991045682577]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testRepair() {
		String parameter = "{\"command\":\"REPAIR\",\"projectId\":11,\"objId\":100004920,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49921,40.01939],[116.50081515312195,40.01987505355837],[116.49862,40.02015]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testBreak()
	{
		String parameter = "{\"command\":\"BREAK\",\"projectId\":11,\"objId\":100004794,\"data\":{\"longitude\":116.47228754983091,\"latitude\":40.01638015848891},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			testBreak();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
