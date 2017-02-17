package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * @param linkPid
	 * @return
	 * @throws SQLException 
	 */
	private List<Integer> getRdObjectPidList(int linkPid) throws SQLException {
			//该link所在的有landmark的crfo
			String sql = "SELECT ROI1.PID FROM RD_OBJECT_INTER ROI1, RD_INTER_LINK RIL1,RD_OBJECT_NAME RON1"
					+ " WHERE RIL1.LINK_PID = " + linkPid
					+ " AND RIL1.PID = ROI1.INTER_PID"
					+ " AND RON1.PID = ROI1.PID"
					+ " AND ROI1.U_RECORD <> 2"
					+ " AND RIL1.U_RECORD <> 2"
					+ " AND RON1.U_RECORD <> 2"
					+ " UNION "
					+ "SELECT ROR1.PID FROM RD_OBJECT_ROAD ROR1, RD_ROAD_LINK RRL1,RD_OBJECT_NAME RON1"
					+ " WHERE RRL1.LINK_PID = " + linkPid
					+ " AND RRL1.PID = ROR1.ROAD_PID"
					+ " AND RON1.PID = ROR1.PID"
					+ " AND ROR1.U_RECORD <> 2"
					+ " AND RRL1.U_RECORD <> 2"
					+ " AND RON1.U_RECORD <> 2"
					+ " UNION "
					+ "SELECT ROL1.PID FROM RD_OBJECT_LINK ROL1,RD_OBJECT_NAME RON1"
					+ " WHERE ROL1.LINK_PID = " + linkPid
					+ " AND RON1.PID = ROL1.PID"
					+ " AND ROL1.U_RECORD <> 2"
					+ " AND RON1.U_RECORD <> 2";
			
			PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
			ResultSet resultSet = pstmt.executeQuery();
			List<Integer> rdObjectPidList=new ArrayList<Integer>();

			while (resultSet.next()){
				rdObjectPidList.add(resultSet.getInt("PID"));
			} 
			resultSet.close();
			pstmt.close();
			return rdObjectPidList;			
	}

	/**
	 * @param pid
	 * @throws Exception 
	 */
	private boolean checkRdObject(int pid) throws Exception {
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
		sb.append(" AND ROI.PID = " + pid);
		sb.append(" UNION");
		sb.append(" SELECT ROL.LINK_PID FROM RD_OBJECT_LINK ROL");
		sb.append(" WHERE ROL.U_RECORD <> 2");
		sb.append(" AND ROL.PID = " + pid);
		sb.append(" UNION");
		sb.append(" SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR, RD_ROAD_LINK RRL");
		sb.append(" WHERE ROR.ROAD_PID = RRL.PID");
		sb.append(" AND ROR.U_RECORD <> 2");
		sb.append(" AND RRL.U_RECORD <> 2");
		sb.append(" AND ROR.PID = "  + pid + ")");
		sb.append(" UNION");
		sb.append(" SELECT N.LINK_PID FROM RD_LINK_NAME N");
		sb.append(" WHERE N.NAME_TYPE = 2");
		sb.append(" AND N.U_RECORD <> 2");
		sb.append(" AND N.LINK_PID IN (SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI, RD_INTER_LINK RIL");
		sb.append(" WHERE ROI.INTER_PID = RIL.PID");
		sb.append(" AND ROI.U_RECORD <> 2");
		sb.append(" AND RIL.U_RECORD <> 2");
		sb.append(" AND ROI.PID = " + pid);
		sb.append(" UNION");
		sb.append(" SELECT ROL.LINK_PID FROM RD_OBJECT_LINK ROL");
		sb.append(" WHERE ROL.U_RECORD <> 2");
		sb.append(" AND ROL.PID = " + pid);
		sb.append(" UNION");
		sb.append(" SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR, RD_ROAD_LINK RRL");
		sb.append(" WHERE ROR.ROAD_PID = RRL.PID");
		sb.append(" AND ROR.U_RECORD <> 2");
		sb.append(" AND RRL.U_RECORD <> 2");
		sb.append(" AND ROR.PID = "  + pid + ")");
		sb.append(")");

		String sql = sb.toString();
		log.info("RdObject后检查GLM02267:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		//不存在名称类型为“1 立交桥名(连接路)”且属性为“15 匝道”或者名称类型为“2 立交桥名(主路)”的link
		if(Integer.parseInt(resultList.get(0).toString())==0){
			return true;
		}
		
		return false;
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean checkFlg = false;
		//删除RdLinkForm
		if(rdLinkForm.status().equals(ObjStatus.DELETE)){
			int formOfWay = rdLinkForm.getFormOfWay();
			//非匝道触发检查
			if(formOfWay==15){
				checkFlg = true;
			}
		}
		//修改RdLinkForm类型
		else if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				if(rdLinkForm.getFormOfWay()==15){
					int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
					if(formOfWay!=15){
						checkFlg = true;
					}
				}
			}
		}
		
		
		if(checkFlg){
			//所有涉及到的有名字的RdObject
			List<Integer> rdObjectPidList = getRdObjectPidList(rdLinkForm.getLinkPid());
			for(Integer rdObjectPid:rdObjectPidList){
				if(checkRdObject(rdObjectPid)){
					String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
					this.setCheckResult("", target, 0);
					return;
				}
			}
		}
	}

	/**
	 * @param linkPid
	 * @throws Exception 
	 */
	private void checkRdLink(int linkPid) throws Exception {
		StringBuilder sb = new StringBuilder();
		//该link所在的有landmark的crfo
		String innerSql = "(SELECT ROI1.PID FROM RD_OBJECT_INTER ROI1, RD_INTER_LINK RIL1,RD_OBJECT_NAME RON1"
				+ " WHERE RIL1.LINK_PID = " + linkPid
				+ " AND RIL1.PID = ROI1.INTER_PID"
				+ " AND RON1.PID = ROI1.PID"
				+ " AND ROI1.U_RECORD <> 2"
				+ " AND RIL1.U_RECORD <> 2"
				+ " AND RON1.U_RECORD <> 2"
				+ " UNION "
				+ "SELECT ROR1.PID FROM RD_OBJECT_ROAD ROR1, RD_ROAD_LINK RRL1,RD_OBJECT_NAME RON1"
				+ " WHERE RRL1.LINK_PID = " + linkPid
				+ " AND RRL1.PID = ROR1.ROAD_PID"
				+ " AND RON1.PID = ROR1.PID"
				+ " AND ROR1.U_RECORD <> 2"
				+ " AND RRL1.U_RECORD <> 2"
				+ " AND RON1.U_RECORD <> 2"
				+ " UNION "
				+ "SELECT ROL1.PID FROM RD_OBJECT_LINK ROL1,RD_OBJECT_NAME RON1"
				+ " WHERE ROL1.LINK_PID = " + linkPid
				+ " AND RON1.PID = ROL1.PID"
				+ " AND ROL1.U_RECORD <> 2"
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
		sb.append(" SELECT ROL.LINK_PID FROM RD_OBJECT_LINK ROL");
		sb.append(" WHERE ROL.U_RECORD <> 2");
		sb.append(" AND ROL.PID = " + innerSql);
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
		sb.append(" SELECT ROL.LINK_PID FROM RD_OBJECT_LINK ROL");
		sb.append(" WHERE ROL.U_RECORD <> 2");
		sb.append(" AND ROL.PID = " + innerSql);
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
			String target = "[RD_LINK," + linkPid + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

	/**
	 * @param rdLinkName
	 * @throws Exception 
	 */
	private void checkRdLinkName(RdLinkName rdLinkName) throws Exception {
		
		boolean checkFlg = false;
//		//新增RdLinkName
//		if(rdLinkName.status().equals(ObjStatus.INSERT)){
//			int nameType = rdLinkName.getNameType();
//			if(nameType!=1||nameType!=2){
//				checkFlg = true;
//			}
//		}
		//修改RdLinkName类型
		if(rdLinkName.status().equals(ObjStatus.UPDATE)){
			if(rdLinkName.changedFields().containsKey("nameType")){
				int nameType = Integer.parseInt(rdLinkName.changedFields().get("nameType").toString());
				if(nameType!=1||nameType!=2){
					checkFlg = true;
				}
			}
		}
		//删除RdLinkName
		if(rdLinkName.status().equals(ObjStatus.DELETE)){
			int nameType = rdLinkName.getNameType();
			if(nameType!=1||nameType!=2){
				checkFlg = true;
			}
		}
		
		if(checkFlg){
			//获取所涉及到的有名字的RdObjectPid
			List<Integer> rdObjectPidList = getRdObjectPidList(rdLinkName.getLinkPid());
			for(Integer rdObjectPid:rdObjectPidList){
				if(checkRdObject(rdObjectPid)){
					String target = "[RD_LINK," + rdLinkName.getLinkPid() + "]";
					this.setCheckResult("", target, 0);
					return;
				}
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
			if(checkRdObject(rdObjectName.getPid())){
				String target = "[RD_OBJECT," + rdObjectName.getPid() + "]";
				this.setCheckResult("", target, 0);
				return;
			}
		}
	}

}
