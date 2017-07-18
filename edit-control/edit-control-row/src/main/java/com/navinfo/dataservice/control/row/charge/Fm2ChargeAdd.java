package com.navinfo.dataservice.control.row.charge;

import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.log.LoggerRepos;

import net.sf.json.JSONObject;

/**
 * FM增量导入桩家
 * @ClassName Fm2ChargeAdd
 * @author Han Shaoming
 * @date 2017年7月17日 下午8:16:02
 * @Description TODO
 */
public class Fm2ChargeAdd {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	private volatile static Fm2ChargeAdd instance;
	public static Fm2ChargeAdd getInstance(){
		if(instance==null){
			synchronized(Fm2ChargeInit.class){
				if(instance==null){
					instance=new Fm2ChargeAdd();
				}
			}
		}
		return instance;
	}
	private Fm2ChargeAdd(){}
	
	public JSONObject excute(List<Region> regionList) {
		// TODO Auto-generated method stub
		return null;
	}

}
