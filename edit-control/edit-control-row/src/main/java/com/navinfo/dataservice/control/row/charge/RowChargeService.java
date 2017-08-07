package com.navinfo.dataservice.control.row.charge;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName RowChargeController
 * @author Han Shaoming
 * @date 2017年7月17日 下午6:47:10
 * @Description TODO
 */
@Controller
public class RowChargeService {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private volatile static RowChargeService instance;
	public static RowChargeService getInstance(){
		if(instance==null){
			synchronized(RowChargeService.class){
				if(instance==null){
					instance=new RowChargeService();
				}
			}
		}
		return instance;
	}
	private RowChargeService(){}
	
	/**
	 * 处理充电站数据
	 * @author Han Shaoming
	 * @param type
	 * @param time
	 * @param syncTime 
	 * @return
	 * @throws Exception 
	 */
	public JSONObject chargePoiConvertor(int type, String lastSyncTime, String syncTime,List<Integer> dbIdList) throws Exception {
		log.info("开始数据转化,获取所有的大区库");
		JSONObject result = null;
		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		List<Region> regionList = manApi.queryRegionList();
		
		if(type==1){
			//初始化
			Fm2ChargeInit fm2ChargeInit = new Fm2ChargeInit();
			result = fm2ChargeInit.excute(regionList,dbIdList);
		}else if(type==2){
			//增量
			Fm2ChargeAdd fm2ChargeAdd = new Fm2ChargeAdd();
			result = fm2ChargeAdd.excute(regionList,lastSyncTime,syncTime,dbIdList);
		}
		log.info("数据转化结束");
		return result;
	}
	
	
	
}
