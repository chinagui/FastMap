package com.navinfo.dataservice.engine.meta.model;


/** 
* @ClassName:  ScModelRepdelG 
* @author code generator
* @date 2017-03-22 09:22:39 
* @Description: TODO
*/
public class ScModelRepdelG  {
	private String convBefore ;
	private String convOut ;
	private String kind ;
	
	public ScModelRepdelG (){
	}
	
	public ScModelRepdelG (String convBefore ,String convOut,String kind){
		this.convBefore=convBefore ;
		this.convOut=convOut ;
		this.kind=kind ;
	}
	public String getConvBefore() {
		return convBefore;
	}
	public void setConvBefore(String convBefore) {
		this.convBefore = convBefore;
	}
	public String getConvOut() {
		return convOut;
	}
	public void setConvOut(String convOut) {
		this.convOut = convOut;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScModelRepdelG [convBefore=" + convBefore +",convOut="+convOut+",kind="+kind+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((convBefore == null) ? 0 : convBefore.hashCode());
		result = prime * result + ((convOut == null) ? 0 : convOut.hashCode());
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
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
		ScModelRepdelG other = (ScModelRepdelG) obj;
		if (convBefore == null) {
			if (other.convBefore != null)
				return false;
		} else if (!convBefore.equals(other.convBefore))
			return false;
		if (convOut == null) {
			if (other.convOut != null)
				return false;
		} else if (!convOut.equals(other.convOut))
			return false;
		if (kind == null) {
			if (other.kind != null)
				return false;
		} else if (!kind.equals(other.kind))
			return false;
		return true;
	}
	
	
	
}
