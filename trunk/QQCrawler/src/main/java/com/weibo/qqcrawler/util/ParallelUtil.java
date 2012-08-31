package com.weibo.qqcrawler.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.weibo.qqcrawler.model.UserSourceInfo;


public class ParallelUtil {
	private static String mysqlURL = "jdbc:mysql://10.1.1.175:3306/weibo";
//	private static String redisServer = "10.1.9.111";
	private static String mysqlUser = "weibo";
	private static String mysqlPW = "process_Ld1iOdJd2ums9aoI";

//	private Jedis jedis = new Jedis(redisServer,6379,200000);
	private Connection conn;

	public ParallelUtil(){		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(mysqlURL,mysqlUser,mysqlPW);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public UserSourceInfo getUnCrawledUserID() throws SQLException{
		UserSourceInfo userSrcInfo = new UserSourceInfo();
		String query = "select User_ID,User_Source_Type,User_Source_Desc from qq_weibo_user_info where is_crawled = 0 limit 200";
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		while(rs.next()){
			String userID = rs.getString(1);
			int sourceType = Integer.parseInt(rs.getString(2));
			String sourceDesc = rs.getString(3);
//			if(!isProcessing(userID)){

				userSrcInfo.setUserId(userID);
				userSrcInfo.setSourceType(sourceType);
				userSrcInfo.setSourceDesc(sourceDesc);
				st.close();
				return userSrcInfo;
//			}
		}
		st.close();
		return null;
	}

	public void finishCrawl(String userID,String updateTime) throws SQLException{
		try{
			String updateSql = "update qq_weibo_user_info set is_crawled = 1,Update_Time = '" + updateTime + "' where user_id = '" + userID + "'";
			Statement st2 = conn.createStatement();
			st2.executeUpdate(updateSql);
			st2.close();
		} catch (Exception e) {
			String updateSql = "update qq_weibo_user_info set is_crawled = 1,Update_Time = '2000-01-01 00:00:00' where user_id = '" + userID + "'";
			Statement st2 = conn.createStatement();
			st2.executeUpdate(updateSql);
			st2.close();
		}
//		while(true){
//			try{				
//				jedis.del(userID);
//				return;
//			}catch(Exception e){
//				//e.printStackTrace();
//				jedis = new Jedis(redisServer,6379,200000);
//				jedis.select(3);
//			}
//		}
	}

	public void crawlFailed(String userID) throws SQLException{
		String updateSql = "update qq_weibo_user_info set is_crawled = 1,Crawl_Failed = 1 where user_id = '" + userID + "'";
		Statement st2 = conn.createStatement();
		st2.executeUpdate(updateSql);
		st2.close();
//		while(true){
//			try{				
//				jedis.del(userID);
//				return;
//			}catch(Exception e){
//				//e.printStackTrace();
//				jedis = new Jedis(redisServer,6379,200000);
//				jedis.select(3);
//			}
//		}
	}

	public void insertMysql(String userID, int type, String typeDesc){
		try{
			String selectQuery = "select user_id from qq_weibo_user_info where user_id='" + userID + "'";
			Statement st1 = conn.createStatement();
			ResultSet rs = st1.executeQuery(selectQuery);
			if(!rs.next()){		
				String query = "insert into qq_weibo_user_info(user_id,update_time,is_crawled,user_source_type,user_source_desc) VALUES ('" + userID + "','2000-01-01',0," + type +",'" + typeDesc + "')";
				Statement st = conn.createStatement();
				st.executeUpdate(query);
				st.close();
			}
			st1.close();
		} catch (Exception e) {
		}
	}

	public ArrayList<String> getSeedUser() throws SQLException{
		ArrayList<String> resultList = new ArrayList<String>();
		String query = "select User_ID from qq_weibo_user_info where is_selected = 0 limit 200";
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		while(rs.next()){
			String userID = rs.getString(1);
			resultList.add(userID);
		}
		st.close();
		return resultList;
	}

	public void finishSelected(String userID) throws SQLException{
		String updateSql = "update qq_weibo_user_info set is_selected = 1 where user_id = '" + userID + "'";
		Statement st2 = conn.createStatement();
		st2.executeUpdate(updateSql);	
		st2.close();
	}

	public ArrayList<String> getSendUser(int start, int end) throws SQLException{
		ArrayList<String> resultList = new ArrayList<String>();
		String query = "select User_ID from qq_weibo_user_info where is_selected = 0 limit " + start + "," + end ;
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		while(rs.next()){
			String userID = rs.getString(1);
			resultList.add(userID);
		}
		st.close();
		return resultList;
	}



}
