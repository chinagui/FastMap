package com.navinfo.dataservice.impcore.flushbylog;

import java.util.ArrayList;
import java.util.List;

public class FlushResult {

	private int total;
	
	private int updateTotal;
	
	private int deleteTotal;
	
	private int insertTotal;
	
	private int failedTotal;
	
	private int updateFailed;
	
	private int deleteFailed;
	
	private int insertFailed;
	
	private int logMoved;
	
	private List<String> insertFailedList = new ArrayList<String>();
	
	private List<String> updateFailedList = new ArrayList<String>();
	
	private List<String> deleteFailedList = new ArrayList<String>();
	
	public int getLogMoved() {
		return logMoved;
	}

	public void setLogMoved(int logMoved) {
		this.logMoved = logMoved;
	}

	public void addInsertFailedRowId(String rowId){
		insertFailedList.add(rowId);
	}
	
	public void addDeleteFailedRowId(String rowId){
		deleteFailedList.add(rowId);
	}
	
	public void addUpdateFailedRowId(String rowId){
		updateFailedList.add(rowId);
	}
	
	public int getFailedTotal() {
		return failedTotal;
	}
	public void setFailedTotal(int failedTotal) {
		this.failedTotal = failedTotal;
	}
	public List<String> getInsertFailedList() {
		return insertFailedList;
	}
	public void setInsertFailedList(List<String> insertFailedList) {
		this.insertFailedList = insertFailedList;
	}
	public List<String> getUpdateFailedList() {
		return updateFailedList;
	}
	public void setUpdateFailedList(List<String> updateFailedList) {
		this.updateFailedList = updateFailedList;
	}
	public List<String> getDeleteFailedList() {
		return deleteFailedList;
	}
	public void setDeleteFailedList(List<String> deleteFailedList) {
		this.deleteFailedList = deleteFailedList;
	}
	
	public void addTotal(){
		total+=1;
	}
	
	public void addUpdateTotal(){
		updateTotal+=1;
	}
	public void addDeleteTotal(){
		deleteTotal+=1;
	}
	public void addInsertTotal(){
		insertTotal+=1;
	}
	public void addUpdateFailed(){
		updateFailed+=1;
	}
	public void addDeleteFailed(){
		deleteFailed+=1;
	}
	public void addInsertFailed(){
		insertFailed+=1;
	}
	
	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getUpdateTotal() {
		return updateTotal;
	}

	public void setUpdateTotal(int updateTotal) {
		this.updateTotal = updateTotal;
	}

	public int getDeleteTotal() {
		return deleteTotal;
	}

	public void setDeleteTotal(int deleteTotal) {
		this.deleteTotal = deleteTotal;
	}

	public int getInsertTotal() {
		return insertTotal;
	}

	public void setInsertTotal(int insertTotal) {
		this.insertTotal = insertTotal;
	}

	public int getUpdateFailed() {
		return updateFailed;
	}

	public void setUpdateFailed(int updateFailed) {
		this.updateFailed = updateFailed;
	}

	public int getDeleteFailed() {
		return deleteFailed;
	}

	public void setDeleteFailed(int deleteFailed) {
		this.deleteFailed = deleteFailed;
	}

	public int getInsertFailed() {
		return insertFailed;
	}

	public void setInsertFailed(int insertFailed) {
		this.insertFailed = insertFailed;
	}
	
}
