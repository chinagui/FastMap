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
 * @ClassName GLM02264
 * @author Han Shaoming
 * @date 2017年1月11日 下午3:33:20
 * @Description TODO
 * 道路名的名称类型不能为“4.桥”、“7.出口编号”、“8.编号名称”、“14.点门牌”，否则报log
 * 名称类型编辑	服务端后检查
 */
public class GLM02264 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//名称类型编辑
			if (row instanceof RdLinkName){
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
		if(!changedFields.isEmpty()){
			//名称类型编辑
			if(changedFields.containsKey("nameType")){
				int nameType = (int) changedFields.get("nameType");
				if(nameType == 4 || nameType == 7 || nameType == 8 || nameType == 14){
					List<Object> resultList = this.check(rdLinkName.getLinkPid());
					
					if(!resultList.isEmpty()){
						this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), 
								(int)resultList.get(2),resultList.get(3).toString());
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
		
		sb.append("WITH T AS(SELECT RLN.LINK_PID,DECODE(RLN.NAME_TYPE,4,'桥',7,'出口编号',8,'编号名称',14,'点门牌') TYPE");
		sb.append(" FROM RD_LINK_NAME RLN WHERE RLN.LINK_PID ="+pid);
		sb.append(" AND RLN.U_RECORD <> 2)");
		sb.append(" SELECT DISTINCT 0 AS GEOMETRY, '[RD_LINK,' || T.LINK_PID || ']' TARGET,0 AS MESH_ID,");
		sb.append(" '\"' || LISTAGG (TYPE, '、') WITHIN GROUP (ORDER BY LINK_PID) || '\"名称类型错误' LOG");
		sb.append(" FROM T GROUP BY T.LINK_PID");
		String sql = sb.toString();
		log.info("后检查GLM02264--sql:" + sql);
		
		DatabaseOperatorResult getObj = new DatabaseOperatorResult();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		return resultList;
	}
}
