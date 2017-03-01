package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM04008_1
 * @author songdongyan
 * @date 2016年12月5日
 * @Description: 双向道路上制作了一个方向的大门，但另一个方向没有禁止通行（永久）的普通交限，报双向道路上制作了一个方向的大门，在另一个方向没有禁止通行（永久）的交限
 * 大门方向编辑服务端后检查:如果大门为单向，则执行检查。如果大门进入线与退出线为双向道路，且另一个方向并未制作禁止通行的普通交限，则检查不通过。
 * 新增交限服务端后检查：新增交限，如果为非禁止通行（永久）的普通交限，则执行检查。如果交限进入线退出线为双向道路，且另一个方向建立了单方向大门，则检查不通过。
 * 修改交限服务端后检查：修改交限，如果修改为非禁止通行（永久）的普通交限，则执行检查。如果交限进入线退出线为双向道路，且另一个方向建立了单方向大门，则检查不通过；
 * 		如果修改为禁止通行（永久）的普通交限，则执行检查。如果交限进入线退出线为双向道路，且另一个方向建立非单方向大门，则检查不通过。
 * 		修改RD_RESTRICTION，RD_RESTRICTION_DETAIL会触发检查项=-=
 * 大门方向编辑服务端前检查：如果大门为单向，则执行检查。如果大门进入线与退出线为双向道路，且另一个方向并未制作禁止通行的普通交限，则检查不通过。
 */
public class GLM04008_1 extends baseRule{
	
