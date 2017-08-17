package com.navinfo.dataservice.service.monitor.service;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import com.navinfo.dataservice.commons.log.LoggerRepos;


/**
 * 
 * @ClassName MonitorService
 * @author Han Shaoming
 * @date 2017年7月14日 下午7:44:57
 * @Description TODO
 */
public class MonitorService {

	 protected Logger log = LoggerRepos.getLogger(this.getClass());

	private volatile static MonitorService instance;

	public static MonitorService getInstance() {
		if (instance == null) {
			synchronized (MonitorService.class) {
				if (instance == null) {
					instance = new MonitorService();
				}
			}
		}
		return instance;
	}

	private MonitorService() {
	}

	/**
	 * 处理监控日志
	 * 
	 * @author Han Shaoming
	 * @param logId
	 * @return
	 * @throws Exception 
	 */
	public String handleLog(String logId) throws Exception {
		String result = null;
		if (logId.contains("_")) {
			result = this.handleEsLog(logId);
		} else {
			result = this.handleJobserverLog(logId);
		}
		return result;
	}

	/**
	 * 处理jobserver日志
	 * 
	 * @author Han Shaoming
	 * @param logId
	 * @return
	 */
	private String handleJobserverLog(String logId) {
		StringBuilder result = new StringBuilder();
//		String filePath = "D:/temp/data/resources/job-server/logs";
		String filePath = "/app/trunk/svr/job-server/logs";
		File file = new File(filePath);
		File[] listFiles = file.listFiles();
		if (listFiles == null || listFiles.length == 0) {
			return "logId为("+logId+")的日志不存在!";
		}
		for (File logFile : listFiles) {
			Scanner lines = null;
			try {
				if (!logFile.exists()) {
					log.info("路径为:" + logFile + ",文件目录不存在");
//					System.out.println("路径为:" + logFile + ",文件目录不存在");
					continue;
				}
				// 判断文件类型
				if (logFile.isFile()) {
					String fileName = logFile.getName();
					String id = fileName.split("-")[0];
					if (!id.equals(logId)) {
						continue;
					}
					// 获取转换数据
					lines = new Scanner(new FileInputStream(logFile));
					while (lines.hasNextLine()) {
						String line = lines.nextLine();
						result.append(line);
						result.append("<br/>");
					}
				}
			} catch (Exception e) {
//				e.printStackTrace();
				log.error("查询Jobserver日志报错:"+e.getMessage(), e);
			} finally {
				try {
					if (lines != null) {
						lines.close();
					}
				} catch (Exception e2) {
//					e2.printStackTrace();
					log.error(e2.getMessage(), e2);
				}
			}
		}
		if(result.length() == 0){
			result.append("logId为("+logId+")的日志不存在!");
		}
		return result.toString();
	}

	/**
	 * 处理elasticsearch插件日志
	 * 
	 * @author Han Shaoming
	 * @param logId
	 * @return
	 * @throws Exception
	 */
	public String handleEsLog(String logId) throws Exception {
		TransportClient client = null;
		try {
			// 通过setting对象指定集群配置信息, 配置的集群名
			Settings settings = Settings.settingsBuilder().put("cluster.name", "es_cluster") // 设置集群名
					.put("client.transport.sniff", true) // 开启嗅探 , 开启后会一直连接不上,
					.build();
			client = TransportClient.builder().settings(settings).build()
					.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress("192.168.4.188", 8041)));
			System.out.println("success connect");
			// 设置查询条件
			QueryBuilder query = new TermQueryBuilder("message", logId);
			// 不填索引名称,表示查询全部的
			SearchRequestBuilder search = client.prepareSearch();
			search.setTypes("log").setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
			// 排序
			search.addSort("@timestamp", SortOrder.ASC);
			// 分页,如果不分页,默认显示10个
			search.setFrom(0).setSize(10000).setExplain(true);
			search.setQuery(query);
			SearchResponse response = search.execute().actionGet();
			SearchHits hits = response.getHits();
			System.out.println("============================查询文档个数:" + hits.totalHits());
			StringBuilder data = new StringBuilder();
			data.append("<h1 align=\"center\">====================记录总数:"+hits.totalHits()+"====================");
			data.append("</h1>");
			for (SearchHit searchHit : hits) {
				Map<String, Object> map = searchHit.getSource();
				String message = (String) map.get("message");
				data.append(message);
				data.append("<br/>");
			}
			if(data.length() == 0){
				data.append("logId为("+logId+")的日志不存在!");
			}
			return data.toString();
		} catch (Exception e) {
			log.error("查询elasticsearch出错:" + e.getMessage());
			throw new Exception("查询elasticsearch出错:" + e.getMessage(), e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

//	public static void main(String[] args) throws Exception {
////		String data = MonitorService.getInstance().handleJobserverLog("11775400_49_698");
//		String data = MonitorService.getInstance().handleEsLog("11775400_49_698");
//		if (data == null || "".equals(data)) {
//			System.out.println("未找到相应的jobserver日志");
//		}
//		System.out.println(data);
//	}
}
