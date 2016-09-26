package com.navinfo.dataservice.engine.sys.msg;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.navicommons.database.Page;

public class SysMsgTest {
	
	@Test
	public void sysMsgInsert(){
		SysMsg sysMsg = new SysMsg();
		sysMsg.setMsgTitle("测试9");
		sysMsg.setMsgContent("测试9");
		sysMsg.setPushUserId(1664L);
		long[] targetUserIds = {1,4};
		try {
			SysMsgPublisher.publishMsg(sysMsg.getMsgTitle(), sysMsg.getMsgContent(), sysMsg.getPushUserId(), targetUserIds);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}
	
	@Test
	public void getUnReadSysMsg(){
		SysMsgService sysMsgService = SysMsgService.getInstance();
		long userId = 1L;
		try {
			List<SysMsg> unread = sysMsgService.getUnread(userId);
			System.out.println(unread);
			System.out.println(unread.size());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}
	
	@Test
	public void getReadSysMsg(){
		SysMsgService sysMsgService = SysMsgService.getInstance();
		long userId = 3655L;
		int pageNum = 1;
		int pageSize = 20;
		try {
			Page page = sysMsgService.getReadMsg(userId, pageNum, pageSize);
			System.out.println(page.getResult());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}
	
	@Test
	public void checkSysMsg(){
		SysMsgService sysMsgService = SysMsgService.getInstance();
		long userId = 1L;
		long msgId = 62L;
		try {
			sysMsgService.updateMsgStatusToRead(msgId, userId);
			} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}
	
	@Test
	public void deleteSysMsg(){
		SysMsgService sysMsgService = SysMsgService.getInstance();
		long userId = 1L;
		long msgId = 62L;
		try {
			sysMsgService.deleteMsg(msgId, userId);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}

}
