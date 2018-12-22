package com.ycglj.manage.controller;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.ycglj.manage.dao.LicenseDAO;
import com.ycglj.manage.daoModel.Position;
import com.ycglj.manage.service.UserService;
import com.ycglj.manage.tools.MyTestUtil;
import com.ycglj.sqlserver.context.Connect;



@Controller
@RequestMapping("/baiduMap")
public class BaiduMapController {

	ApplicationContext applicationContext=new Connect().get();
	
	LicenseDAO licenseDAO=(LicenseDAO) applicationContext.getBean("licensedao");
	
	private UserService userService;
	
	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@RequestMapping("/getAllAssetPosition")
	public @ResponseBody Map getAllAssetPosition(){
		
		Map map=licenseDAO.getAllLicensePosition();
		
		MyTestUtil.print(map);
		
		return map;
		
	}
	
	@RequestMapping("/getAllLicensePositionJoin")
	public @ResponseBody Map getAllLicensePositionJoin(String name,Long startTime,Long endTime,
			String yit,String any){
		
		System.out.println(name+" "+startTime+" "+endTime+" "+yit+" "+any);
		
		SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
		
		String startDate = null,endDate = null;
		
		String[] yitStrings = null,anyStrings = null;
		
		if(startTime!=null){			
			startDate=sdf.format(startTime);
		}
		
		if(endTime!=null){
			endDate=sdf.format(endTime);
		}
		
		System.out.println("startDate="+startDate+"   endDate="+endDate);
		
		if (yit != null && !yit.equals("")) {

			yitStrings = yit.split(",");
			
			MyTestUtil.print(yitStrings);

		}

		if (any != null && !any.equals("")) {
			
			anyStrings = any.split(",");
			
			MyTestUtil.print(anyStrings);
		}
		
		System.out.println("yitStrings="+yitStrings+"   anyStrings="+anyStrings);
		
		
		
		Map map=licenseDAO.getAllLicensePositionJoin(name, startDate, endDate, yitStrings, anyStrings);
		
		MyTestUtil.print(map);
		
		return map;
		
	}
	
	@RequestMapping("/getPosition")
	public @ResponseBody JSONObject getPosition() {
		JSONObject jsonObject=new JSONObject();
		Position position=new Position();
		position.setLimit(10);
		position.setOffset(0);		
		position.setSort("date");
		position.setOrder("desc");
		position.setNotIn("id");
        		
		try{
			position=(Position) licenseDAO.findPosition(position).get(0);
			jsonObject.put("lat", position.getLat());
			jsonObject.put("lng", position.getLng());
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return jsonObject;
		
	}
	
	@RequestMapping("/location")
	public @ResponseBody JSONObject baiduSwitch(HttpServletRequest request){
		JSONObject jsonObject=null;
		String requestUrl = "http://api.map.baidu.com/location/ip?ak=pQFgFpS0VnMXwCRN6cTc1jDOcBVi3XoD&coor=bd09ll";
		
		HttpGet g = new HttpGet(requestUrl);
    	RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
    	CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
    	CloseableHttpResponse r;
    	String content = null;
		try {
			r = httpClient.execute(g);
			content = EntityUtils.toString(r.getEntity());
	        r.close();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		jsonObject=JSONObject.parseObject(content);
  		
		return jsonObject;
		
	}
}