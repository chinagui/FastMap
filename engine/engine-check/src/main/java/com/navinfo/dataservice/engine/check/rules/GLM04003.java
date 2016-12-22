package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM04003
 * @author songdongyan
 * @date 2016年12月7日
 * @Description: EG类型的大门进入Link或退出link,不能存在“车辆限制类型（有车辆类型）”信息含：“禁止”中包含“急救车”，或“允许”中不包含“急救车”
 * 大门类型编辑服务端前检查：大门类型为EG，触发检查
 * 
 */
public class GLM04003 extends baseRule{

	/**
	 * 
	 */
	public GLM04003() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 大门类型编辑RdGate
			if (obj instanceof RdGate) {
				RdGate rdGate = (RdGate) obj;
				checkRdGate(rdGate,checkCommand.getOperType());
			}	
		}
		
	}

	/**
	 * @param rdGate
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdGate(RdGate rdGate, OperType operType) throws Exception {
		if(rdGate.getType()==0){
			Set<Integer> rdLinkPidSet = new HashSet<Integer>();
			rdLinkPidSet.add(rdGate.getInLinkPid());
			rdLinkPidSet.add(rdGate.getOutLinkPid());
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 'EG类型大门的进入和退出link上的“永久车辆限制”信息的“允许”必须包含“急救车”' LOG");
			sb.append(" FROM RD_LINK_LIMIT L");
			sb.append(" WHERE L.LINK_PID IN (" + StringUtils.join(rdLinkPidSet.toArray(),",") + ")");
			sb.append(" AND L.TYPE = 2");
			sb.append(" AND L.TIME_DOMAIN IS NULL");
			sb.append(" AND L.VEHICLE <> 0");
			sb.append(" AND BITAND(L.VEHICLE, 2147483648) = 2147483648");
			sb.append(" AND BITAND(L.VEHICLE, 128) = 0");
//			sb.append(" AND BIT_UTIL.BITSUB(L.VEHICLE, 7, 1) = 0");
//			sb.append(" AND BIT_UTIL.BITSUB(L.VEHICLE, 31, 1) = 1");
			sb.append(" UNION ALL");
			sb.append(" SELECT 'EG类型大门的进入和退出link上的“永久车辆限制”信息的“禁止”不能包含“急救车”' LOG");
			sb.append(" FROM RD_LINK_LIMIT L");
			sb.append(" WHERE L.LINK_PID IN (" + StringUtils.join(rdLinkPidSet.toArray(),",") + ")");
			sb.append(" AND L.TYPE = 2");
			sb.append(" AND L.TIME_DOMAIN IS NULL");
			sb.append(" AND L.VEHICLE <> 0");
			sb.append(" AND BITAND(L.VEHICLE, 2147483648) = 0");
			sb.append(" AND BITAND(L.VEHICLE, 128) = 128");
//			sb.append(" AND BIT_UTIL.BITSUB(L.VEHICLE, 7, 1) = 1");
//			sb.append(" AND BIT_UTIL.BITSUB(L.VEHICLE, 31, 1) = 0");
			
			String sql = sb.toString();

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (!resultList.isEmpty()) {
				this.setCheckResult("", "", 0,resultList.get(0).toString());
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
