package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResult;

/**
 * @ClassName GLM02257
 * @author Han Shaoming
 * @date 2017年1月11日 下午4:11:13
 * @Description TODO
 * 道路形态含有“隧道”属性，满足以下条件：
 * 1、官方名中只有一个名称包含“隧道”，则该名称的名称类型必须为“隧道”，否则报log1；
 * 2、官方名中有多个名称包含“隧道”，则报log2；
 * 3、所有官方名中不包含“隧道”，且没有一个名称与隧道串两端挂接的link的官方名都不相同，则报log3；
 * 4、所有官方名中不包含“隧道”，只有一个名称与隧道串两端挂接的link的官方名都不相同，则该名称的名称类型必须为“隧道”，否则报log1；
 * 5、所有官方名中不包含“隧道”，有多个名称与隧道串两端挂接的link的官方名都不相同，则报log2；
 * 6、官方名称中如果有多个隧道类型的，报log4。
 * 道路属性编辑	服务端后检查
 * 名称分类编辑	服务端后检查
 * 名称类型编辑	服务端后检查
 * 道路名称编辑	服务端后检查
 * 新增道路名	服务端后检查
 */
public class GLM02257 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//道路属性编辑
			if (row instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm) row;
				this.checkRdLinkForm(rdLinkForm);
			}
			//名称分类编辑,名称类型编辑,道路名称编辑,新增道路名
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
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		// TODO Auto-generated method stub
		boolean checkFlag = false;
		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdLinkForm.changedFields();
			if(!changedFields.isEmpty()){
				//道路属性编辑
				if(changedFields.containsKey("formOfWay")){
					int formOfWay = (int) changedFields.get("formOfWay");
					if(formOfWay == 31){
					checkFlag = true;
					}
				}
			}
		}else if (rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay == 31){
				checkFlag = true;
			}
		}
		if(checkFlag){
			List<Object> resultList = this.check(rdLinkForm.getLinkPid());
			
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

	/**
	 * @author Han Shaoming
	 * @param rdLinkName
	 * @throws Exception 
	 */
	private void checkRdLinkName(RdLinkName rdLinkName) throws Exception {
		// TODO Auto-generated method stub
		//名称分类编辑,名称类型编辑,道路名称编辑
		if(rdLinkName.status().equals(ObjStatus.UPDATE)){
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
				// 道路名称编辑
				if (changedFields.containsKey("nameGroupid")) {
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
		//新增道路名
		else if (rdLinkName.status().equals(ObjStatus.INSERT)){
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
	
	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception
	 */
	private List<Object> check(int pid) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		
		sb.append("WITH S_NAME AS");
		sb.append(" (SELECT DISTINCT RLN.LINK_PID, RLN.NAME_GROUPID, RLN.NAME_TYPE");
		sb.append(" FROM RD_LINK_FORM RLF, RD_LINK_NAME RLN, RD_NAME N");
		sb.append(" WHERE RLF.LINK_PID = "+pid+" AND RLF.LINK_PID = RLN.LINK_PID");
		sb.append(" AND RLF.FORM_OF_WAY = 31 AND RLN.NAME_GROUPID = N.NAME_GROUPID");
		sb.append(" AND RLN.NAME_CLASS = 1 AND N.LANG_CODE IN ('CHI', 'CHT') AND N.NAME LIKE '%隧道%'");
		sb.append(" AND RLF.U_RECORD <>2 AND RLN.U_RECORD <>2 AND N.U_RECORD <>2),");
		sb.append(" S_NAME_NUM AS");
		sb.append(" (SELECT T.LINK_PID, COUNT(1) AS NUM FROM S_NAME T GROUP BY T.LINK_PID),");
		sb.append(" ALINK_NAME AS");
		sb.append(" (SELECT DISTINCT RL2.LINK_PID,RLN.NAME_GROUPID FROM RD_LINK RL1,RD_LINK RL2,RD_LINK_NAME RLN");
		sb.append(" WHERE RL1.LINK_PID ="+pid+" AND RL2.LINK_PID = RLN.LINK_PID");
		sb.append(" AND ((RL2.S_NODE_PID = RL1.S_NODE_PID OR RL2.E_NODE_PID = RL1.S_NODE_PID)");
		sb.append(" OR (RL2.S_NODE_PID = RL1.E_NODE_PID OR RL2.E_NODE_PID = RL1.E_NODE_PID))");
		sb.append(" AND RLN.NAME_CLASS = 1 AND RL2.LINK_PID <> RL1.LINK_PID");
		sb.append(" AND RL1.U_RECORD <>2 AND RL2.U_RECORD <>2 AND RLN.U_RECORD <>2),");
		sb.append(" N_S_NAME AS");
		sb.append(" (SELECT DISTINCT RLN.LINK_PID, RLN.NAME_GROUPID, RLN.NAME_TYPE");
		sb.append(" FROM RD_LINK_FORM RLF, RD_LINK_NAME RLN, RD_NAME N");
		sb.append(" WHERE RLF.LINK_PID = "+pid+" AND RLF.LINK_PID = RLN.LINK_PID AND RLF.FORM_OF_WAY = 31");
		sb.append(" AND RLN.NAME_GROUPID = N.NAME_GROUPID");
		sb.append(" AND RLN.NAME_CLASS = 1 AND N.LANG_CODE IN ('CHI', 'CHT') AND N.NAME NOT LIKE '%隧道%'");
		sb.append(" AND RLF.U_RECORD <>2 AND RLN.U_RECORD <>2 AND N.U_RECORD <>2");
		sb.append(" AND NOT EXISTS(SELECT 1 FROM ALINK_NAME WHERE RLN.NAME_GROUPID = ALINK_NAME.NAME_GROUPID)),");
		sb.append(" N_S_NAME_NUM AS");
		sb.append(" (SELECT T.LINK_PID, COUNT(1) AS NUM FROM N_S_NAME T GROUP BY T.LINK_PID)");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || S.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '隧道link上的隧道名称类型错误' AS LOG");
		sb.append(" FROM S_NAME S, S_NAME_NUM SN WHERE SN.NUM = 1");
		sb.append(" AND S.LINK_PID = SN.LINK_PID AND S.NAME_TYPE <> 5");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || S.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '隧道link上有多个隧道名' AS LOG FROM S_NAME_NUM S WHERE S.NUM > 1");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || RLF.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '隧道link上没有隧道名' AS LOG");
		sb.append(" FROM RD_LINK_FORM RLF, RD_LINK_NAME RLN, RD_NAME N");
		sb.append(" WHERE RLF.LINK_PID = "+pid+" AND RLF.LINK_PID = RLN.LINK_PID AND RLF.FORM_OF_WAY = 31");
		sb.append(" AND RLN.NAME_GROUPID = N.NAME_GROUPID");
		sb.append(" AND RLN.NAME_CLASS = 1 AND N.LANG_CODE IN ('CHI', 'CHT') AND N.NAME NOT LIKE '%隧道%'");
		sb.append(" AND RLF.U_RECORD <>2 AND RLN.U_RECORD <>2 AND N.U_RECORD <>2");
		sb.append(" AND EXISTS(SELECT 1 FROM ALINK_NAME WHERE RLN.NAME_GROUPID = ALINK_NAME.NAME_GROUPID)");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || S.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '隧道link上的隧道名称类型错误' AS LOG FROM N_S_NAME S, N_S_NAME_NUM SN WHERE SN.NUM = 1");
		sb.append(" AND S.LINK_PID = SN.LINK_PID AND S.NAME_TYPE <> 5");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || SN.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '隧道link上有多个隧道名' AS LOG FROM N_S_NAME_NUM SN WHERE SN.NUM > 1");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '官方名称中有多个隧道类型名称类型为“隧道”的道路不含隧道属性' AS LOG");
		sb.append(" FROM RD_LINK RL,RD_LINK_FORM RLF,RD_LINK_NAME RLN");
		sb.append(" WHERE RL.LINK_PID ="+pid);
		sb.append(" AND RL.LINK_PID = RLF.LINK_PID AND RLF.FORM_OF_WAY = 31");
		sb.append(" AND RL.LINK_PID = RLN.LINK_PID AND RLN.NAME_CLASS = 1 AND RLN.NAME_TYPE = 5");
		sb.append(" AND RL.U_RECORD <>2 AND RLF.U_RECORD <>2 AND RLN.U_RECORD <>2 ");
		sb.append(" GROUP BY RL.LINK_PID HAVING COUNT(1) > 1");
		String sql = sb.toString();
		log.info("后检查GLM02257--sql:" + sql);

		DatabaseOperatorResult getObj = new DatabaseOperatorResult();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		return resultList;
	}

}

