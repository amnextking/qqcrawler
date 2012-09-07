package com.weibo.qqcrawler.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.weibo.qqcrawler.model.UserSourceInfo;


public class ParallelUtil {
	private static final Logger logger = LoggerFactory.getLogger(ParallelUtil.class);
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public ParallelUtil(){		
		try{
			ApplicationContext ac = new ClassPathXmlApplicationContext("beans.xml");
			Object obj = ac.getBean("jdbcTemplate");
			if(null != obj && obj instanceof JdbcTemplate){
				jdbcTemplate = (JdbcTemplate)obj;
			}else{
				logger.error("load beans error:");
			}
			
		}catch(Exception e){
			logger.error("load beans error:" + e);
		}
	}

	public UserSourceInfo getUnCrawledUserID() throws SQLException{
		UserSourceInfo userSrcInfo = new UserSourceInfo();
		String sql = "select user_id, user_source_type, user_source_desc from qq_weibo_user where is_crawled = 0 limit 200";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		
		for(Map<String, Object> map : list){
			String userID = map.get("user_id").toString();
			int sourceType = Integer.parseInt(map.get("user_source_type").toString());
			String sourceDesc = map.get("user_source_desc").toString();
			
			userSrcInfo.setUserId(userID);
			userSrcInfo.setSourceType(sourceType);
			userSrcInfo.setSourceDesc(sourceDesc);
			return userSrcInfo;
		}
		
		return null;
	}

	public void finishCrawl(String userID,String updateTime) throws SQLException{
		try{
			String updateSql = "update qq_weibo_user set is_crawled = 1,update_time = '" + updateTime + "' where user_id = '" + userID + "'";
			jdbcTemplate.update(updateSql);
		} catch (Exception e) {
			String updateSql = "update qq_weibo_user set is_crawled = 1,update_time = '2000-01-01 00:00:00' where user_id = '" + userID + "'";
			jdbcTemplate.update(updateSql);
		}
	}

	public void crawlFailed(String userID) throws SQLException{
		String updateSql = "update qq_weibo_user set is_crawled = 1,crawl_failed = 1 where user_id = '" + userID + "'";
		jdbcTemplate.update(updateSql);
	}

	public void insertMysql(String userID, int type, String typeDesc){
		try{
			String selectSql = "select user_id from qq_weibo_user where user_id='" + userID + "'";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql);

			if(null == list || list.size() <= 0){		
				String sql = "insert into qq_weibo_user(user_id,update_time,is_crawled,user_source_type,user_source_desc) VALUES ('" + userID + "','2000-01-01',0," + type +",'" + typeDesc + "')";
				jdbcTemplate.execute(sql);
			}
		} catch (Exception e) {
		}
	}

	public ArrayList<String> getSeedUser() throws SQLException{
		ArrayList<String> resultList = new ArrayList<String>();
		String sql = "select User_ID from qq_weibo_user where is_selected = 0 limit 200";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		for(Map<String, Object> map : list){
			String userID = map.get("user_id").toString();
			
			resultList.add(userID);
		}
		return resultList;
	}

	public void finishSelected(String userID) throws SQLException{
		String updateSql = "update qq_weibo_user set is_selected = 1 where user_id = '" + userID + "'";
		jdbcTemplate.update(updateSql);
	}

	public ArrayList<String> getSendUser(int start, int end) throws SQLException{
		
		ArrayList<String> resultList = new ArrayList<String>();
		String sql = "select user_id from qq_weibo_user where send_time = 0 limit " + start + "," + end ;
		
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		
		for(Map<String, Object> map : list){
			String userID = map.get("user_id").toString();
			
			resultList.add(userID);
		}
		return resultList;
	}

	public void finishSend(String userID) throws SQLException{
		String updateSql = "update qq_weibo_user set send_time = send_time + 1 where user_id = '" + userID + "'";
		jdbcTemplate.update(updateSql);
	}

	public void deleteUser(String userID) throws SQLException{
		String updateSql = "delete from qq_weibo_user where user_id = '" + userID + "'";
		jdbcTemplate.update(updateSql);
	}
	
	public static void main( String[] args ){
		try {
			
			ParallelUtil parallelUtil = new ParallelUtil();
			ArrayList<String> resultList = parallelUtil.getSendUser(0, 10);
			
			for(String userId: resultList){
				System.out.println("userId: " + userId );
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
    }

}








