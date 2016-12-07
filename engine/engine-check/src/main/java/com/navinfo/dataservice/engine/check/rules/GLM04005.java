package com.navinfo.dataservice.engine.check.rules;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM04005
 * @author songdongyan
 * @date 2016年12月5日
 * @Description: 大门的进入线到退出线不能有禁止交限（货车交限和时间段交限允许）和顺行
 * 新增交限服务端后检查：禁止交限（不包含货车交限和时间段交限）进入线、退出线、经过线中不允许出现大门进入线、退出线。
 * 新增顺行服务端后检查：顺行进入线、退出线、经过线中不允许出现大门进入线、退出线。
 */
public class GLM04005 extends baseRule{

	/**
	 * 
	 */
	public GLM04005() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 交限RdRestriction
			if (obj instanceof RdRestriction) {
				RdRestriction rdRestriction = (RdRestriction) obj;
				checkRdRestriction(rdRestriction,checkCommand.getOperType());
			}
			// 顺行RdDirectroute
			else if (obj instanceof RdDirectroute) {
				RdDirectroute rdDirectroute = (RdDirectroute) obj;
				checkRdDirectroute(rdDirectroute,checkCommand.getOperType());
			}		
		}
	}

	/**
	 * @param rdDirectroute
	 * @param operType 
	 * @return
	 * @throws Exception 
	 */
	private void checkRdDirectroute(RdDirectroute rdDirectroute, OperType operType) throws Exception {
		// TODO Auto-generated method stub
		if(operType == OperType.CREATE)
		{
			Set<Integer> linkPidSet = new HashSet<Integer>();
			linkPidSet.add(rdDirectroute.getInLinkPid());
			linkPidSet.add(rdDirectroute.getOutLinkPid());

			for(Map.Entry<String, RdDirectrouteVia> entry:rdDirectroute.directrouteViaMap.entrySet()){
				linkPidSet.add(entry.getValue().getLinkPid());
			}

			StringBuilder sb = new StringBuilder();

			sb.append("SELECT RR.PID FROM RD_GATE RG WHERE RG.U_RECORD !=2");
			sb.append(" AND RG.IN_LINK_PID IN ");
			sb.append(StringUtils.join(linkPidSet.toArray(),","));
			sb.append(" OR RG.OUT_LINK_PID IN ");
			sb.append(StringUtils.join(linkPidSet.toArray(),","));

			String sql = sb.toString();

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (!resultList.isEmpty()) {
				this.setCheckResult("", "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]", 0);
			}
		}
	}

	/**
	 * @param rdRestriction
	 * @param operType
	 * @return
	 * @throws Exception 
	 */
	private void checkRdRestriction(RdRestriction rdRestriction, OperType operType) throws Exception {
		// TODO Auto-generated method stub
		//新增交限
		if(operType == OperType.CREATE)
		{
			//遍历RdRestrictionDetail，RdRestrictionVia，构造需要检查的linkPid
			Set<Integer> linkPidSet = new HashSet<Integer>();
			int inLinkPid = rdRestriction.getInLinkPid();
			linkPidSet.add(inLinkPid);

			for(Map.Entry<Integer, RdRestrictionDetail> entry:rdRestriction.detailMap.entrySet()){
				boolean flg = false;
				RdRestrictionDetail rdRestrictionDetail = entry.getValue();
				//非禁止交限，不参与检查
				if(entry.getValue().getType()!=2){
					continue;
				}
				for(Map.Entry<String, RdRestrictionCondition> entryCondition:rdRestrictionDetail.conditionMap.entrySet()){
					RdRestrictionCondition rdRestrictionCondition = entryCondition.getValue();
					//时间段交限，不参与检查
					if(rdRestrictionCondition.getTimeDomain()!=null){
						continue;
					}
					//火车交限，不参与检查:01配送卡车,001运输卡车
					if((rdRestrictionCondition.getVehicle()&6)>=2){
						continue;
					}
					flg = true;
				}
				if(flg){
					linkPidSet.add(rdRestrictionDetail.getOutLinkPid());
					for(Map.Entry<String, RdRestrictionVia> entryVia:rdRestrictionDetail.viaMap.entrySet()){
						linkPidSet.add(entryVia.getValue().getLinkPid());
					}
				}
			}

			StringBuilder sb = new StringBuilder();

			sb.append("SELECT RR.PID FROM RD_GATE RG WHERE RG.U_RECORD !=2");
			sb.append(" AND RG.IN_LINK_PID IN ");
			sb.append(StringUtils.join(linkPidSet.toArray(),","));
			sb.append(" OR RG.OUT_LINK_PID IN ");
			sb.append(StringUtils.join(linkPidSet.toArray(),","));

			String sql = sb.toString();

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (!resultList.isEmpty()) {
				this.setCheckResult("", "[RD_RESTRICTION," + rdRestriction.getPid() + "]", 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 交限RdRestriction
			if (obj instanceof RdRestriction) {
				RdRestriction rdRestriction = (RdRestriction) obj;
				checkRdRestriction(rdRestriction,checkCommand.getOperType());
			}
			// 顺行RdDirectroute
			else if (obj instanceof RdDirectroute) {
				RdDirectroute rdDirectroute = (RdDirectroute) obj;
				checkRdDirectroute(rdDirectroute,checkCommand.getOperType());
			}		
		}		
	}
}
