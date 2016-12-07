package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGateCondition;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/** 
 * @ClassName: GLM04006
 * @author songdongyan
 * @date 2016年12月5日
 * @Description: 大门的进入线或退出线有一根为10级路或者都为10级路，则大门的“通行对象”只能为“行人”
 * 新增大门,编辑端后检查:
 * Link种别编辑，编辑端后检查
 */
public class GLM04006 extends baseRule{

	/**
	 * 
	 */
	public GLM04006() {
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
			// 大门RdGate
			if (obj instanceof RdGate) {
				RdGate rdGate = (RdGate) obj;
				checkRdGate(rdGate,checkCommand.getOperType());
			}
			// 大门RdGateCondition
			else if (obj instanceof RdGateCondition) {
				RdGateCondition rdGateCondition = (RdGateCondition) obj;
				checkRdGateCondition(rdGateCondition,checkCommand.getOperType());
			}			
			// Link种别编辑
			else if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink,checkCommand.getOperType());
			}	
		}
		
	}

	

	/**
	 * @param rdGateCondition
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdGateCondition(RdGateCondition rdGateCondition, OperType operType) throws Exception {
		//通行对象为车辆，触发检查
		if(rdGateCondition.getValidObj()==0){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1");
			sb.append(" FROM RD_LINK R, RD_GATE G");
			sb.append(" WHERE R.KIND = 10");
			sb.append(" AND (R.LINK_PID = G.IN_LINK_PID OR R.LINK_PID = G.OUT_LINK_PID)");
			sb.append(" AND R.U_RECORD != 2");
			sb.append(" AND G.U_RECORD != 2");
			sb.append(" AND G.PID = " + rdGateCondition.getPid());

			String sql = sb.toString();

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (resultList.size()>0) {
				this.setCheckResult("","[RD_GATE," + rdGateCondition.getPid() + "]", 0);
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

		sb.append("SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']', R.MESH_ID");
		sb.append(" FROM RD_LINK R, RD_GATE G, RD_GATE_CONDITION C");
		sb.append(" WHERE G.PID = RDGATE_PID");
		sb.append(" AND G.PID = C.PID");
		sb.append(" AND R.U_RECORD != 2");
		sb.append(" AND G.U_RECORD != 2");
		sb.append(" AND C.U_RECORD != 2");
		sb.append(" AND R.KIND = 10");
		sb.append(" AND C.VALID_OBJ <> 1");
		sb.append(" AND (G.IN_LINK_PID = R.LINK_PID OR G.OUT_LINK_PID = R.LINK_PID)");
		sb.append(" AND R.LINK_PID = " + rdLink.getPid()) ;

		String sql = sb.toString();

		DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
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
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_LINK R, RD_GATE G, RD_GATE_CONDITION C ");
		sb.append("WHERE G.PID = " + rdGate.getPid());
		sb.append("AND G.PID = C.PID ");
		sb.append("AND R.U_RECORD != 2 ");
		sb.append("AND G.U_RECORD != 2 ");
		sb.append("AND C.U_RECORD != 2 ");
		sb.append("AND R.KIND = 10 ");
		sb.append("AND C.VALID_OBJ <> 1 ");
		sb.append("AND (G.IN_LINK_PID = R.LINK_PID OR G.OUT_LINK_PID = R.LINK_PID) ");

		String sql = sb.toString();

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_GATE," + rdGate.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}

}
