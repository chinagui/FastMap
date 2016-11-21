package com.navinfo.dataservice.engine.check.rules;
import java.util.List;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;

/** 
 * @ClassName: CheckRuleFMZY20238
 * @author Gao Pengrong
 * @date 2016-11-15 上午8:41:24 
 * @Description: 1.可以为空；2.字符串；3.不为空时，不能含有非法字符,且不能含有空格；可含有特殊字符：＠　＿　－　／　；　：　～　＾　” ‘ ’　”　，
				　．　？　！　＊　＃　（　）　＜　＞　￥　＄　％　＆  ＋ ＇　　＂ •《》、·、。、|  ；存在非全角的内容报出
					Log1：汽车租赁地址描述含有非法字符“XX”。
					Log2：汽车租赁地址描述内容含有半角字符。
					Log3：汽车租赁地址描述内容含有空格。
					注：合法字符表中包含上述特殊字符，因此程序中不做特殊判断
 */

public class CheckRuleFMZY20238 extends baseRule {
	
	public void preCheck(CheckCommand checkCommand){
	}

	public void postCheck(CheckCommand checkCommand) throws Exception{
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof IxPoi){
				IxPoi ixPoi = (IxPoi)obj;
				innerCheck(ixPoi);	
				}
			}
	}
	
	public void innerCheck(IxPoi ixPoi) throws Exception{
		List<IRow> rows = ixPoi.getCarrentals();
		String log;
		for(IRow row : rows)
		{
			IxPoiCarrental poiCarrental = (IxPoiCarrental) row;
			String address=poiCarrental.getAddress();
			
			if (!"".equals(address)){
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
						this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
					}
					
					if (!characterMap.has(str)){
						illegalChar+=illegalChar;
					}
				}
//				注：特殊字符是合法字符，因此不做特殊判断
				if (!"".equals(illegalChar)){
					log = "汽车租赁地址描述含有非法字符'"+illegalChar+"';";
					this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
				}
				if (!address.equals(ExcelReader.h2f(address))){
					log = "汽车租赁地址描述内容含有半角字符。";
					this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
				}
			}
			
		}
	}
}