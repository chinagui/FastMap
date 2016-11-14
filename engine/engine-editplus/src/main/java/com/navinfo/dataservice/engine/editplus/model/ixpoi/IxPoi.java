package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import com.navinfo.dataservice.engine.editplus.model.AbstractIx;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

/** 
 * @ClassName: IxPoi
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: IxPoi.java
 */
public class IxPoi extends AbstractIx {

	public IxPoi(long objPid) {
		super(objPid);
	}


	protected String kindCode;
	protected int side=0;
	protected long nameGroupid=0;
	protected int roadFlag=0;
	//...

	@Override
	public String tableName() {
		return "IX_POI";
	}


//	@Override
//	public String getObjType() {
//		return ObjectType.IX_POI;
//	}

//	@Override
//	public long getGeoPid() {
//		return getObjPid();
//	}

//	@Override
//	public String getGeoType() {
//		return getObjType();
//	}

//	@Override
//	public String primaryKey() {
//		return "PID";
//	}


//	@Override
//	public boolean isGeoChanged() {
//		if(opType==OperationType.INSERT
//			||opType==OperationType.DELETE
//			||(opType==OperationType.UPDATE&&oldValues!=null&&oldValues.keySet().contains("GEOMETRY"))){
//			return true;
//		}
//		return false;
//	}
}
