package com.weibo.qqcrawler.model;


public class MircoBlogInfo {
	private String weiboID;
	private String selfContent;
	private String forWardContent;
	private Integer forwardNum;
	private Integer commentNum;
	private String timestamp;
	public String getWeiboID() {
		return weiboID;
	}
	public void setWeiboID(String weiboID) {
		this.weiboID = weiboID;
	}
	public String getSelfContent() {
		return selfContent;
	}
	public void setSelfContent(String selfContent) {
		this.selfContent = selfContent;
	}
	public String getForWardContent() {
		return forWardContent;
	}
	public void setForWardContent(String forWardContent) {
		this.forWardContent = forWardContent;
	}
	public Integer getForwardNum() {
		return forwardNum;
	}
	public void setForwardNum(Integer forwardNum) {
		this.forwardNum = forwardNum;
	}
	public Integer getCommentNum() {
		return commentNum;
	}
	public void setCommentNum(Integer commentNum) {
		this.commentNum = commentNum;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public MircoBlogInfo(){
		weiboID = "";
		selfContent = "";
		forWardContent = "";
		forwardNum = 0;
		commentNum = 0;
		timestamp = "";
	}
}
