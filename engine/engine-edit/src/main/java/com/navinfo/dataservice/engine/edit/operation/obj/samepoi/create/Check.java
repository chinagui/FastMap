package com.navinfo.dataservice.engine.edit.operation.obj.samepoi.create;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;

import net.sf.json.JSONObject;

/**
 * @Title: Check.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年9月7日 下午5:11:35
 * @version: v1.0
 */
public class Check {

	private static Logger logger = LoggerFactory.getLogger(Check.class);

	public Check() {
	}

	public void checkKindOfPOI(IxPoi poi, IxPoi otherPoi) throws Exception {
//		MetadataApi apiService = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
//		JSONObject metaData = null;
//		try {
//			metaData = apiService.getMetadataMap();
//		} catch (Exception e) {
//			logger.error("无法获取元数据库POI的KindCode数据", e);
//		}
//		JSONObject kindCode = metaData.getJSONObject("kindCode");
		if ("180400".equals(poi.getKindCode()) && !"210304".equals(otherPoi.getKindCode())) {

		} else if (!"210304".equals(poi.getKindCode()) && "180400".equals(otherPoi.getKindCode())) {

		} else {
			throw new Exception("创建同一POI失败，创建原则:\"一方分类为180400，另一方分类为除了210304外的其他分类\"");
		}
	}
}
