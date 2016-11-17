package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

/**
 * @ClassName: PoiBatchProcessorFM_BAT_20_197
 * @author: zhangpengpeng
 * @date: 2016年11月11日
 * @Desc: 汽车租赁网址和电话批处理 具体原则见：深度信息批处理配置表——sheet汽车租赁批处理
 */
public class PoiBatchProcessorFM_BAT_20_197 implements IBatch {
	private JSONObject chainWebsite = new JSONObject();
	private JSONObject chainContact = new JSONObject();

	public PoiBatchProcessorFM_BAT_20_197() {
		// chainWebsite init
		chainWebsite.put("8007", "http://www.top1.cn/");
		chainWebsite.put("8006", "http://www.1hai.cn");
		chainWebsite.put("8004", "http://www.avis.cn");
		chainWebsite.put("8003", "http://www.zuche.com/");
		chainWebsite.put("3000", "http://www.izuche.com");
		chainWebsite.put("3854", "http://www.hnqzw.com.cn");
		chainWebsite.put("3902", "http://www.cdizu.com");
		chainWebsite.put("8005", "https://www.hertz.cn");
		chainWebsite.put("2FAA", "http://www.reocar.com");
		chainWebsite.put("36AD", "http://www.ezucoo.com");
		chainWebsite.put("36AE", "http://www.xinshengbo.com");
		chainWebsite.put("36AF", "http://www.dafang24.com");
		chainWebsite.put("3958", "http://www.green-go.cn");
		chainWebsite.put("3959", "http://www.evcardchina.com");
		// chainContact init chain=3000不批处理contact，所以不在chainContact
		chainContact.put("8007", "4006788588");
		chainContact.put("8006", "4008886608");
		chainContact.put("8004", "4008821119");
		chainContact.put("8003", "4006166666");
		chainContact.put("3854", "4008716166");
		chainContact.put("3902", "4008120556");
		chainContact.put("8005", "4009211138");
		chainContact.put("2FAA", "4007770888");
		chainContact.put("36AD", "4008308899");
		chainContact.put("36AE", "4006035155");
		chainContact.put("36AF", "4000600112");
		chainContact.put("3958", "4008080899");
		chainContact.put("3959", "4009208050");
	}

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json, EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			String kindCode = poi.getKindCode();
			// 非汽车租赁分类，不进行汽车租赁网址和电话批处理
			if (!kindCode.equals("200201")) {
				return result;
			}
			String chain = poi.getChain();
			JSONArray carentalDataArray = new JSONArray();
			List<IRow> carrentals = poi.getCarrentals();
			// chain值在汽车租赁网址范围内的批
			if (chainWebsite.containsKey(chain) && StringUtils.isNotEmpty(chain)) {
				//POI有汽车租赁，则更新website,phone400
				if (carrentals.size() > 0) {
					for (IRow carrental : carrentals) {
						JSONObject carrentalData = new JSONObject();
						IxPoiCarrental ixPoiCarrental = (IxPoiCarrental) carrental;
						carrentalData.put("rowId", ixPoiCarrental.getRowId());
						carrentalData.put("objStatus", ObjStatus.UPDATE.toString());
						carrentalData.put("webSite", chainWebsite.getString(chain));
						//首汽租车 chain=3000,400电话不需要批
						if (!"3000".equals(chain)){
							carrentalData.put("phone400", chainContact.getString(chain));
						}
						carentalDataArray.add(carrentalData);
					}
				}else{
					//POI没有汽车租赁，则新增一条汽车租赁，并且设置website,phone400值
					IxPoiCarrental carrentalObj = new IxPoiCarrental();
					//关联主表POI
					carrentalObj.setPoiPid(poi.getPid());
					//设置webSite
					carrentalObj.setWebSite(chainWebsite.getString(chain));
					//设置phone400,如果chain=3000则不需要批处理赋值
					if (!"3000".equals(chain)){
						carrentalObj.setPhone400(chainContact.getString(chain));
					}
					JSONObject carrentalJsonObject = carrentalObj.Serialize(null);
					carrentalJsonObject.put("objStatus", ObjStatus.INSERT.toString());
					carrentalJsonObject.remove("uDate");
					carentalDataArray.add(carrentalJsonObject);
				}
			}
			if (carentalDataArray.size() > 0) {
				result.put("carrentals", carentalDataArray);
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		}
	}
}
