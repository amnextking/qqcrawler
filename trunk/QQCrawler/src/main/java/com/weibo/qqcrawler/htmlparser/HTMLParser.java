package com.weibo.qqcrawler.htmlparser;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.weibo.qqcrawler.model.MircoBlogInfo;


public class HTMLParser {
	public DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String htmlTagsFilter(String str){
		Pattern p1 = Pattern.compile("(<.*?>)");
		Matcher m1 = p1.matcher(str); 
		return m1.replaceAll("");
	}

	public String getUserID(StringBuilder urlContent) throws ParserException{
		Pattern pattern = Pattern.compile("<div\\sclass=\"custjWrap\".*?account\\s=\\s'(.*?)'",Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);		
		if(matcher.find()){
			String userID = matcher.group(1);
			return userID;
		}else{
			pattern = Pattern.compile("<div\\sclass=\"clubInfo\">.*?href=\"/(.*?)\"",Pattern.DOTALL);
			matcher = pattern.matcher(urlContent);	
			if(matcher.find()){
				String userID = matcher.group(1);
				return userID;
			}else{			
				throw new ParserException("parse UserID failed!");
			}
		}		
	}

	public String getUserName(StringBuilder urlContent) throws ParserException{
		Pattern pattern = Pattern.compile("<div\\sclass=\"custjWrap\".*?nick\\s=\\s'(.*?)'",Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);
		if(matcher.find()){
			String userName = matcher.group(1);
			return userName;
		}else{
			pattern = Pattern.compile("<div\\sclass=\"clubInfo\">.*?title=\"(.*?)\\(",Pattern.DOTALL);
			matcher = pattern.matcher(urlContent);	
			if(matcher.find()){
				String userName = matcher.group(1);
				return userName;
			}else{		
				throw new ParserException("parse UserName failed!");
			}
		}		
	}

	public String getUserTitle(StringBuilder urlContent){
		if(urlContent.indexOf("class=\"clubInfo\"") >= 0)
			return "腾讯机构认证";
		
		int beginIndex = urlContent.indexOf("<span class=\"text_user\">");
		int endIndex = urlContent.indexOf("<em>", beginIndex);
		String subContent = urlContent.substring(beginIndex, endIndex);

		Pattern pattern = Pattern.compile("<span\\sclass=\"text_user\">.*?<a.*?title=\"(.*?)\"\\starget=\"_blank\"",Pattern.DOTALL);
		Matcher matcher = pattern.matcher(subContent);
		if(matcher.find()){
			String userTitle = matcher.group(1);
			return userTitle;
		}else{
			return "普通用户";
		}		
	}

	public String getUserGender(StringBuilder urlContent){
		Pattern pattern = Pattern.compile("<div\\sclass=\"custjWrap\".*?gender\\s=\\s'(.*?)'",Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);
		if(matcher.find()){
			String userGender = matcher.group(1);
			return userGender;
		}else{
			return "它";
		}		
	}

	public String getUserArea(StringBuilder urlContent){
		Pattern pattern = Pattern.compile("<span\\sclass=\"info\">.*?<a.*?>(.*?)</a>",Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);
		if(matcher.find()){
			String userArea = matcher.group(1);
			return userArea;
		}else{
			return "";
		}
	}

	public String getUserJob(StringBuilder urlContent){
		Pattern p1 = Pattern.compile("<p\\sclass=\"desc\">(.*?)</span>",Pattern.DOTALL);
		Matcher m1 = p1.matcher(urlContent);
		if(m1.find()){
			String subContent = m1.group(1);
			Pattern pattern = Pattern.compile("<span\\sclass=\"info\">.*?<a.*?>.*?</a>.*?<a.*?>(.*?)</a>",Pattern.DOTALL);
			Matcher matcher = pattern.matcher(subContent);
			if(matcher.find()){
				String userJob = matcher.group(1);
				return userJob;
			}else{
				return "";
			}
		}else{
			return "";
		}
	}

