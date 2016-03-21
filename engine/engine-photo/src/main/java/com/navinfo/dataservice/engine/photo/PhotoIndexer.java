package com.navinfo.dataservice.engine.photo;

import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.db.SolrAddress;

/**
 * 照片库索引
 */
public class PhotoIndexer {

	public boolean rebuildIndex(HBaseAddress ha, SolrAddress sa, String uuid) {
		return false;
	}
}
