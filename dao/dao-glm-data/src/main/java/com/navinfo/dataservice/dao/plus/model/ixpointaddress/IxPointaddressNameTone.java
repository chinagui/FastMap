package com.navinfo.dataservice.dao.plus.model.ixpointaddress;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/**
 * @ClassName: IxPointaddressNameTone
 * @author code generator
 * @date 2017-09-21 11:24:32
 * @Description: TODO
 */
public class IxPointaddressNameTone extends BasicRow {
	private long nameId;
	private String jyutp;
	private String toneA;
	private String toneB;
	private String lhA;
	private String lhB;
	private String paJyutp;
	private String paToneA;
	private String paToneB;
	private String paLhA;
	private String paLhB;
	private String memo;

	public IxPointaddressNameTone(long objPid) {
		super(objPid);
	}

	public long getNameId() {
		return nameId;
	}

	public void setNameId(long nameId) {
		if(this.checkValue("NAME_ID",this.nameId,nameId)){
			this.nameId = nameId;
		}
	}

	public String getJyutp() {
		return jyutp;
	}

	public void setJyutp(String jyutp) {
		if(this.checkValue("JYUTP",this.jyutp,jyutp)){
			this.jyutp = jyutp;
		}
	}

	public String getToneA() {
		return toneA;
	}

	public void setToneA(String toneA) {
		if(this.checkValue("TONE_A",this.toneA,toneA)){
			this.toneA = toneA;
		}
	}

	public String getToneB() {
		return toneB;
	}

	public void setToneB(String toneB) {
		if(this.checkValue("TONE_B",this.toneB,toneB)){
			this.toneB = toneB;
		}
	}

	public String getLhA() {
		return lhA;
	}

	public void setLhA(String lhA) {
		if(this.checkValue("LH_A",this.lhA,lhA)){
			this.lhA = lhA;
		}
	}

	public String getLhB() {
		return lhB;
	}

	public void setLhB(String lhB) {
		if(this.checkValue("LH_B",this.lhB,lhB)){
			this.lhB = lhB;
		}
	}

	public String getPaJyutp() {
		return paJyutp;
	}

	public void setPaJyutp(String paJyutp) {
		if(this.checkValue("PA_JYUTP",this.paJyutp,paJyutp)){
			this.paJyutp = paJyutp;
		}
	}

	public String getPaToneA() {
		return paToneA;
	}

	public void setPaToneA(String paToneA) {
		if(this.checkValue("PA_TONE_A",this.paToneA,paToneA)){
			this.paToneA = paToneA;
		}
	}

	public String getPaToneB() {
		return paToneB;
	}

	public void setPaToneB(String paToneB) {
		if(this.checkValue("PA_TONE_B",this.paToneB,paToneB)){
			this.paToneB = paToneB;
		}
	}

	public String getPaLhA() {
		return paLhA;
	}

	public void setPaLhA(String paLhA) {
		if(this.checkValue("PA_LH_A",this.paLhA,paLhA)){
			this.paLhA = paLhA;
		}
	}

	public String getPaLhB() {
		return paLhB;
	}

	public void setPaLhB(String paLhB) {
		if(this.checkValue("PA_LH_B",this.paLhB,paLhB)){
			this.paLhB = paLhB;
		}
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		if(this.checkValue("MEMO",this.memo,memo)){
			this.memo = memo;
		}
	}

	@Override
	public String tableName() {
		// TODO Auto-generated method stub
		return "IX_POINTADDRESS_NAME_TONE";
	}

}
