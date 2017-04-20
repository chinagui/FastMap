package com.navinfo.dataservice.scripts.tmp.diff;

import java.util.List;

public class DiffResult {
	String fid;
	List<DiffField> diffResult;
	int oracleExists = 1; //默认存在
	public DiffResult(String fid, List<DiffField> diffResult) {
		super();
		this.fid = fid;
		this.diffResult = diffResult;
	}
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	public List<DiffField> getDiffResult() {
		return diffResult;
	}
	public void setDiffResult(List<DiffField> diffResult) {
		this.diffResult = diffResult;
	}
	public void setOracleExists(int oracleExists){
		this.oracleExists = oracleExists;
	}
	public int getOracleExists(){
		return oracleExists;
	}
}
