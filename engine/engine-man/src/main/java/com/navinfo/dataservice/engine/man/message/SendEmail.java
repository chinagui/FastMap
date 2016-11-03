package com.navinfo.dataservice.engine.man.message;

import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.email.SendEmailUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;

/**
 * @author zhangli5174
 * 2016/11/01
 * 发送邮件
 */
public class SendEmail {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
		// 设置服务器
		private static  String VALUE_SMTP = "smtp.qq.com"; //这里用的163 邮箱的,也可以换别的邮箱的
		// 发件人用户名、密码
		//private static String SEND_EMAil = "xxxxxxx@163.com";//必填项 
		//private static String SEND_PWD = "*****";//必填项
		private static String SEND_EMAil = "531476471@qq.com";//必填项 
		private static String SEND_PWD = "hhm712643hsm";//必填项
	
	/**
	 * @Title: sendEmail
	 * @Description: TODO
	 * @param toMail
	 * @param mailTitle
	 * @param mailContent  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月1日 下午3:58:26 
	 */
	public static void sendEmail(String toMail,String mailTitle,String mailContent){
		SendEmailUtil.sendEmail(VALUE_SMTP, SEND_EMAil, SEND_EMAil, SEND_PWD, toMail, mailTitle, mailContent);
	}
	
	public static void main(String[] args) {
		//sendEmail("zhangli5174@navinfo.com","邮件主题","邮件内容3333333333");
		sendEmail("hhmhsm@163.com","邮件主题","邮件内容3333333333");
	}
}
