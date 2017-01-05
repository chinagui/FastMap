package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;


import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiCarrental;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import net.sf.json.JSONObject;

/**
 * 1.可以为空；2.字符串；3.不为空时，不能含有非法字符,且不能含有空格；可含有特殊字符：＠　＿　－　／　；　：　～　＾　” ‘ ’　”　，
				　．　？　！　＊　＃　（　）　＜　＞　￥　＄　％　＆  ＋ ＇　　＂ •《》、·、。、|  ；存在非全角的内容报出
					Log1：汽车租赁地址描述含有非法字符“XX”。
					Log2：汽车租赁地址描述内容含有半角字符。
					Log3：汽车租赁地址描述内容含有空格。
					注：合法字符表中包含上述特殊字符，因此程序中不做特殊判断
 * @author gaopengrong
 */
public class FMZY20238 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();	
			List<IxPoiCarrental> carrentals = poiObj.getIxPoiCarrentals();
			String log;
			for(IxPoiCarrental poiCarrental : carrentals){
				String address=poiCarrental.getAddress();
				
				if (StringUtils.isNotEmpty(address)){
					//调用元数据请求接口
					MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
					JSONObject characterMap = metaApi.getCharacterMap();
					
					char[] addresslist=new char[address.length()];
					addresslist=address.toCharArray();
					
					String illegalChar = "";
					
					for(int i = 0; i < addresslist.length; i++){
						
						char c=addresslist[i];
						String str=String.valueOf(c);
						//全角空格
						if ("　".equals(str) || " ".equals(str)){
							log = "汽车租赁地址描述内容含有空格。";
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						}
						
						if (!characterMap.has(str)){
							illegalChar+=str;
						}
					}
					//注：特殊字符是合法字符，因此不做特殊判断
					if (!"".equals(illegalChar)){
						log = "汽车租赁地址描述含有非法字符'"+illegalChar+"';";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
					if (!address.equals(ExcelReader.h2f(address))){
						log = "汽车租赁地址描述内容含有半角字符。";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
				}
			}

		}
	}
}
