package com.weibo.qqcrawler.qqutil;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import com.weibo.qqcrawler.util.ParallelUtil;

public class Setting {	
	public static int maxBlogNum = 10000;
	public static String su = "277150165";
	public static String sp = "huixiang";
	public static String logFilePath = "f:/weibo/";
	public static String basicInfoPath = "f:/weibo/";
	public static String weiboInfoPath = "f:/weibo/";
	public static String virifyImgPath = "f:/weibo/";
	public static void load() {
//		Properties pro;
//	    FileInputStream proReader = null;
//		try{
//			proReader = new FileInputStream("const.properties");
//			pro = new Properties();
//			pro.load(proReader);
//			maxBlogNum = Integer.parseInt(pro.getProperty("max_weibo_number"));
//			su = pro.getProperty("weibo_user_name");
//			sp = pro.getProperty("weibo_user_pwd");
//			logFilePath = pro.getProperty("log_file_path");
//			basicInfoPath = pro.getProperty("basic_info_file");
//			weiboInfoPath = pro.getProperty("weibo_info_file");
//			virifyImgPath = pro.getProperty("verify_img_path");
//			
//		}catch(Exception e){
//			System.out.println("read conf error: " + e.getMessage() );
//		}	
		
		maxBlogNum = 10000;
//		su = "277150165";
//		sp = "huixiang";
		su = "75957893";
		sp = "huixiang";
		logFilePath = "f:/weibo/";
		basicInfoPath = "f:/weibo/";
		weiboInfoPath = "f:/weibo/";
		virifyImgPath = "f:/weibo/";
		
	}
	
	public static void main( String[] args ){
		Setting.load();
		
    }
}
