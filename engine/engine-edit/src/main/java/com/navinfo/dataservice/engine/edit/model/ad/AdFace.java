package com.navinfo.dataservice.engine.edit.model.ad;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.model.AbstractFace;
import com.navinfo.dataservice.engine.edit.model.BasicObj;
import com.navinfo.dataservice.engine.edit.model.BasicRow;

/** 
 * @ClassName: AdFace
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AdFace.java
 */
public class AdFace extends AbstractFace {

	@Override
	public String primaryKey() {
		return "FACE_PID";
	}

	@Override
	public Map<Class<? extends BasicRow>, List<BasicRow>> childRows() {
		return null;
	}

	@Override
	public Map<Class<? extends BasicObj>, List<BasicObj>> childObjs() {
		return null;
	}

	@Override
	public String tableName() {
		return "AD_FACE";
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.model.BasicRow#objType()
	 */
	@Override
	public ObjType objType() {
		// TODO Auto-generated method stub
		return ObjType.ADFACE;
	}

}
