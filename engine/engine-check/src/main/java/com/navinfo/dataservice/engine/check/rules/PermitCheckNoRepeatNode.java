package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * SHAPING_CHECK_CROSS_RDLINK_RDLINK	两条Link相交，必须做立交或者打断	两条Link相交，必须做立交或者打断
 * 该位置已有节点,同一坐标不能有两个节点,请创建点点立交
 * 移动端点 服务端后检查:
 * 分离节点 服务端后检查:
 */

public class PermitCheckNoRepeatNode extends baseRule {

	public PermitCheckNoRepeatNode() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//移动端点
			if (row instanceof RdNode){
				RdNode rdNode = (RdNode) row;
				this.checkRdNode(rdNode);
			}
			//分离节点
			else if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
			}
		}
		/*
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;

				int sPid = rdLink.getsNodePid();
				int ePid = rdLink.geteNodePid();
				LineString rdLine = (LineString) rdLink.getGeometry();
				Point rdS = rdLine.getStartPoint();
				Point rdE = rdLine.getEndPoint();
				Geometry geo = rdLink.getGeometry();

				// 相交的线是否建立立交
				String sql = "SELECT B.*" + "  FROM RD_LINK A, RD_LINK B" + " WHERE A.LINK_PID = " + rdLink.getPid()
						+ "   AND B.LINK_PID <> A.LINK_PID AND A.U_RECORD != 2 AND B.U_RECORD != 2 "
						+ "   AND SDO_RELATE(B.GEOMETRY, A.GEOMETRY, 'MASK=TOUCH') = 'TRUE'";
				RdLinkSelector linkSelector = new RdLinkSelector(getConn());
				List<RdLink> rdList = linkSelector.loadBySql(sql, false);

				RdGscSelector gscSelector = new RdGscSelector(getConn());
				boolean isError = false;
				for (int i = 0; i < rdList.size(); i++) {
					RdLink linkB = rdList.get(i);
					// 属于挂接link
					if (sPid == linkB.getsNodePid() || sPid == linkB.geteNodePid() || ePid == linkB.getsNodePid()
							|| ePid == linkB.geteNodePid()) {
						continue;
					}

					LineString rdLineB = (LineString) linkB.getGeometry();
					Point rdSB = rdLineB.getStartPoint();
					Point rdEB = rdLineB.getEndPoint();
					Point touchPoint = null;
					if ((rdSB.getX() == rdS.getX() && rdSB.getY() == rdS.getY())
							|| (rdSB.getX() == rdE.getX() && rdSB.getY() == rdE.getY())) {
						touchPoint = rdSB;
					}
					if ((rdEB.getX() == rdS.getX() && rdEB.getY() == rdS.getY())
							|| (rdEB.getX() == rdE.getX() && rdEB.getY() == rdE.getY())) {
						touchPoint = rdEB;
					}

					if (touchPoint == null) {
						continue;
					}

					String sqltmp = "SELECT G.*" + "  FROM RD_GSC_LINK L1, RD_GSC_LINK L2, RD_GSC G"
							+ " WHERE L1.TABLE_NAME = 'RD_LINK'" + "   AND L2.TABLE_NAME = 'RD_LINK'"
							+ "   AND L1.U_RECORD != 2 " + "   AND L2.U_RECORD != 2 " + "   AND G.U_RECORD != 2 "
							+ "   AND L1.LINK_PID = " + rdLink.getPid() + "   AND L1.PID = L2.PID"
							+ "   AND L2.LINK_PID = " + linkB.getPid() + "   AND L1.PID = G.PID"
							+ "	  AND L1.START_END IN (1, 2)" + "	  AND L2.START_END IN (1, 2)";
					List<RdGsc> gscList = gscSelector.loadBySql(sqltmp, false);
					if (gscList.size() == 0) {
						isError = true;
						break;
					}
					boolean isGsc = false;
					for (int m = 0; m < gscList.size(); m++) {
						RdGsc gscTmp = gscList.get(m);
						Point gscPoint = (Point) gscTmp.getGeometry();
						if (GeoHelper.isPointEquals(touchPoint.getX(), touchPoint.getY(), gscPoint.getX(),
								gscPoint.getY())) {
							isGsc = true;
							break;
						}
						if (!isGsc) {
							isError = true;
							break;
						}
					}

					if (isError) {
						this.setCheckResult(geo, "[RD_LINK," + rdLink.getPid() + "]", rdLink.getMeshId());
					}
				}
			}
		}
		*/
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdNode
	 * @throws Exception 
	 */
	private void checkRdNode(RdNode rdNode) throws Exception {
		// TODO Auto-generated method stub
		boolean check = this.check(rdNode.getPid());

		if(check){
			String target = "[RD_NODE," + rdNode.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		// TODO Auto-generated method stub
		Set<Integer> nodePids = new HashSet<Integer>();
		//分离节点
		Map<String, Object> changedFields = rdLink.changedFields();
		if(!changedFields.isEmpty()){
			Integer sNodePid = null;
			Integer eNodePid = null;
			if(changedFields.containsKey("sNodePid")){
				sNodePid = (Integer) changedFields.get("sNodePid");
				if(sNodePid != null){
					nodePids.add(sNodePid);
				}
			}
			if(changedFields.containsKey("eNodePid")){
				eNodePid = (Integer) changedFields.get("eNodePid");
				if(eNodePid != null){
					nodePids.add(eNodePid);
				}
			}
		}
		for (Integer nodePid : nodePids) {
			boolean check = this.check(nodePid);

			if(check){
				String target = "[RD_LINK," + rdLink.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

	/**
	 * 
	 * @author Han Shaoming
	 * @param nodePid
	 * @return
	 * @throws Exception 
	 */
	private boolean check(Integer nodePid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		  
		sb.append("SELECT B.NODE_PID FROM RD_NODE A, RD_NODE B");
		sb.append("  WHERE A.NODE_PID = "+nodePid);
		sb.append(" AND B.NODE_PID <> A.NODE_PID AND A.U_RECORD <> 2 AND B.U_RECORD <> 2");
		sb.append(" AND SDO_RELATE(B.GEOMETRY, A.GEOMETRY, 'MASK=EQUAL') = 'TRUE'");
		
		String sql = sb.toString();
		log.info("后检查PERMIT_CHECK_NO_REPEAT_NODE--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
