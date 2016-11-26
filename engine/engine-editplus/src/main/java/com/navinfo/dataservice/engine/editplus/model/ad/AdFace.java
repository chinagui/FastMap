package com.navinfo.dataservice.engine.editplus.model.ad;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.glm.NonObjPidException;
import com.navinfo.dataservice.dao.plus.obj.ObjectType;
import com.navinfo.dataservice.engine.editplus.model.AbstractFace;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;

/** 
 * @ClassName: AdFace
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AdFace.java
 */
public class AdFace extends AbstractFace {

	public AdFace(long objPid) {
		super(objPid);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String tableName() {
		return "AD_FACE";
	}



}
