package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: FMDPA005
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description:检查条件：
					非删除点门牌对象：
				检查原则：
    				DP_NAME字段中不允许存在：
        				(1)前后空格；
        				(2)连续空格；
        				(3)回车符或换行符
        			如果存在，将此条点门牌报log：外业门牌号格式错误！
 * @Author: LittleDog
 * @Date: 2017年10月14日
 * @Version: V1.0
 */
public class FMDPA005 extends BasicCheckRule {
	
	public void run() throws Exception {

		for (Map.Entry<Long, BasicObj> entryRow : getRowList().entrySet()) {
			BasicObj basicObj = entryRow.getValue();

			// 已删除的数据不检查
			if (basicObj.opType().equals(OperationType.PRE_DELETED)) {
				continue;
			}

			if (basicObj.objName().equals(ObjectName.IX_POINTADDRESS)) {
				IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) basicObj;
				IxPointaddress ixPointaddress = (IxPointaddress) ixPointaddressObj.getMainrow();
				if (!StringUtils.isEmpty(ixPointaddress.getDpName())) {
					String dpName = ixPointaddress.getDpName();

					if (checkStr(dpName)) {
						Geometry geo = ixPointaddress.getGeometry();
						int meshId = ixPointaddress.getMeshId();
						setCheckResult(geo, String.format("[IX_POINTADDRESS,%s]", basicObj.objPid()), meshId);
					}
				}
			}
		}

	}

	private boolean checkStr(String word) {
		boolean flag = false;

		if (word.startsWith(" ") || word.endsWith(" ") || word.contains("  ")) {
			flag = true;
		}
		Pattern pattern = Pattern.compile("\\r|\n|\r\n");
		Matcher matcher = pattern.matcher(word);
		if (matcher.find()) {
			flag = true;
		}
		return flag;
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
