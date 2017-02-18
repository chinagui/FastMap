package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectName;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM02266
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: Landmark必须有名称，并且名称组只能为1组。
 * 如果选择的CRFO中，满足以下条件之一，则Landmark必须有名称，并且名称组只能为1组。
 * 1、存在名称类型为“立交桥名（连接路）”并且属性为“匝道”；
 * 2、名称类型为“立交桥名（主路）”的link。
 */
public class GLM02266 extends baseRule{

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
			//RdObjectName删除会触发
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
			//匝道触发检查
			if(formOfWay==15){
				checkFlg = true;
			}
		}
		//修改RdLinkName类型
		else if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				if(rdLinkForm.getFormOfWay()!=15){
					int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
					if(formOfWay==15){
						checkFlg = true;
					}
				}
			}
		}
		
		if(checkFlg){
			
			checkRdLink(rdLinkForm.getLinkPid());
		}
		
	}

	/**
	 * @param rdLinkName
	 * @throws Exception 
	 */
	private void checkRdLinkName(RdLinkName rdLinkName) throws Exception {
		boolean checkFlg = false;
		//新增RdLinkName
		if(rdLinkName.status().equals(ObjStatus.INSERT)){
			int nameType = rdLinkName.getNameType();
			if(nameType==1||nameType==2){
				checkFlg = true;
			}
		}
		//修改RdLinkName类型
		else if(rdLinkName.status().equals(ObjStatus.UPDATE)){
			if(rdLinkName.changedFields().containsKey("nameType")){
				if(rdLinkName.getNameType()!=1&&rdLinkName.getNameType()!=2){
					int nameType = Integer.parseInt(rdLinkName.changedFields().get("nameType").toString());
					if(nameType==1||nameType==2){
						checkFlg = true;
					}
				}
			}
		}
		if(checkFlg){
			checkRdLink(rdLinkName.getLinkPid());
		}
		
	}

	/**
	 * @param linkPid
	 * @throws Exception 
	 */
	private void checkRdLink(int linkPid) throws Exception {
		//rdLink涉及到哪些RdObject
		List<Integer> rdObjectPidList = getRdObjectPidList(linkPid);
		for(Integer rdObjectPid:rdObjectPidList){
			//CRFO是否存在满足条件的link
			if(isRdObjectShouldBeNamed(rdObjectPid)){
				//CRFO是否只存在一组名称
				if(getRdObjectNameNum(rdObjectPid)!=1){
					String target = "[RD_LINK," + linkPid + "]";
					this.setCheckResult("", target, 0);
					return;
				}
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
			String sql = "SELECT ROI1.PID FROM RD_OBJECT_INTER ROI1, RD_INTER_LINK RIL1"
					+ " WHERE RIL1.LINK_PID = " + linkPid
					+ " AND RIL1.PID = ROI1.INTER_PID"
					+ " AND ROI1.U_RECORD <> 2"
					+ " AND RIL1.U_RECORD <> 2"
					+ " UNION "
					+ "SELECT ROR1.PID FROM RD_OBJECT_ROAD ROR1, RD_ROAD_LINK RRL1"
					+ " WHERE RRL1.LINK_PID = " + linkPid
					+ " AND RRL1.PID = ROR1.ROAD_PID"
					+ " AND ROR1.U_RECORD <> 2"
					+ " AND RRL1.U_RECORD <> 2"
					+ " UNION "
					+ "SELECT ROL1.PID FROM RD_OBJECT_LINK ROL1"
					+ " WHERE ROL1.LINK_PID = " + linkPid
					+ " AND ROL1.U_RECORD <> 2";
			
			log.info("RdObject后检查GLM02266:" + sql);
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
	 * @param rdObjectName
	 * @throws Exception 
	 */
	private void checkRdObjectName(RdObjectName rdObjectName) throws Exception {
		boolean checkFlg = false;
		//删除RdObjectName
		if(rdObjectName.status().equals(ObjStatus.DELETE)){
			checkFlg = true;
		}
		//新增CRFO名称
		if(rdObjectName.status().equals(ObjStatus.INSERT)){
			checkFlg = true;
		}
		if(checkFlg){
			//CRFO是否存在满足条件的link
			boolean flg = isRdObjectShouldBeNamed(rdObjectName.getPid());
			if(flg){
				//CRFO是否只存在一组名称
				if(getRdObjectNameNum(rdObjectName.getPid())!=1){
					String target = "[RD_OBJECT," + rdObjectName.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
	}

	/**
	 * @param pid
	 * @throws Exception 
	 * CRFO是否只存在一组名称
	 */
	private int getRdObjectNameNum(int pid) throws Exception {
		int num = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(1) NUM FROM RD_OBJECT_NAME RON WHERE RON.U_RECORD <> 2  AND RON.PID = " + pid +" GROUP BY RON.NAME_GROUPID");
		
		String sql = sb.toString();
		log.info("RdObject后检查GLM02266:" + sql);
		
		PreparedStatement pstmt = this.getConn().prepareStatement(sb.toString());	
		ResultSet resultSet = pstmt.executeQuery();
		
		if (resultSet.next()){
			num = resultSet.getInt("NUM");
		}
		resultSet.close();
		pstmt.close();

		return num;
		
	}

	/**
	 * @param pid
	 * @return
	 * @throws SQLException 
	 * CRFO是否存在满足条件的link
	 */
	private boolean isRdObjectShouldBeNamed(int pid) throws SQLException {
		boolean flg = false;
		//CRFO是否存在满足条件的link
		StringBuilder sb = new StringBuilder();
		
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
		
		log.info("RdObject后检查GLM02266:" + sb.toString());
		PreparedStatement pstmt = this.getConn().prepareStatement(sb.toString());	
		ResultSet resultSet = pstmt.executeQuery();

		
		while (resultSet.next()){
			flg = true;
		} 
		resultSet.close();
		pstmt.close();
		return flg;
	}

}
