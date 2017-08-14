package com.navinfo.dataservice.engine.editplus.diff;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author jicaihua
 * 字符串操作工具
 */
public class StringUtil {
	public static String standardLeft(String content, String c, long length) {
		while (content.length() < length) {
			content = c + content;
		}
		return content;
	}
	
	public static List<String> split(String s,String t) {
		 List<String>  list=new ArrayList<String>();
		 if(s==null)
		 return list;
		 if(s.length()==0)
		 {list.add(""); return list;}
	     while(s.length()>=0)
	     {
	         int post=s.indexOf(t);
	         if(post==-1)
	         {
	        	 list.add(s); break;
	         }
	         else
	         {
	        	 list.add(s.substring(0, s.indexOf(t))); 
	        	 s=s.substring(s.indexOf(t)+t.length(), s.length());
	         }
	     }
		 return list;
	}
	
	/**
	 * 格式化联系方式
	 * @param contact
	 * @return
	 */
	public static String contactFormat(String contact){
		if(!"".equals(contact) && null != contact){
			StringBuffer str = new StringBuffer();
			String[] split = contact.split("\\|");
			for(String temp :split){
				if(temp.indexOf("-") == -1 && temp.length() == 11){//手机
					temp = "0086-"+temp+"-";
				}else{//电话
					if(temp.indexOf("-") == -1){
						temp = "-"+temp+"-";
					}else{
						if(temp.split("-").length < 3){
							if((temp.substring(0, temp.indexOf("-")).length())>5){
								temp = "-"+temp;
							}else{
								temp = temp+"-";
							}
						}
						
					}
				}
				str.append(temp).append(";");
			}
			return str.toString().substring(0,str.toString().length()-1);
		}
		return "";
	}
	
	public static void main(String[] args){
//		System.out.println(contactFormat("110;010-87919466-00;11111"));;
//		System.out.println("110;010-87919466-00;11111".replaceAll("\\D", ""));;
		
		System.out.println(sortPhone("13910101000;+86 13910101000;010-87919466;88919466;(010)88919466;13810101000;+86 13810101001"));;
		System.out.println(sortPhone("010-87919466;(010)88919466;13910101000;+86 13910101000;13810101000;88919466;+86 13810101001"));;
		System.out.println(sortPhone("010-87919466;88919466;+86 13910101000;13810101000;(010)88919466;13910101000;+86 13810101001"));;
		System.out.println(sortPhone("(010)88919466;13910101000;010-87919466;88919466;+86 13910101000;13810101000;+86 13810101001"));;
		System.out.println(sortPhone("+86 13910101000;13810101000;010-87919466;88919466;(010)88919466;13910101000;+86 13810101001"));;
	}
	
	public static void w(String file, String content) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true))); 
			out.write(content);
			out.write("\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				out.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
	
	public static String formatPhone(String phone){
		phone = phone.replace("+86", "");
		phone = phone.replace("|", ";");
		return phone;
		
	}
	
	/**
	 * 差分代理店数据时，对电话做排序处理
	 * @param phone
	 * @return
	 */
	public static String sortPhone(String phone){ 
		String phones[] = phone.split(";");
		//去掉+86、括号等非数字字符
		for(int i = 0; i <phones.length; i++){
			phones[i] = formatPhone(phones[i]);
		}
		
		for(int i = 0; i < phones.length; i ++){
			for(int j = i+1; j< phones.length; j ++){
				if(phones[i].compareTo(phones[j]) > 0){
					String temp = phones[i];
					phones[i] = phones[j];
					phones[j] = temp;
				}
			}
		}
		
		StringBuffer str= new StringBuffer();
		for(int i = 0;i < phones.length; i++){
			str.append(String.valueOf(phones[i])).append(";");
		}
		
		return str.toString();
	}
	
	
}
