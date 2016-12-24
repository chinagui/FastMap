package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResult;

/**
 * 
 * @ClassName GLM34001
 * @author Han Shaoming
 * @date 2016年12月13日 下午2:20:42
 * @Description 
 * 等级为高速、城市高速、8级路、9级路、10级路、人渡、轮渡的道路link上制作了减速带时，报log
 * Link种别编辑服务端后检查:RdLink
 */
public class GLM34001 extends baseRule {
	protected Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//Link种别编辑
			if(row instanceof RdLink){
				RdLink rdLink = (RdLink)row;
				checkRdLink(rdLink);
			}
		}
	}
	
	/**
	 * 
	 * @author Han Shaoming
	 * @param rdLink
	 * @throws Exception
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		//Link种别编辑,触发检查
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT R.GEOMETRY,'[RD_LINK,' || R.LINK_PID || ']' TARGET,R.MESH_ID,");
		sb.append(" CASE R.KIND WHEN 1 THEN '高速道路上制作了减速带'");
		sb.append(" WHEN 2 THEN '城市高速上制作了减速带'");
		sb.append(" WHEN 8 THEN '8级路上制作了减速带'");
		sb.append(" WHEN 9 THEN '9级路上制作了减速带'");
		sb.append(" WHEN 10 THEN '10级路上制作了减速带'");
		sb.append(" WHEN 11 THEN '人渡上制作了减速带'");
		sb.append(" WHEN 13 THEN '轮渡上制作了减速带' END LOG ");
		sb.append(" FROM RD_LINK R WHERE R.LINK_PID = "+rdLink.getPid());
		sb.append(" AND R.KIND IN (1,2,8,9,10,11,13) AND R.U_RECORD <> 2 ");
		sb.append(" AND EXISTS( ");
		sb.append(" SELECT 1 FROM RD_SPEEDBUMP S WHERE R.LINK_PID=S.LINK_PID ");
		sb.append(" AND S.LINK_PID= "+rdLink.getPid());
		sb.append(" AND S.U_RECORD <> 2)");
		String sql = sb.toString();
		log.info("RdLink后检查GLM34001--sql:" + sql);
		
		DatabaseOperatorResult getObj = new DatabaseOperatorResult();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int)resultList.get(2),resultList.get(3).toString());
		}
		/*if(rdLink.getKind()==1||rdLink.getKind()==2||rdLink.getKind()==8
				||rdLink.getKind()==9||rdLink.getKind()==10
				||rdLink.getKind()==11||rdLink.getKind()==13){
		}*/
		
	}

}
