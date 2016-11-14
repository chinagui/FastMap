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

}
