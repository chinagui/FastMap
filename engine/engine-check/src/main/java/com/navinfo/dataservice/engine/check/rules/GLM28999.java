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
 * @ClassName: GLM28999
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: CRF对象的名称来源为“翻译”时，语言代码一定为ENG，否则报log
 */
public class GLM28999 extends baseRule{

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
		String target = "[RD_OBJECT," + rdObjectName.getPid() + "]";
		//新增RdObjectName
		if(rdObjectName.status().equals(ObjStatus.INSERT)){
			//名称来源
			int srcFlag = rdObjectName.getSrcFlag();
			//语言代码
			String langCode = rdObjectName.getLangCode();
			if((srcFlag==1)&&(!langCode.equals("ENG"))){
				this.setCheckResult("", target, 0);
			}
		}
		//修改RdObjectName
		else if(rdObjectName.status().equals(ObjStatus.UPDATE)){
			//是否查库标志
			boolean checkFlag = false;
			StringBuilder sb = new StringBuilder();
			if(rdObjectName.changedFields().containsKey("srcFlag")){
				int srcFlag = Integer.parseInt(rdObjectName.changedFields().get("srcFlag").toString());
				if(srcFlag == 1){
					//同时修改语言代码、名称来源，且CRF对象的名称来源为“翻译”时，语言代码为ENG，不报log
					if(rdObjectName.changedFields().containsKey("langCode")){
						String langCode = rdObjectName.changedFields().get("langCode").toString();
						if(langCode.equals("ENG")){
							return;
						}
					}
					sb.append("SELECT 1 FROM RD_OBJECT_NAME RON");
					sb.append(" WHERE RON.LANG_CODE <> 'ENG'");
					sb.append(" AND RON.U_RECORD <> 2");
					sb.append(" AND RON.NAME_ID =" + rdObjectName.getNameId());
					checkFlag = true;
				}
			}
			if(rdObjectName.changedFields().containsKey("langCode")){
				String langCode = rdObjectName.changedFields().get("langCode").toString();
				if(!langCode.equals("ENG")){
					sb.append("SELECT 1 FROM RD_OBJECT_NAME RON");
					sb.append(" WHERE RON.SRC_FLAG =1");
					sb.append(" AND RON.U_RECORD <> 2");
					sb.append(" AND RON.NAME_ID =" + rdObjectName.getNameId());
					checkFlag = true;
				}
			}
			if(checkFlag){
				String sql = sb.toString();
				log.info("RdObjectName后检查GLM28999:" + sql);
				
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
