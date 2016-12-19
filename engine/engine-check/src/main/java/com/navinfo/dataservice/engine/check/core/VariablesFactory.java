package com.navinfo.dataservice.engine.check.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignasreal;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.crosswalk.RdCrosswalk;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneCondition;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgateName;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;

public class VariablesFactory {

	public static Set<String> getRdLinkPid(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdLink) {
			rdLinkSet.add(String.valueOf(((RdLink) data).getPid()));
		}
		if (data instanceof RdLinkForm) {
			rdLinkSet.add(String.valueOf(((RdLinkForm) data).getLinkPid()));
		}
		if (data instanceof RdRestriction) {
			rdLinkSet.add(String.valueOf(((RdRestriction) data).getInLinkPid()));
		}
		if (data instanceof RdRestrictionDetail) {
			rdLinkSet.add(String.valueOf(((RdRestrictionDetail) data).getOutLinkPid()));
		}
		if (data instanceof RdLinkForm) {
			rdLinkSet.add(String.valueOf(((RdLinkForm) data).getLinkPid()));
		}
		if (data instanceof RdWarninginfo) {
			rdLinkSet.add(String.valueOf(((RdWarninginfo) data).getLinkPid()));
		}
		if (data instanceof RdDirectroute) {
			rdLinkSet.add(String.valueOf(((RdDirectroute) data).getInLinkPid()));
			rdLinkSet.add(String.valueOf(((RdDirectroute) data).getOutLinkPid()));
		}
		if (data instanceof RdGate) {
			rdLinkSet.add(String.valueOf(((RdGate) data).getInLinkPid()));
			rdLinkSet.add(String.valueOf(((RdGate) data).getOutLinkPid()));
		}
		if (data instanceof RdSe) {
			rdLinkSet.add(String.valueOf(((RdSe) data).getInLinkPid()));
		}
		if(data instanceof RdGsc)
		{
			RdGsc gsc = (RdGsc) data;
			for(IRow row : gsc.getLinks())
			{
				rdLinkSet.add(String.valueOf(((RdGscLink) row).getLinkPid()));
			}
		}
		if (data instanceof RdGscLink)
		{
			rdLinkSet.add(String.valueOf(((RdGscLink) data).getLinkPid()));
		}
		if (data instanceof RdTmclocation) {
			List<IRow> rows = ((RdTmclocation) data).getLinks();
			for (IRow row : rows) {
				RdTmclocationLink tmcLink = (RdTmclocationLink) row;
				rdLinkSet.add(String.valueOf(tmcLink.getLinkPid()));
			}
		}
		return rdLinkSet;
	}

	public static Set<String> getRdNodePid(IRow data) {
		Set<String> rdNodeSet = new HashSet<String>();
		if (data instanceof RdLink) {
			RdLink rdLink = (RdLink) data;
			rdNodeSet.add(String.valueOf(rdLink.geteNodePid()));
			rdNodeSet.add(String.valueOf(rdLink.getsNodePid()));
		}
		if (data instanceof RdBranch) {
			RdBranch rdBranch = (RdBranch) data;
			rdNodeSet.add(String.valueOf(rdBranch.getNodePid()));
		}
		if (data instanceof RdSlope) {
			RdSlope rdSlope = (RdSlope) data;
			rdNodeSet.add(String.valueOf(rdSlope.getNodePid()));
		}
		if (data instanceof RdWarninginfo) {
			rdNodeSet.add(String.valueOf(((RdWarninginfo) data).getNodePid()));
		}
		if (data instanceof RdDirectroute) {
			rdNodeSet.add(String.valueOf(((RdDirectroute) data).getNodePid()));
		}
		if (data instanceof RdTrafficsignal) {
			rdNodeSet.add(String.valueOf(((RdTrafficsignal) data).getNodePid()));
		}
		if (data instanceof RdSe) {
			rdNodeSet.add(String.valueOf(((RdSe) data).getNodePid()));
		}
		return rdNodeSet;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdGateInLinkPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdGate) {
			RdGate rdGate = (RdGate) data;
			rdLinkSet.add(String.valueOf(rdGate.getInLinkPid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdSePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdSe) {
			RdSe rdSe = (RdSe) data;
			rdLinkSet.add(String.valueOf(rdSe.getPid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdSeNodePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdSe) {
			RdSe rdSe = (RdSe) data;
			rdLinkSet.add(String.valueOf(rdSe.getNodePid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdSeInLinkPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdSe) {
			RdSe rdSe = (RdSe) data;
			rdLinkSet.add(String.valueOf(rdSe.getInLinkPid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdSeOutLinkPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdSe) {
			RdSe rdSe = (RdSe) data;
			rdLinkSet.add(String.valueOf(rdSe.getOutLinkPid()));
		}
		return rdLinkSet;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdGateOutLinkPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdGate) {
			RdGate rdGate = (RdGate) data;
			rdLinkSet.add(String.valueOf(rdGate.getOutLinkPid()));
		}
		return rdLinkSet;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdDirectroutePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdDirectroute) {
			RdDirectroute rdDirectroute = (RdDirectroute) data;
			rdLinkSet.add(String.valueOf(rdDirectroute.getPid()));
		}
		return rdLinkSet;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdSlopePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdSlope) {
			RdSlope rdSlope = (RdSlope) data;
			rdLinkSet.add(String.valueOf(rdSlope.getPid()));
		}
		return rdLinkSet;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdWarninginfoPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdWarninginfo) {
			RdWarninginfo rdWarninginfo = (RdWarninginfo) data;
			rdLinkSet.add(String.valueOf(rdWarninginfo.getPid()));
		}
		return rdLinkSet;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdCrosswalkPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdCrosswalk) {
			RdCrosswalk rdCrosswalk = (RdCrosswalk) data;
			rdLinkSet.add(String.valueOf(rdCrosswalk.getPid()));
		}
		return rdLinkSet;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdBranchPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdBranch) {
			RdBranch rdBranch = (RdBranch) data;
			rdLinkSet.add(String.valueOf(rdBranch.getPid()));
		}

		if (data instanceof RdBranchDetail) {
			RdBranchDetail rdBranchDetail = (RdBranchDetail) data;
			rdLinkSet.add(String.valueOf(rdBranchDetail.getBranchPid()));
		}
		if (data instanceof RdBranchRealimage) {
			RdBranchRealimage rdBranchRealimage = (RdBranchRealimage) data;
			rdLinkSet.add(String.valueOf(rdBranchRealimage.getBranchPid()));
		}
		if (data instanceof RdBranchSchematic) {
			RdBranchSchematic rdBranchSchematic = (RdBranchSchematic) data;
			rdLinkSet.add(String.valueOf(rdBranchSchematic.getBranchPid()));
		}
		if (data instanceof RdSignasreal) {
			RdSignasreal rdSignasreal = (RdSignasreal) data;
			rdLinkSet.add(String.valueOf(rdSignasreal.getBranchPid()));
		}
		if (data instanceof RdSignboard) {
			RdSignboard rdSignboard = (RdSignboard) data;
			rdLinkSet.add(String.valueOf(rdSignboard.getBranchPid()));
		}
		if (data instanceof RdSeriesbranch) {
			RdSeriesbranch rdSeriesbranch = (RdSeriesbranch) data;
			rdLinkSet.add(String.valueOf(rdSeriesbranch.getBranchPid()));
		}
		return rdLinkSet;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdVoiceGuidePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdVoiceguide) {
			RdVoiceguide rdVoiceguide = (RdVoiceguide) data;
			rdLinkSet.add(String.valueOf(rdVoiceguide.getPid()));
		}
		return rdLinkSet;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdTollgatePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdTollgate) {
			RdTollgate rdTollgate = (RdTollgate) data;
			rdLinkSet.add(String.valueOf(rdTollgate.getPid()));
		}

		if (data instanceof RdTollgateName) {
			RdTollgateName rdTollgateName = (RdTollgateName) data;
			rdLinkSet.add(String.valueOf(rdTollgateName.getPid()));
		}
		return rdLinkSet;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdGatePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdGate) {
			RdGate rdGate = (RdGate) data;
			rdLinkSet.add(String.valueOf(rdGate.getPid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdElectroniceye(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdElectroniceye) {
			rdLinkSet.add(String.valueOf(((RdElectroniceye) data).getPid()));
		}
		if (data instanceof RdEleceyePart) {
			rdLinkSet.add(String.valueOf(((RdEleceyePart) data).getEleceyePid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdLanePid(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdLane) {
			rdLinkSet.add(String.valueOf(((RdLane) data).getPid()));
		}
		if (data instanceof RdLaneCondition) {
			rdLinkSet.add(String.valueOf(((RdLaneCondition) data).getLanePid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdLaneConnexityPid(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdLaneConnexity) {
			rdLinkSet.add(String.valueOf(((RdLaneConnexity) data).getPid()));
		}
		if (data instanceof RdLaneTopology) {
			rdLinkSet.add(String.valueOf(((RdLaneTopology) data).getConnexityPid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdRestrictionPid(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdRestriction) {
			rdLinkSet.add(String.valueOf(((RdRestriction) data).getPid()));
		}
		if (data instanceof RdRestrictionDetail) {
			rdLinkSet.add(String.valueOf(((RdRestrictionDetail) data).getRestricPid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdVoiceguideDetailPid(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdVoiceguideDetail) {
			rdLinkSet.add(String.valueOf(((RdVoiceguideDetail) data).getPid()));
		}

		return rdLinkSet;
	}

	public static Set<String> getRdTrafficsignalPid(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdTrafficsignal) {
			rdLinkSet.add(String.valueOf(((RdTrafficsignal) data).getPid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdSpeedbumpPid(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdSpeedbump) {
			rdLinkSet.add(String.valueOf(((RdSpeedbump) data).getPid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdCrossPid(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdCross) {
			rdLinkSet.add(String.valueOf(((RdCross) data).getPid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdSpeedlimitPid(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdSpeedlimit) {
			rdLinkSet.add(String.valueOf(((RdSpeedlimit) data).getPid()));
		}
		return rdLinkSet;
	}

	public static Set<String> getRdBranchDetailPid(IRow data) {
		Set<String> rdLinkSet = new HashSet<String>();
		if (data instanceof RdBranchDetail) {
			rdLinkSet.add(String.valueOf(((RdBranchDetail) data).getPid()));
		}
		return rdLinkSet;
	}
}
