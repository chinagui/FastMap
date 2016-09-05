package com.navinfo.dataservice.engine.check.core;

import java.util.HashSet;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;

public class VariablesFactory {

	public static Set<String> getRdLinkPid(IRow data){
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdLink){rdLinkSet.add(String.valueOf(((RdLink) data).getPid()));}
		if(data instanceof RdRestriction){rdLinkSet.add(String.valueOf(((RdRestriction) data).getInLinkPid()));}
		if(data instanceof RdRestrictionDetail){rdLinkSet.add(String.valueOf(((RdRestrictionDetail) data).getOutLinkPid()));}
		if(data instanceof RdLinkForm){rdLinkSet.add(String.valueOf(((RdLinkForm) data).getLinkPid()));}
		if(data instanceof RdWarninginfo){rdLinkSet.add(String.valueOf(((RdWarninginfo) data).getLinkPid()));}
		if(data instanceof RdDirectroute){
			rdLinkSet.add(String.valueOf(((RdDirectroute) data).getInLinkPid()));
			rdLinkSet.add(String.valueOf(((RdDirectroute) data).getOutLinkPid()));
		}
		if(data instanceof RdGate){
			rdLinkSet.add(String.valueOf(((RdGate) data).getInLinkPid()));
			rdLinkSet.add(String.valueOf(((RdGate) data).getOutLinkPid()));
		}
		return rdLinkSet;
	}
	public static Set<String> getRdNodePid(IRow data){
		Set<String> rdNodeSet=new HashSet<String>();
		if(data instanceof RdLink){
			RdLink rdLink=(RdLink) data;
			rdNodeSet.add(String.valueOf(rdLink.geteNodePid()));
			rdNodeSet.add(String.valueOf(rdLink.getsNodePid()));}
		if(data instanceof RdBranch){
			RdBranch rdBranch=(RdBranch) data;
			rdNodeSet.add(String.valueOf(rdBranch.getNodePid()));}
		if(data instanceof RdSlope){
			RdSlope rdSlope=(RdSlope) data;
			rdNodeSet.add(String.valueOf(rdSlope.getNodePid()));}
		if(data instanceof RdWarninginfo){
			rdNodeSet.add(String.valueOf(((RdWarninginfo) data).getNodePid()));}
		if(data instanceof RdDirectroute){
			rdNodeSet.add(String.valueOf(((RdDirectroute) data).getNodePid()));}
		
		
		return rdNodeSet;
	}
	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdGateInLinkPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdGate){
			RdGate rdGate=(RdGate) data;
			rdLinkSet.add(String.valueOf(rdGate.getInLinkPid()));
		}
		return rdLinkSet;
	}
	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdGateOutLinkPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdGate){
			RdGate rdGate=(RdGate) data;
			rdLinkSet.add(String.valueOf(rdGate.getOutLinkPid()));}
		return rdLinkSet;
	}
	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdDirectroutePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdGate){
			RdDirectroute rdDirectroute=(RdDirectroute) data;
			rdLinkSet.add(String.valueOf(rdDirectroute.getPid()));}
		return rdLinkSet;
	}
	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdSlopePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdSlope){
			RdSlope rdSlope=(RdSlope) data;
			rdLinkSet.add(String.valueOf(rdSlope.getPid()));}
		return rdLinkSet;
	}
	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdWarninginfoPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdWarninginfo){
			RdWarninginfo rdWarninginfo=(RdWarninginfo) data;
			rdLinkSet.add(String.valueOf(rdWarninginfo.getPid()));}
		return rdLinkSet;
	}
	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdBranchPid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdBranch){
			RdBranch rdBranch=(RdBranch) data;
			rdLinkSet.add(String.valueOf(rdBranch.getPid()));}
		return rdLinkSet;
	}
	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdVoiceGuidePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdVoiceguide){
			RdVoiceguide rdVoiceguide=(RdVoiceguide) data;
			rdLinkSet.add(String.valueOf(rdVoiceguide.getPid()));}
		return rdLinkSet;
	}
	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdTollgatePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdTollgate){
			RdTollgate rdTollgate=(RdTollgate) data;
			rdLinkSet.add(String.valueOf(rdTollgate.getPid()));}
		return rdLinkSet;
	}
	/**
	 * @param data
	 * @return
	 */
	public static Set<String> getRdGatePid(IRow data) {
		// TODO Auto-generated method stub
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdGate){
			RdGate rdGate=(RdGate) data;
			rdLinkSet.add(String.valueOf(rdGate.getPid()));}
		return rdLinkSet;
	}
}
