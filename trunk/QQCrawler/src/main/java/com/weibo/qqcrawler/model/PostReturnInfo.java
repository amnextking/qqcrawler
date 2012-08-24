package com.weibo.qqcrawler.model;

public class PostReturnInfo {
	private int returnCode;
	private String returnMsg;
	public int getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	public String getReturnMsg() {
		return returnMsg;
	}
	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}
	public PostReturnInfo(){
		returnCode = 0;
		returnMsg = "";
	}
}
