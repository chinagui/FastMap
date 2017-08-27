package com.navinfo.dataservice.engine.es;

import com.navinfo.dataservice.commons.constant.EsConstant;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import java.io.IOException;
import java.net.InetAddress;

public class EsConnector {

	private static class SingletonHolder {
		private static final EsConnector INSTANCE = new EsConnector();
	}

	public static final EsConnector getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private TransportClient client;
	
	public TransportClient getClient() throws IOException {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					Settings settings = Settings.builder().put("cluster.name", EsConstant.cluster).build();
					client = new PreBuiltTransportClient(settings)
							.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(EsConstant.master), EsConstant.port));
				}
			}
		}
		return client;
	}
}
