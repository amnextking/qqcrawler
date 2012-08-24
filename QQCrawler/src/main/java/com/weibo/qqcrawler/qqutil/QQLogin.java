package com.weibo.qqcrawler.qqutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.weibo.qqcrawler.htmlparser.HTMLParser;
import com.weibo.qqcrawler.htmlparser.ParserException;
import com.weibo.qqcrawler.model.MircoBlogInfo;
import com.weibo.qqcrawler.model.PostReturnInfo;
import com.weibo.qqcrawler.model.UserWeiboInfo;
import com.weibo.qqcrawler.util.ParallelUtil;


public class QQLogin {
	private String pwd;
	private HttpClient dhc;
	private String su;
	public static String cookiesString="";
	private HTMLParser htmlParser;

	private class MyLogHander extends Formatter { 
		@Override 
		public String format(LogRecord record) { 
			StringBuilder sb = new StringBuilder();       	
			sb.append(getCurrTime());
			sb.append("\t");
			sb.append(record.getLevel());
			sb.append(": ");
			sb.append(record.getMessage());
			sb.append("\n");
			return sb.toString(); 
		} 
	}
	public Logger log; 
	private LogManager logManger;

	private String getCurrTime(){
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return tempDate.format(new Date());

	}

	public QQLogin(HttpClient dhc,String account,String pwd) throws SecurityException, IOException{
		this.dhc = dhc;
		this.pwd = pwd;
		this.su = account;
		htmlParser = new HTMLParser();		
		logManger = LogManager.getLogManager();
		logManger.reset();
		log = Logger.getLogger(account);
		SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmmss");
		FileHandler fileHandler = new FileHandler(Setting.logFilePath + "/" + dateForm.format(new Date())+".log");
		fileHandler.setFormatter(new MyLogHander()); 
		log.addHandler(fileHandler);			
	}

	private void downloadImg(InputStream in,String fileName) throws IOException{
		File outFile = new File(fileName);
		OutputStream os = new FileOutputStream(outFile);
		InputStream is = in;
		byte[] buff = new byte[1024];
		while(true) {
			int readed = is.read(buff);
			if(readed == -1) {
				break;
			}
			byte[] temp = new byte[readed];
			System.arraycopy(buff, 0, temp, 0, readed);
			os.write(temp);
		}
		is.close(); 
		os.close();

	}

