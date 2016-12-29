package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/** 
 * @ClassName: GLM01091
 * @author songdongyan
 * @date 2016年12月6日
 * @Description: 大门的进入和退出link的FC必须为5
 * 功能等级编辑服务端后检查
 * 新增大门服务端后检查
 */
public class GLM01091 extends baseRule {

	/**
	 * 
	 */
	public GLM01091() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 功能等级编辑RdLink
			if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink,checkCommand.getOperType());
			}
			// 新增大门RdGate
			else if (obj instanceof RdGate) {
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
//		operType = OperType.CREATE;
		if(operType.equals(OperType.CREATE)){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK R, RD_GATE G");
			sb.append(" WHERE G.PID = " + rdGate.getPid());
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND G.U_RECORD <> 2");
			sb.append(" AND (G.IN_LINK_PID = R.LINK_PID OR G.OUT_LINK_PID = R.LINK_PID)");
			sb.append(" AND R.FUNCTION_CLASS <> 5") ;

			String sql = sb.toString();
			log.info("RdGate后检查GLM01091:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				String target = "[RD_GATE," + rdGate.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

	/**
	 * @param rdLink
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink, OperType operType) throws Exception {
		//rd_link修改功能等级=5
		if(rdLink.changedFields().containsKey("functionClass")){
			int functionClass = Integer.parseInt(rdLink.changedFields().get("functionClass").toString()) ;
			//非单向道路，不触发检查
			if(functionClass!=5){
				return;
			}
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']', R.MESH_ID");
			sb.append(" FROM RD_LINK R, RD_GATE G");
			sb.append(" WHERE (G.IN_LINK_PID = R.LINK_PID OR G.OUT_LINK_PID = R.LINK_PID)");
			sb.append(" AND R.LINK_PID = " + rdLink.getPid());

			String sql = sb.toString();
			log.info("RdLink后检查GLM01091:" + sql);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),(int)resultList.get(2));
			}
		}
		
	}

}
