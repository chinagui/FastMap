package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName: RELATING_CHECK_NOSAME_LINE_LINE_RELATION
 * @author songdongyan
 * @date 2016年8月18日
 * @Description: 相同的进入线，进入点，经过线，退出线，不能创建两组相同类型分歧
 *               相同的进入线、进入点不能创建两组（车信、普通交限、顺行、分歧、语音引导、收费站、大门、自然语音引导）
 *               相同的进入线、进入点、退出线不能创建两组分叉口提示
 */
public class RELATING_CHECK_NOSAME_LINE_LINE_RELATION extends baseRule {

	/**
	 * 
	 */
	public RELATING_CHECK_NOSAME_LINE_LINE_RELATION() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.
	 * dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 分歧RdBranch
			if (obj instanceof RdBranch) {
				RdBranch rdBranch = (RdBranch) obj;
				boolean result = checkRdBranch(rdBranch);
				if (!result) {
					this.setCheckResult("", "", 0, "相同的进入线，进入点，经过线，退出线，不能创建两组相同类型分歧");
					return;
				}
			} else if (obj instanceof RdBranchDetail) {
				RdBranchDetail rdBranchDetail = (RdBranchDetail) obj;
				boolean result = checkRdBranchDetail(rdBranchDetail);
				if (!result) {
					this.setCheckResult("", "", 0, "相同的进入线，进入点，经过线，退出线，不能创建两组相同类型分歧");
					return;
				}
			}
			// 大门RdGate
			else if (obj instanceof RdGate) {
				RdGate rdGate = (RdGate) obj;
				boolean result = checkRdGate(rdGate);
				if (!result) {
					this.setCheckResult("", "", 0, "相同的进入线、进入点不能创建两组大门");
					return;
				}
			}
			// 交限RdRestriction
			else if (obj instanceof RdRestriction) {
				RdRestriction rdRestriction = (RdRestriction) obj;
				boolean result = checkRdRestriction(rdRestriction,checkCommand.getOperType());
				if (!result) {
					this.setCheckResult("", "", 0, "相同的进入线、进入点不能创建两组普通交限");
					return;
				}
			}
			// 修改交限退出线
			else if (obj instanceof RdRestrictionDetail) {
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail) obj;
				boolean result = checkRdRestriction(rdRestrictionDetail);
				if (!result) {
					this.setCheckResult("", "", 0, "相同进入线、进入点、退出线不能创建两组普通交限");
					return;
				}
			}
			// 顺行RdDirectroute
			else if (obj instanceof RdDirectroute) {
				RdDirectroute rdDirectroute = (RdDirectroute) obj;
				boolean result = checkRdDirectroute(rdDirectroute);
				if (!result) {
					this.setCheckResult("", "", 0, "相同的进入线、进入点不能创建两组顺行");
					return;
				}
			}
			// 分岔口提示RdSe
			else if (obj instanceof RdSe) {
				RdSe rdSe = (RdSe) obj;
				boolean result = checkRdSe(rdSe);
				if (!result) {
					this.setCheckResult("", "", 0, "相同的进入线、进入点、退出线不能创建两组分叉口提示");
					return;
				}
			}
			// 收费站RdTollgate
			else if (obj instanceof RdTollgate) {
				RdTollgate rdTollgate = (RdTollgate) obj;
				boolean result = checkRdTollgate(rdTollgate);
				if (!result) {
					this.setCheckResult("", "", 0, "相同的进入线、进入点不能创建两组收费站");
					return;
				}
			}
			// 语音引导RdVoiceguide
			else if (obj instanceof RdVoiceguide) {
				RdVoiceguide rdVoiceguide = (RdVoiceguide) obj;
				boolean result = checkRdVoiceguide(rdVoiceguide);
				if (!result) {
					this.setCheckResult("", "", 0, "相同的进入线、进入点不能创建两组语音引导");
					return;
				}
			}
			// 车信RdLaneConnexity
			else if (obj instanceof RdLaneConnexity) {
				RdLaneConnexity rdLaneConnexity = (RdLaneConnexity) obj;
				boolean result = checkRdLaneConnexity(rdLaneConnexity);
				if (!result) {
					this.setCheckResult("", "", 0, "相同的进入线、进入点不能创建两组车信");
					return;
				}
			}
		}

	}

	/**
	 * @param rdRestrictionDetail
	 * @return
	 * @throws Exception
	 */
	private boolean checkRdRestriction(RdRestrictionDetail rdRestrictionDetail) throws Exception {
		if(rdRestrictionDetail.changedFields().containsKey("outLinkPid"))
		{
			int restrictPid = rdRestrictionDetail.getRestricPid();

			int outLinkPid = (int) rdRestrictionDetail.changedFields().get("outLinkPid");

			StringBuilder sb = new StringBuilder();

			sb.append("select detail_id from RD_RESTRICTION_detail where U_RECORD !=2 ");
			sb.append(" and RESTRIC_PID = ");
			sb.append(restrictPid);
			sb.append(" and OUT_LINK_PID = ");
			sb.append(outLinkPid);

			String sql = sb.toString();

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (resultList.size() > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param rdLaneConnexity
	 * @return
	 * @throws Exception
	 */
	private boolean checkRdLaneConnexity(RdLaneConnexity rdLaneConnexity) throws Exception {
		// TODO Auto-generated method stub
		int inLinkPid = rdLaneConnexity.getInLinkPid();
		int nodePid = rdLaneConnexity.getNodePid();

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT RLC.PID FROM RD_LANE_CONNEXITY RLC WHERE RLC.u_record !=2");
		sb.append(" AND RLC.IN_LINK_PID = ");
		sb.append(inLinkPid);
		sb.append(" AND RLC.NODE_PID = ");
		sb.append(nodePid);

		String sql = sb.toString();

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * @param rdVoiceguide
	 * @return
	 * @throws Exception
	 */
	private boolean checkRdVoiceguide(RdVoiceguide rdVoiceguide) throws Exception {
		// TODO Auto-generated method stub
		int inLinkPid = rdVoiceguide.getInLinkPid();
		int nodePid = rdVoiceguide.getNodePid();

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT RV.PID FROM RD_VOICEGUIDE RV WHERE RV.u_record !=2");
		sb.append(" AND RV.IN_LINK_PID = ");
		sb.append(inLinkPid);
		sb.append(" AND RV.NODE_PID = ");
		sb.append(nodePid);

		String sql = sb.toString();

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * @param rdTollgate
	 * @return
	 * @throws Exception
	 */
	private boolean checkRdTollgate(RdTollgate rdTollgate) throws Exception {
		// TODO Auto-generated method stub
		int inLinkPid = rdTollgate.getInLinkPid();
		int nodePid = rdTollgate.getNodePid();

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT RT.PID FROM RD_TOLLGATE RT WHERE RT.u_record !=2");
		sb.append(" AND RT.IN_LINK_PID = ");
		sb.append(inLinkPid);
		sb.append(" AND RT.NODE_PID = ");
		sb.append(nodePid);

		String sql = sb.toString();

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * @param rdSe
	 * @return
	 * @throws Exception
	 */
	private boolean checkRdSe(RdSe rdSe) throws Exception {
		// TODO Auto-generated method stub
		int inLinkPid = rdSe.getInLinkPid();
		int nodePid = rdSe.getNodePid();
		int outLinkPid = rdSe.getOutLinkPid();

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT RS.PID FROM RD_SE RS WHERE RS.u_record !=2");
		sb.append(" AND RS.IN_LINK_PID = ");
		sb.append(inLinkPid);
		sb.append(" AND RS.OUT_LINK_PID = ");
		sb.append(outLinkPid);
		sb.append(" AND RS.NODE_PID = ");
		sb.append(nodePid);

		String sql = sb.toString();

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * @param rdDirectroute
	 * @return
	 * @throws Exception
	 */
	private boolean checkRdDirectroute(RdDirectroute rdDirectroute) throws Exception {
		// TODO Auto-generated method stub
		int inLinkPid = rdDirectroute.getInLinkPid();
		int nodePid = rdDirectroute.getNodePid();

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT RD.PID FROM RD_DIRECTROUTE RD WHERE RD.u_record !=2");
		sb.append(" AND RD.IN_LINK_PID = ");
		sb.append(inLinkPid);
		sb.append(" AND RD.NODE_PID = ");
		sb.append(nodePid);

		String sql = sb.toString();

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * @param rdRestriction
	 * @param operType 
	 * @return
	 * @throws Exception
	 */
	private boolean checkRdRestriction(RdRestriction rdRestriction, OperType operType) throws Exception {
		if(operType == OperType.CREATE)
		{
			int inLinkPid = rdRestriction.getInLinkPid();
			int nodePid = rdRestriction.getNodePid();

			StringBuilder sb = new StringBuilder();

			sb.append("SELECT RR.PID FROM RD_RESTRICTION RR WHERE RR.u_record !=2");
			sb.append(" AND RR.IN_LINK_PID = ");
			sb.append(inLinkPid);
			sb.append(" AND RR.NODE_PID = ");
			sb.append(nodePid);

			String sql = sb.toString();

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (resultList.size() > 0) {
				return false;
			}
		}
		return true;
	}

	private boolean checkRdBranch(RdBranch rdBranch) throws Exception {
		int inLinkPid = rdBranch.getInLinkPid();
		int outLinkPid = rdBranch.getOutLinkPid();
		int nodePid = rdBranch.getNodePid();
		RdBranchDetail detail = (RdBranchDetail) rdBranch.getDetails().get(0);
		int branchType = detail.getBranchType();
		StringBuilder sb = new StringBuilder();
		sb.append("select rb.branch_pid" + "  from rd_branch rb, rd_branch_detail d"
				+ " where rb.branch_pid = d.branch_pid" + "   AND d.branch_type=" + branchType
				+ "   AND RB.u_record != 2" + "   AND d.u_record != 2");
		sb.append(" and rb.in_link_pid = ");
		sb.append(inLinkPid);
		sb.append(" and rb.out_link_pid = ");
		sb.append(outLinkPid);
		sb.append(" and rb.node_pid = ");
		sb.append(nodePid);

		String sql = sb.toString();

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {
			return false;
		}
		return true;
	}

	private boolean checkRdBranchDetail(RdBranchDetail rdBranchDetail) throws Exception {
		int branchType = rdBranchDetail.getBranchType();
		StringBuilder sb = new StringBuilder();
		sb.append("select rb.branch_pid" + "  from rd_branch rb, rd_branch_detail d"
				+ " where rb.branch_pid = d.branch_pid" + "   AND d.branch_type=" + branchType
				+ "   AND RB.u_record != 2" + "   AND d.u_record != 2");
		sb.append(" and rb.branch_pid = ");
		sb.append(rdBranchDetail.getBranchPid());

		String sql = sb.toString();

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * @param rdGate
	 * @return
	 * @throws Exception
	 */
	private boolean checkRdGate(RdGate rdGate) throws Exception {
		// TODO Auto-generated method stub
		int inLinkPid = rdGate.getInLinkPid();
		int nodePid = rdGate.getNodePid();

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT RG.PID FROM RD_GATE RG WHERE RG.u_record !=2");
		sb.append(" AND RG.IN_LINK_PID = ");
		sb.append(inLinkPid);
		sb.append(" AND RG.NODE_PID = ");
		sb.append(nodePid);

		String sql = sb.toString();

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.
	 * dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
