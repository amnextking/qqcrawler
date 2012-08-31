package com.weibo.qqcrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;

import com.weibo.qqcrawler.model.PostReturnInfo;
import com.weibo.qqcrawler.qqutil.QQLogin;
import com.weibo.qqcrawler.qqutil.Setting;


public class SendMain {
	public static String targetUserFile = "./bf_cdkey_user.txt";
	public static String sendSuccessFile = "./send_success.txt";
	public static String sendFailFile = "./send_fail.txt";
	public static String contentFile = "./content.txt";
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String weiboContent = "";
	public static ArrayList<String> targetUserList = new ArrayList<String>();
	public static HashSet<String> sendedUserSet = new HashSet<String>();
	public static HashSet<String> failedUserSet = new HashSet<String>();

	public static void init() throws IOException{
		BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(targetUserFile), "UTF-8"));
		String line = "";
		while((line = reader1.readLine()) != null){
			targetUserList.add(line);			
		}
		reader1.close();

		BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(contentFile), "UTF-8"));
		weiboContent = reader2.readLine();
		reader2.close();
		System.out.println(weiboContent);

		//System.out.println(targetUserList.get(2));		
		//System.out.println(cdkeyList.get(0));
		BufferedReader reader3 = new BufferedReader(new FileReader(sendSuccessFile));
		while((line = reader3.readLine()) != null){
			String ss[] = line.split("\t");
			sendedUserSet.add(ss[1]);
		}
		reader3.close();


		BufferedReader reader4 = new BufferedReader(new FileReader(sendFailFile));
		while((line = reader4.readLine()) != null){
			String ss[] = line.split("\t");
			failedUserSet.add(ss[0]);			
		}
		reader4.close();

	}

	public static int getweiboStrSize(String str){
		int count = 0;
		for(int i = 0; i < str.length(); i++){
			char c = str.charAt(i);
			if(c < '\u00ff'){
				count++;
			}else{
				count += 2;
			}
		}
		int realCount = Math.round((float)count/2);
		return realCount;
	}

	public static void main(String[] args) throws Exception {
		System.setProperty( "org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog" );
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(9000000); 
		client.getHttpConnectionManager().getParams().setSoTimeout(9000000);
		Setting.load();
		QQLogin qqLogin = new QQLogin(client, Setting.su, Setting.sp);
		try{
			qqLogin.login();
		}catch (Exception e) {

			System.out.println("login failed, try to login with verify image");
			qqLogin.loginWithVerify();
			System.out.println("login with verify image successed");
		}
		init();

		BufferedWriter writer1 = new BufferedWriter(new FileWriter(sendSuccessFile, true));
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(sendFailFile, true));
		int index = 0;
		ArrayList<String> atUsersList = new ArrayList<String>();
		StringBuilder atContent = new StringBuilder();

		while(index < targetUserList.size()){
			String targetUserInfo = targetUserList.get(index);
			String ss[] = targetUserInfo.split("\t");
			String userID = ss[0];


			if(sendedUserSet.contains(userID) || failedUserSet.contains(userID)){
				index++;
				continue;
			}

			atUsersList.add(userID);			
			String content = atContent.toString();
			atContent.append("@").append(userID);
			if(getweiboStrSize(atContent.toString()+weiboContent) >= 140){
				String realContent = content + weiboContent;
				System.out.println(realContent);
				atUsersList.remove(atUsersList.size()-1);
				int atCount = 0;
				while(atCount < atUsersList.size()){
					String commentUserID = atUsersList.get(atCount);
					String weiboID = qqLogin.getLatestWeiboID(commentUserID);
					PostReturnInfo postReturnInfo = qqLogin.comment(weiboID, realContent);
					if(postReturnInfo.getReturnCode() == 0){
						for(String atUserID : atUsersList){
							qqLogin.log.info("send " + atUserID + " with " + realContent + " successed by comment");
							sendedUserSet.add(atUserID);
							String userCdkey = "116\t" + atUserID + "\t" + weiboContent + "\t" + dateFormat.format(new Date()) + "\t" + "comment";
							writer1.write(userCdkey);
							writer1.newLine();
							writer1.flush();							
						}
						TimeUnit.SECONDS.sleep(10);
						break;
					}else if(postReturnInfo.getReturnMsg().contains("你的操作过于频繁")){
						qqLogin.log.info("操作过于频繁,等待10分钟");
						TimeUnit.MINUTES.sleep(10);						
						continue;
					}else if(postReturnInfo.getReturnMsg().contains("未登录")){
						qqLogin.reconnect();
						qqLogin.login();
						continue;
					}else{
						qqLogin.log.info("mail error code:" + postReturnInfo.getReturnCode());
						qqLogin.log.info("mail error message:" + postReturnInfo.getReturnMsg());
					}
					atCount++;
				}
				atContent = new StringBuilder();
				atUsersList.clear();
				index--;
			}
			index++;
			//TimeUnit.SECONDS.sleep(1);
		}
		writer1.close();
		writer2.close();
	}

}
