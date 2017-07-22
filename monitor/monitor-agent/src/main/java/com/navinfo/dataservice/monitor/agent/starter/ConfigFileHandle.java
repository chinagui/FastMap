package com.navinfo.dataservice.monitor.agent.starter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.dataservice.commons.exception.ConfigParseException;
import com.navinfo.dataservice.commons.log.LoggerRepos;

/**
 * 处理推送及监控服务器
 * 
 * @ClassName ConfigFileHandle
 * @author Han Shaoming
 * @date 2017年7月9日 下午7:53:01
 * @Description TODO
 */
public class ConfigFileHandle {

	protected static Logger log = LoggerRepos.getLogger(ConfigFileHandle.class);
	// 存取推送服务地址
	private static Map<String, String> agentServer = new HashMap<String, String>();
	// 存取推送服务地址
	private static List<Map<String, String>> monitorServer = new ArrayList<Map<String, String>>();

	public static Map<String, String> getAgentServer() {
		return agentServer;
	}

	public static List<Map<String, String>> getMonitorServer() {
		return monitorServer;
	}

	/**
	 * 解析配置文件
	 * 
	 * @author Han Shaoming
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void handleConfigFile() throws IOException {
		log.info("load task config start...");
		// URL xmlpath = XmlReader.class.getClassLoader().getResource("");
		// System.out.println(xmlpath);
		String configFile = "/monitor-config.xml";

		// 加载管理库的信息
		log.info("parse file " + configFile);
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
			if (in == null) {
				in = ConfigFileHandle.class.getResourceAsStream(configFile);
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(in);
			Element root = document.getRootElement();
			// 推送数据服务器
			Element agent = root.element("agent");
			String agentHost = agent.elementText("host");
			String agentPort = agent.elementText("port");
			agentServer.put("host", agentHost);
			agentServer.put("port", agentPort);
			log.info("推送数据服务器,host="+agentHost+",port="+agentPort);
			// 监控服务器
			Element monitors = root.element("monitors");
			List<Element> elements = monitors.elements();
			for (int i = 0; i < elements.size(); i++) {
				Map<String, String> map = new HashMap<String, String>();
				Element element = elements.get(i);

				String host = element.elementText("host");
				String port = element.elementText("port");
				String name = element.elementText("name");
				map.put("host", host);
				map.put("port", port);
				map.put("name", name);
				monitorServer.add(map);
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
            throw new ConfigParseException("读取monitor映射文件" + configFile + "错误", e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
	}

//	public static void main(String[] args) {
//		try {
//			handleConfigFile();
//			System.out.println(agentServer.toString());
//			System.out.println(monitorServer.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
