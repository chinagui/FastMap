package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResult;

/**
 * @ClassName GLM02012
 * @author Han Shaoming
 * @date 2017年1月11日 下午2:45:10
 * @Description TODO
 * 1）一根link上的道路名（包括官方名和别名）如果只有1种类型且为虚拟名称或线门牌时，名称只能有一个为官方名，其他全部为别名， 否则报Log1； 
 * 2）如果名称类型有多种时，虚拟名称或线门牌只能为别名，否则报Log2
 * 3）虚拟名称或线门牌类型的道路名不能为曾用名，否则报Log3 
 * 4）如果名称类型中既有虚拟名称又有线门牌时，报Log4
 * 名称分类编辑 服务端后检查 
 * 名称类型编辑 服务端后检查
 */
public class GLM02012 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for (IRow row : checkCommand.getGlmList()) {
			// 名称分类编辑,名称类型编辑
			if (row instanceof RdLinkName) {
				RdLinkName rdLinkName = (RdLinkName) row;
				this.checkRdLinkName(rdLinkName);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdLinkName
	 * @throws Exception
	 */
	private void checkRdLinkName(RdLinkName rdLinkName) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdLinkName.changedFields();
		if (!changedFields.isEmpty()) {
			// 名称分类编辑
			if (changedFields.containsKey("nameClass")) {
				List<Object> resultList = this.check(rdLinkName.getLinkPid());

				if (!resultList.isEmpty()) {
					int j = 0;
					for (int i = 0; i < resultList.size()/4; i++) {
						
						this.setCheckResult(resultList.get(j).toString(), resultList.get(j+1).toString(),
								(int) resultList.get(j+2), resultList.get(j+3).toString());
						j +=4;
					}
				}
			}
			// 名称类型编辑
			if (changedFields.containsKey("nameType")) {
				List<Object> resultList = this.check(rdLinkName.getLinkPid());

				if (!resultList.isEmpty()) {
					int j = 0;
					for (int i = 0; i < resultList.size()/4; i++) {
						
						this.setCheckResult(resultList.get(j).toString(), resultList.get(j+1).toString(),
								(int) resultList.get(j+2), resultList.get(j+3).toString());
						j +=4;
					}
				}
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception
	 */
	private List<Object> check(int pid) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || LN.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '道路名类型有1个时，且为“虚拟名称”类型，道路名只能有一个官方名称' LOG");
		sb.append(" FROM RD_LINK_NAME LN WHERE LN.LINK_PID = " + pid + " AND LN.NAME_CLASS = 1");
		sb.append(" AND LN.SEQ_NUM <> 1 AND LN.NAME_TYPE =6 AND LN.U_RECORD <>2 AND NOT EXISTS");
		sb.append(" (SELECT 1 FROM RD_LINK_NAME LN1 WHERE LN1.LINK_PID = LN.LINK_PID AND LN1.NAME_CLASS IN (1, 2)");
		sb.append(" AND LN.NAME_GROUPID <> LN1.NAME_GROUPID AND LN1.NAME_TYPE <>6 AND LN1.U_RECORD <>2)");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || LN.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '道路名类型有1个时，且为“线门牌”类型，道路名只能有一个官方名称' LOG");
		sb.append(" FROM RD_LINK_NAME LN WHERE LN.LINK_PID =" + pid + " AND LN.NAME_CLASS = 1");
		sb.append(" AND LN.SEQ_NUM <> 1 AND LN.NAME_TYPE =15 AND LN.U_RECORD <>2 AND NOT EXISTS");
		sb.append(" (SELECT 1 FROM RD_LINK_NAME LN1 WHERE LN1.LINK_PID = LN.LINK_PID AND LN1.NAME_CLASS IN (1, 2)");
		sb.append(" AND LN.NAME_GROUPID <> LN1.NAME_GROUPID AND LN1.NAME_TYPE <>15 AND LN1.U_RECORD <>2)");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || LN.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '道路名类型有多个时，“虚拟名称”类型的道路名不能做官方名称' LOG");
		sb.append(" FROM RD_LINK_NAME LN WHERE LN.LINK_PID =" + pid + " AND LN.NAME_TYPE =6");
		sb.append(" AND LN.NAME_CLASS = 1 AND LN.U_RECORD <>2 AND EXISTS (SELECT 1 FROM RD_LINK_NAME LN1");
		sb.append(" WHERE LN1.LINK_PID = LN.LINK_PID AND LN.NAME_GROUPID <> LN1.NAME_GROUPID");
		sb.append(" AND LN1.NAME_CLASS IN (1, 2) AND LN1.NAME_TYPE <>6 AND LN1.U_RECORD <>2)");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || LN.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '道路名类型有多个时，“线门牌”类型的道路名不能做官方名称' LOG");
		sb.append(" FROM RD_LINK_NAME LN WHERE LN.LINK_PID =" + pid + " AND LN.NAME_TYPE =15");
		sb.append(" AND LN.NAME_CLASS = 1 AND LN.U_RECORD <>2 AND EXISTS (SELECT 1 FROM RD_LINK_NAME LN1");
		sb.append(" WHERE LN1.LINK_PID = LN.LINK_PID AND LN.NAME_GROUPID <> LN1.NAME_GROUPID");
		sb.append(" AND LN1.NAME_CLASS IN (1, 2) AND LN1.NAME_TYPE <>15 AND LN1.U_RECORD <>2)");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || LN.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '虚拟名称类型(或线门牌类型)的道路名不能为曾用名' AS LOG");
		sb.append(" FROM RD_LINK_NAME LN WHERE LN.LINK_PID =" + pid + " AND LN.U_RECORD <>2");
		sb.append(" AND LN.NAME_CLASS = 3 AND LN.NAME_TYPE IN (6, 15)");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || LN1.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '同时存在虚拟名称类型和线门牌类型时，必须为虚拟名称' AS LOG");
		sb.append(" FROM RD_LINK_NAME LN1, RD_LINK_NAME LN2");
		sb.append(" WHERE LN1.LINK_PID =" + pid + "  AND LN1.U_RECORD <>2 AND LN2.U_RECORD <>2 ");
		sb.append(" AND LN1.LINK_PID = LN2.LINK_PID AND LN1.NAME_TYPE = 6 AND LN2.NAME_TYPE = 15");
		String sql = sb.toString();
		log.info("后检查GLM02012--sql:" + sql);

		DatabaseOperatorResult getObj = new DatabaseOperatorResult();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		return resultList;
	}
}
