package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 	FMDGC009
	检查条件：非删除POI对象
	检查原则：
	1、当POI分类KIND_CODE与SC_POINT_TRUCK中TYPE=1中KIND相同时，若POI.TRUCK_FLAG与SC_POINT_TRUCK中TRUCK不一致，则报log:卡车标识应为***(SC_POINT_TRUCK.TRUCK值域含义－见备注);
	2、当POI品牌CHAIN与SC_POINT_TRUCK中TYPE=2中CHAIN相同时，若POI.TRUCK_FLAG与SC_POINT_TRUCK中TRUCK不一致，则报log:卡车标识应为***(SC_POINT_TRUCK.TRUCK值域含义－见备注);
	3、当POI分类KIND_CODE+品牌CHAIN与SC_POINT_TRUCK中TYPE=3中KIND+CHAIN相同时，若POI.TRUCK_FLAG与SC_POINT_TRUCK中TRUCK不一致，则报log:卡车标识应为***(SC_POINT_TRUCK.TRUCK值域含义－见备注);
	4、当POI分类KIND_CODE=230215加油站且燃料类型FUEL_TYPE包含柴油(FUEL_TYPE包含0)时，若POI.TRUCK_FLAG不为2(通用POI)，则报log:卡车标识应为卡车＋小汽车；
	备注：SC_POINT_TRUCK.TRUCK值域：0 非卡车 ； 1 仅卡车 ；2 卡车＋小汽车
 * @author sunjiawei
 */
public class FMDGC009 extends BasicCheckRule {
	
	private final static String logBasic = "卡车标识应为";
	
	private final static String truckZero = "非卡车";
	
	private final static String truckOne = "仅卡车";
	
	private final static String truckTwo = "卡车＋小汽车";
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String chain=poi.getChain();
			String kind=poi.getKindCode();
			int truckFlag = poi.getTruckFlag();
			if(kind==null||kind.isEmpty()){return;}			
			MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			if(kind.equals("230215")){
				 List<IxPoiGasstation> gasstations = poiObj.getIxPoiGasstations();
	             if (!CollectionUtils.isEmpty(gasstations)) {
	            	  for (IxPoiGasstation gasstation : gasstations) {
	                      if (StringUtils.isNotBlank(gasstation.getFuelType()) && 
	                    		  gasstation.getFuelType().contains("0") && truckFlag!=2) {
	                    	  setCheckResult(poi.getGeometry(),poiObj, poi.getMeshId(), logBasic+truckTwo);
	          				  return;
	                      }
	                  }
	             }
			}
			
			int truck = api.getTruck(kind, chain, "");
			if(truck!=-1&&truckFlag!=truck){
				if(truck==0){
					setCheckResult(poi.getGeometry(),poiObj, poi.getMeshId(), logBasic+truckZero);
     				return;
				}else if(truck==1){
					setCheckResult(poi.getGeometry(),poiObj, poi.getMeshId(), logBasic+truckOne);
     				return;
				}else if(truck==2){
					setCheckResult(poi.getGeometry(),poiObj, poi.getMeshId(), logBasic+truckTwo);
     				return;
				}
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
