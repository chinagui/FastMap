package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM06005
 * @author Han Shaoming
 * @date 2017年1月9日 下午2:07:08
 * @Description TODO
 * 分岔口中相同进入线、进入点，只能对应两条或两条以上的退出线
 * 新增分岔口提示	服务端后检查
 * 删除分岔口提示	服务端后检查
 * 分离节点	服务端后检查
 */
public class GLM06005 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
	}


	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//新增分岔口提示,删除分岔口提示
			if (row instanceof RdSe){
				RdSe rdSe = (RdSe) row;
				this.checkRdSe(rdSe);
			}
			//分离节点
			else if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdSe
	 * @throws Exception 
	 */
	private void checkRdSe(RdSe rdSe) throws Exception {
		// TODO Auto-generated method stub
		if((ObjStatus.INSERT.equals(rdSe.status()))||(ObjStatus.DELETE.equals(rdSe.status()))){
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT S.PID ");
			sb.append("  FROM RD_SE S ");
			sb.append(" WHERE S.U_RECORD <> 2 ");
			sb.append("   AND S.IN_LINK_PID = " + rdSe.getInLinkPid());
			sb.append("   AND S.NODE_PID = " + rdSe.getNodePid());
			sb.append("   AND S.OUT_LINK_PID <> " + rdSe.getNodePid());
			
			String sql = sb.toString();
			log.info("RdSe GLM06005 sql:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()==1){
				String target = "[RD_SE," + resultList.get(0) + "]";
				this.setCheckResult("", target, 0);
			}
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
		if(ObjStatus.UPDATE.equals(rdLink.status())){
			//分离节点之前的node点
			if(changedFields.containsKey("sNodePid")){
				nodePids.add(rdLink.getsNodePid());
			}
			if(changedFields.containsKey("eNodePid")){
				nodePids.add(rdLink.geteNodePid());
			}
		}
		//删除link
		else if (ObjStatus.DELETE.equals(rdLink.status())){
			nodePids.add(rdLink.getsNodePid());
			nodePids.add(rdLink.geteNodePid());
		}
		
		check(nodePids);
		
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private void check(Set<Integer> nodePids) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		  
		sb.append("SELECT DISTINCT SE.PID");
		sb.append("  FROM RD_SE SE");
		sb.append(" WHERE SE.NODE_PID IN (" + StringUtils.join(nodePids.toArray(),",") + ")");
		sb.append("   AND SE.U_RECORD <> 2");
		sb.append("   AND (SELECT COUNT(1)");
		sb.append("          FROM RD_SE SEE");
		sb.append("         WHERE SEE.NODE_PID = SE.NODE_PID");
		sb.append("           AND SEE.IN_LINK_PID = SE.IN_LINK_PID");
		sb.append("           AND SEE.U_RECORD <> 2) < 2");
		
		String sql = sb.toString();
		log.info("后检查GLM06005--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		Iterator rdSePidIt = resultList.iterator();
        while(rdSePidIt.hasNext()){
        	String target = "[RD_SE," + rdSePidIt.next() + "]";
			this.setCheckResult("", target, 0);
        }

	}
}