	public String getBriefIntro(StringBuilder urlContent){
		Pattern pattern = Pattern.compile("<span\\sclass=\"ffsong\">“</span>(.*?)<span\\sclass=\"ffsong\">”</span>",Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);
		if(matcher.find()){
			String userBriefIntro = matcher.group(1);
			userBriefIntro = userBriefIntro.trim();
			return userBriefIntro;
		}else{
			pattern = Pattern.compile("<p\\sclass=\"clubInfo-con\\sinfo-color\">(.*?)</p>",Pattern.DOTALL);
			matcher = pattern.matcher(urlContent);
			if(matcher.find()){
				String userBriefIntro = matcher.group(1);
				userBriefIntro = htmlTagsFilter(userBriefIntro.trim());
				return userBriefIntro;
			}else{
				return "";
			}
		}
	}

	public Integer getFollowNum(StringBuilder urlContent) throws ParserException{
		Pattern pattern = Pattern.compile("class=\"text_atr\">听众.*?<a.*?>([0-9]*?)</a>");
		Matcher matcher = pattern.matcher(urlContent);

		if(matcher.find()){
			Integer followNum = Integer.parseInt(matcher.group(1));
			return followNum;
		}else{
			pattern = Pattern.compile("<li\\sclass=\"title-color\">听众.*?<a.*?>([0-9]*?)</a>",Pattern.DOTALL);
			matcher = pattern.matcher(urlContent);
			if(matcher.find()){
				Integer followNum = Integer.parseInt(matcher.group(1));
				return followNum;
			}else{
				throw new ParserException("parse user follow number failed!");
			}
		}
	}

	public Integer getFansNum(StringBuilder urlContent) throws ParserException{
		Pattern pattern = Pattern.compile("class=\"text_atr\">收听.*?<a.*?>([0-9]*?)</a>");
		Matcher matcher = pattern.matcher(urlContent);

		if(matcher.find()){
			Integer fansNum = Integer.parseInt(matcher.group(1));
			return fansNum;
		}else{
			pattern = Pattern.compile("<li\\sclass=\"title-color\">收听.*?<a.*?>([0-9]*?)</a>",Pattern.DOTALL);
			matcher = pattern.matcher(urlContent);
			if(matcher.find()){
				Integer fansNum = Integer.parseInt(matcher.group(1));
				return fansNum;
			}else{
				throw new ParserException("parse user fans number failed!");
			}
		}
	}

	public Integer getWeiboNum(StringBuilder urlContent) throws ParserException, IOException{
		Pattern pattern = Pattern.compile("class=\"text_atr\">广播.*?<a.*?>([0-9]*?)</a>");
		Matcher matcher = pattern.matcher(urlContent);

		if(matcher.find()){
			Integer weiboNum = Integer.parseInt(matcher.group(1));
			return weiboNum;
		}else{
			pattern = Pattern.compile("<li\\sclass=\"title-color\">广播.*?<a.*?>([0-9]*?)</a>",Pattern.DOTALL);
			matcher = pattern.matcher(urlContent);
			if(matcher.find()){
				Integer weiboNum = Integer.parseInt(matcher.group(1));
				return weiboNum;
			}else{
				throw new ParserException("parse user weibo number failed!");
			}
		}
	}

