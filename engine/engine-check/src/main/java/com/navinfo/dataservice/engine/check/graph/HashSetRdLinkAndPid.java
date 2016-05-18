package com.navinfo.dataservice.engine.check.graph;

import java.util.HashSet;
import java.util.Iterator;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class HashSetRdLinkAndPid{
	private HashSet<RdLink> rdLinkSet=new HashSet<RdLink>();
	private HashSet<Integer> rdLinkPidSet=new HashSet<Integer>();
	public HashSet<RdLink> getRdLinkSet() {
		return rdLinkSet;
	}
	public void setRdLinkSet(HashSet<RdLink> rdLinkSet) {
		this.rdLinkSet = rdLinkSet;
	}
	public HashSet<Integer> getRdLinkPidSet() {
		return rdLinkPidSet;
	}
	public void setRdLinkPidSet(HashSet<Integer> rdLinkPidSet) {
		this.rdLinkPidSet = rdLinkPidSet;
	}
	
	public int size(){
		return rdLinkPidSet.size();
	}
	
	public void addAll(HashSetRdLinkAndPid rdObjSet){
		this.rdLinkSet.addAll(rdObjSet.getRdLinkSet());
		this.rdLinkPidSet.addAll(rdObjSet.getRdLinkPidSet());
	}
	
	public boolean contains(int pid){
		return this.rdLinkPidSet.contains(pid);
	}
	
	public void add(RdLink rdLinkObj){
		this.rdLinkSet.add(rdLinkObj);
		this.rdLinkPidSet.add(rdLinkObj.getPid());
	}
	
	public Iterator<RdLink> iterator(){
		return this.rdLinkSet.iterator();
	}
}
