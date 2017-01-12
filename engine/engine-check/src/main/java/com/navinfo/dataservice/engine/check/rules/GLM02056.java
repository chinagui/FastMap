package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM02056
 * @author Han Shaoming
 * @date 2017年1月11日 下午3:11:14
 * @Description TODO
 * link有别名或曾用名，不能没有官方名称；除高速、城市高速之外种别的道路不应该有曾用名.增加屏蔽条件：点门牌类型名称不查
 * 名称分类编辑	服务端后检查
 */
public class GLM02056 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//名称分类编辑
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
			//名称分类编辑
			if(changedFields.containsKey("nameClass")){
				boolean check = this.check(rdLinkName.getLinkPid());
				
				if(check){
					String target = "[RD_LINK," + rdLinkName.getLinkPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private boolean check(int pid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		 
		sb.append("SELECT N1.LINK_PID FROM RD_LINK_NAME N1");
		sb.append(" WHERE N1.LINK_PID ="+pid+" AND N1.NAME_CLASS IN (2,3)");
		sb.append(" AND N1.NAME_TYPE <> 14 AND N1.U_RECORD <>2");
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_NAME N2");
		sb.append(" WHERE N2.LINK_PID = N1.LINK_PID AND N2.U_RECORD <>2");
		sb.append(" AND N2.NAME_CLASS = 1)");
		sb.append(" UNION");
		sb.append(" SELECT L.LINK_PID FROM RD_LINK L, RD_LINK_NAME N");
		sb.append(" WHERE L.LINK_PID ="+pid+" AND L.KIND <>1 AND L.KIND <>2 AND L.LINK_PID = N.LINK_PID");
		sb.append(" AND N.NAME_TYPE <> 14 AND N.NAME_CLASS = 3 AND L.U_RECORD <>2 AND N.U_RECORD <>2");
		String sql = sb.toString();
		log.info("后检查GLM02056--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
