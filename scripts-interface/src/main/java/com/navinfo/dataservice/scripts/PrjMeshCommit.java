package com.navinfo.dataservice.scripts;

import com.navinfo.dataservice.impcore.flushbylog.FlushGdb;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;

public class PrjMeshCommit {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FlushResult result = FlushGdb.prjMeshCommit(args);
		
		result.print();
	}

}
