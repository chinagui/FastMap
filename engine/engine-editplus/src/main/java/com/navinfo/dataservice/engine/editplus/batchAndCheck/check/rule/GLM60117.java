package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * IX_POI_ADDRESS表中，若“FLOOR(楼层)”字段不为空且
 * 1)  “FLOOR(楼层)”字段中含有大写的“零、一、二、三、四、五、六、七、八、九、十、百、千、壹、贰、叁、肆、伍、陆、柒、捌、玖、拾、佰、仟”
 *   报log：“楼层”中含有大写的“xx”！
 * 2)  “FLOOR(楼层)”字段中不包含配置表“大陆地址检查配置表”SC_POINT_ADDRCK（地址拆分用检查配置表）中“TYPE=2”且“HM_FLAG=D”的关键字（PRE_KEY），
 *   报出log：楼层”没有包含“大陆地址检查配置表”中“TYPE=2,PRE_KEY”列的关键字！
	举例说明：
	如果是“四”：就报“楼层”字段中含有大写的“四”且不包含层或者楼；
	如果是“四层”：就报“楼层”字段中含有大写的“四”；
	如果是“4”：就报“楼层”字段中不包含层或者楼。
 */
public class GLM60117 extends BasicCheckRule {
	
	private List<String> FloorStr = new ArrayList<String>();
	
	public GLM60117() {
		FloorStr.add("零");
		FloorStr.add("一");
		FloorStr.add("二");
		FloorStr.add("三");
		FloorStr.add("四");
		FloorStr.add("五");
		FloorStr.add("六");
		FloorStr.add("七");
		FloorStr.add("八");
		FloorStr.add("九");
		FloorStr.add("十");
		FloorStr.add("百");
		FloorStr.add("千");
		FloorStr.add("壹");
		FloorStr.add("贰");
		FloorStr.add("叁");
		FloorStr.add("肆");
		FloorStr.add("伍");
		FloorStr.add("陆");
		FloorStr.add("柒");
		FloorStr.add("捌");
		FloorStr.add("玖");
		FloorStr.add("拾");
		FloorStr.add("佰");
		FloorStr.add("仟");
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		IxPoiAddress address=poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		if (address.getFullname() == null || address.getFullname().isEmpty()) {
			return;
		}
		String floor = address.getFloor();
		if (floor == null || floor.isEmpty()) {
			return;
		}
		List<String> errList = new ArrayList<String>();
		for (String floorIndex:FloorStr) {
			if (floor.indexOf(floorIndex)>=0) {
				errList.add("“楼层”字段中含有大写的“"+floorIndex+"”");
			}
		}
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		List<String> addrck = metaApi.getAddrck(2, "D");
		boolean isInclude = false;
		for (String addck:addrck) {
			if (floor.indexOf(addck)>=0) {
				isInclude = true;
			}
		}
		if (!isInclude) {
			errList.add("“楼层”字段不包含层或者楼");
		}
		if (errList.size()>0) {
			String errStr = StringUtils.join(errList, ",");
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),errStr);
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
