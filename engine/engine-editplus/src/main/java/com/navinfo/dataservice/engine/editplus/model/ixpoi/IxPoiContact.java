package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

/** 
 * @ClassName: IxPoiContact
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: IxPoiContact.java
 */
public class IxPoiContact extends BasicRow {

	public IxPoiContact(long objPid) {
		super(objPid);
	}

	protected long poiPid=0;
	protected int contactType=0;
	protected String contact;
	//...属性

	@Override
	public String tableName() {
		return "IX_POI_CONTACT";
	}

}
