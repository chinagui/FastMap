package com.navinfo.navicommons.geo.computation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/** 
* @ClassName: DoublePolyline 
* @author Xiao Xiaowen 
* @date 2016年4月29日 上午11:24:17 
* @Description: TODO
*/
public class DoublePolyline {
	private DoubleLine[] lines;
	public DoublePolyline(DoublePoint[] points){
		int lineSize = points.length-1;
		lines = new DoubleLine[lineSize];
		for(int i=0;i<lineSize;i++){
			lines[i]=new DoubleLine(points[i],points[i+1]);
		}
	}
	public DoublePolyline(DoubleLine[] lines){
		this.lines=lines;
	}
	public int getLineSize() {
		return lines==null?0:lines.length;
	}
	public DoublePoint getSpoint() {
		return (lines==null||lines.length==0)?null:lines[0].getSpoint();
	}
	public DoublePoint getEpoint() {
		return (lines==null||lines.length==0)?null:lines[lines.length-1].getEpoint();
	}
	public DoubleLine[] getLines() {
		return lines;
	}
	public void setLines(DoubleLine[] lines) {
		this.lines = lines;
	}
	public DoubleLine getFirstLine(){
		if(lines==null||lines.length==0){
			return null;
		}
		return lines[0];
	}
	public DoubleLine getLastLine(){
		if(lines==null||lines.length==0){
			return null;
		}
		return lines[lines.length-1];
	}
	public void reverse(){
		List<DoubleLine> ls = Arrays.asList(lines);
		Collections.reverse(ls);
		lines = ls.toArray(new DoubleLine[0]);
		for(DoubleLine line:lines){
			line.reverse();
		}
	}
	public boolean extend(DoublePoint point,boolean isTail){
		if(point==null||lines==null||lines.length==0) return false;
		DoubleLine[] newLines = new DoubleLine[lines.length+1];
		int moveIndex=0;
		DoubleLine extendLine = null;
		if(isTail){
			moveIndex = 0;
			extendLine = new DoubleLine(this.getEpoint(),point);
			newLines[lines.length]=extendLine;
		}else{
			moveIndex = 1;
			extendLine = new DoubleLine(point,this.getSpoint());
			newLines[0] = extendLine;
		}
		//数组直接赋值
		int i = 0;
		for(DoubleLine line:lines){
			newLines[i+moveIndex]=line;
			i++;
		}
		lines = newLines;
		return true;
	}
	public String toString(){
		StringBuilder s = new StringBuilder("[");
		s.append(StringUtils.join(lines));
		s.append("]");
		return s.toString();
	}
	public static void main(String[] args){
		Double[] a = new Double[]{new Double(0.0),new Double(1.0)};
		Double[] b = new Double[]{new Double(5.0),new Double(6.0),new Double(7.0)};
		a = b;
		System.out.println(a.length);
	}
}
