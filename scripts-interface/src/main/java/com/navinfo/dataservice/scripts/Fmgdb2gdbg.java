package com.navinfo.dataservice.scripts;

import com.navinfo.dataservice.impcore.flushbylog.FlushGdb;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;

public class Fmgdb2gdbg {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FlushResult result = FlushGdb.fmgdb2gdbg(args);
		
		result.print();
//		System.out.println("This scripts had been discarded...");
	}

}