	public ArrayList<MircoBlogInfo> getBlogList(StringBuilder urlContent){
		ArrayList<MircoBlogInfo> resultList = new ArrayList<MircoBlogInfo>();
		Pattern p1 = Pattern.compile("<li\\sid=\"([0-9]*?)\"\\srel=\"([0-9]*?)\"(.*?)</li>",Pattern.DOTALL);
		Matcher m1 = p1.matcher(urlContent);
		while(m1.find()){
			MircoBlogInfo blogInfo = new MircoBlogInfo();
			blogInfo.setWeiboID(m1.group(1));
			Long dateSeconds = Long.parseLong(m1.group(2));
			Date postTime = new Date(dateSeconds*1000);
			blogInfo.setTimestamp(dateFormat.format(postTime));
			String subContent = m1.group(3);
			Pattern selfContentPattern = Pattern.compile("<div\\sclass=\"msgCnt\">(.*?)</div>",Pattern.DOTALL);
			Matcher selfContentMatcher = selfContentPattern.matcher(subContent);
			if(selfContentMatcher.find()){
				blogInfo.setSelfContent(htmlTagsFilter(selfContentMatcher.group(1)));				
			}
			Pattern farwardContentPattern = Pattern.compile("<div\\sclass=\"replyBox\">.*?<div\\sclass=\"msgCnt\">(.*?)</div>",Pattern.DOTALL);
			Matcher farwardContentMatcher = farwardContentPattern.matcher(subContent);
			if(farwardContentMatcher.find()){
				blogInfo.setForWardContent(htmlTagsFilter(farwardContentMatcher.group(1)));
			}
			Pattern farwardNumPattern = Pattern.compile("<div\\sclass=\"funBox\">.*?class=\"relay\"\\snum=\"([0-9]*?)\"",Pattern.DOTALL);
			Matcher farwardNumMatcher = farwardNumPattern.matcher(subContent);
			if(farwardNumMatcher.find()){
				blogInfo.setForwardNum(Integer.parseInt(farwardNumMatcher.group(1)));
			}
			Pattern commentNumPattern = Pattern.compile("<div\\sclass=\"funBox\">.*?class=\"comt\"\\snum=\"([0-9]*?)\"",Pattern.DOTALL);
			Matcher commentNumMatcher = commentNumPattern.matcher(subContent);
			if(commentNumMatcher.find()){
				blogInfo.setCommentNum(Integer.parseInt(commentNumMatcher.group(1)));
			}
			resultList.add(blogInfo);
		}
		return resultList;
	}

	public ArrayList<String> getKeywordUsers(StringBuilder urlContent){
		ArrayList<String> resultList = new ArrayList<String>();
		String searchNo = "<div class=\"noresult\">";
		if(urlContent.indexOf(searchNo) >= 0){
			return resultList;
		}
		Pattern pattern = Pattern.compile("<div\\sclass=\"userName\"\\srel=\"(.*?)\"", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);
		while(matcher.find()){
			String userID = matcher.group(1);
			resultList.add(userID);
		}
		return resultList;
	}

	public ArrayList<String> getTagUsers(StringBuilder urlContent){
		ArrayList<String> resultList = new ArrayList<String>();
		String searchNo = "<div class=\"noresult\">";
		if(urlContent.indexOf(searchNo) >= 0){
			return resultList;
		}
		Pattern pattern = Pattern.compile("<span\\sclass=\"cId\">\\(@(.*?)\\)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);
		while(matcher.find()){
			String userID = matcher.group(1);
			resultList.add(userID);
		}
		return resultList;
	}

	public ArrayList<String> getQunUsers(StringBuilder urlContent){
		ArrayList<String> resultList = new ArrayList<String>();
		Pattern pattern = Pattern.compile("<li\\sid=\"q.(.*?)\"",Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);
		while(matcher.find()){
			resultList.add(matcher.group(1));
		}
		return resultList;
	}

	public ArrayList<String> getCommFarUsers(StringBuilder urlContent){
		ArrayList<String> resultList = new ArrayList<String>();
		Pattern pattern = Pattern.compile("<div\\sclass=\"userName\"\\srel=\"(.*?)\"", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);
		while(matcher.find()){
			String userID = matcher.group(1);
			resultList.add(userID);
		}
		return resultList;
	}

	public String getNextPageURL(StringBuilder urlContent){
		Pattern pattern = Pattern.compile("<div\\sid=\"pageNav\".*<a\\shref=\"(.*?)\"\\sclass=\"pageBtn\">下一页", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);

		if(matcher.find()){
			return matcher.group(1);
		}else{
			return null;
		}
	}

	public ArrayList<String> getFansFollowList(StringBuilder urlContent){
		ArrayList<String> resultList = new ArrayList<String>();
		Pattern pattern = Pattern.compile("href=\"#\"\\saccount=\"(.*?)\"", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(urlContent);
		while(matcher.find()){
			String userID = matcher.group(1);
			resultList.add(userID);
		}
		return resultList;
	}
	
	

	public String unicodeToString(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			str = str.replace(matcher.group(1), ch + "");
		}
		return str;
	}

}
