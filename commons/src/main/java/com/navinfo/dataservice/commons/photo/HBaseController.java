package com.navinfo.dataservice.commons.photo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.constant.PropConstant;

import net.sf.json.JSONObject;

public class HBaseController {


	
	/**
	 * 获取POI的照片标志
	 * @param objs<pid:{"photo_rowkey":""},pid1:{"photo_rowkey":"10c6cfec60f4400a9e5136f44be0c665,49e25f199f7e4e46961ca19692dc84b8"}>
	 * @return <pid:[0,1]全部,pid1:[0]没有当前版本照片,pid2:[1]仅有当前版本照片>
	 * @throws Exception
	 */
	public Map<Long, Set<Integer>> getPOIPhotosFlag(Map<Long, JSONObject> objs) throws Exception{
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		Table htab = null;
		Map<Long, Set<Integer>> photos = new HashMap<>();
		try{
			String seasonVersion=SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.photoTab));
			for (Long pid : objs.keySet()){
				JSONObject data = objs.get(pid);
				Set<Integer> photoFlag = new HashSet<>();
				if (data != null && data.containsKey("photo_rowkey")){
					String photo_rowkey = data.getString("photo_rowkey");
					if (StringUtils.isNotEmpty(photo_rowkey)){
						String[] rowkeys = photo_rowkey.split(",");
						List<Get> getList=new ArrayList<Get>();
						for (String rowkey : rowkeys){
							Get get = new Get(((String)rowkey).getBytes());
							getList.add(get);
						}
						Result[] rs = htab.get(getList);
						for (Result result : rs) {
							if (result.isEmpty()) {continue;}
							Map<String, Object> photoMap=new HashMap<String, Object>();
							String rowkey = new String(result.getRow());
							photoMap.put("rowkey", rowkey);
							String attribute = new String(result.getValue("data".getBytes(),
									"attribute".getBytes()));
							
							JSONObject attrJson = JSONObject.fromObject(attribute);
							String photo_version = attrJson.getString("a_version") == null?"":attrJson.getString("a_version");
							if(seasonVersion!=null&&seasonVersion.equals(photo_version)){
								photoFlag.add(1);
							}else{
								photoFlag.add(0);
							}
						}
					}
				}
				photos.put(pid, photoFlag);
			}
		}catch (Exception e) {
			throw e;
		}finally {
			if(htab!=null){
				htab.close();
			}
		}
		return photos;
	}
	
}
