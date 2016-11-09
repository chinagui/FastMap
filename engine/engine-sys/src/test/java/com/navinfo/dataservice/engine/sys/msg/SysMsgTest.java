package com.navinfo.dataservice.engine.sys.msg;

import java.util.List;

import org.junit.Test;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.navicommons.database.Page;

public class SysMsgTest {
	
	@Test
	public void sysMsgInsert(){
		SysMsg sysMsg = new SysMsg();
		sysMsg.setMsgTitle("测试00AA00");
		sysMsg.setMsgContent("测试00AA00");
		sysMsg.setPushUserId(0L);
		long[] targetUserIds = {2,1672};
		try {
			SysMsgPublisher.publishMsg(sysMsg.getMsgTitle(), sysMsg.getMsgContent(), sysMsg.getPushUserId(), targetUserIds, 1, null, "测试00AA00");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}
	
	@Test
	public void getUnReadSysMsg(){
		SysMsgService sysMsgService = SysMsgService.getInstance();
		long userId = 1664L;
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
