package com.navinfo.dataservice.control.dealership.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxDealershipChain 
* @author code generator
* @date 2017-05-27 03:25:34 
* @Description: TODO
*/
public class IxDealershipChain  {
	private String chainCode ;
	private String chainName ;
	private int chainWeight ;
	private int chainStatus ;
	private int workType ;
	private int workStatus ;
	
	public IxDealershipChain (){
	}
	
	public IxDealershipChain (String chainCode ,String chainName,int chainWeight,int chainStatus,int workType,int workStatus){
		this.chainCode=chainCode ;
		this.chainName=chainName ;
		this.chainWeight=chainWeight ;
		this.chainStatus=chainStatus ;
		this.workType=workType ;
		this.workStatus=workStatus ;
	}
	public String getChainCode() {
		return chainCode;
	}
	public void setChainCode(String chainCode) {
		this.chainCode = chainCode;
	}
	public String getChainName() {
		return chainName;
	}
	public void setChainName(String chainName) {
		this.chainName = chainName;
	}
	public int getChainWeight() {
		return chainWeight;
	}
	public void setChainWeight(int chainWeight) {
		this.chainWeight = chainWeight;
	}
	public int getChainStatus() {
		return chainStatus;
	}
	public void setChainStatus(int chainStatus) {
		this.chainStatus = chainStatus;
	}
	public int getWorkType() {
		return workType;
	}
	public void setWorkType(int workType) {
		this.workType = workType;
	}
	public int getWorkStatus() {
		return workStatus;
	}
	public void setWorkStatus(int workStatus) {
		this.workStatus = workStatus;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IxDealershipChain [chainCode=" + chainCode +",chainName="+chainName+",chainWeight="+chainWeight+",chainStatus="+chainStatus+",workType="+workType+",workStatus="+workStatus+"]";
	}

	
}
