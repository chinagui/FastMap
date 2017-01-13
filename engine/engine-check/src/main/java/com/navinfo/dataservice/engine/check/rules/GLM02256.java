package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResult;

/**
 * @ClassName GLM02256
 * @author Han Shaoming
 * @date 2017年1月11日 下午4:57:19
 * @Description TODO
 * 检查对象：非高速和城高道路
 * 检查原则：
 * 1、国道种别的道路名的路线属性和主从CODE必须为“工作中"和"主"，否则报LOG1
 * 2、三级以下种别的路线属性和主从CODE必须为"工作中"和“作品”,否则报LOG2
 * 路线属性编辑	服务端后检查
 * 主从CODE编辑	服务端后检查
 * Link种别编辑	服务端后检查
 */
public class GLM02256 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//Link种别编辑
			if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
			}
			//路线属性编辑,主从CODE编辑
			else if (row instanceof RdLinkName){
				RdLinkName rdLinkName = (RdLinkName) row;
				this.checkRdLinkName(rdLinkName);
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdLink.changedFields();
		if(!changedFields.isEmpty()){
			//Link种别编辑
			if(changedFields.containsKey("kind")){
				int kind = (int) changedFields.get("kind");
				if(kind != 0 && kind != 1 && kind != 2){
					List<Object> resultList = this.check(rdLink.getPid());
					
					if (!resultList.isEmpty()) {
						for (int i = 0; i < resultList.size()/4; i++) {
							this.setCheckResult(resultList.get(i).toString(), resultList.get(i+1).toString(),
									(int) resultList.get(i+2), resultList.get(i+3).toString());
						}
					}
				}
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
			// 路线属性编辑
			if (changedFields.containsKey("routeAtt")) {
				List<Object> resultList = this.check(rdLinkName.getLinkPid());

				if (!resultList.isEmpty()) {
					for (int i = 0; i < resultList.size()/4; i++) {
						this.setCheckResult(resultList.get(i).toString(), resultList.get(i+1).toString(),
								(int) resultList.get(i+2), resultList.get(i+3).toString());
					}
				}
			}
			// 主从CODE编辑
			if (changedFields.containsKey("code")) {
				List<Object> resultList = this.check(rdLinkName.getLinkPid());

				if (!resultList.isEmpty()) {
					for (int i = 0; i < resultList.size()/4; i++) {
						this.setCheckResult(resultList.get(i).toString(), resultList.get(i+1).toString(),
								(int) resultList.get(i+2), resultList.get(i+3).toString());
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
		
		sb.append("SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '错误道路名+国道LINK路线属性与主从code错误 ' AS LOG");
		sb.append(" FROM RD_LINK L, RD_LINK_NAME RLN,RD_NAME RN");
		sb.append(" WHERE L.LINK_PID ="+pid+" AND L.KIND = 3 AND L.LINK_PID = RLN.LINK_PID");
		sb.append(" AND (RLN.CODE <> 1 OR RLN.ROUTE_ATT <> 0) AND RLN.NAME_GROUPID=RN.NAME_GROUPID");
		sb.append(" AND RN.LANG_CODE IN ('CHI','CHT') AND L.U_RECORD <>2 AND RLN.U_RECORD <>2 AND RN.U_RECORD <>2");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '错误道路名+普通LINK路线属性与主从code错误 ' AS LOG");
		sb.append(" FROM RD_LINK L, RD_LINK_NAME RLN,RD_NAME RN WHERE L.LINK_PID ="+pid+" AND L.KIND > 3");
		sb.append(" AND L.LINK_PID = RLN.LINK_PID AND (RLN.CODE <> 0 OR RLN.ROUTE_ATT <> 0)");
		sb.append(" AND RLN.NAME_GROUPID=RN.NAME_GROUPID AND RN.LANG_CODE IN ('CHI','CHT')");
		sb.append(" AND L.U_RECORD <>2 AND RLN.U_RECORD <>2 AND RN.U_RECORD <>2");
		String sql = sb.toString();
		log.info("后检查GLM02256--sql:" + sql);

		DatabaseOperatorResult getObj = new DatabaseOperatorResult();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		return resultList;
	}
}
