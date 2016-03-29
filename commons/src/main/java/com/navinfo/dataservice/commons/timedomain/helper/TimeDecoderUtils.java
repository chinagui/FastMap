package com.navinfo.dataservice.commons.timedomain.helper;

import org.apache.commons.math3.util.Pair;

public class TimeDecoderUtils {

	public static int getPairCharPositon(String strAim, char lc, char rc,
			int startIndex, int nDirection) {
		if (strAim.isEmpty())
			return -1;
		if (startIndex >= strAim.length())
			return -1;
		char ac = 0;
		int sum = 0;
		if (nDirection >= 0) {
			for (int i = startIndex; i < strAim.length(); i++) {
				ac = strAim.charAt(i);
				if (ac == lc)
					sum++;
				if (ac == rc)
					sum--;
				if (sum < 0)
					return -1;
				if (sum == 0)
					return i;
			}
		} else {
			for (int i = startIndex; i > 0; i--) {
				ac = strAim.charAt(i);
				if (ac == rc)
					sum++;
				if (ac == lc)
					sum--;
				if (sum < 0)
					return -1;
				if (sum == 0)
					return i;
			}
		}
		return -1;
	}

	public static boolean isNeedDelete(String sInput) {
		if (sInput.isEmpty() || sInput.length() < 4)
			return false;

		if (sInput.startsWith("[") && sInput.charAt(1) == '['
				&& sInput.endsWith("]"))
			return true;
		return false;
	}

	public static String getSubStr(String sInput, int nBeg, int nEnd) {
		String s = "";
		if(sInput.length() < nBeg || sInput.length() < nEnd)
			return s;
		for(int i = nBeg; i < nEnd; i++){
			s += sInput.charAt(i);
		}

		return s;
	}
	
	public static boolean validPairChar(String strValided,char lc,char rc)
	{
	    if (strValided.isEmpty()) return false;
	    int sum=0;
	    char ac=0;
	    for(int i=0; i<strValided.length(); i++)
	    {
	    	ac=strValided.charAt(i);
	    	if(ac==lc)  sum++;
	    	if(ac==rc)  sum--;
	    	if (sum<0) return false;
	    }
	    return sum==0;
	}
	
	public static Pair<Integer,Integer> getIntersection(Pair<Integer,Integer> fp, Pair<Integer,Integer> sp)
	{
		if(fp.getValue() == 0 && fp.getKey() == 0){
			return sp;
		}
		if(sp.getKey() == 0 && sp.getValue() == 0){
			return fp;
		}

		if(fp.getValue() < sp.getKey() || sp.getValue() < fp.getKey()){
			return new Pair<Integer,Integer>(0,0);
		}else if(fp.getKey() <= sp.getKey() && fp.getValue() <= sp.getValue()){
			return new Pair<Integer,Integer>(sp.getKey(),fp.getValue());
		}else if(fp.getKey() <= sp.getKey() && fp.getValue() >= sp.getValue() ){
			return sp;
		}else if(sp.getKey() <= fp.getKey() && sp.getValue() <= fp.getValue()){
			return new Pair<Integer,Integer>(fp.getKey(),sp.getValue());
		}else if(sp.getKey() <= fp.getKey() && sp.getValue() >= fp.getValue()){
			return fp;
		}
		return fp;
	}
}
