package com.navinfo.dataservice.scripts;

import com.navinfo.dataservice.impcore.flushbylog.FlushGdb;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;

public class PrjMeshCommit {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String [] a =new String[2];
		a[0]="c:/1/flush_data_bylog.props";
		a[1]="c:/1/meshes";

		FlushResult result = FlushGdb.prjMeshCommit(a);
		
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
