package com.navinfo.dataservice.dao.fcc;

import java.util.ArrayList;

import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;
import org.hbase.async.Scanner;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.ByteUtils;

public class HBaseController {

	private ArrayList<KeyValue> getByRowkey(String tableName, String rowkey,
			String famliy, String... qualifiers) throws Exception {

		final GetRequest get = new GetRequest(tableName, rowkey);

		if(famliy != null){
			get.family(famliy);
		}
		
		if(qualifiers.length > 0){
			get.qualifiers(ByteUtils.toBytes(qualifiers));
		}

		ArrayList<KeyValue> list = HBaseConnector.getInstance().getClient()
				.get(get).joinUninterruptibly();

		return list;
	}

	public ArrayList<KeyValue> getTipsByRowkey(String rowkey) throws Exception {

		return getByRowkey(HBaseConstant.tipTab, rowkey, null);
	}

	public ArrayList<ArrayList<KeyValue>> scan(String tableName,
			String startKey, String stopKey, String family,
			String... qualifiers) throws Exception {

		Scanner scanner = HBaseConnector.getInstance().getClient()
				.newScanner(tableName);

		scanner.setStartKey(startKey);

		scanner.setStopKey(stopKey);

		scanner.setFamily(family);

		scanner.setQualifiers(ByteUtils.toBytes(qualifiers));

		ArrayList<ArrayList<KeyValue>> rows;

		ArrayList<ArrayList<KeyValue>> result = new ArrayList<ArrayList<KeyValue>>();

		while ((rows = scanner.nextRows().joinUninterruptibly()) != null) {

			for (ArrayList<KeyValue> list : rows) {
				result.add(list);
			}
		}

		return result;
	}

}
