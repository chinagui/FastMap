package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.glm.NonGeoPidException;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

/** 
 * @ClassName: IxPoiAddress
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: IxPoiAddress.java
 */
public class IxPoiAddress extends BasicRow {

	protected void setNameId(long nameId) {
		this.nameId = nameId;
	}

	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}

	protected void setNameGroupid(long nameGroupid) {
		this.nameGroupid = nameGroupid;
	}

	protected void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	protected void setSrcFlag(int srcFlag) {
		this.srcFlag = srcFlag;
	}

	public IxPoiAddress(long objPid) {
		super(objPid);
	}

	protected long nameId=0L;
	protected long poiPid=0L;
	protected long nameGroupid=0L;
	protected String langCode;
	protected int srcFlag=0;

	@Override
	public String tableName() {
		return "IX_POI_ADDRESS";
	}

	public long getNameId() {
		return nameId;
	}

	public long getPoiPid() {
		return poiPid;
	}

	public long getNameGroupid() {
		return nameGroupid;
	}

	public String getLangCode() {
		return langCode;
	}

	public int getSrcFlag() {
		return srcFlag;
	}

}
