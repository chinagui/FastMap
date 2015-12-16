package com.navinfo.dataservice.FosEngine.photos;

import com.navinfo.dataservice.FosEngine.comm.db.HBaseAddress;
import com.navinfo.dataservice.FosEngine.comm.db.SolrAddress;

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
	public boolean importPhoto(HBaseAddress ha, String dir, SolrAddress sa);
}
