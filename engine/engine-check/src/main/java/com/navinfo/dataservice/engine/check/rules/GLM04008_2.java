package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/** 
 * @ClassName: GLM04008_2
 * @author songdongyan
 * @date 2016年12月5日
 * @Description: 双方向大门的进入link或者退出link为单方向，报出log大门方向与道路方向矛盾
 * 道路方向编辑服务端后检查
 * 大门方向编辑服务端后检查
 * 新增大门服务端后检查
 * 大门方向编辑服务端前检查
 */
public class GLM04008_2 extends baseRule {
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * 
	 */
	public GLM04008_2() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 大门RdGate
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
			if(dir!=2){
				return;
			}
			Set<Integer> linkPidSet = new HashSet<Integer>();
			linkPidSet.add(rdGate.getInLinkPid());
			linkPidSet.add(rdGate.getOutLinkPid());
			
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK R ");
			sb.append("WHERE R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
			sb.append("AND R.U_RECORD != 2 ");
			sb.append("AND R.DIRECT in (2,3) ");

			String sql = sb.toString();
			log.info("RdGate前检查GLM04008_2:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}else{
			return;
		}
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 大门RdGate
			if (obj instanceof RdGate) {
				RdGate rdGate = (RdGate) obj;
				checkRdGate(rdGate,checkCommand.getOperType());
			}
			// Link方向编辑
			else if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink,checkCommand.getOperType());
			}	
		}
		
		
	}

	/**
	 * @param rdLink
	 * @param operType
	 * @return
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink, OperType operType) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT R.GEOMETRY,'[RD_LINK,' || R.LINK_PID || ']',R.MESH_ID FROM RD_LINK R ");
		sb.append("WHERE R.LINK_PID = " + rdLink.getPid());
		sb.append("AND R.U_RECORD != 2 ");
		sb.append("AND EXISTS( ");
		sb.append("SELECT 1 FROM RD_GATE G ");
		sb.append("WHERE G.DIR=2 ");
		sb.append("AND (R.LINK_PID = G.IN_LINK_PID OR R.LINK_PID = G.OUT_LINK_PID ) ");
		sb.append("AND R.DIRECT in (2,3) ");
		sb.append("AND G.U_RECORD <> 2 ") ;
		sb.append(")") ;

		String sql = sb.toString();
		log.info("RdLink后检查GLM04008_2:" + sql);

		DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int)resultList.get(2));
		}
	}

	/**
	 * @param rdGate
	 * @param operType
	 * @return
	 * @throws Exception 
	 */
	private void checkRdGate(RdGate rdGate, OperType operType) throws Exception {
		Set<Integer> linkPidSet = new HashSet<Integer>();
		linkPidSet.add(rdGate.getInLinkPid());
		linkPidSet.add(rdGate.getOutLinkPid());
		
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_LINK R ");
		sb.append("WHERE R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
		sb.append("AND R.U_RECORD != 2 ");
		sb.append("AND EXISTS( ");
		sb.append("SELECT 1 FROM RD_GATE G ");
		sb.append("WHERE G.DIR=2 ");
		sb.append("AND (R.LINK_PID = G.IN_LINK_PID OR R.LINK_PID = G.OUT_LINK_PID ) ");
		sb.append("AND R.DIRECT in (2,3) ");
		sb.append("AND G.U_RECORD <> 2 ") ;
		sb.append(")") ;

		String sql = sb.toString();
		log.info("RdGate后检查GLM04008_2:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_GATE," + rdGate.getPid() + "]";
			this.setCheckResult("", target, 0);
		}

	}

}
