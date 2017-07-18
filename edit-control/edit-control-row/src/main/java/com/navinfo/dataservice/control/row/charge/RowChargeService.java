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
	 * @return
	 * @throws Exception 
	 */
	public JSONObject chargePoiConvertor(String type, String time) throws Exception {
		log.info("开始数据转化,获取所有的大区库");
		JSONObject result = null;
		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		List<Region> regionList = manApi.queryRegionList();
		if("1".equals(type)){
			//初始化
			result = Fm2ChargeInit.getInstance().excute(regionList);
		}else if("2".equals(type)){
			//增量
			result = Fm2ChargeAdd.getInstance().excute(regionList);
		}
		log.info("数据转化结束库");
		return result;
	}
	
	
	
}
