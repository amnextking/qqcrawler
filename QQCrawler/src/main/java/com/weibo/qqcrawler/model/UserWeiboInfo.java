package com.weibo.qqcrawler.model;
import java.util.ArrayList;


public class UserWeiboInfo {
	private String UserID;
	private String UserName;
	private String userTitle;
	private String sex;
	private String area;
	private String job;
	private String briefIntro;
	private String tags;
	private Integer followNum;
	private Integer fansNum;
	private Integer BlogNum;
	private String updateTime;
	private ArrayList<MircoBlogInfo> microBlogList;
	public String getUserID() {
		return UserID;
	}
	public void setUserID(String userID) {
		UserID = userID;
	}
	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public String getUserTitle() {
		return userTitle;
	}
	public void setUserTitle(String userTitle) {
		this.userTitle = userTitle;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public String getBriefIntro() {
		return briefIntro;
	}
	public void setBriefIntro(String briefIntro) {
		this.briefIntro = briefIntro;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public Integer getFollowNum() {
		return followNum;
	}
	public void setFollowNum(Integer followNum) {
		this.followNum = followNum;
	}
	public Integer getFansNum() {
		return fansNum;
	}
	public void setFansNum(Integer fansNum) {
		this.fansNum = fansNum;
	}
	public Integer getBlogNum() {
		return BlogNum;
	}
	public void setBlogNum(Integer blogNum) {
		BlogNum = blogNum;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public ArrayList<MircoBlogInfo> getMicroBlogList() {
		return microBlogList;
	}
	public void setMicroBlogList(ArrayList<MircoBlogInfo> microBlogList) {
		this.microBlogList = microBlogList;
	}
	public UserWeiboInfo(){
		UserID = "";
		UserName = null;
		sex = null;
		area = null;
		briefIntro = null;
		tags = null;
		followNum = 0;
		fansNum = 0;
		BlogNum = 0;
		updateTime = null;
		microBlogList = new ArrayList<MircoBlogInfo>();
	}
}
