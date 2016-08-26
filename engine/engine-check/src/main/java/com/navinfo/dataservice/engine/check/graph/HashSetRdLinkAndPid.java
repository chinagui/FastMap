package com.navinfo.dataservice.engine.check.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sun.tools.tree.ThisExpression;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class HashSetRdLinkAndPid{
	private HashSet<RdLink> rdLinkSet=new HashSet<RdLink>();
	//private HashSet<Integer> rdLinkPidSet=new HashSet<Integer>();
	private Map<Integer, RdLink> pidLinkMap=new HashMap<Integer, RdLink>();
	
	public HashSet<RdLink> getRdLinkSet() {
		return rdLinkSet;
	}
	public Set<Integer> getRdLinkPidSet() {
		return pidLinkMap.keySet();
	}
	public Map<Integer, RdLink> getPidLinkMap() {
		return pidLinkMap;
	}
	
	public int size(){
		return rdLinkSet.size();
	}
	
	public void addAll(HashSetRdLinkAndPid rdObjSet){
		if(rdObjSet==null||rdObjSet.size()==0){return;}
		if(this.size()==0){
			this.rdLinkSet.addAll(rdObjSet.getRdLinkSet());
			this.pidLinkMap.putAll(rdObjSet.getPidLinkMap());
			return;
		}
		HashSet<Integer> linkPidSet=new HashSet<Integer>();
		linkPidSet.addAll(rdObjSet.getRdLinkPidSet());
		linkPidSet.removeAll(this.getRdLinkPidSet());
		if(!linkPidSet.isEmpty()){
			Iterator<RdLink> iterator=rdObjSet.iterator();
			while (iterator.hasNext()) {
				this.add(iterator.next());
			}
		}
	}
	
	public void addAll(Collection<RdLink> rdLinkSet){
		if(rdLinkSet==null||rdLinkSet.size()==0){return;}
		Iterator<RdLink> iterator=rdLinkSet.iterator();
		while (iterator.hasNext()) {
			this.add(iterator.next());
		}
	}
	
	public boolean contains(int pid){
		return this.getRdLinkPidSet().contains(pid);
	}
	
	public void add(RdLink rdLinkObj){
		int LinkPid=rdLinkObj.getPid();
		if(!this.getRdLinkPidSet().contains(LinkPid)){
			this.rdLinkSet.add(rdLinkObj);
			this.pidLinkMap.put(LinkPid, rdLinkObj);
		}
	}
	
	public Iterator<RdLink> iterator(){
		return this.rdLinkSet.iterator();
	}
	
	public void remove(RdLink link) {
		int linkPid=link.getPid();
		if(!this.contains(link.getPid())){return;}
		this.rdLinkSet.remove(this.pidLinkMap.get(linkPid));
		this.pidLinkMap.remove(linkPid);
	}
}
