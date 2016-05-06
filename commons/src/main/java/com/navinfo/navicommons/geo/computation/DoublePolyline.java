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
	private int lineSize = 0;
	private DoublePoint spoint;
	private DoublePoint epoint;
	private DoubleLine[] lines;
	public DoublePolyline(DoublePoint[] points){
		lineSize = points.length-1;
		spoint = points[0];
		epoint = points[lineSize];
		lines = new DoubleLine[lineSize];
		for(int i=0;i<lineSize;i++){
			lines[i]=new DoubleLine(points[i],points[i+1]);
		}
	}
	public DoublePolyline(DoubleLine[] lines){
		lineSize = lines.length;
		this.spoint=lines[0].getSpoint();
		this.epoint=lines[lineSize-1].getEpoint();
		this.lines=lines;
	}
	public int getLineSize() {
		return lineSize;
	}
	public void setLineSize(int lineSize) {
		this.lineSize = lineSize;
	}
	public DoublePoint getSpoint() {
		return spoint;
	}
	public void setSpoint(DoublePoint spoint) {
		this.spoint = spoint;
	}
	public DoublePoint getEpoint() {
		return epoint;
	}
	public void setEpoint(DoublePoint epoint) {
		this.epoint = epoint;
	}
	public DoubleLine[] getLines() {
		return lines;
	}
	public void setLines(DoubleLine[] lines) {
		this.lines = lines;
	}
	public DoubleLine getFirstLine(){
		if(lines!=null&&lineSize>0){
			return lines[0];
		}
		return null;
	}
	public DoubleLine getLastLine(){
		if(lines!=null&&lineSize>0){
			return lines[lineSize-1];
		}
		return null;
	}
	public void reverse(){
		epoint = spoint;
		List<DoubleLine> ls = Arrays.asList(lines);
		Collections.reverse(ls);
		lines = ls.toArray(new DoubleLine[0]);
		for(DoubleLine line:lines){
			line.reverse();
		}
		spoint = lines[0].getSpoint();
	}
	public String toString(){
		StringBuilder s = new StringBuilder("[");
		s.append(StringUtils.join(lines));
		s.append("]");
		return s.toString();
	}
}
