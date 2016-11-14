package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import com.navinfo.dataservice.engine.editplus.glm.NonObjPidException;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

/** 
 * @ClassName: IxPoiNameFlag
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: IxPoiNameFlag.java
 */
public class IxPoiNameFlag extends BasicRow {
	
	public IxPoiNameFlag(long objPid) {
		super(objPid);
		// TODO Auto-generated constructor stub
	}

	protected long nameId=0;
	protected String flagCode;

	@Override
	public String tableName() {
		return "IX_POI_NAME_FLAG";
	}

	public long getNameId() {
		return nameId;
	}

	public void setNameId(long nameId) {
		this.nameId = nameId;
	}

	public String getFlagCode() {
		return flagCode;
	}

	public void setFlagCode(String flagCode) {
		this.flagCode = flagCode;
	}
}
