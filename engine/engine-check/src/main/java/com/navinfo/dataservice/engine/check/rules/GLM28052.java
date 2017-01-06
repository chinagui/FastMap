package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM28052
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: LandMark的名称不能超过35个汉字，拼音不能超过206个字符
 */
public class GLM28052 extends baseRule{

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
		for(IRow obj : checkCommand.getGlmList()){
			//RdObjectName新增修改都会触发
			if (obj instanceof RdObjectName){
				RdObjectName rdObjectName = (RdObjectName)obj;
				checkRdObjectName(rdObjectName);
			}
		}
		
	}

	/**
	 * @param rdObjectName
	 * @throws Exception 
	 */
	private void checkRdObjectName(RdObjectName rdObjectName) throws Exception {
		String target = "[RD_ROAD," + rdObjectName.getPid() + "]";
		//新增RdObjectName
		if(rdObjectName.status().equals(ObjStatus.INSERT)){
			//语言代码
			String langCode = rdObjectName.getLangCode();
			if(langCode.equals("CHI")){
				if(rdObjectName.getName().length()>35||rdObjectName.getPhonetic().length()>206){
					this.setCheckResult("", target, 0);
				}
			}
		}
		//修改RdObjectName
		else if(rdObjectName.status().equals(ObjStatus.UPDATE)){
			if(rdObjectName.changedFields().containsKey("langCode")||rdObjectName.changedFields().containsKey("name")||rdObjectName.changedFields().containsKey("phonetic")){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_OBJECT_NAME N");
				sb.append(" WHERE (LENGTH(N.NAME) > 35 OR LENGTH(N.PHONETIC) > 206)");
				sb.append(" AND N.LANG_CODE = 'CHI'");
				sb.append(" AND N.U_RECORD <> 2");
				sb.append(" AND N.NAME_ID = " + rdObjectName.getNameId());

				String sql = sb.toString();
				log.info("RdObjectName后检查GLM28052:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(resultList.size()>0){
					this.setCheckResult("", target, 0);
				}
			}
		}
		
	}

}
