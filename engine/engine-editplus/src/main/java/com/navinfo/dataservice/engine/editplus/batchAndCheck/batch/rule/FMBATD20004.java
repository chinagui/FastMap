package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiFlag;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiFlagMethod;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.utils.AdFaceSelector;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 批处理对象：日编提交的POI数据（新增、修改(包含鲜度验证)、删除）；
批处理原则：
 总原则，按照PID查询是否存在记录，如果存在，则更新POI_FLAG记录，否则插入一条记录；
1.若日编提交为常规子任务类型，则根据新增、修改和删除状态，批处理如下：
 1.1 若提交POI数据为新增，则批处理如下：
   1.1.1 记录级来源标识POI_FLAG.SRC_RECORD有值且不为0则不处理，否则POI_FLAG.SRC_RECORD批处理赋值为1；
   1.1.2 记录级验证标识POI_FLAG.VER_RECORD赋值为1；
   1.1.3 源是否被外业已验证POI_FLAG.FIELD_VERIFIED赋值1；
   1.1.4 若记录级来源标识POI_FLAG.SRC_RECORD=1，则IX_POI_FLAG表插入一条flag记录，flag_code=110000260000(若存在此flag_code，则不处理)
 1.2 若提交POI数据为修改(包含鲜度验证)或删除，则批处理如下：
   1.2.1 记录级验证标识POI_FLAG.VER_RECORD赋值为1；
   1.2.2 源是否被外业已验证POI_FLAG.FIELD_VERIFIED赋值1；
2.若日编提交为多源子任务类型，则根据新增、修改和删除状态，批处理如下：
 2.1 若提交POI数据为新增，则批处理如下：
   2.1.1 记录级来源标识POI_FLAG.SRC_RECORD有值且不为0，则不处理否则POI_FLAG.SRC_RECORD批处理赋值为3；
   2.1.2 记录级验证标识POI_FLAG.VER_RECORD赋值为3；
   2.1.3 若记录级来源标识POI_FLAG.SRC_RECORD=3，则IX_POI_FLAG表插入一条flag记录，flag_code=110000270000(若存在此flag_code，则不处理)
  2.2 若提交POI数据为修改(包含鲜度验证)或删除，则批处理如下：
   2.2.1 记录级验证标识POI_FLAG.VER_RECORD赋值为3；
3.若日编提交为众包子任务类型，则根据新增、修改和删除状态，批处理如下：
 3.1 若提交POI数据为新增，则批处理如下：
   3.1.1 记录级来源标识POI_FLAG.SRC_RECORD有值且不为0，则不处理，否则POI_FLAG.SRC_RECORD批处理赋值为4；
   3.1.2 记录级验证标识POI_FLAG.VER_RECORD赋值为4；
   3.1.3 若记录级来源标识POI_FLAG.SRC_RECORD=3，则IX_POI_FLAG表插入一条flag记录，flag_code=110000290000(若存在此flag_code，则不处理)
 3.2 若提交POI数据为修改(包含鲜度验证)或删除，则批处理如下：
   3.2.1 记录级验证标识POI_FLAG.VER_RECORD赋值为4；
4.若POI为各业务新增或修改，则执行如下批处理：
  如果FIELD_VERIFIED=0，库中IX_POI_FLAG若存在“110001110000”则删除，不存在则不更新；如果FIELD_VERIFIED=1，库中IX_POI_FLAG若存在“110001110000”则不更新，不存在则增加一条“110001110000”的flag记录；
以上，批处理生成履历；
 * 
 * @author gaopengrong
 *
 */
public class FMBATD20004 extends BasicBatchRule {
	private int  subTaskWorkKind=0;

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
		if(getBatchRuleCommand().getParameter()!=null&&getBatchRuleCommand().getParameter().containsKey("subTaskWorkKind")){
			subTaskWorkKind=(int) getBatchRuleCommand().getParameter().get("subTaskWorkKind");
		}
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			List<IxPoiFlagMethod> poiFlags = poiObj.getIxPoiFlagMethods();
			List<IxPoiFlag> ixPoiFlags = poiObj.getIxPoiFlags();
			
			boolean flagCode0=false;
			boolean flagCode1=false;
			boolean flagCode3=false;
			boolean flagCode4=false;
			if(ixPoiFlags!=null&&ixPoiFlags.size()>0){
				for(IxPoiFlag ixPoiFlag:ixPoiFlags){
					if(ixPoiFlag.getFlagCode().equals("110001110000")){flagCode0=true;}
					if(ixPoiFlag.getFlagCode().equals("110000260000")){flagCode1=true;}
					if(ixPoiFlag.getFlagCode().equals("110000270000")){flagCode3=true;}
					if(ixPoiFlag.getFlagCode().equals("110000290000")){flagCode4=true;}
				}
			}
			
