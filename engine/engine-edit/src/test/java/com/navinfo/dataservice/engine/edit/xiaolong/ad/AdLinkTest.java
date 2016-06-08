package com.navinfo.dataservice.engine.edit.xiaolong.ad;

import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

/**
 * @author zhaokk
 *
 */
public class AdLinkTest {

	public static void testBreakAdLink() {
		String parameter = "{\"command\":\"BREAK\",\"projectId\":11,\"objId\":100031763,\"data\":{\"longitude\":116.4725149146884,\"latitude\":40.012285569566565},\"type\":\"ADLINK\"}";

		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testAddAdLink() {
		String parameter = "{\"command\":\"CREATE\",\"projectId\":11,\"data\":{\"eNodePid\":100022297,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49953842163086,40.01006399956417],[116.50036454200745,40.01035983085892],[116.49960279464722,40.01040913595003],[116.49999976158142,40.01075427059075],[116.49999976158142,40.011444534636745],[116.50035381317137,40.011674621100866],[116.49877667427062,40.011485621562215]]},\"catchLinks\":[{\"nodePid\":100022300,\"lon\":116.49999976158142,\"lat\":40.01075427059075},{\"nodePid\":100022297,\"lon\":116.49999976158142,\"lat\":40.011444534636745}]},\"type\":\"ADLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		testAddAdLink();
	}
}
