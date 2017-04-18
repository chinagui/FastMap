package com.navinfo.dataservice.engine.meta.model;


/** 
* @ClassName:  ScBranchSpecc 
* @author code generator
* @date 2017-03-22 09:23:23 
* @Description: TODO
*/
public class ScBranchSpecc  {
	private String branch1 ;
	private String branch2 ;
	private String seriesbranch1 ;
	
	public ScBranchSpecc (){
	}
	
	public ScBranchSpecc (String branch1 ,String branch2,String seriesbranch1){
		this.branch1=branch1 ;
		this.branch2=branch2 ;
		this.seriesbranch1=seriesbranch1 ;
	}
	public String getBranch1() {
		return branch1;
	}
	public void setBranch1(String branch1) {
		this.branch1 = branch1;
	}
	public String getBranch2() {
		return branch2;
	}
	public void setBranch2(String branch2) {
		this.branch2 = branch2;
	}
	public String getSeriesbranch1() {
		return seriesbranch1;
	}
	public void setSeriesbranch1(String seriesbranch1) {
		this.seriesbranch1 = seriesbranch1;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScBranchSpecc [branch1=" + branch1 +",branch2="+branch2+",seriesbranch1="+seriesbranch1+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((branch1 == null) ? 0 : branch1.hashCode());
		result = prime * result + ((branch2 == null) ? 0 : branch2.hashCode());
		result = prime * result + ((seriesbranch1 == null) ? 0 : seriesbranch1.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScBranchSpecc other = (ScBranchSpecc) obj;
		if (branch1 == null) {
			if (other.branch1 != null)
				return false;
		} else if (!branch1.equals(other.branch1))
			return false;
		if (branch2 == null) {
			if (other.branch2 != null)
				return false;
		} else if (!branch2.equals(other.branch2))
			return false;
		if (seriesbranch1 == null) {
			if (other.seriesbranch1 != null)
				return false;
		} else if (!seriesbranch1.equals(other.seriesbranch1))
			return false;
		return true;
	}
	
	
	
}
