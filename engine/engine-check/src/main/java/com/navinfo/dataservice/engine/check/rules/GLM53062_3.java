package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM53062_3
 * @author songdongyan
 * @date 2017年1月10日
 * @Description: 公交专用道上不能有RTIC信息，否则报log
 * 道路形态编辑
 */
public class GLM53062_3 extends baseRule{

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
		for(IRow obj:checkCommand.getGlmList()){
			//道路种别编辑
			if(obj instanceof RdLinkForm ){
				RdLinkForm rdLinkForm=(RdLinkForm) obj;
				checkRdLinkForm(rdLinkForm);
			}
		}
		
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean checkFlag = false;
		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
				if((formOfWay == 22)){
					checkFlag = true;
				}
			}
		}else if (rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if((formOfWay == 22)){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_LINK_INT_RTIC R");
			sb2.append(" WHERE R.U_RECORD <> 2");
			sb2.append(" AND R.LINK_PID = " + rdLinkForm.getLinkPid());

			String sql2 = sb2.toString();
			log.info("RdLinkForm后检查GLM53062_3:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}
}