			//处理poi_flag
			if(poiFlags!=null&&poiFlags.size()>0){
				//存在poi_flag记录
				for(IxPoiFlagMethod poiFlag:poiFlags){
					//常规子任务
					if(subTaskWorkKind==1){
						if(poi.getHisOpType().equals(OperationType.INSERT)){
							if(poiFlag.getSrcRecord()==0){poiFlag.setSrcRecord(1);}
							if(poiFlag.getVerRecord()!=1){poiFlag.setVerRecord(1);}
							if(poiFlag.getFieldVerified()!=1){poiFlag.setFieldVerified(1);}
							if(poiFlag.getSrcRecord()==1&&!flagCode1){
								addIxPoiFlag(poiObj,"110000260000");
							}
						}else{
							if(poiFlag.getVerRecord()!=1){poiFlag.setVerRecord(1);}
							if(poiFlag.getFieldVerified()!=1){poiFlag.setFieldVerified(1);}
						}
					}
					//众包子任务
					if(subTaskWorkKind==2){
						if(poi.getHisOpType().equals(OperationType.INSERT)){
							if(poiFlag.getSrcRecord()==0){poiFlag.setSrcRecord(4);}
							if(poiFlag.getVerRecord()!=4){poiFlag.setVerRecord(4);}
							if(poiFlag.getSrcRecord()==4&&!flagCode4){
								addIxPoiFlag(poiObj,"110000290000");
							}
						}else{
							if(poiFlag.getVerRecord()!=4){poiFlag.setVerRecord(4);}
						}
					}
					//多源子任务
					if(subTaskWorkKind==4){
						if(poi.getHisOpType().equals(OperationType.INSERT)){
							if(poiFlag.getSrcRecord()==0){poiFlag.setSrcRecord(3);}
							if(poiFlag.getVerRecord()!=3){poiFlag.setVerRecord(3);}
							if(poiFlag.getSrcRecord()==3&&!flagCode3){
								addIxPoiFlag(poiObj,"110000270000");
							}
						}else{
							if(poiFlag.getVerRecord()!=3){poiFlag.setVerRecord(3);}
						}
					}
				}
			}else{
				IxPoiFlagMethod poiFlag=poiObj.createIxPoiFlagMethod();
				//常规子任务
				if(subTaskWorkKind==1){
					poiFlag.setSrcRecord(1);
					poiFlag.setVerRecord(1);
					poiFlag.setFieldVerified(1);
					poiFlag.setPoiPid(poi.getPid());
					if(poi.getHisOpType().equals(OperationType.INSERT) && !flagCode1){
						addIxPoiFlag(poiObj,"110000260000");
					}
				}
				//众包子任务
				if(subTaskWorkKind==2){
					poiFlag.setSrcRecord(4);
					poiFlag.setVerRecord(4);
					poiFlag.setPoiPid(poi.getPid());
					if(poi.getHisOpType().equals(OperationType.INSERT) && !flagCode4){
						addIxPoiFlag(poiObj,"110000290000");
					}
				}
				//多源子任务
				if(subTaskWorkKind==4){
					poiFlag.setSrcRecord(3);
					poiFlag.setVerRecord(3);
					poiFlag.setPoiPid(poi.getPid());
					if(poi.getHisOpType().equals(OperationType.INSERT) && !flagCode3){
						addIxPoiFlag(poiObj,"110000270000");
					}
				}
			}
			//处理ix_poi_flag
			if(poi.getHisOpType().equals(OperationType.INSERT)||poi.getHisOpType().equals(OperationType.UPDATE)){
				if(poiFlags != null && poiFlags.size() > 0){
					for(IxPoiFlagMethod poiFlag:poiFlags){
						if(poiFlag.getFieldVerified()==0&&flagCode0){
							delIxPoiFlag(poiObj,"110001110000");
						}
						if(poiFlag.getFieldVerified()==1&&!flagCode0){
							addIxPoiFlag(poiObj,"110001110000");
						}
					}
				}
			}
			
			
		}
	}
	private void addIxPoiFlag(IxPoiObj poiObj,String flagCode) throws Exception {
		IxPoiFlag ixPoiFlag=poiObj.createIxPoiFlag();
		ixPoiFlag.setFlagCode(flagCode);
		ixPoiFlag.setPoiPid(poiObj.getMainrow().getObjPid());
	}
	private void delIxPoiFlag(IxPoiObj poiObj,String flagCode) throws Exception {
		List<IxPoiFlag> ixPoiFlags = poiObj.getIxPoiFlags();
		if(ixPoiFlags!=null&&ixPoiFlags.size()>0){
			for(IxPoiFlag ixPoiFlag:ixPoiFlags){
				if(flagCode.equals(ixPoiFlag.getFlagCode())){
					poiObj.deleteSubrow(ixPoiFlag);
				}
			}
		}
	}

}
