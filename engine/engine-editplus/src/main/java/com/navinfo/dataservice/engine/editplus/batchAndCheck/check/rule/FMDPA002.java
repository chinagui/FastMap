package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @Title: FMDPA002
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description:检查条件：
					非删除点门牌对象：
				检查原则：
					DPR_NAME字段中允许存在“TY_CHARACTER_EGALCHAR_EXT”表，“EXTENTION_TYPE”字段中“GBK”、“ENG_F_U”、“ENG_F_L”、“DIGIT_F”、“SYMBOL_F”类型对应的“CHARACTER”字段的内容，如果存在其他字符，将此条点门牌报log：外业道路名中含有非法字符“xx”；
				备注：检查时，先把name转成全角，再查
 * @Author: LittleDog
 * @Date: 2017年10月14日
 * @Version: V1.0
 */
public class FMDPA002 extends BasicCheckRule {

	public static MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");

	public void run() throws Exception {

		Map<String, List<String>> map = metadataApi.tyCharacterEgalcharExtGetExtentionTypeMap();

		for (Map.Entry<Long, BasicObj> entryRow : getRowList().entrySet()) {
			BasicObj basicObj = entryRow.getValue();

			// 已删除的数据不检查
			if (basicObj.opType().equals(OperationType.PRE_DELETED)) {
				continue;
			}

			if (basicObj.objName().equals(ObjectName.IX_POINTADDRESS)) {
				IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) basicObj;
				IxPointaddress ixPointaddress = (IxPointaddress) ixPointaddressObj.getMainrow();
				if (!StringUtils.isEmpty(ixPointaddress.getDprName())) {
					String dprName = ixPointaddress.getDprName();
					dprName = CheckUtil.strB2Q(dprName);

					List<String> errorList = new ArrayList<String>();
					for (char nameSub : dprName.toCharArray()) {
						String nameSubStr = String.valueOf(nameSub);
						if (!map.get("GBK").contains(nameSubStr) && !map.get("ENG_F_U").contains(nameSubStr)
								&& !map.get("ENG_F_L").contains(nameSubStr) && !map.get("DIGIT_F").contains(nameSubStr)
								&& !map.get("SYMBOL_F").contains(nameSubStr)) {

							errorList.add(nameSubStr);
						}
					}
					if(errorList != null && errorList.size() != 0){
						setCheckResult(ixPointaddress.getGeometry(),
								String.format("[IX_POINTADDRESS,%s]", basicObj.objPid()), ixPointaddress.getMeshId(),
								"外业道路名中含有非法字符“" + errorList.toString().replace("[", "").replace("]", "") + "”!");
					}
				}
			}
		}

	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
	}

}
