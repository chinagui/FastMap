package com.navinfo.dataservice.engine.check.core;

import java.util.HashSet;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;

public class VariablesFactory {

	public static Set<String> getRdLinkPid(IRow data){
		Set<String> rdLinkSet=new HashSet<String>();
		if(data instanceof RdLink){rdLinkSet.add(String.valueOf(((RdLink) data).getPid()));}
		if(data instanceof RdRestriction){rdLinkSet.add(String.valueOf(((RdRestriction) data).getInLinkPid()));}
		if(data instanceof RdRestrictionDetail){rdLinkSet.add(String.valueOf(((RdRestrictionDetail) data).getOutLinkPid()));}
		if(data instanceof RdLinkForm){rdLinkSet.add(String.valueOf(((RdLinkForm) data).getLinkPid()));}
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
}