	/**
	 * 
	 */
	public GLM04008_1() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 大门方向编辑RdGate
			if (obj instanceof RdGate) {
				RdGate rdGate = (RdGate) obj;
				checkRdGatePre(rdGate);
			}	
		}
		
	}

	/**
	 * @param rdGate
	 * @throws Exception 
	 */
	private void checkRdGatePre(RdGate rdGate) throws Exception {
		if(rdGate.changedFields.containsKey("dir")){
			int dir = Integer.parseInt(rdGate.changedFields.get("dir").toString()) ;
			//非单向大门，不触发检查
			if(dir!=1){
				return;
			}
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK L1, RD_LINK L2");
			sb.append(" WHERE L1.LINK_PID=" + rdGate.getInLinkPid());
			sb.append(" AND L1.DIRECT = 1");
			sb.append(" AND L2.LINK_PID=" + rdGate.getOutLinkPid());
			sb.append(" AND L2.DIRECT = 1");
			sb.append(" AND L1.U_RECORD <> 2");
			sb.append(" AND L2.U_RECORD <> 2") ;
			sb.append(" AND NOT EXISTS (SELECT 1") ;
			sb.append(" FROM RD_RESTRICTION R, RD_RESTRICTION_DETAIL D") ;
			sb.append(" WHERE R.PID = D.RESTRIC_PID") ;
			sb.append(" AND R.IN_LINK_PID = " + rdGate.getOutLinkPid()) ;
			sb.append(" AND D.OUT_LINK_PID = " + rdGate.getInLinkPid()) ;
			sb.append(" AND D.TYPE = 1") ;
			sb.append(" AND R.U_RECORD <> 2") ;
			sb.append(" AND D.U_RECORD <> 2)") ;

			String sql = sb.toString();
			log.info("RdGate前检查GLM04008_1:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
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
			//交限RdRestrictionDetail
			else if(obj instanceof RdRestrictionDetail){
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)obj;
				checkRdRestrictionDetail(rdRestrictionDetail);
			}
//			// 大门RdGate
//			else if (obj instanceof RdGate) {
//				RdGate rdGate = (RdGate) obj;
//				checkRdGate(rdGate,checkCommand.getOperType());
//			}	
			else if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink);
			}
		}
		
	}


	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		if(rdLink.changedFields().containsKey("direct")){
			int direct = Integer.parseInt(rdLink.changedFields().get("direct").toString());
			if(direct==1){
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT  DISTINCT 1 FROM RD_LINK RL1, RD_LINK RL2");
				sb.append(" WHERE RL1.DIRECT = 1");
				sb.append("   AND RL2.DIRECT = 1");
				sb.append("   AND RL1.U_RECORD <> 2");
				sb.append("   AND RL2.U_RECORD <> 2");
				sb.append(" AND ((RL1.LINK_PID = " + rdLink.getPid() + ") OR (RL2.LINK_PID = " + rdLink.getPid() + "))");
				sb.append("   AND NOT EXISTS");
				sb.append(" (SELECT 1 FROM RD_RESTRICTION R, RD_RESTRICTION_DETAIL D, RD_GATE G");
				sb.append("         WHERE R.PID = D.RESTRIC_PID");
				sb.append("           AND D.TYPE = 1");
				sb.append("           AND R.IN_LINK_PID = RL1.LINK_PID");
				sb.append("           AND D.OUT_LINK_PID = RL2.LINK_PID");
				sb.append("           AND G.IN_LINK_PID = D.OUT_LINK_PID");
				sb.append("           AND G.OUT_LINK_PID = R.IN_LINK_PID");
				sb.append("           AND R.U_RECORD <> 2");
				sb.append("           AND D.U_RECORD <> 2");
				sb.append("           AND G.DIR <> 1");
				sb.append("           AND G.U_RECORD <> 2)");
				
				String sql = sb.toString();
				log.info("RdLink后检查GLM04008_1:" + sql);

				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);

				if(resultList.size()>0){
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
		
	}

	/**
	 * @param rdRestrictionDetail
	 * @throws Exception 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail) throws Exception {
		//禁止进入交限
		boolean checkFlg = false;
		if(rdRestrictionDetail.status().equals(ObjStatus.INSERT)){
			if(rdRestrictionDetail.getType()!=1){
				checkFlg = true;
			}
		}else if(rdRestrictionDetail.changedFields().containsKey("type")){
			int type = Integer.parseInt(rdRestrictionDetail.changedFields().get("type").toString());
			if(type != 1){
				checkFlg = true;
			}
		}
		
		if(checkFlg){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_RESTRICTION R, RD_LINK RL1, RD_LINK RL2, RD_GATE G");
			sb.append(" WHERE R.IN_LINK_PID = RL1.LINK_PID");
			sb.append(" AND RL2.LINK_PID = " + rdRestrictionDetail.getOutLinkPid());
			sb.append(" AND RL1.DIRECT = 1");
			sb.append(" AND RL2.DIRECT = 1");
			sb.append(" AND G.IN_LINK_PID = " + rdRestrictionDetail.getOutLinkPid());
			sb.append(" AND G.OUT_LINK_PID = R.IN_LINK_PID");
			sb.append(" AND G.DIR = 1");
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND RL1.U_RECORD <> 2");
			sb.append(" AND RL2.U_RECORD <> 2");
			sb.append(" AND G.U_RECORD <> 2");
			sb.append(" AND R.PID = " + rdRestrictionDetail.getRestricPid());
			
			String sql = sb.toString();
			log.info("RdRestrictionDetail后检查GLM04008_1:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_RESTRICTION," + rdRestrictionDetail.getRestricPid() + "]";
				this.setCheckResult("", target, 0);
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
		//新增交限:新增交限非永久交限，相反方向有单向大门
		if(rdRestriction.status().equals(ObjStatus.INSERT)){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK RL1, RD_LINK RL2, RD_GATE G");
			sb.append(" WHERE RL1.DIRECT = 1");
			sb.append(" AND RL2.DIRECT = 1");
			sb.append(" AND G.IN_LINK_PID = RL1.LINK_PID");
			sb.append(" AND G.OUT_LINK_PID = RL2.LINK_PID");
			sb.append(" AND G.DIR = 1");
			sb.append(" AND RL1.U_RECORD <> 2");
			sb.append(" AND RL2.U_RECORD <> 2");
			sb.append(" AND G.U_RECORD <> 2");
			
			sb.append("   AND NOT EXISTS");
			sb.append(" (SELECT 1 FROM RD_RESTRICTION R, RD_RESTRICTION_DETAIL D");
			sb.append("         WHERE R.PID = D.RESTRIC_PID");
			sb.append("           AND D.TYPE = 1");
			sb.append("           AND G.IN_LINK_PID = D.OUT_LINK_PID");
			sb.append("           AND G.OUT_LINK_PID = R.IN_LINK_PID");
			sb.append("           AND R.U_RECORD <> 2");
			sb.append("           AND D.U_RECORD <> 2)");
			

			String sql = sb.toString();
			log.info("RdRestriction后检查GLM04008_1:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_RESTRICTION," + rdRestriction.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
		//删除交限
		else if(rdRestriction.status().equals(ObjStatus.DELETE)){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT G.PID FROM RD_RESTRICTION R, RD_RESTRICTION_DETAIL D, RD_LINK RL1, RD_LINK RL2, RD_GATE G");
			sb.append(" WHERE R.PID = D.RESTRIC_PID");
			sb.append(" AND D.TYPE = 1");
			sb.append(" AND R.IN_LINK_PID = RL1.LINK_PID");
			sb.append(" AND D.OUT_LINK_PID = RL2.LINK_PID");
			sb.append(" AND RL1.DIRECT = 1");
			sb.append(" AND RL2.DIRECT = 1");
			sb.append(" AND G.IN_LINK_PID = D.OUT_LINK_PID");
			sb.append(" AND G.OUT_LINK_PID = R.IN_LINK_PID");
			sb.append(" AND G.DIR = 1");
			sb.append(" AND RL1.U_RECORD <> 2");
			sb.append(" AND RL2.U_RECORD <> 2");
			sb.append(" AND G.U_RECORD <> 2");
			
			sb.append(" AND R.PID = " + rdRestriction.getPid());
			

			String sql = sb.toString();
			log.info("RdRestriction后检查GLM04008_1:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_GATE," + resultList.get(0).toString() + "]";
				this.setCheckResult("", target, 0);
			}
		}

	}


	/**
	 * @param rdGate
	 * @param operType
	 * @return
	 * @throws Exception 
	 */
	private void checkRdGate(RdGate rdGate, OperType operType) throws Exception {
		//大门方向编辑	
		if(rdGate.changedFields.containsKey("dir")){
			int dir = Integer.parseInt(rdGate.changedFields.get("dir").toString()) ;
			//非单向大门，不触发检查
			if(dir!=1){
				return;
			}
			
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK L1, RD_LINK L2");
			sb.append(" WHERE L1.LINK_PID=" + rdGate.getInLinkPid());
			sb.append(" AND L1.DIRECT = 1");
			sb.append(" AND L2.LINK_PID=" + rdGate.getOutLinkPid());
			sb.append(" AND L2.DIRECT = 1");
			sb.append(" AND L1.U_RECORD <> 2");
			sb.append(" AND L2.U_RECORD <> 2") ;
			sb.append(" AND NOT EXISTS (SELECT 1") ;
			sb.append(" FROM RD_RESTRICTION R, RD_RESTRICTION_DETAIL D") ;
			sb.append(" WHERE R.PID = D.RESTRIC_PID") ;
			sb.append(" AND R.IN_LINK_PID = " + rdGate.getOutLinkPid()) ;
			sb.append(" AND D.OUT_LINK_PID = " + rdGate.getInLinkPid()) ;
			sb.append(" AND D.TYPE = 1") ;
			sb.append(" AND R.U_RECORD <> 2") ;
			sb.append(" AND D.U_RECORD <> 2)") ;

			String sql = sb.toString();
			log.info("RdGate后检查GLM04008_1:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_GATE," + rdGate.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}
	
}
