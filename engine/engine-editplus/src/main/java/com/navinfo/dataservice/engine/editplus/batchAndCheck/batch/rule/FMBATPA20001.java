package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressFlag;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;

/**
 * @ClassName: FMBATPA20001
 * @author: zhangpengpeng
 * @date: 2017年10月9日
 * @Desc: FMBATPA20001.java 查询条件： 新增点门牌或修改（含鲜度验证）的点门牌对象 批处理：
 *        针对新增或修改（含鲜度验证）的数据，若IX_POINTADDRESS_FLAG中不存在FLAG_CODE＝150000060000的记录，
 *        则新增一条记录且若存在其它非FLAG_CODE＝150000060000记录则删除；
 *        同时来源POISRC_PID赋值0、来源方式SRC_TYPE赋值空；且数据采集版本DATA_VERSION赋值当前版本； 以上均生成履历；
 */
public class FMBATPA20001 extends BasicBatchRule {

	private String pointFlagCode = "150000060000";
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub
		IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) obj;
		IxPointaddress ixPonitaddress = (IxPointaddress) ixPointaddressObj.getMainrow();
		if (ixPointaddressObj.opType().equals(OperationType.PRE_DELETED)) {
			return;
		}
		// 处理FLAG子表记录
		boolean existFlagCode = false;
		List<IxPointaddressFlag> pointFlags = ixPointaddressObj.getIxPointaddressFlags();
		Iterator<IxPointaddressFlag> it = pointFlags.iterator();
		while(it.hasNext()){
			IxPointaddressFlag pointFlag = it.next();
			if(!pointFlagCode.equals(pointFlag.getFlagCode())){
				ixPointaddressObj.deleteSubrow(pointFlag);
			}else{
				existFlagCode = true;
			}
		}
		if(!existFlagCode){
			IxPointaddressFlag pointFlag = ixPointaddressObj.createIxPointaddressFlag();
			pointFlag.setFlagCode(pointFlagCode);
		}
		// 处理主表SRC_PID,SRC_TYPE,DATA_VERSION
		ixPonitaddress.setSrcPid(0);
//		ixPonitaddress.setSrcType(null);
		ixPonitaddress.setSrcType("");
		ixPonitaddress.setDataVersion(SystemConfigFactory.getSystemConfig()
				.getValue(PropConstant.seasonVersion));
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
