package com.navinfo.dataservice.engine.photo;


/**
 * 照片导入基类
 */
public interface IPhotoImporter {

	/**
	 * 导入照片
	 * 
	 * @param ha
	 *            HBase连接
	 * @param dir
	 *            目录
	 * @param sa
	 *            Solr连接
	 * @return True 成功
	 */
	public boolean importPhoto(String dir);
}
