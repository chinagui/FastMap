package com.navinfo.dataservice.commons.email;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;

/**
 * @author zhangli5174
 * 2016/11/01
 * 功能描述:邮件发送公共方法
 */
public class SendEmailUtil {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	/**
	 * @Title: sendEmail
	 * @Description: TODO
	 * @param valueSmtp
	 * @param fromMail
	 * @param user
	 * @param password
	 * @param toMail
	 * @param mailTitle
	 * @param mailContent  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月1日 下午3:14:41 
	 */
	public static void sendEmail(String valueSmtp,String fromMail, final String user, final String password,  
            String toMail,  
            String mailTitle,  
            String mailContent){
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", valueSmtp);
		props.put("mail.smtp.auth", "true");
	
		Session s =  Session.getDefaultInstance(props, new Authenticator(){
		      protected PasswordAuthentication getPasswordAuthentication() {
		          return new PasswordAuthentication(user, password);
		      }});
		s.setDebug(true);
		MimeMessage message = new MimeMessage(s);
		try {
			// 发件人
			InternetAddress from = new InternetAddress(user);
			message.setFrom(from);
			// 收件人
			InternetAddress to = new InternetAddress(toMail);
			message.setRecipient(Message.RecipientType.TO, to);
			// 邮件标题
			message.setSubject(mailTitle);
			//String content = mailTitle.toString();
			// 邮件内容,也可以使纯文本"text/plain"
			message.setContent(mailContent, "text/html;charset=GBK");
			message.saveChanges();
			Transport transport = s.getTransport("smtp");
			// smtp验证，就是你用来发邮件的邮箱用户名密码
			transport.connect(valueSmtp, user, password);
			// 发送
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			System.out.println("send success!");
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}
}
