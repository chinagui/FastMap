package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import com.navinfo.dataservice.engine.editplus.glm.NonObjPidException;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

/** 
 * @ClassName: IxPoiNameTone
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: IxPoiNameTone.java
 */
public class IxPoiNameTone extends BasicRow {
	
	public IxPoiNameTone(long objPid) {
		super(objPid);
		// TODO Auto-generated constructor stub
	}


	protected long nameId=0L;
	protected String toneA;
	protected String toneB;
	protected String lhA;
	protected String lhB;
	protected String jyutp;
	protected String memo;

	@Override
	public String tableName() {
		return "IX_POI_NAME_TONE";
	}

	public long getNameId() {
		return nameId;
	}


	public void setNameId(long nameId) {
		this.nameId = nameId;
	}


	public String getToneA() {
		return toneA;
	}


	public void setToneA(String toneA) {
		this.toneA = toneA;
	}


	public String getToneB() {
		return toneB;
	}


	public void setToneB(String toneB) {
		this.toneB = toneB;
	}


	public String getLhA() {
		return lhA;
	}


	public void setLhA(String lhA) {
		this.lhA = lhA;
	}


	public String getLhB() {
		return lhB;
	}


	public void setLhB(String lhB) {
		this.lhB = lhB;
	}


	public String getJyutp() {
		return jyutp;
	}


	public void setJyutp(String jyutp) {
		this.jyutp = jyutp;
	}


	public String getMemo() {
		return memo;
	}


	public void setMemo(String memo) {
		this.memo = memo;
	}

}
