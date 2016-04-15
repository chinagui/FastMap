package com.navinfo.dataservice.expcore.source.parameter;

public class SampleCoordinate {
	private Double startCoordinateX;
	private Double endCoordinateX;
	private Double startCoordinateY;
	private Double endCoordinateY;
	public Double getStartCoordinateX() {
		return startCoordinateX;
	}
	public void setStartCoordinateX(Double startCoordinateX) {
		this.startCoordinateX = startCoordinateX;
	}
	public Double getEndCoordinateX() {
		return endCoordinateX;
	}
	public void setEndCoordinateX(Double endCoordinateX) {
		this.endCoordinateX = endCoordinateX;
	}
	public Double getStartCoordinateY() {
		return startCoordinateY;
	}
	public void setStartCoordinateY(Double startCoordinateY) {
		this.startCoordinateY = startCoordinateY;
	}
	public Double getEndCoordinateY() {
		return endCoordinateY;
	}
	public void setEndCoordinateY(Double endCoordinateY) {
		this.endCoordinateY = endCoordinateY;
	}
	/**
	 * X,Y两者都有相交部分视为有重合
	 * X：两矩形的中心在X轴方向的距离和矩形的边长/2和比较，小于则相交
	 * @param tarCoor
	 * @return
	 */
	public boolean isIntersect(SampleCoordinate tarCoor){
		boolean isXIntersect=false;
		boolean isYIntersect=false;
		double centerDistanceX = Math.abs(((this.endCoordinateX+this.startCoordinateX)/2)-((tarCoor.endCoordinateX+tarCoor.startCoordinateX)/2));
		if(centerDistanceX<((this.endCoordinateX-this.startCoordinateX)/2+(tarCoor.endCoordinateX-tarCoor.startCoordinateX)/2)){
			isXIntersect=true;
		}
		double centerDistanceY = Math.abs(((this.endCoordinateY+this.startCoordinateY)/2)-((tarCoor.endCoordinateY+tarCoor.startCoordinateY)/2));
		if(centerDistanceY<((this.endCoordinateY-this.startCoordinateY)/2+(tarCoor.endCoordinateY-tarCoor.startCoordinateY)/2)){
			isYIntersect=true;
		}
		if(isXIntersect&&isYIntersect){
			return true;
		}
		return false;
	}
}