	public void login() throws Exception {
		System.out.println(su);
		String preUrl="http://check.ptlogin2.qq.com/check?uin=" + su + "&appid=46000101&ptlang=2052&r=0.9206080922285321";		
		GetMethod preLoginGetMethod = new GetMethod(preUrl);
		preLoginGetMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler()); 
		preLoginGetMethod.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		int statusCode = dhc.executeMethod(preLoginGetMethod);
		if (statusCode != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + preLoginGetMethod.getStatusLine());
		}
		String preloginResponseStr = preLoginGetMethod.getResponseBodyAsString();
		System.out.println(preloginResponseStr);
		String ss[] = preloginResponseStr.split(",");
		String verifyCode = ss[1].substring(1, ss[1].length()-1);
		String uin = ss[2].substring(1, ss[2].length()-3);
		QQEncoder qqEncoder = new QQEncoder();
		String encodedPwd = qqEncoder.pwdEncoder(this.pwd, uin, verifyCode);		
		Header[] headers = preLoginGetMethod.getResponseHeaders("Set-Cookie");

		StringBuilder preLoginCookieInfo = new StringBuilder();
		for(Header header : headers){
			preLoginCookieInfo.append(header.getValue());
		}

		String loginUrl = "http://ptlogin2.qq.com/login?ptlang=2052&u=" + this.su + "&p=" + encodedPwd + "&verifycode=" + verifyCode + "&aid=46000101&u1=http%3A%2F%2Ft.qq.com&ptredirect=1&h=1&from_ui=1&dumy=&fp=loginerroralert&action=2-15-18767&g=1&t=1&dummy=";
		GetMethod loginGetMethod=new GetMethod(loginUrl);
		loginGetMethod.addRequestHeader("Cookie", preLoginCookieInfo.toString());
		loginGetMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		loginGetMethod.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		int statusCode2 = dhc.executeMethod(loginGetMethod);
		if (statusCode2 != HttpStatus.SC_OK) {
			throw new Exception("returned " + statusCode2 + " code,failed!");
		}
		String responseBody = loginGetMethod.getResponseBodyAsString();
		System.out.println(responseBody);
		if(responseBody.contains("您输入的帐号或密码不正确")){
			throw new Exception("用户名密码错误！");
		}
		Cookie[] cookies = dhc.getState().getCookies();

		StringBuilder cookieInfo2 = new StringBuilder();
		for(Cookie cookie : cookies){
			cookieInfo2.append(cookie.toExternalForm()).append("; ");
		}
		cookieInfo2.deleteCharAt(cookieInfo2.length()-1);
		cookiesString = cookieInfo2.toString();
		InetAddress addr = null;
		try{
			addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress();	
			System.out.println(ip + " login successed");
		} catch (Exception e) {
		}
	}

	public void loginWithVerify() throws Exception{
		System.out.println(su);
		String preUrl="http://check.ptlogin2.qq.com/check?uin=" + su + "&appid=46000101&ptlang=2052&r=0.9206080922285321";		
		GetMethod preLoginGetMethod = new GetMethod(preUrl);
		preLoginGetMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler()); 
		preLoginGetMethod.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		int statusCode = dhc.executeMethod(preLoginGetMethod);
		if (statusCode != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + preLoginGetMethod.getStatusLine());
		}
		String preloginResponseStr = preLoginGetMethod.getResponseBodyAsString();
		System.out.println(preloginResponseStr);
		String ss[] = preloginResponseStr.split(",");
		String verifyCode = ss[1].substring(1, ss[1].length()-1);
		String uin = ss[2].substring(1, ss[2].length()-3);
		String verifyImgUrl = "http://captcha.qq.com/getimage?aid=46000101&r=0.6163378055717136&uin=" + su;
		GetMethod verifyGetMethod = new GetMethod(verifyImgUrl);
		verifyGetMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler()); 
		verifyGetMethod.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		int statusCode3 = dhc.executeMethod(verifyGetMethod);
		if (statusCode3 != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + verifyGetMethod.getStatusLine());
		}				
		downloadImg(verifyGetMethod.getResponseBodyAsStream(), Setting.virifyImgPath + verifyCode + ".jpg");
		L1:
			while(true){
				TimeUnit.SECONDS.sleep(10);
				File imgPath = new File(Setting.virifyImgPath);
				File[] files = imgPath.listFiles();
				for(File file : files){
					String fileName = file.getName();
					String[] ss2 = fileName.split("\\.");
					if(ss2[0].equals(verifyCode) && ss2.length == 3){
						verifyCode = ss2[2];
						break L1;
					}
				}

			}

		QQEncoder qqEncoder = new QQEncoder();
		String encodedPwd = qqEncoder.pwdEncoder(this.pwd, uin, verifyCode);		
		Header[] headers = verifyGetMethod.getResponseHeaders("Set-Cookie");

		StringBuilder verifyCookieInfo = new StringBuilder();
		for(Header header : headers){
			verifyCookieInfo.append(header.getValue());
		}

		String loginUrl = "http://ptlogin2.qq.com/login?ptlang=2052&u=" + this.su + "&p=" + encodedPwd + "&verifycode=" + verifyCode + "&low_login_enable=1&low_login_hour=720&aid=46000101&u1=http%3A%2F%2Ft.qq.com&ptredirect=1&h=1&from_ui=1&dumy=&fp=loginerroralert&action=2-15-18767&g=1&t=1&dummy=";
		GetMethod loginGetMethod=new GetMethod(loginUrl);
		loginGetMethod.addRequestHeader("Cookie", verifyCookieInfo.toString());
		loginGetMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		loginGetMethod.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		int statusCode2 = dhc.executeMethod(loginGetMethod);
		if (statusCode2 != HttpStatus.SC_OK) {
			throw new Exception("returned " + statusCode2 + " code,failed!");
		}
		String responseBody = loginGetMethod.getResponseBodyAsString();
		System.out.println(responseBody);
		Cookie[] cookies = dhc.getState().getCookies();

		StringBuilder cookieInfo2 = new StringBuilder();
		for(Cookie cookie : cookies){
			cookieInfo2.append(cookie.toExternalForm()).append("; ");
		}
		cookieInfo2.deleteCharAt(cookieInfo2.length()-1);
		cookiesString = cookieInfo2.toString();
	}

	public void reconnect(){
		this.dhc = new HttpClient();
		this.dhc.getHttpConnectionManager().getParams().setConnectionTimeout(9000000); 
		this.dhc.getHttpConnectionManager().getParams().setSoTimeout(9000000);		
	}

	public StringBuilder getURLContent(String url){
		while(true){
			GetMethod getMethod=new GetMethod(url);
			try{				
				getMethod.setRequestHeader("Cookie", cookiesString);	
				getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
				getMethod.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
				int statusCode = dhc.executeMethod(getMethod);
				if (statusCode != HttpStatus.SC_OK) {
					throw new Exception("returned " + statusCode + " code,failed!");
				}

				//String responseBody = new String(getMethod.getResponseBody(), "UTF-8");

				//String responseBody = ""; 
				StringBuilder responseBody = new StringBuilder();
				BufferedReader br = new BufferedReader(new InputStreamReader(getMethod.getResponseBodyAsStream(), "UTF-8"));
				String line = "";
				while((line = br.readLine()) != null){
					//responseBody = responseBody + line + "\n";
					responseBody.append(line).append("\n");
				}
				getMethod.releaseConnection();
				return responseBody;

			}catch (Exception e) {
				getMethod.releaseConnection();
				String wrongMessage = e.getMessage();
				log.severe("get url: \"" + url + "\" failed! reason: " + wrongMessage);
				try{
					TimeUnit.SECONDS.sleep(5);
					InetAddress addr = InetAddress.getLocalHost();
					String ip = addr.getHostAddress();
					System.out.println(ip + " retry!");
					//login();
				} catch (Exception e2) {
					log.severe("relogin failed! reason: " + e2.getMessage());
				}
			}

		}
	}

	public String getUserTags(String userID) throws HttpException, IOException{
		PostMethod post = new PostMethod("http://api.t.qq.com/asyn/tag_oper.php");
		post.addRequestHeader("Cookie", cookiesString);
		post.addRequestHeader("Referer", "http://api.t.qq.com/proxy.html");
		post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		post.addParameter("op", "4");
		post.addParameter("u", userID);
		post.addParameter("apiType", "8");
		post.addParameter("apiHost", "http%3A%2F%2Fapi.t.qq.com");

		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);

		int statusCode3 = dhc.executeMethod(post);
		String response = post.getResponseBodyAsString();
		Pattern pattern = Pattern.compile("<a\\shref=.*?>(.*?)<\\\\/a>",Pattern.DOTALL);
		Matcher matcher = pattern.matcher(response);
		StringBuilder tags = new StringBuilder();
		while(matcher.find()){
			String tag = htmlParser.unicodeToString(matcher.group(1));
			tags.append(tag).append("/");
		}
		post.releaseConnection();
		return tags.toString();
	}

	public UserWeiboInfo getUserInfo(String userID,boolean isOrigin,Date timestamp) throws ParserException, HttpException, IOException, ParseException{
		UserWeiboInfo userWeiboInfo = new UserWeiboInfo();
		String url = "http://t.qq.com/" + userID;
		if(isOrigin){
			url = url + "?filter=1";
		}
		StringBuilder urlContent = new StringBuilder("class=\"pageBtn\">下一页");
		int page = 0;
		Long time = 0l;
		while(userWeiboInfo.getMicroBlogList().size() < Setting.maxBlogNum && urlContent.indexOf("class=\"pageBtn\">下一页") >= 0){
			page++;
			urlContent = getURLContent(url);
			if(page == 1){
				userWeiboInfo.setArea(htmlParser.getUserArea(urlContent));
				userWeiboInfo.setBlogNum(htmlParser.getWeiboNum(urlContent));
				userWeiboInfo.setBriefIntro(htmlParser.getBriefIntro(urlContent));
				userWeiboInfo.setFansNum(htmlParser.getFansNum(urlContent));
				userWeiboInfo.setFollowNum(htmlParser.getFollowNum(urlContent));
				userWeiboInfo.setJob(htmlParser.getUserJob(urlContent));
				userWeiboInfo.setSex(htmlParser.getUserGender(urlContent));
				userWeiboInfo.setTags(getUserTags(userID));
				userWeiboInfo.setUpdateTime(htmlParser.dateFormat.format(timestamp));
				userWeiboInfo.setUserID(userID);
				userWeiboInfo.setUserName(htmlParser.getUserName(urlContent));
				userWeiboInfo.setUserTitle(htmlParser.getUserTitle(urlContent));				
			}

			ArrayList<MircoBlogInfo> blogList = htmlParser.getBlogList(urlContent);
			userWeiboInfo.getMicroBlogList().addAll(blogList);
			if(blogList.size() < 1){
				break;
			}
			time = htmlParser.dateFormat.parse(blogList.get(blogList.size()-1).getTimestamp()).getTime() / 1000 ;
			url = "http://t.qq.com/" + userID + "?time=" + time;
			if(isOrigin){
				url = url + "&filter=1";
			}
		}
		return userWeiboInfo;
	}

	public String getLatestWeiboID(String userID){
		try{
			String url = "http://t.qq.com/" + userID + "?filter=1";
			StringBuilder urlContent = getURLContent(url);
			ArrayList<MircoBlogInfo> blogList = htmlParser.getBlogList(urlContent);
			if(blogList.size() == 0)
				return "";
			String weiboID = blogList.get(0).getWeiboID();
			return weiboID;
		} catch (Exception e) {
			return "";
		}
	}
	
	public List<String> getFollowerList(String userID){
		ArrayList<String> resultList = new ArrayList<String>();
		//String urlContent = "class=\"pageBtn\">下一页";
		StringBuilder urlContent = new StringBuilder("class=\"pageBtn\">下一页");
		int page = 0;
		while(urlContent.indexOf("class=\"pageBtn\">下一页") >= 0){
			page++;
			String url = "http://t.qq.com/" + userID + "/following?p=" + page;
			//System.out.println(url);
			urlContent = getURLContent(url);
			ArrayList<String> onePageFollowingList = htmlParser.getFansFollowList(urlContent);			
			if(onePageFollowingList.size() == 0){
				break;
			}			
			resultList.addAll(onePageFollowingList);
		}			
		return resultList;		
	}

	public void addUserByKeyword(String keyword,String othertype) throws UnsupportedEncodingException, SQLException, ClassNotFoundException, InterruptedException{
		ParallelUtil parallelUtil = new ParallelUtil();
		String urlKeyword = URLEncoder.encode(keyword, "UTF-8");
		int page = 0;
		StringBuilder urlContent = new StringBuilder("class=\"pageBtn\">下一页");
		while(urlContent.indexOf("class=\"pageBtn\">下一页") >= 0){
			page++;
			String url = "http://search.t.qq.com/index.php?pos=201&k=" + urlKeyword + "&p=" + page + othertype;
			System.out.println(url);
			urlContent = getURLContent(url);
			ArrayList<String> resultList = htmlParser.getKeywordUsers(urlContent);
			System.out.println(resultList.size());
			if(resultList.size() == 0){
				return;
			}
			for(String userID : resultList){
				parallelUtil.insertMysql(userID, 1, keyword);
			}
			TimeUnit.SECONDS.sleep(2);
		}
	}
	
	public void addUserByTag(String tag) throws SQLException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException{
		ParallelUtil parallelUtil = new ParallelUtil();
		String urlTag = URLEncoder.encode(tag, "UTF-8");
		int page = 0;
		StringBuilder urlContent = new StringBuilder("class=\"pageBtn\">下一页");
		while(urlContent.indexOf("class=\"pageBtn\">下一页") >= 0){
			page++;
			String url = "http://search.t.qq.com/user.php?pos=425&t=" + urlTag + "&keyType=4&p=" + page;
			System.out.println(url);
			urlContent = getURLContent(url);
			ArrayList<String> resultList = htmlParser.getTagUsers(urlContent);
			System.out.println(resultList.size());
			if(resultList.size() == 0){
				return;
			}
			for(String userID : resultList){
				parallelUtil.insertMysql(userID, 2, tag);
			}
			TimeUnit.SECONDS.sleep(2);
		}
	}

	public void addUserByQun(String qunID) throws SQLException, ClassNotFoundException{
		ParallelUtil parallelUtil = new ParallelUtil();
		StringBuilder urlContent = new StringBuilder("class=\"pageBtn\">下一页");
		int page = 0;
		while(urlContent.indexOf("class=\"pageBtn\">下一页") >= 0){
			page++;
			String url = "http://qun.t.qq.com/" + qunID + "/members?p=" + page;
			System.out.println(url);
			urlContent = getURLContent(url);
			ArrayList<String> resultList = htmlParser.getQunUsers(urlContent);
			System.out.println(resultList.size());
			if(resultList.size() == 0){
				return;
			}
			for(String userID : resultList){
				parallelUtil.insertMysql(userID, 5, qunID);
			}
		}
	}

	public void addUserByCommFar(String weiboID,String userName) throws SQLException, ClassNotFoundException{
		ParallelUtil parallelUtil = new ParallelUtil();
		String farWardUrl = "http://t.qq.com/p/t/" + weiboID + "?filter=5";
		while(farWardUrl != null){
			StringBuilder urlContent = getURLContent(farWardUrl);
			ArrayList<String> resultList = htmlParser.getCommFarUsers(urlContent);
			for(String userID : resultList){
				parallelUtil.insertMysql(userID, 4, userName);
			}
			farWardUrl = htmlParser.getNextPageURL(urlContent);
		}

		String commentUrl = "http://t.qq.com/p/t/" + weiboID + "?filter=6";
		while(commentUrl != null){
			StringBuilder urlContent = getURLContent(commentUrl);
			ArrayList<String> resultList = htmlParser.getCommFarUsers(urlContent);
			for(String userID : resultList){
				parallelUtil.insertMysql(userID, 4, userName);
			}
			commentUrl = htmlParser.getNextPageURL(urlContent);
		}
	}

	public void addUserByFans(String userID) throws SQLException, ClassNotFoundException{
		ParallelUtil parallelUtil = new ParallelUtil();
		StringBuilder urlContent = new StringBuilder("class=\"pageBtn\">下一页");
		int page = 0;
		while(urlContent.indexOf("class=\"pageBtn\">下一页") >= 0){
			page++;
			String url = "http://t.qq.com/" + userID + "/follower?st=1&t=1&p=" + page;
			System.out.println(url);
			urlContent = getURLContent(url);
			ArrayList<String> resultList = htmlParser.getFansFollowList(urlContent);
			if(resultList.size() == 0){
				break;
			}
			for(String fansID : resultList){
				parallelUtil.insertMysql(fansID, 6, userID);
			}
		}
		urlContent = new StringBuilder("class=\"pageBtn\">下一页");
		page = 0;
		while(urlContent.indexOf("class=\"pageBtn\">下一页") >= 0){
			page++;
			String url = "http://t.qq.com/" + userID + "/follower?st=3&t=1&p=" + page;
			System.out.println(url);
			urlContent = getURLContent(url);
			ArrayList<String> resultList = htmlParser.getFansFollowList(urlContent);
			if(resultList.size() == 0){
				break;
			}
			for(String fansID : resultList){
				parallelUtil.insertMysql(fansID, 6, userID);
			}
		}
		urlContent = new StringBuilder("class=\"pageBtn\">下一页");
		page = 0;
		while(urlContent.indexOf("class=\"pageBtn\">下一页") >= 0){
			page++;
			String url = "http://t.qq.com/" + userID + "/follower?st=1&t=5&p=" + page;
			System.out.println(url);
			urlContent = getURLContent(url);
			ArrayList<String> resultList = htmlParser.getFansFollowList(urlContent);
			if(resultList.size() == 0){
				break;
			}
			for(String fansID : resultList){
				parallelUtil.insertMysql(fansID, 6, userID);
			}
		}
		urlContent = new StringBuilder("class=\"pageBtn\">下一页");
		page = 0;
		while(urlContent.indexOf("class=\"pageBtn\">下一页") >= 0){
			page++;
			String url = "http://t.qq.com/" + userID + "/follower?st=0&t=7&p=" + page;
			System.out.println(url);
			urlContent = getURLContent(url);
			ArrayList<String> resultList = htmlParser.getFansFollowList(urlContent);
			if(resultList.size() == 0){
				break;
			}
			for(String fansID : resultList){
				parallelUtil.insertMysql(fansID, 6, userID);
			}
		}
		urlContent = new StringBuilder("class=\"pageBtn\">下一页");
		page = 0;
		while(urlContent.indexOf("class=\"pageBtn\">下一页") >= 0){
			page++;
			String url = "http://t.qq.com/" + userID + "/follower?st=3&t=7&p=" + page;
			System.out.println(url);
			urlContent = getURLContent(url);
			ArrayList<String> resultList = htmlParser.getFansFollowList(urlContent);
			if(resultList.size() == 0){
				break;
			}
			for(String fansID : resultList){
				parallelUtil.insertMysql(fansID, 6, userID);
			}
		}
	}

	public PostReturnInfo mail(String userID,String content) throws HttpException, IOException{
		PostReturnInfo postReturnInfo = new PostReturnInfo();
		PostMethod post = new PostMethod("http://api.t.qq.com/inbox/pm_mgr.php");
		post.addRequestHeader("Cookie", cookiesString);
		post.addRequestHeader("Referer", "http://api.t.qq.com/proxy.html?v=110321");
		post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		post.addParameter("source", "profile");
		post.addParameter("ptid", "");
		post.addParameter("roomid", "");
		post.addParameter("content", content);
		post.addParameter("fid", "");
		post.addParameter("arturl", "");
		post.addParameter("murl", "");
		post.addParameter("target", userID);
		post.addParameter("func", "send");
		post.addParameter("ef", "js");
		post.addParameter("pmlang", "zh_CN");
		post.addParameter("apiType", "8");
		post.addParameter("apiHost", "http%3A%2F%2Fapi.t.qq.com");
		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);

		int statusCode2 = dhc.executeMethod(post);		
		String ttemp = post.getResponseBodyAsString();
		//System.out.println(ttemp);
		Pattern idpattern = Pattern.compile("errcode:\"(.*?)\"");
		Matcher idmatcher = idpattern.matcher(ttemp);
		if(idmatcher.find()){
			try{
				postReturnInfo.setReturnCode(Integer.parseInt(idmatcher.group(1)));
			} catch (Exception e) {
				postReturnInfo.setReturnCode(-1);
			}
		}

		Pattern msgpattern = Pattern.compile("errmsg:\"(.*?)\"");
		Matcher msgmatcher = msgpattern.matcher(ttemp);
		if(msgmatcher.find()){
			postReturnInfo.setReturnMsg(msgmatcher.group(1));
		}
		post.releaseConnection();
		return postReturnInfo;
	}

	public PostReturnInfo comment(String weiboID,String content) throws HttpException, IOException{
		PostReturnInfo postReturnInfo = new PostReturnInfo();
		PostMethod post = new PostMethod("http://api.t.qq.com/old/publish.php");
		post.addRequestHeader("Cookie", cookiesString);
		post.addRequestHeader("Referer", "http://api.t.qq.com/proxy.html");
		post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		post.addParameter("content", content);
		//post.addParameter("startTime", "");
		//post.addParameter("endTime", "");
		post.addParameter("countType", "");
		post.addParameter("viewModel", "");
		post.addParameter("attips", "");
		post.addParameter("pId", weiboID);
		post.addParameter("type", "5");
		post.addParameter("pic", "");
		post.addParameter("apiType", "8");
		post.addParameter("share", "0");
		post.addParameter("syncQzone", "0");
		post.addParameter("syncQQSign", "0");
		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);

		int statusCode2 = dhc.executeMethod(post);		
		String ttemp = post.getResponseBodyAsString();
		//System.out.println(ttemp);
		Pattern idpattern = Pattern.compile("result\":(.*?),");
		Matcher idmatcher = idpattern.matcher(ttemp);
		if(idmatcher.find()){
			try{
				postReturnInfo.setReturnCode(Integer.parseInt(idmatcher.group(1)));
			} catch (Exception e) {
				postReturnInfo.setReturnCode(-1);
			}
		}

		Pattern msgpattern = Pattern.compile("msg\":\"(.*?)\"");
		Matcher msgmatcher = msgpattern.matcher(ttemp);
		if(msgmatcher.find()){
			postReturnInfo.setReturnMsg(htmlParser.unicodeToString(msgmatcher.group(1)));
		}
		post.releaseConnection();
		return postReturnInfo;
	}
}
