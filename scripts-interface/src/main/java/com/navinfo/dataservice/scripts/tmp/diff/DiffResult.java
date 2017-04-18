package com.navinfo.dataservice.scripts.tmp.diff;

import java.util.List;

public class DiffResult {
	String fid;
	List<DiffField> diffResult;
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
	
}
