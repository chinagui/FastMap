package com.navinfo.dataservice.scripts;

import com.navinfo.dataservice.impcore.flushbylog.FlushGdb;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;

public class PrjMeshReturnHistory {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FlushResult result = FlushGdb.prjMeshReturnHistory(args);
		
		System.out.println("Total:"+result.getTotal());
		
		System.out.println("Insert total:"+result.getInsertTotal());
		
		System.out.println("Update total:"+result.getUpdateTotal());
		
		System.out.println("Delete total:"+result.getDeleteTotal());
		
		System.out.println("Failed total:"+result.getFailedTotal());
		
		System.out.println("Insert failed:"+result.getInsertFailed());
		
		if(result.getInsertFailed()>0){
			System.out.println("RowIds:"+result.getInsertFailedList());
		}
		
		System.out.println("Update failed:"+result.getUpdateFailed());
		
		if(result.getUpdateFailed()>0){
			System.out.println("RowIds:"+result.getUpdateFailedList());
		}
		
		System.out.println("Delete failed:"+result.getDeleteFailed());
		
		if(result.getDeleteFailed()>0){
			System.out.println("RowIds:"+result.getDeleteFailedList());
		}
		
		System.out.println("Log moved:"+result.getLogMoved());
	}

}
