package com.navinfo.dataservice.engine.meta.model;


/** 
* @ClassName:  ScBranchCommc 
* @author code generator
* @date 2017-03-22 09:23:12 
* @Description: TODO
*/
public class ScBranchCommc  {
	private String branch1 ;
	private String branch2 ;
	private String branch3 ;
	private String branch4 ;
	private String branch5 ;
	private String seriesbranch1 ;
	private String seriesbranch2 ;
	private String seriesbranch3 ;
	private String seriesbranch4 ;
	
	public ScBranchCommc (){
	}
	
	public ScBranchCommc (String branch1 ,String branch2,String branch3,String branch4,String branch5,String seriesbranch1,String seriesbranch2,String seriesbranch3,String seriesbranch4){
		this.branch1=branch1 ;
		this.branch2=branch2 ;
		this.branch3=branch3 ;
		this.branch4=branch4 ;
		this.branch5=branch5 ;
		this.seriesbranch1=seriesbranch1 ;
		this.seriesbranch2=seriesbranch2 ;
		this.seriesbranch3=seriesbranch3 ;
		this.seriesbranch4=seriesbranch4 ;
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
	public String getBranch3() {
		return branch3;
	}
	public void setBranch3(String branch3) {
		this.branch3 = branch3;
	}
	public String getBranch4() {
		return branch4;
	}
	public void setBranch4(String branch4) {
		this.branch4 = branch4;
	}
	public String getBranch5() {
		return branch5;
	}
	public void setBranch5(String branch5) {
		this.branch5 = branch5;
	}
	public String getSeriesbranch1() {
		return seriesbranch1;
	}
	public void setSeriesbranch1(String seriesbranch1) {
		this.seriesbranch1 = seriesbranch1;
	}
	public String getSeriesbranch2() {
		return seriesbranch2;
	}
	public void setSeriesbranch2(String seriesbranch2) {
		this.seriesbranch2 = seriesbranch2;
	}
	public String getSeriesbranch3() {
		return seriesbranch3;
	}
	public void setSeriesbranch3(String seriesbranch3) {
		this.seriesbranch3 = seriesbranch3;
	}
	public String getSeriesbranch4() {
		return seriesbranch4;
	}
	public void setSeriesbranch4(String seriesbranch4) {
		this.seriesbranch4 = seriesbranch4;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScBranchCommc [branch1=" + branch1 +",branch2="+branch2+",branch3="+branch3+",branch4="+branch4+",branch5="+branch5+",seriesbranch1="+seriesbranch1+",seriesbranch2="+seriesbranch2+",seriesbranch3="+seriesbranch3+",seriesbranch4="+seriesbranch4+"]";
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
		result = prime * result + ((branch3 == null) ? 0 : branch3.hashCode());
		result = prime * result + ((branch4 == null) ? 0 : branch4.hashCode());
		result = prime * result + ((branch5 == null) ? 0 : branch5.hashCode());
		result = prime * result + ((seriesbranch1 == null) ? 0 : seriesbranch1.hashCode());
		result = prime * result + ((seriesbranch2 == null) ? 0 : seriesbranch2.hashCode());
		result = prime * result + ((seriesbranch3 == null) ? 0 : seriesbranch3.hashCode());
		result = prime * result + ((seriesbranch4 == null) ? 0 : seriesbranch4.hashCode());
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
		ScBranchCommc other = (ScBranchCommc) obj;
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
		if (branch3 == null) {
			if (other.branch3 != null)
				return false;
		} else if (!branch3.equals(other.branch3))
			return false;
		if (branch4 == null) {
			if (other.branch4 != null)
				return false;
		} else if (!branch4.equals(other.branch4))
			return false;
		if (branch5 == null) {
			if (other.branch5 != null)
				return false;
		} else if (!branch5.equals(other.branch5))
			return false;
		if (seriesbranch1 == null) {
			if (other.seriesbranch1 != null)
				return false;
		} else if (!seriesbranch1.equals(other.seriesbranch1))
			return false;
		if (seriesbranch2 == null) {
			if (other.seriesbranch2 != null)
				return false;
		} else if (!seriesbranch2.equals(other.seriesbranch2))
			return false;
		if (seriesbranch3 == null) {
			if (other.seriesbranch3 != null)
				return false;
		} else if (!seriesbranch3.equals(other.seriesbranch3))
			return false;
		if (seriesbranch4 == null) {
			if (other.seriesbranch4 != null)
				return false;
		} else if (!seriesbranch4.equals(other.seriesbranch4))
			return false;
		return true;
	}
	
	
	
}
