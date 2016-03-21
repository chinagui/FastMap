package com.navinfo.dataservice.FosEngine.edit.display;

import java.util.ArrayList;
import java.util.List;

import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.db.HBaseAddress;

/**
 * 瓦片的查询类
 */
public class TileSelector {

	/**
	 * 获取某瓦片内的数据
	 * 
	 * @param x
	 *            瓦片x
	 * @param y
	 *            瓦片y
	 * @param z
	 *            瓦片等级
	 * @return 瓦片数据列表
	 * @throws Exception
	 */
	public static List<String> getTiles(int x, int y, int z, int projectId) throws Exception {

		List<String> listResult = new ArrayList<String>();

		String key = String.format("%02d", z) + String.format("%08d", x)
				+ String.format("%07d", y);

		final GetRequest get = new GetRequest("linkTile_"+projectId, key);

		ArrayList<KeyValue> list = HBaseAddress.getHBaseClient().get(get)
				.joinUninterruptibly();

		for (KeyValue kv : list) {
			listResult.add(new String(kv.value()));
		}

		return listResult;
	}
}
