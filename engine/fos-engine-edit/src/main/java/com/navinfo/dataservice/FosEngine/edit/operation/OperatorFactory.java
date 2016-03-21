package com.navinfo.dataservice.FosEngine.edit.operation;

import java.sql.Connection;

import com.navinfo.dataservice.FosEngine.edit.model.IOperator;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranch;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchVia;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSignasreal;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSignboard;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSignboardName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkForm;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkLimit;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkLimitTruck;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkRtic;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkSidewalk;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkWalkstair;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkZone;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeForm;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeMesh;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch.RdBranchDetailOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch.RdBranchNameOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch.RdBranchOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch.RdBranchRealimageOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch.RdBranchSchematicOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch.RdBranchViaOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch.RdSeriesbranchOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch.RdSignasrealOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch.RdSignboardNameOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch.RdSignboardOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.cross.RdCrossLinkOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.cross.RdCrossNameOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.cross.RdCrossNodeOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.cross.RdCrossOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.laneconnexity.RdLaneConnexityOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.laneconnexity.RdLaneTopologyOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.laneconnexity.RdLaneViaOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkFormOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkIntRticOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkLimitOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkLimitTruckOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkNameOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkRticOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkSidewalkOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkSpeedlimitOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkWalkstairOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link.RdLinkZoneOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.node.RdNodeFormOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.node.RdNodeMeshOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.node.RdNodeNameOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.node.RdNodeOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.restrict.RdRestrictionConditionOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.restrict.RdRestrictionDetailOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.restrict.RdRestrictionOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.restrict.RdRestrictionViaOperator;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.speedlimit.RdSpeedlimitOperator;

/**
 * 操作类工厂
 */
public class OperatorFactory {

	/**
	 * 操作结果写入数据库
	 * 
	 * @param conn
	 *            数据库连接
	 * @param result
	 *            操作结果
	 * @throws Exception
	 */
	public static void recordData(Connection conn, Result result)
			throws Exception {
		for (IRow obj : result.getDelObjects()) {

			getOperator(conn, obj).deleteRow();
		}

		for (IRow obj : result.getAddObjects()) {

			getOperator(conn, obj).insertRow();
		}

		for (IRow obj : result.getUpdateObjects()) {

			getOperator(conn, obj).updateRow();
		}
	}

	/**
	 * 根据对象，返回操作类
	 * 
	 * @param conn
	 *            数据库连接
	 * @param obj
	 *            对象
	 * @return
	 */
	private static IOperator getOperator(Connection conn, IRow obj) {
		switch (obj.objType()) {
		case RDLINK:
			return new RdLinkOperator(conn, (RdLink) obj);
		case RDLINKFORM:
			return new RdLinkFormOperator(conn, (RdLinkForm) obj);
		case RDLINKLIMIT:
			return new RdLinkLimitOperator(conn, (RdLinkLimit) obj);
		case RDLINKNAME:
			return new RdLinkNameOperator(conn, (RdLinkName) obj);
		case RDLINKLIMITTRUNK:
			return new RdLinkLimitTruckOperator(conn, (RdLinkLimitTruck) obj);
		case RDLINKSPEEDLIMIT:
			return new RdLinkSpeedlimitOperator(conn, (RdLinkSpeedlimit) obj);
		case RDLINKSIDEWALK:
			return new RdLinkSidewalkOperator(conn, (RdLinkSidewalk) obj);
		case RDLINKWALKSTAIR:
			return new RdLinkWalkstairOperator(conn, (RdLinkWalkstair) obj);
		case RDLINKRTIC:
			return new RdLinkRticOperator(conn, (RdLinkRtic) obj);
		case RDLINKINTRTIC:
			return new RdLinkIntRticOperator(conn, (RdLinkIntRtic) obj);
		case RDLINKZONE:
			return new RdLinkZoneOperator(conn, (RdLinkZone) obj);
		case RDNODE:
			return new RdNodeOperator(conn, (RdNode) obj);
		case RDNODEFORM:
			return new RdNodeFormOperator(conn, (RdNodeForm) obj);
		case RDNODEMESH:
			return new RdNodeMeshOperator(conn, (RdNodeMesh) obj);
		case RDNODENAME:
			return new RdNodeNameOperator(conn, (RdNodeName) obj);
		case RDRESTRICTION:
			return new RdRestrictionOperator(conn, (RdRestriction) obj);
		case RDRESTRICTIONDETAIL:
			return new RdRestrictionDetailOperator(conn,
					(RdRestrictionDetail) obj);
		case RDRESTRICTIONCONDITION:
			return new RdRestrictionConditionOperator(conn,
					(RdRestrictionCondition) obj);
		case RDRESTRICTIONVIA:
			return new RdRestrictionViaOperator(conn, (RdRestrictionVia) obj);
		case RDCROSS:
			return new RdCrossOperator(conn, (RdCross) obj);
		case RDCROSSLINK:
			return new RdCrossLinkOperator(conn, (RdCrossLink) obj);
		case RDCROSSNAME:
			return new RdCrossNameOperator(conn, (RdCrossName) obj);
		case RDCROSSNODE:
			return new RdCrossNodeOperator(conn, (RdCrossNode) obj);
		case RDSPEEDLIMIT:
			return new RdSpeedlimitOperator(conn, (RdSpeedlimit) obj);
		case RDBRANCH:
			return new RdBranchOperator(conn, (RdBranch) obj);
		case RDBRANCHDETAIL:
			return new RdBranchDetailOperator(conn, (RdBranchDetail) obj);
		case RDBRANCHNAME:
			return new RdBranchNameOperator(conn, (RdBranchName) obj);
		case RDBRANCHREALIMAGE:
			return new RdBranchRealimageOperator(conn, (RdBranchRealimage) obj);
		case RDBRANCHSCHEMATIC:
			return new RdBranchSchematicOperator(conn, (RdBranchSchematic) obj);
		case RDBRANCHVIA:
			return new RdBranchViaOperator(conn, (RdBranchVia) obj);
		case RDSERIESBRANCH:
			return new RdSeriesbranchOperator(conn, (RdSeriesbranch) obj);
		case RDSIGNASREAL:
			return new RdSignasrealOperator(conn, (RdSignasreal) obj);
		case RDSIGNBOARD:
			return new RdSignboardOperator(conn, (RdSignboard) obj);
		case RDSIGNBOARDNAME:
			return new RdSignboardNameOperator(conn, (RdSignboardName) obj);
		case RDLANECONNEXITY:
			return new RdLaneConnexityOperator(conn, (RdLaneConnexity) obj);
		case RDLANETOPOLOGY:
			return new RdLaneTopologyOperator(conn, (RdLaneTopology) obj);
		case RDLANEVIA:
			return new RdLaneViaOperator(conn, (RdLaneVia) obj);
		default:
			return null;
		}
	}
}
