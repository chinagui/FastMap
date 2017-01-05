package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: PERMIT_CREATE_SAME_WARNINFO
 * @author songdongyan
 * @date 2016年12月29日
 * @Description: 不能创建相同的警示信息
 * 标牌类型编辑前检查
 */
public class PERMIT_CREATE_SAME_WARNINFO extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			//标牌类型编辑
			if(obj instanceof RdWarninginfo){
				RdWarninginfo rdWarninginfo=(RdWarninginfo) obj;
				checkRdWarningInfo(rdWarninginfo);
			}
		}
		
	}
	/**
	 * @param rdWarninginfo
	 * @throws Exception 
	 */
	private void checkRdWarningInfo(RdWarninginfo rdWarninginfo) throws Exception {
		//标牌类型编辑
		if(rdWarninginfo.changedFields().containsKey("typeCode")){
			String typeCode = rdWarninginfo.changedFields().get("typeCode").toString();

				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_WARNINGINFO W1, RD_WARNINGINFO W2");
				sb.append(" WHERE W1.LINK_PID = W2.LINK_PID");
				sb.append(" AND W1.NODE_PID = W2.NODE_PID");
				sb.append(" AND W1.U_RECORD != 2");
				sb.append(" AND W2.U_RECORD != 2");
				sb.append(" AND W2.TYPE_CODE ='" + typeCode + "'");
				sb.append(" AND W1.PID <> W2.PID");
				sb.append(" AND W1.PID =" + rdWarninginfo.getPid());

				String sql = sb.toString();
				log.info("RdWarninginfo前检查PERMIT_CREATE_SAME_WARNINFO:" + sql);

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
		// TODO Auto-generated method stub
		
	}

}
