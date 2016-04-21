package com.navinfo.dataservice.engine.check.core;


public class NiValException {
	//String ruleId, String loc, String targets,int meshId, String worker
	private String ruleId;
	private String loc;
	private String targets;
	private int meshId;
	
	public NiValException(String ruleId, String loc, String targets,int meshId){
		this.setRuleId(ruleId);
		this.setLoc(loc);
		this.setTargets(targets);
		this.setMeshId(meshId);
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
	}

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}
	
}
