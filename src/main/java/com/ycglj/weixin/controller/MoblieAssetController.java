package com.ycglj.weixin.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.ycglj.manage.dao.LicenseDAO;
import com.ycglj.manage.dao.OrderDAO;
import com.ycglj.manage.dao.UserDAO;
import com.ycglj.manage.daoModel.Position;
import com.ycglj.manage.daoModel.Temp_User_License;
import com.ycglj.manage.daoModel.Temp_Users;
import com.ycglj.manage.daoModel.User_License;
import com.ycglj.manage.daoModel.Users;
import com.ycglj.manage.service.PhotoService;
import com.ycglj.manage.service.SellerService;
import com.ycglj.manage.service.UserService;
import com.ycglj.manage.service.WeiXinService;
import com.ycglj.sqlserver.context.Connect;

@Controller
@RequestMapping("/mobile/asset")
public class MoblieAssetController {

private SellerService sellerService;
	
	public SellerService getSellerService() {
		return sellerService;
	}

	@Autowired
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}
	
	private WeiXinService weixinService;
	
	@Autowired
	public void setweixinService(WeiXinService weiXinService) {
		this.weixinService=weiXinService;
	}
	
	private UserService userService;

	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	private PhotoService photoService;
	
	@Autowired
	public void setPhotoService(PhotoService photoService) {
		this.photoService = photoService;
	}
	
	ApplicationContext applicationContext=new Connect().get();
	
	UserDAO userDao=(UserDAO) applicationContext.getBean("userdao");
	
	OrderDAO orderDao=(OrderDAO) applicationContext.getBean("orderdao");
	
	LicenseDAO licenseDAO=(LicenseDAO) applicationContext.getBean("licensedao");
	
	@RequestMapping(value="updateAffire")
	public @ResponseBody Integer uploadFilesSpecifyPath(@RequestParam Integer type,@RequestParam String arrays,HttpServletRequest request,HttpServletResponse response) throws Exception {  

		HttpSession session = request.getSession();
		
		String openId=session.getAttribute("openId").toString();
		
		long startTime=System.currentTimeMillis();   //获取开始时间  
		 
		 JSONArray jsonArray = JSONArray.parseArray(arrays);

		 int up = 0;
		 
		 if(jsonArray!=null&&jsonArray.size()>0){
	            //组合image名称，“;隔开”
	            
                System.out.println("length="+jsonArray.size());
	            try {
	            	
	            	List<String> list=new ArrayList<>();
	            	
	                for (int i = 0; i < jsonArray.size(); i++) {
	                    String uuid=jsonArray.getString(i);
	                    
	                    list.add(uuid);
	                }

	                up=userDao.updateUserDataAffirm(openId,list,type);
	                
	                //上传成功
	                if(up>0){
	                    return 1;
	                 //   Constants.printJson(response, fileName);;
	                }else {
	                  //  Constants.printJson(response, "上传失败！文件格式错误！");
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	              //  Constants.printJson(response, "上传出现异常！异常出现在：class.UploadController.insert()");
	            }
	        }else {
	         //   Constants.printJson(response, "没有检测到文件！");
	        }
	    
		 
		 try {

				if (up > 0) {

					WechatSendMessageController wechatSendMessageController = new WechatSendMessageController();

					Map searchMap = new HashMap<>();

					searchMap.put("open_id = ", openId);

					List list2 = (List) userDao.getAllUser(1, 0, "", "", searchMap).get("data");

					Users users = (Users) list2.get(0);

					users.getPhone();
					
					List list=userService.getUserByTransact();					
					
					Runnable r = new Runnable() {

						@Override
						public void run() {

							String title="";
							
							if(type==4){
								title="停业";
							}else if(type==5){
								title="补办";
							}else if(type==6){
								title="恢复营业";
							}else if(type==7){
								title="变更";
							}else if(type==8){
								title="歇业";
							}
							
							Date date=new Date();		
							SimpleDateFormat sdf  =   new  SimpleDateFormat( " yyyy-MM-dd HH:mm:ss " ); 
							String time = sdf.format(date);
							
							WechatSendMessageController wechatSendMessageController = new WechatSendMessageController();

							Iterator iterator=list.iterator();
													
							while (iterator.hasNext()) {
								
								com.ycglj.manage.model.Users users=(com.ycglj.manage.model.Users) iterator.next();
								
								String transactOpenId=users.getOpenId();
								
								wechatSendMessageController.sendMessage(transactOpenId, "moOQnWapjZo99FItokfrzEPGjBsmElvO1bIcIWyW6XY", //申请待审核通知
										//"1vQfPSl4pSvi5UnmmDhVtueutq2R1w7XYRMts294URg", 
										title+"申请",
										"http://lzgfgs.com/ycglj/mobile/asset/",
										users.getName(), title, time, "已提交申请", "", "",
										"");

							}
							
						}
					};

					Thread t = new Thread(r);
					t.start();

				}

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
	        
	        return 0;
 
	} 
	
	@RequestMapping("getAllTransactUser")
	public @ResponseBody Map<String, Object> getAllTransactUser(@RequestParam Integer limit,@RequestParam Integer page,String sort,String order,
			String search,String area,HttpServletRequest request){
		
		Map searchMap=new HashMap<>();
		
		searchMap.put("authentication > ", "3");
		
		if(search!=null&&!search.trim().equals("")){
			search="%"+search+"%";  
			searchMap.put("name like ", search);
		}		

		if(area!=null&&!area.trim().equals("")){
			searchMap.put("area =", area);
		}	
		
		int offset=(page-1)*limit;
		
		return userDao.getAllUserJoin(limit, offset, sort,order, searchMap);
		
	}
			
	@RequestMapping("getAllNewUser")
	public @ResponseBody Map<String, Object> getAllNewUser(@RequestParam Integer limit,@RequestParam Integer page,String sort,String order,
			String search,String area,HttpServletRequest request){
			
			Map searchMap=new HashMap<>();
			
			if(search!=null&&!search.trim().equals("")){
				search="%"+search+"%";  
				searchMap.put("name like ", search);
			}		
						
			if(area!=null&&!area.trim().equals("")){
				searchMap.put("area =", area);
			}	
			
			int offset=(page-1)*limit;
			
			return userDao.getAllTempUserJoin(limit, offset, sort, order, searchMap);
				
	}
	
	@RequestMapping(value="newTransact")
	public @ResponseBody Integer newTransact(@RequestParam String openId,@RequestParam String license,
			@RequestParam String startTime,@RequestParam String endTime,@RequestParam Integer area,
			@RequestParam String region,HttpServletRequest request){
		
		Temp_Users temp_Users=new Temp_Users();
		
		temp_Users.setLimit(1);
		temp_Users.setOffset(0);
		temp_Users.setNotIn("id");
		
		String[] where={"open_id=",openId};
		
		temp_Users.setWhere(where);
		
		Temp_Users temp_Users2=userDao.getTempUsers(temp_Users);
		
		Users users=new Users();
		
		users.setOpen_id(openId);
		users.setName(temp_Users2.getName());
		users.setCard_type(temp_Users2.getCard_type());
		users.setId_number(temp_Users2.getId_number());
		users.setPhone(temp_Users2.getPhone());
		users.setDate(new Date());
		
		Date license_start_date = null;
		Date license_end_date = null;
		
		SimpleDateFormat sdf  =   new  SimpleDateFormat("yyyy-MM-dd"); 
		
		try {
			if(startTime!=null&&!startTime.equals(""))
				license_start_date = sdf.parse(startTime);
			if(endTime!=null&&!endTime.equals(""))
				license_end_date=sdf.parse(endTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		User_License user_License=new User_License();
		
		user_License.setOpen_id(openId);
		user_License.setPhone(temp_Users2.getPhone());
		user_License.setLicense(license);
		user_License.setAuthentication(1);
		user_License.setAuthen_date(new Date());
		user_License.setRegion(region);
		user_License.setArea(area);
		
		if(license_start_date!=null)
			user_License.setLicense_start_date(license_start_date);
		if(license_end_date!=null)
			user_License.setLicense_end_date(license_end_date);
		
		int i=userDao.insertUser(users, user_License);
		
		if (i > 0) {
			
			userDao.deleteTempUsers(temp_Users);
			
			Position position = new Position();

			position.setLicense(license);
			position.setLng(temp_Users2.getLng());
			position.setLat(temp_Users2.getLat());
			
			licenseDAO.updatePositionByLicense(position, false);			
		}

		return i;
		
	}
	
	@RequestMapping(value="transact")
	public @ResponseBody Integer transact(@RequestParam Integer type,@RequestParam String license,
			String startTime,String endTime,HttpServletRequest request){
		
		HttpSession session = request.getSession();
		
		String openId=session.getAttribute("openId").toString();
		
		int i = 0;
		
		if(type==4){
			
			User_License user_License=new User_License();
			user_License.setLimit(1);
			user_License.setOffset(0);
			user_License.setNotIn("license");
			
			String[] where={"license=",license};
			
			user_License.setWhere(where);
			
			User_License user_License2=userDao.getUserLicense(user_License);
			
			SimpleDateFormat sdf  =   new  SimpleDateFormat("yyyy-MM-dd"); 
			
			Date stop_start_date = null;
			Date stop_end_data = null;
			
			try {
				if(startTime!=null&&!startTime.equals(""))
					stop_start_date = sdf.parse(startTime);
				if(endTime!=null&&!endTime.equals(""))
					stop_end_data=sdf.parse(endTime);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(stop_start_date!=null)
				user_License2.setStop_start_date(stop_start_date);
			if(stop_end_data!=null)
				user_License2.setStop_end_data(stop_end_data);
			
			i=licenseDAO.deleteLicense(openId, user_License2);
			
		}else if(type==6){
			 
			Temp_User_License temp_User_License=new Temp_User_License();
			
			temp_User_License.setLimit(1);
			temp_User_License.setOffset(0);
			temp_User_License.setNotIn("open_id");
			
			String[] where={"license=",license};
			
			Temp_User_License temp_User_License2=licenseDAO.getTempUserLicense(temp_User_License);
			
			i=licenseDAO.insertLicense(temp_User_License);
			
		}
		
		return i;
		
	}
	
}
