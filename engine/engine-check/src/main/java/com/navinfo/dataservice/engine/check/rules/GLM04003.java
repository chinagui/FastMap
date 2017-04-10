package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
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
		if(rdGate.changedFields.containsKey("type")){
			int type = Integer.parseInt(rdGate.changedFields.get("type").toString()) ;
			if(type!=0){
				return;
			}
			else{
				//获取进入线退出线信息
				Set<Integer> rdLinkPidSet = new HashSet<Integer>();
				if(rdGate.changedFields.containsKey("inLinkPid")){
					rdLinkPidSet.add((Integer) rdGate.changedFields.get("inLinkPid"));
				}else{
					rdLinkPidSet.add(rdGate.getInLinkPid());
				}
				if(rdGate.changedFields.containsKey("outLinkPid")){
					rdLinkPidSet.add((Integer) rdGate.changedFields.get("outLinkPid"));
				}else{
					rdLinkPidSet.add(rdGate.getOutLinkPid());
				}
				
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 'EG类型大门的进入和退出link上的“永久车辆限制”信息的“允许”必须包含“急救车”' LOG");
				sb.append(" FROM RD_LINK_LIMIT L");
				sb.append(" WHERE L.LINK_PID IN (" + StringUtils.join(rdLinkPidSet.toArray(),",") + ")");
				sb.append(" AND L.TYPE = 2");
				sb.append(" AND L.TIME_DOMAIN IS NULL");
				sb.append(" AND L.VEHICLE <> 0");
				sb.append(" AND BITAND(L.VEHICLE, 2147483648) = 2147483648");
				sb.append(" AND BITAND(L.VEHICLE, 128) = 0");
				sb.append(" UNION ALL");
				sb.append(" SELECT 'EG类型大门的进入和退出link上的“永久车辆限制”信息的“禁止”不能包含“急救车”' LOG");
				sb.append(" FROM RD_LINK_LIMIT L");
				sb.append(" WHERE L.LINK_PID IN (" + StringUtils.join(rdLinkPidSet.toArray(),",") + ")");
				sb.append(" AND L.TYPE = 2");
				sb.append(" AND L.TIME_DOMAIN IS NULL");
				sb.append(" AND L.VEHICLE <> 0");
				sb.append(" AND BITAND(L.VEHICLE, 2147483648) = 0");
				sb.append(" AND BITAND(L.VEHICLE, 128) = 128");
				
				String sql = sb.toString();

				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);

				if (!resultList.isEmpty()) {
					this.setCheckResult("", "", 0,resultList.get(0).toString());
				}
				
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 大门类型编辑RdGate
			if (obj instanceof RdGate) {
				RdGate rdGate = (RdGate) obj;
				if(rdGate.status().equals(ObjStatus.UPDATE)){
					int type = Integer.parseInt(rdGate.changedFields().get("type").toString()) ;
					if(type==0){
						checkRdGate(rdGate);
					}
				}
			}
			else if(obj instanceof RdLinkLimit) {
				RdLinkLimit rdLinkLimit = (RdLinkLimit) obj;
				checkRdLinkLimit(rdLinkLimit);
			}
		}
	}

	/**
	 * @param rdLinkLimit
	 * @throws Exception 
	 */
	private void checkRdLinkLimit(RdLinkLimit rdLinkLimit) throws Exception {
		boolean checkFlg = false;
		if(rdLinkLimit.status().equals(ObjStatus.INSERT)){
			if(rdLinkLimit.getType() == 2){
				checkFlg = true;
			}
		}else if(rdLinkLimit.status().equals(ObjStatus.UPDATE)){
			if(rdLinkLimit.changedFields().containsKey("type")){
				int type = Integer.parseInt(rdLinkLimit.changedFields().get("type").toString()) ;
				if(type==2){
					checkFlg = true;
				}
			}
			if(rdLinkLimit.changedFields().containsKey("vehicle")){
				checkFlg = true;
			}
		}
		
		if(checkFlg){
			StringBuilder sb = new StringBuilder();

			sb.append(" SELECT 'EG类型大门的进入和退出link上的“永久车辆限制”信息的“允许”必须包含“急救车”' LOG");
			sb.append("  FROM RD_LINK_LIMIT L, RD_GATE G");
			sb.append(" WHERE (L.LINK_PID = G.IN_LINK_PID OR L.LINK_PID = G.OUT_LINK_PID)");
			sb.append("   AND L.TYPE = 2");
			sb.append("   AND L.TIME_DOMAIN IS NULL");
			sb.append("   AND L.VEHICLE <> 0");
			sb.append("   AND BITAND(L.VEHICLE, 2147483648) = 2147483648");
			sb.append("   AND BITAND(L.VEHICLE, 128) = 0");
			sb.append("   AND L.U_RECORD <> 2");
			sb.append("   AND G.U_RECORD <> 2");
			sb.append("   AND L.LINK_PID  = " + rdLinkLimit.getLinkPid());
			sb.append(" UNION ALL");
			sb.append(" SELECT 'EG类型大门的进入和退出link上的“永久车辆限制”信息的“禁止”不能包含“急救车”' LOG");
			sb.append("  FROM RD_LINK_LIMIT L, RD_GATE G");
			sb.append(" WHERE (L.LINK_PID = G.IN_LINK_PID OR L.LINK_PID = G.OUT_LINK_PID)");
			sb.append("   AND L.TYPE = 2");
			sb.append("   AND L.TIME_DOMAIN IS NULL");
			sb.append("   AND L.VEHICLE <> 0");
			sb.append("   AND BITAND(L.VEHICLE, 2147483648) = 0");
			sb.append("   AND BITAND(L.VEHICLE, 128) = 128");
			sb.append("   AND L.U_RECORD <> 2");
			sb.append("   AND G.U_RECORD <> 2");
			sb.append("   AND L.LINK_PID  = " + rdLinkLimit.getLinkPid());
			
			String sql = sb.toString();
			log.info("GLM04003 RdLinkLimit sql:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (!resultList.isEmpty()) {
				String target = "[RD_LINK," + rdLinkLimit.getLinkPid() + "]";
				this.setCheckResult("", target, 0,resultList.get(0).toString());
			}
		}
	}

	/**
	 * @param rdGate
	 * @throws Exception 
	 */
	private void checkRdGate(RdGate rdGate) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT 'EG类型大门的进入和退出link上的“永久车辆限制”信息的“允许”必须包含“急救车”' LOG");
		sb.append("  FROM RD_LINK_LIMIT L, RD_GATE G");
		sb.append(" WHERE (L.LINK_PID = G.IN_LINK_PID OR L.LINK_PID = G.OUT_LINK_PID)");
		sb.append("   AND L.TYPE = 2");
		sb.append("   AND L.TIME_DOMAIN IS NULL");
		sb.append("   AND L.VEHICLE <> 0");
		sb.append("   AND BITAND(L.VEHICLE, 2147483648) = 2147483648");
		sb.append("   AND BITAND(L.VEHICLE, 128) = 0");
		sb.append("   AND L.U_RECORD <> 2");
		sb.append("   AND G.U_RECORD <> 2");
		sb.append("   AND G.PID = " + rdGate.getPid());
		sb.append(" UNION ALL");
		sb.append(" SELECT 'EG类型大门的进入和退出link上的“永久车辆限制”信息的“禁止”不能包含“急救车”' LOG");
		sb.append("  FROM RD_LINK_LIMIT L, RD_GATE G");
		sb.append(" WHERE (L.LINK_PID = G.IN_LINK_PID OR L.LINK_PID = G.OUT_LINK_PID)");
		sb.append("   AND L.TYPE = 2");
		sb.append("   AND L.TIME_DOMAIN IS NULL");
		sb.append("   AND L.VEHICLE <> 0");
		sb.append("   AND BITAND(L.VEHICLE, 2147483648) = 0");
		sb.append("   AND BITAND(L.VEHICLE, 128) = 128");
		sb.append("   AND L.U_RECORD <> 2");
		sb.append("   AND G.U_RECORD <> 2");
		sb.append("   AND G.PID = " + rdGate.getPid());
		
		String sql = sb.toString();
		log.info("GLM04003 RdGate sql:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
			String target = "[RD_GATE," + rdGate.getPid() + "]";
			this.setCheckResult("", target, 0,resultList.get(0).toString());
		}
		
	}

}
