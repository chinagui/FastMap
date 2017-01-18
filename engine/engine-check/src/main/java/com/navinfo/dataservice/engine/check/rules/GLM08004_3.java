package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM08004_3
 * @author songdongyan
 * @date 2017年1月13日
 * @Description: GLM08004_3.java
 */
public class GLM08004_3 extends baseRule {

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
			if(obj instanceof RdLinkLimit ){
				RdLinkLimit rdLinkLimit=(RdLinkLimit) obj;
				checkRdLinkLimit(rdLinkLimit);
			}
		}
		
	}
	
	/**
	 * @param rdLinkLimit
	 * @throws Exception 
	 */
	private void checkRdLinkLimit(RdLinkLimit rdLinkLimit) throws Exception {
		boolean checkFlg = false;
		//进不去出不来的link
		if(rdLinkLimit.status().equals(ObjStatus.INSERT)){
			if((rdLinkLimit.getType()==2)&&(rdLinkLimit.getVehicle()==2147484040L)&&(rdLinkLimit.getTimeDomain().isEmpty())){
				checkFlg = true;
			}
		}
		else if(rdLinkLimit.status().equals(ObjStatus.UPDATE)){
			int type;
			long vehicle;
			String timeDomain;
			if(rdLinkLimit.changedFields().containsKey("type")){
				type = Integer.parseInt(rdLinkLimit.changedFields().get("type").toString());
			}else{
				type = rdLinkLimit.getType();
			}
			
			if(rdLinkLimit.changedFields().containsKey("vehicle")){
				vehicle = Long.parseLong(rdLinkLimit.changedFields().get("vehicle").toString());
			}else{
				vehicle = rdLinkLimit.getVehicle();
			}
			
			if(rdLinkLimit.changedFields().containsKey("timeDomain")){
				if(rdLinkLimit.changedFields().get("timeDomain")!=null){
					timeDomain = rdLinkLimit.changedFields().get("timeDomain").toString();
					if(timeDomain.isEmpty()){
						timeDomain = null;
					}
				}
				else{
					timeDomain = null;
				}
			}else{
				timeDomain = null;
			}
			
			if((type==2)&&(vehicle==2147484040L)&&(timeDomain==null)){
				checkFlg = true;
			}
		}
		
		if(checkFlg){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
			sb2.append(" WHERE RR.IN_LINK_PID = " + rdLinkLimit.getLinkPid());
			sb2.append(" AND RR.U_RECORD <> 2");
			sb2.append(" AND RR.PID = RRD.RESTRIC_PID");
			sb2.append(" AND RRD.U_RECORD <> 2");
			sb2.append(" AND RRD.TYPE = 1");
			sb2.append(" UNION");
			sb2.append(" SELECT 1 FROM RD_RESTRICTION_DETAIL RRD");
			sb2.append(" WHERE RRD.OUT_LINK_PID = " + rdLinkLimit.getLinkPid());
			sb2.append(" AND RRD.U_RECORD <> 2");
			sb2.append(" AND RRD.TYPE = 1");
			
			String sql2 = sb2.toString();
			log.info("RdLinkLimit后检查GLM08004_3:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLinkLimit.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

}
