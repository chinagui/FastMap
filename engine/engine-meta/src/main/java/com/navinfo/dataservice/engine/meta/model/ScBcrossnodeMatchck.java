package com.navinfo.dataservice.engine.meta.model;


/** 
* @ClassName:  ScBcrossnodeMatchck 
* @author code generator
* @date 2017-03-22 09:23:34 
* @Description: TODO
*/
public class ScBcrossnodeMatchck  {
	private String schematicCode ;
	private String arrowCode ;
	private Integer seq ;
	
	public ScBcrossnodeMatchck (){
	}
	
	public ScBcrossnodeMatchck (String schematicCode ,String arrowCode,int seq){
		this.schematicCode=schematicCode ;
		this.arrowCode=arrowCode ;
		this.seq=seq ;
	}
	public String getSchematicCode() {
		return schematicCode;
	}
	public void setSchematicCode(String schematicCode) {
		this.schematicCode = schematicCode;
	}
	public String getArrowCode() {
		return arrowCode;
	}
	public void setArrowCode(String arrowCode) {
		this.arrowCode = arrowCode;
	}
	public Integer getSeq() {
		return seq;
	}
	public void setSeq(Integer seq) {
		this.seq = seq;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScBcrossnodeMatchck [schematicCode=" + schematicCode +",arrowCode="+arrowCode+",seq="+seq+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((schematicCode == null) ? 0 : schematicCode.hashCode());
		result = prime * result + ((arrowCode == null) ? 0 : arrowCode.hashCode());
		result = prime * result + ((seq == null) ? 0 : seq.hashCode());
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
		ScBcrossnodeMatchck other = (ScBcrossnodeMatchck) obj;
		if (schematicCode == null) {
			if (other.schematicCode != null)
				return false;
		} else if (!schematicCode.equals(other.schematicCode))
			return false;
		if (arrowCode == null) {
			if (other.arrowCode != null)
				return false;
		} else if (!arrowCode.equals(other.arrowCode))
			return false;
		if (seq == null) {
			if (other.seq != null)
				return false;
		} else if (!seq.equals(other.seq))
			return false;
		return true;
	}
	
	
	
}
