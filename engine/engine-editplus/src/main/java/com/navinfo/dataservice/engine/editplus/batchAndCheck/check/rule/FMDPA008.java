package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule.BasicCheckRule;

import net.sf.json.JSONObject;

/**
 * @ClassName: FMDPA008
 * @author: zhangpengpeng
 * @date: 2017年10月14日
 * @Desc: FMDPA008.java 检查条件： 非删除点门牌对象 检查原则：
 *        DPR_NAME字段不能有繁体字。查找的繁体字在TY_CHARACTER_FJT_HZ中所在行的CONVERT字段的值是0，
 *        则DPR_NAME不能是FT对应的值，否则报log：外业道路名＊＊是繁体字，对应的简体字是＊＊，请确认是否正确！
 */
public class FMDPA008 extends BasicCheckRule {

	public static MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");

	@Override
	public void run() throws Exception {
		Map<String, JSONObject> ftMap = metadataApi.tyCharacterFjtHzCheckSelectorGetFtExtentionTypeMap();
		Map<Long, BasicObj> rows = getRowList();
		for (Long key : rows.keySet()) {
			BasicObj obj = rows.get(key);
			if (obj.objName().equals(ObjectName.IX_POINTADDRESS) && !obj.opType().equals(OperationType.PRE_DELETED)) {
				IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) obj;
				IxPointaddress ixPonitaddress = (IxPointaddress) ixPointaddressObj.getMainrow();
				String dprName = ixPonitaddress.getDprName();
				if (StringUtils.isEmpty(dprName)) {
					continue;
				}
				Set<String> fts = new HashSet<>();
				Set<String> jts = new HashSet<>();

				for (char c : dprName.toCharArray()) {
					String str = String.valueOf(c);
					if (ftMap.containsKey(str)) {
						JSONObject data = ftMap.get(str);
						Object convert = data.get("convert");
						if (convert.equals(0)) {
							String jt = data.getString("jt");
							fts.add(str);
							jts.add(jt);
						}
					}
				}
				if (!fts.isEmpty()) {
					String ftsString = "('" + StringUtils.join(fts.toArray(), "','") + "')";
					String jtsString = "('" + StringUtils.join(jts.toArray(), "','") + "')";
					String log = "外业道路名" + ftsString + "是繁体字，对应的简体字是" + jtsString + "，请确认是否正确！";
					setCheckResult(ixPonitaddress.getGeometry(), ixPointaddressObj, ixPonitaddress.getMeshId(), log);
				}
			}
		}
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
