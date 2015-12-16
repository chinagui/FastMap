package com.navinfo.dataservice.FosEngine.photos;

import com.navinfo.dataservice.FosEngine.comm.db.HBaseAddress;
import com.navinfo.dataservice.FosEngine.comm.db.SolrAddress;

/**
 * 照片库索引
 */
public class PhotoIndexer {

	public boolean rebuildIndex(HBaseAddress ha, SolrAddress sa, String uuid) {
		return false;
	}
}
