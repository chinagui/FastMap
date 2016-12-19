package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM08004
 * @author songdongyan
 * @date 上午10:28:16
 * @Description: 步行街/公交车专用道不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线
 * 修改交限服务端前检查：RdRestriction，RdRestrictionDetail
 */
public class GLM08004 extends baseRule {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			//
			if(obj instanceof RdRestriction ){
				RdRestriction rdRestriction = (RdRestriction) obj;
				checkRdRestriction(rdRestriction,checkCommand.getOperType());
			}
			else if(obj instanceof RdRestrictionDetail){
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail) obj;
				checkRdRestrictionDetail(rdRestrictionDetail,checkCommand.getOperType());
			}
		}
		
	}

	/**
	 * @param rdRestrictionDetail
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail, OperType operType) throws Exception {
		String sql = "select form_of_way from rd_link_form "
				+ "where link_pid = " + rdRestrictionDetail.getOutLinkPid()
				+ " and form_of_way in (20,22) AND U_RECORD != 2 ";
		log.info("RdGate后检查GLM04008_1:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			if(resultList.get(0).equals("20")){
				setRuleLog("步行街不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线");
				this.setCheckResult("", "", 0);
			}
			else if(resultList.get(0).equals("22")){
				setRuleLog("公交车专用道不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线");
				this.setCheckResult("", "", 0);
				return;
			}
		}	
		
	}

	/**
	 * @param rdRestriction
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdRestriction(RdRestriction rdRestriction, OperType operType) throws Exception {
		//获取inLinkPid/outLinkPid
		List<Integer> linkPids = new ArrayList<Integer>();

		linkPids.add(rdRestriction.getInLinkPid());
						
		for(IRow deObj:rdRestriction.getDetails()){
			if(deObj instanceof RdRestrictionDetail){
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)deObj;
				linkPids.add(rdRestrictionDetail.getOutLinkPid());
			}
		}
		
		
		String sql = "select form_of_way from rd_link_form "
				+ "where link_pid in ("+StringUtils.join(linkPids,",")+") "
				+ "and form_of_way in (20,22) AND U_RECORD != 2 ";
		log.info("RdGate后检查GLM04008_1:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			if(resultList.get(0).equals("20")){
				setRuleLog("步行街不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线");
				this.setCheckResult("", "", 0);
			}
			else if(resultList.get(0).equals("22")){
				setRuleLog("公交车专用道不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线");
				this.setCheckResult("", "", 0);
				return;
			}
		}	
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		

	}

}
