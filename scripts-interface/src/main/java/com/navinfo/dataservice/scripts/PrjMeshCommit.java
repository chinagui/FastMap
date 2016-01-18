package com.navinfo.dataservice.scripts;

import com.navinfo.dataservice.impcore.flushbylog.FlushGdb;

public class PrjMeshCommit {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FlushGdb.flush(args);
	}

}
