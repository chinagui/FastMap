package com.navinfo.navicommons.net.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-7-26
 */
public class SSHUtils
{
    private static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SSHUtils.class);
    private static Pattern pattern = Pattern.compile("\\[(\\d+)\\]:");

    public static String sshExecute(String host, String user, String pwd,String command) throws Exception
    {
        return sshExecute(host,user,pwd,22,command);
    }


    /**
     * known_hosts文件可以在linux机器上使用ssh命令连接目标linux，会在原机器的/root/.ssh/下产生known_hosts文件，文件中存放的密钥
     * @param host 机器
     * @param user 用户名
     * @param pwd 密码
     * @param port 端口
     * @param command 命令串
     * @return 执行命令后的输出
     * @throws Exception
     */
    public static String sshExecute(String host, String user, String pwd,int port,String command) throws Exception
    {
        log.debug("=======调用ssh执行［" + command + "]");
        String osName = System.getProperty("os.name");
        StringBuilder sb = new StringBuilder();
        JSch jsch = new JSch();
        log.debug("==========osName" + osName.toUpperCase());
//        if (osName.toUpperCase().indexOf("WINDOWS") > -1)
//        {
//            jsch.setKnownHosts("c:\\known_hosts");
//        } else
//        {
//            jsch.setKnownHosts("/root/.ssh/known_hosts");
//        }
        Session session = null;
        Channel channel = null;
        try {
            session = jsch.getSession(user, host, port);
//            session.setPassword(pwd);
            UserInfo ui = new DefaultUserInfo();
            session.setPassword(pwd);
            session.setUserInfo(ui);
            session.connect();
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            InputStream in = channel.getInputStream();
            channel.connect();
            int nextChar;
            while (true)
            {
                while ((nextChar = in.read()) != -1)
                {
                    sb.append((char) nextChar);
                }
                if (channel.isClosed())
                {
                    //插入退出代码
                    sb.insert(0,"[" + channel.getExitStatus() + "]:");
                    log.debug("退出代码: " + channel.getExitStatus());
                    break;
                }
                try
                {
                    Thread.sleep(500);
                } catch (InterruptedException e)
                {
                    log.error("等待命令执行完成时错误",e);
                    throw new Exception("等待命令执行完成时错误",e);
                }
            }
        } catch (JSchException e)
        {
            log.error("执行ssh命令失败[" + command + "]",e);
            throw new Exception("执行ssh命令失败[" + command + "]",e);
        } catch (IOException e)
        {
            log.error("执行ssh命令失败[" + command + "]",e);
            throw new Exception("执行ssh命令失败[" + command + "]",e);

        } finally
        {
            if(channel != null)
                channel.disconnect();
            if(session != null)
                session.disconnect();
        }
        return sb.toString();

    }

    public static boolean isExitNormal(String out)
    {
        boolean flag = false;
        if(StringUtils.isNotEmpty(out) && out.startsWith("["))
        {
            Matcher matcher = pattern.matcher(out);
            if(matcher.find())
            {
                String status = matcher.group(1);
                if("0".equals(status))
                    flag = true;
            }
        }
        return flag;
    }

    public static class DefaultUserInfo implements UserInfo, UIKeyboardInteractive
    {

        public String getPassphrase() {
            return null;
        }

        public String getPassword() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return false;
        }

        public boolean promptPassword(String message) {
            return false;
        }

        public boolean promptYesNo(String message) {
            return true;//为true是标识为受信任，则不用know hosts文件了，主要是在连接本机时使用know hosts文件有问题
        }

        public void showMessage(String message) {

        }

        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            return null;
        }
    }

    public static void main(String [] args) throws Exception
    {
        //String out = sshExecute("192.168.3.228","root","123456","ls -l");
       // System.out.println(isExitNormal(out));
        //System.out.println(sshExecute("192.168.3.228","root","123456","ls -l"));
        //System.out.println(sshExecute("192.168.3.228","root","**","unzip /home/test/testzip/GearsStudy.zip -d /home/test/testzip/"));
        //System.out.println(sshExecute("192.168.3.228","root","**"," cp -r /home/test/testzip/* /home/test/cptest/"));
        
       String out = SSHUtils.sshExecute(SystemConfigFactory.getSystemConfig().getValue("dms.ftp.host"), "root","123456", "unzip /data/map-data/2010-09-14/6f941e40a79149509b49412ea3ecd5c9/20100914.zip -d /data/map-data/2010-09-14/6f941e40a79149509b49412ea3ecd5c9/");
        System.out.println(out);
    }

}
