package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

/** 
 * @ClassName: IxPoiName
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: IxPoiName.java
 */
public class IxPoiName extends BasicRow {

	public IxPoiName(long objPid) {
		super(objPid);
	}

	protected long nameId=0L;
	protected long poiPid=0L;
	protected long nameGroupid=0L;
	protected String langCode;
	//...

	@Override
	public String tableName() {
		return "IX_POI_NAME";
	}


}
