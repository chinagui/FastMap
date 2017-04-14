package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * 坡度信息记录主点的坡度信息小于等于1或大于3条，报log
 * 
 * @author wangdongbin
 *
 */
public class GLM10001 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			if (obj instanceof RdSlope) {
				RdSlope rdSlope = (RdSlope) obj;
				checkRdSlope(rdSlope);
			}
		}

	}

	/**
	 * 检查坡度
	 * @param rdSlope
	 * @throws Exception
	 */
	private void checkRdSlope(RdSlope rdSlope) throws Exception {
		int nodePid = rdSlope.getNodePid();
		StringBuilder sb = new StringBuilder();
		sb.append("select 1");
		sb.append(" from rd_slope s");
		sb.append(" where s.node_pid="+nodePid);
		sb.append(" and s.u_record <> 2");
		
		// 查询当前删除的坡度所在点上的其他坡度信息
		String sql = sb.toString();
		log.info("RdSlope后检查GLM10001:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		// 坡度信息小于等于1或大于3条，报log
		if(resultList.size()>3 || resultList.size()<=1){
			String target = "[RD_NODE," + rdSlope.getNodePid() + "]";
			this.setCheckResult("", target, 0);
		}
	}

}
