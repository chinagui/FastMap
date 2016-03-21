package com.navinfo.dataservice.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.navinfo.dataservice.commons.db.ConfigLoader;

@WebListener
public class StartupListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {

		String path = arg0.getServletContext().getRealPath(
				"/WEB-INF/classes/config.properties");
		
		ConfigLoader.initDBConn(path);
		
		System.out.println("===============CONFIG LOADED==================");
		
	}

}
