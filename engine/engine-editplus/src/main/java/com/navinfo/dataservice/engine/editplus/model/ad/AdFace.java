package com.navinfo.dataservice.engine.editplus.model.ad;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.glm.NonObjPidException;
import com.navinfo.dataservice.engine.editplus.model.AbstractFace;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

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

	@Override
	public ObjectType objType() {
		// TODO Auto-generated method stub
		return ObjectType.AD_FACE;
	}


}
