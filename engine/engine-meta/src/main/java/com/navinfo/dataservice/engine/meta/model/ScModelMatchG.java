package com.navinfo.dataservice.engine.meta.model;

import java.sql.Timestamp;

import oracle.sql.BLOB;

/** 
* @ClassName:  ScModelMatchG 
* @author code generator
* @date 2017-03-22 09:13:09 
* @Description: TODO
*/
public class ScModelMatchG  {
	private Long fileId ;
	private String productLine ;
	private String version ;
	private String projectNm ;
	private String specification ;
	private String bType ;
	private String mType ;
	private String sType ;
	private String fileName ;
	private String size ;
	private String format ;
	private String impWorker ;
//	private Timestamp impDate ;
	private String impDate ;
	private String urlDb ;
	private String urlFile ;
	private String memo ;
	private BLOB fileContent ;
//	private Integer fileType ;
	private String updateTime ;
	
	public ScModelMatchG (){
	}
	
	public ScModelMatchG (Long fileId ,String productLine,String version,String projectNm,String specification,String bType,String mType,String sType,String fileName,String size,String format,String impWorker,String impDate,String urlDb,String urlFile,String memo,BLOB fileContent,String updateTime){
		this.fileId=fileId ;
		this.productLine=productLine ;
		this.version=version ;
		this.projectNm=projectNm ;
		this.specification=specification ;
		this.bType=bType ;
		this.mType=mType ;
		this.sType=sType ;
		this.fileName=fileName ;
		this.size=size ;
		this.format=format ;
		this.impWorker=impWorker ;
		this.impDate=impDate ;
		this.urlDb=urlDb ;
		this.urlFile=urlFile ;
		this.memo=memo ;
		this.fileContent=fileContent ;
		this.updateTime=updateTime ;
	}
	public Long getFileId() {
		return fileId;
	}
	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}
	public String getProductLine() {
		return productLine;
	}
	public void setProductLine(String productLine) {
		this.productLine = productLine;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getProjectNm() {
		return projectNm;
	}
	public void setProjectNm(String projectNm) {
		this.projectNm = projectNm;
	}
	public String getSpecification() {
		return specification;
	}
	public void setSpecification(String specification) {
		this.specification = specification;
	}
	public String getbType() {
		return bType;
	}
	public void setbType(String bType) {
		this.bType = bType;
	}
	public String getmType() {
		return mType;
	}
	public void setmType(String mType) {
		this.mType = mType;
	}
	public String getsType() {
		return sType;
	}
	public void setsType(String sType) {
		this.sType = sType;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getImpWorker() {
		return impWorker;
	}
	public void setImpWorker(String impWorker) {
		this.impWorker = impWorker;
	}
	public String getImpDate() {
		return impDate;
	}
	public void setImpDate(String impDate) {
		this.impDate = impDate;
	}
	public String getUrlDb() {
		return urlDb;
	}
	public void setUrlDb(String urlDb) {
		this.urlDb = urlDb;
	}
	public String getUrlFile() {
		return urlFile;
	}
	public void setUrlFile(String urlFile) {
		this.urlFile = urlFile;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public BLOB getFileContent() {
		return fileContent;
	}
	public void setFileContent(BLOB fileContent) {
		this.fileContent = fileContent;
	}
	
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScModelMatchG [fileId=" + fileId +",productLine="+productLine+",version="+version+",projectNm="+projectNm+",specification="+specification+",bType="+bType+",mType="+mType+",sType="+sType+",fileName="+fileName+",size="+size+",format="+format+",impWorker="+impWorker+",impDate="+impDate+",urlDb="+urlDb+",urlFile="+urlFile+",memo="+memo+",fileContent="+fileContent+",updateTime="+updateTime+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
		result = prime * result + ((productLine == null) ? 0 : productLine.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((projectNm == null) ? 0 : projectNm.hashCode());
		result = prime * result + ((specification == null) ? 0 : specification.hashCode());
		result = prime * result + ((bType == null) ? 0 : bType.hashCode());
		result = prime * result + ((mType == null) ? 0 : mType.hashCode());
		result = prime * result + ((sType == null) ? 0 : sType.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + ((impWorker == null) ? 0 : impWorker.hashCode());
		result = prime * result + ((impDate == null) ? 0 : impDate.hashCode());
		result = prime * result + ((urlDb == null) ? 0 : urlDb.hashCode());
		result = prime * result + ((urlFile == null) ? 0 : urlFile.hashCode());
		result = prime * result + ((memo == null) ? 0 : memo.hashCode());
		result = prime * result + ((fileContent == null) ? 0 : fileContent.hashCode());
		result = prime * result + ((updateTime == null) ? 0 : updateTime.hashCode());
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
		ScModelMatchG other = (ScModelMatchG) obj;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		if (productLine == null) {
			if (other.productLine != null)
				return false;
		} else if (!productLine.equals(other.productLine))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		if (projectNm == null) {
			if (other.projectNm != null)
				return false;
		} else if (!projectNm.equals(other.projectNm))
			return false;
		if (specification == null) {
			if (other.specification != null)
				return false;
		} else if (!specification.equals(other.specification))
			return false;
		if (bType == null) {
			if (other.bType != null)
				return false;
		} else if (!bType.equals(other.bType))
			return false;
		if (mType == null) {
			if (other.mType != null)
				return false;
		} else if (!mType.equals(other.mType))
			return false;
		if (sType == null) {
			if (other.sType != null)
				return false;
		} else if (!sType.equals(other.sType))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		if (format == null) {
			if (other.format != null)
				return false;
		} else if (!format.equals(other.format))
			return false;
		if (impWorker == null) {
			if (other.impWorker != null)
				return false;
		} else if (!impWorker.equals(other.impWorker))
			return false;
		if (impDate == null) {
			if (other.impDate != null)
				return false;
		} else if (!impDate.equals(other.impDate))
			return false;
		if (urlDb == null) {
			if (other.urlDb != null)
				return false;
		} else if (!urlDb.equals(other.urlDb))
			return false;
		if (urlFile == null) {
			if (other.urlFile != null)
				return false;
		} else if (!urlFile.equals(other.urlFile))
			return false;
		if (memo == null) {
			if (other.memo != null)
				return false;
		} else if (!memo.equals(other.memo))
			return false;
		if (fileContent == null) {
			if (other.fileContent != null)
				return false;
		} else if (!fileContent.equals(other.fileContent))
			return false;
		
		if (updateTime == null) {
			if (other.updateTime != null)
				return false;
		} else if (!updateTime.equals(other.updateTime))
			return false;
		return true;
	}
	
	
	
}
