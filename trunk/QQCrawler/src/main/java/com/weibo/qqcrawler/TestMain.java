package com.weibo.qqcrawler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;

import com.weibo.qqcrawler.model.PostReturnInfo;
import com.weibo.qqcrawler.qqutil.QQLogin;
import com.weibo.qqcrawler.qqutil.Setting;
import com.weibo.qqcrawler.util.ParallelUtil;

public class TestMain {
	
	public static ArrayList<String> sendUserList = new ArrayList<String>();
	ParallelUtil parallelUtil = new ParallelUtil();
	public static String lunwenContent = "    http://www.younilunwen.com  "
				+ "有你论文网由在校博士生与高校教师组成，我们秉承诚信与质量第一的原则，为您提供原创论文代写代发。有你论文网真诚欢迎您的光临与惠顾！！！      " 
				+ "    http://www.younilunwen.com  ";
	
	public static String dianpuContent = "    http://shop70611321.taobao.com  "
				+ "兄弟姐妹朋友们， 还在为话费高而担忧吗？ 还等什么呢，足不出户，网上充值优惠进行时，全网最低价，欢迎你的光顾！！！" 
				+ "    http://shop70611321.taobao.com  ";
	
	public void getSendUserList(int start, int end ){
		try {
			sendUserList = parallelUtil.getSendUser(start, end);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getweiboStrSize(String str){
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
	
	public void sendMessage(String userId, QQLogin qqLogin, String weiboContent)throws Exception{
		
		String ss[] = userId.split("\t");
		String userID = ss[0];
		
		StringBuilder atContent = new StringBuilder();
		String content = atContent.toString();
		atContent.append("@").append(userID);
		
		if(getweiboStrSize(atContent.toString()+weiboContent) < 140){
			String realContent = content + weiboContent;
//			System.out.println(realContent);
			
			if(userID != null){
				String commentUserID = userID;
//				PostReturnInfo postReturnInfo = qqLogin.mail(userID, realContent);
//				if(postReturnInfo.getReturnCode() == 0){
//					System.out.println("send " + userID + " with " + realContent + " successed by comment");
//					parallelUtil.finishSend(userID);//回写数据库，设置已发送
//					TimeUnit.SECONDS.sleep(10);
//					
//				}else {
//					TimeUnit.MINUTES.sleep(1);
					
					String weiboID = qqLogin.getLatestWeiboID(commentUserID);
					PostReturnInfo postReturnInfo = qqLogin.comment(weiboID, realContent);
					if(postReturnInfo.getReturnCode() == 0){
						
						System.out.println("send " + userID + " with comment successed. ");
						parallelUtil.finishSend(userID);//回写数据库，设置已发送
						
						TimeUnit.SECONDS.sleep(5);
					}else if(postReturnInfo.getReturnMsg().contains("你的操作过于频繁")){
						System.out.println("操作过于频繁,等待10分钟");
						TimeUnit.MINUTES.sleep(10);						
					}else if(postReturnInfo.getReturnMsg().contains("未登录")){
						qqLogin.reconnect();
						qqLogin.login();
					}else{
//						System.out.println("mail error code:" + postReturnInfo.getReturnCode());
						System.out.println("mail error message:" + postReturnInfo.getReturnMsg());
						
						parallelUtil.deleteUser(userID);
						System.out.println("send " + userID + " error, delete the user. ");
					}
//				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		TestMain testMain = new TestMain();
		int start =0;
		int step = 500;
		int length = 10;
		
		System.setProperty( "org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog" );
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(9000000); 
		client.getHttpConnectionManager().getParams().setSoTimeout(9000000);
		
		//加载信息
		Setting.load();
		//登录初始化
		QQLogin qqLogin = new QQLogin(client, Setting.su, Setting.sp);
		try{
			qqLogin.login();
		}catch (Exception e) {

			System.out.println("login failed, try to login with verify image");
			qqLogin.loginWithVerify();
			System.out.println("login with verify image successed");
		}
		
		int j =0;
		for(int index=0; index <length; index++ ){
			
			testMain.getSendUserList( ++start, step );
			
			sendUserList.add("sendUserList");
			int size = sendUserList.size();
			for(int i= 0; i<size; i++){
				System.out.print(j++  + " > ");
				try {
					testMain.sendMessage(sendUserList.get(i), qqLogin, lunwenContent );
					TimeUnit.SECONDS.sleep(5);
					testMain.sendMessage(sendUserList.get(i), qqLogin, dianpuContent );
					
				} catch (Exception e) {
					System.out.println(e.getMessage());
					
				}
			}
			
			start = step;
			
		}
		
	}
	

}





