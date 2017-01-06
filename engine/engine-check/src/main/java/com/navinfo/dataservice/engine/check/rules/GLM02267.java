package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectName;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM02267
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: 
 * 如果选择的CRFO中，不存在下列任何一个条件，则Landmark不允许有名称
 * 1、名称类型为“立交桥名（连接路）”且属性为“匝道”
 * 2、名称类型为“立交桥名（主路）”的link
 * 道路名称类型编辑后检查：新增RdLinkName，修改RdLinkName
 * CRFO名称编辑:新增RdObjectName
 */
public class GLM02267 extends baseRule{

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
			//RdObjectName新增会触发
			if (obj instanceof RdObjectName){
				RdObjectName rdObjectName = (RdObjectName)obj;
				checkRdObjectName(rdObjectName);
			}
			//RdLinkName新增修改会触发
			else if (obj instanceof RdLinkName){
				RdLinkName rdLinkName = (RdLinkName)obj;
				checkRdLinkName(rdLinkName);
			}
			//RdLinkForm新增修改会触发
			else if (obj instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm)obj;
				checkRdLinkForm(rdLinkForm);
			}
		}
		
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean checkFlg = false;
		//新增RdLinkForm
		if(rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			//非匝道触发检查
			if(formOfWay!=15){
				checkFlg = true;
			}
		}
		//修改RdLinkName类型
		else if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
				if(formOfWay!=15){
					checkFlg = true;
				}
			}
		}
		
		if(checkFlg){
			
			StringBuilder sb = new StringBuilder();
			
			String innerSql = "(SELECT ROI1.PID FROM RD_OBJECT_INTER ROI1, RD_INTER_LINK RIL1,RD_OBJECT_NAME RON1"
					+ " WHERE RIL1.LINK_PID = " + rdLinkForm.getLinkPid()
					+ " AND RIL1.PID = ROI1.INTER_PID"
					+ " AND RON1.PID = ROI1.PID"
					+ " AND ROI1.U_RECORD <> 2"
					+ " AND RIL1.U_RECORD <> 2"
					+ " AND RON1.U_RECORD <> 2"
					+ " UNION "
					+ "SELECT ROR1.PID FROM RD_OBJECT_ROAD ROR1, RD_ROAD_LINK RRL1,RD_OBJECT_NAME RON1"
					+ " WHERE RRL1.LINK_PID = " + rdLinkForm.getLinkPid()
					+ " AND RRL1.PID = ROR1.ROAD_PID"
					+ " AND RON1.PID = ROR1.PID"
					+ " AND ROR1.U_RECORD <> 2"
					+ " AND RRL1.U_RECORD <> 2"
					+ " AND RON1.U_RECORD <> 2)";
			
			sb.append("SELECT COUNT(1) FROM (");
			sb.append("SELECT N.LINK_PID FROM RD_LINK_FORM F, RD_LINK_NAME N");
			sb.append(" WHERE N.NAME_TYPE = 1");
			sb.append(" AND F.FORM_OF_WAY = 15");
			sb.append(" AND N.LINK_PID = F.LINK_PID");
			sb.append(" AND N.U_RECORD <> 2");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND N.LINK_PID IN (SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI, RD_INTER_LINK RIL");
			sb.append(" WHERE ROI.INTER_PID = RIL.PID");
			sb.append(" AND ROI.U_RECORD <> 2");
			sb.append(" AND RIL.U_RECORD <> 2");
			sb.append(" AND ROI.PID = " + innerSql);
			sb.append(" UNION");
			sb.append(" SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR, RD_ROAD_LINK RRL");
			sb.append(" WHERE ROR.ROAD_PID = RRL.PID");
			sb.append(" AND ROR.U_RECORD <> 2");
			sb.append(" AND RRL.U_RECORD <> 2");
			sb.append(" AND ROR.PID = "  + innerSql + ")");
			sb.append(" UNION");
			sb.append(" SELECT N.LINK_PID FROM RD_LINK_NAME N");
			sb.append(" WHERE N.NAME_TYPE = 2");
			sb.append(" AND N.U_RECORD <> 2");
			sb.append(" AND N.LINK_PID IN (SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI, RD_INTER_LINK RIL");
			sb.append(" WHERE ROI.INTER_PID = RIL.PID");
			sb.append(" AND ROI.U_RECORD <> 2");
			sb.append(" AND RIL.U_RECORD <> 2");
			sb.append(" AND ROI.PID = " + innerSql);
			sb.append(" UNION");
			sb.append(" SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR, RD_ROAD_LINK RRL");
			sb.append(" WHERE ROR.ROAD_PID = RRL.PID");
			sb.append(" AND ROR.U_RECORD <> 2");
			sb.append(" AND RRL.U_RECORD <> 2");
			sb.append(" AND ROR.PID = "  + innerSql + ")");
			sb.append(")");
			
			String sql = sb.toString();
			log.info("RdObjectName后检查GLM02267:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(Integer.parseInt(resultList.get(0).toString())==0){
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
		
	}

	/**
	 * @param rdLinkName
	 * @throws Exception 
	 */
	private void checkRdLinkName(RdLinkName rdLinkName) throws Exception {
		//新增RdLinkName
		boolean checkFlg = false;
		
		if(rdLinkName.status().equals(ObjStatus.INSERT)){
			int nameType = rdLinkName.getNameType();
			if(nameType!=1&&nameType!=2){
				checkFlg = true;
			}
		}
		//修改RdLinkName类型
		else if(rdLinkName.status().equals(ObjStatus.UPDATE)){
			if(rdLinkName.changedFields().containsKey("nameType")){
				int nameType = Integer.parseInt(rdLinkName.changedFields().get("nameType").toString());
				if(nameType!=1&&nameType!=2){
					checkFlg = true;
				}
			}
		}
		if(checkFlg){
			
			StringBuilder sb = new StringBuilder();
			
			String innerSql = "(SELECT ROI1.PID FROM RD_OBJECT_INTER ROI1, RD_INTER_LINK RIL1,RD_OBJECT_NAME RON1"
					+ " WHERE RIL1.LINK_PID = " + rdLinkName.getLinkPid()
					+ " AND RIL1.PID = ROI1.INTER_PID"
					+ " AND RON1.PID = ROI1.PID"
					+ " AND ROI1.U_RECORD <> 2"
					+ " AND RIL1.U_RECORD <> 2"
					+ " AND RON1.U_RECORD <> 2"
					+ " UNION "
					+ "SELECT ROR1.PID FROM RD_OBJECT_ROAD ROR1, RD_ROAD_LINK RRL1,RD_OBJECT_NAME RON1"
					+ " WHERE RRL1.LINK_PID = " + rdLinkName.getLinkPid()
					+ " AND RRL1.PID = ROR1.ROAD_PID"
					+ " AND RON1.PID = ROR1.PID"
					+ " AND ROR1.U_RECORD <> 2"
					+ " AND RRL1.U_RECORD <> 2"
					+ " AND RON1.U_RECORD <> 2)";
			
			sb.append("SELECT COUNT(1) FROM (");
			sb.append("SELECT N.LINK_PID FROM RD_LINK_FORM F, RD_LINK_NAME N");
			sb.append(" WHERE N.NAME_TYPE = 1");
			sb.append(" AND F.FORM_OF_WAY = 15");
			sb.append(" AND N.LINK_PID = F.LINK_PID");
			sb.append(" AND N.U_RECORD <> 2");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND N.LINK_PID IN (SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI, RD_INTER_LINK RIL");
			sb.append(" WHERE ROI.INTER_PID = RIL.PID");
			sb.append(" AND ROI.U_RECORD <> 2");
			sb.append(" AND RIL.U_RECORD <> 2");
			sb.append(" AND ROI.PID = " + innerSql);
			sb.append(" UNION");
			sb.append(" SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR, RD_ROAD_LINK RRL");
			sb.append(" WHERE ROR.ROAD_PID = RRL.PID");
			sb.append(" AND ROR.U_RECORD <> 2");
			sb.append(" AND RRL.U_RECORD <> 2");
			sb.append(" AND ROR.PID = "  + innerSql + ")");
			sb.append(" UNION");
			sb.append(" SELECT N.LINK_PID FROM RD_LINK_NAME N");
			sb.append(" WHERE N.NAME_TYPE = 2");
			sb.append(" AND N.U_RECORD <> 2");
			sb.append(" AND N.LINK_PID IN (SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI, RD_INTER_LINK RIL");
			sb.append(" WHERE ROI.INTER_PID = RIL.PID");
			sb.append(" AND ROI.U_RECORD <> 2");
			sb.append(" AND RIL.U_RECORD <> 2");
			sb.append(" AND ROI.PID = " + innerSql);
			sb.append(" UNION");
			sb.append(" SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR, RD_ROAD_LINK RRL");
			sb.append(" WHERE ROR.ROAD_PID = RRL.PID");
			sb.append(" AND ROR.U_RECORD <> 2");
			sb.append(" AND RRL.U_RECORD <> 2");
			sb.append(" AND ROR.PID = "  + innerSql + ")");
			sb.append(")");
			
			String sql = sb.toString();
			log.info("RdObjectName后检查GLM02267:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(Integer.parseInt(resultList.get(0).toString())==0){
				String target = "[RD_LINK," + rdLinkName.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

	/**
	 * @param rdObjectName
	 * @throws Exception 
	 */
	private void checkRdObjectName(RdObjectName rdObjectName) throws Exception {
		//新增RdObjectName
		if(rdObjectName.status().equals(ObjStatus.INSERT)){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT COUNT(1) FROM (");
			sb.append("SELECT N.LINK_PID FROM RD_LINK_FORM F, RD_LINK_NAME N");
			sb.append(" WHERE N.NAME_TYPE = 1");
			sb.append(" AND F.FORM_OF_WAY = 15");
			sb.append(" AND N.LINK_PID = F.LINK_PID");
			sb.append(" AND N.U_RECORD <> 2");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND N.LINK_PID IN (SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI, RD_INTER_LINK RIL");
			sb.append(" WHERE ROI.INTER_PID = RIL.PID");
			sb.append(" AND ROI.U_RECORD <> 2");
			sb.append(" AND RIL.U_RECORD <> 2");
			sb.append(" AND ROI.PID = " + rdObjectName.getPid());
			sb.append(" UNION");
			sb.append(" SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR, RD_ROAD_LINK RRL");
			sb.append(" WHERE ROR.ROAD_PID = RRL.PID");
			sb.append(" AND ROR.U_RECORD <> 2");
			sb.append(" AND RRL.U_RECORD <> 2");
			sb.append(" AND ROR.PID = "  + rdObjectName.getPid() + ")");
			sb.append(" UNION");
			sb.append(" SELECT N.LINK_PID FROM RD_LINK_NAME N");
			sb.append(" WHERE N.NAME_TYPE = 2");
			sb.append(" AND N.U_RECORD <> 2");
			sb.append(" AND N.LINK_PID IN (SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI, RD_INTER_LINK RIL");
			sb.append(" WHERE ROI.INTER_PID = RIL.PID");
			sb.append(" AND ROI.U_RECORD <> 2");
			sb.append(" AND RIL.U_RECORD <> 2");
			sb.append(" AND ROI.PID = " + rdObjectName.getPid());
			sb.append(" UNION");
			sb.append(" SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR, RD_ROAD_LINK RRL");
			sb.append(" WHERE ROR.ROAD_PID = RRL.PID");
			sb.append(" AND ROR.U_RECORD <> 2");
			sb.append(" AND RRL.U_RECORD <> 2");
			sb.append(" AND ROR.PID = "  + rdObjectName.getPid() + ")");
			sb.append(")");

			String sql = sb.toString();
			log.info("RdObjectName后检查GLM02267:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(Integer.parseInt(resultList.get(0).toString())==0){
				String target = "[RD_OBJECT," + rdObjectName.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

}
