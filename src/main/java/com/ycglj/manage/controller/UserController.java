package com.ycglj.manage.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ycglj.manage.dao.OrderDAO;
import com.ycglj.manage.dao.UserDAO;
import com.ycglj.manage.daoModel.Order_User;
import com.ycglj.manage.daoModel.User_Data;
import com.ycglj.manage.daoModel.Users;
import com.ycglj.manage.service.SellerService;
import com.ycglj.manage.singleton.Singleton;
import com.ycglj.manage.tools.CopyFile;
import com.ycglj.sqlserver.context.Connect;

@Controller
@RequestMapping("/user")
public class UserController {

	private SellerService sellerService;
	
	public SellerService getSellerService() {
		return sellerService;
	}

	@Autowired
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}
	
	ApplicationContext applicationContext=new Connect().get();
	
	UserDAO userDao=(UserDAO) applicationContext.getBean("userdao");
	
	OrderDAO orderDao=(OrderDAO) applicationContext.getBean("orderdao");
	
	@RequestMapping("getAllUser")
	public @ResponseBody Map<String, Object> getAllUser(@RequestParam Integer limit,@RequestParam Integer page,String sort,String order,
			String search,String authentication,String region,HttpServletRequest request){
			
			Map searchMap=new HashMap<>();
			
			if(search!=null&&!search.trim().equals("")){
				search="%"+search+"%";  
				searchMap.put("name like ", search);
			}		
			
			System.out.print("authentication="+authentication);
			
			if(authentication!=null&&!authentication.equals("")&&!authentication.equals("undefined")){
				searchMap.put("authentication = ", authentication);
			}	
			
			if(region!=null&&!region.trim().equals("")){
				searchMap.put("region =", region);
			}	
			
			int offset=(page-1)*limit;
			
			return userDao.getAllUser(limit, offset, sort,order, searchMap);
				
	}
	
	
    @RequestMapping("insertUser")
	public @ResponseBody Integer insertUser(@RequestParam String name,String idNumber,
			@RequestParam String phone,HttpServletRequest request) {
    	
    	Users users = new Users();
    	
    	users.setName(name);
    	users.setOpen_id(phone);
    	users.setPhone(phone);
    	
    	if(idNumber!=null&&!idNumber.equals("")){
    		users.setId_number(idNumber);
    	}
    	
    	return userDao.insertUser(users);
    	
    }
	
	
	@RequestMapping("updateUser")
	public @ResponseBody Integer updateUser(@RequestParam String openId,String name,String idNumber,
			String phone,HttpServletRequest request) {
		// TODO Auto-generated method stub

		Users users = new Users();

		if(name!=null&&!name.equals("")){
			users.setName(name);
		}
		
		if(idNumber!=null&&!idNumber.equals("")){
			users.setId_number(idNumber);
		}
		
		if(phone!=null&&!phone.equals("")){
			users.setPhone(phone);
		}
		
		String[] where = { "open_id = ", openId };

		users.setWhere(where);
		
		users.setUp_date(new Date());

		return userDao.updateUser(users);
		
	}
	
	
	@RequestMapping("updateUserAuth")
	public @ResponseBody Integer updateUserAuth(@RequestParam String openId,Integer authentication,
			HttpServletRequest request) {
		// TODO Auto-generated method stub

		Users users = new Users();

		String[] where = { "open_id = ", openId };

		users.setWhere(where);
		users.setAuthentication(authentication);
		users.setUp_date(new Date());

		return userDao.updateUser(users);
		
	}
	
	@RequestMapping("getUserDateById")
	public @ResponseBody Map<String, Object> getUserDateById(@RequestParam String openId,HttpServletRequest request) {
						
		Order_User order_User=new Order_User();

		Map searchMap=new HashMap<>();
		
		searchMap.put("open_id = ", openId);
		searchMap.put("currently = ", "1");
		
		return userDao.getAllUserData(request,1000, 0, "","", searchMap);
				
	}
	
	
	@RequestMapping("setAuthentication")
	public @ResponseBody Integer setAuthentication(@RequestParam String openId,
			@RequestParam  Integer authentication,HttpServletRequest request) {
					
		Users users=new Users();
		
		users.setAuthentication(authentication);
        users.setAuthen_date(new Date());
		
		String[] where={"open_id = ", openId};
		
		users.setWhere(where);
		
		return userDao.updateUser(users);
	
	}
	
	
	@RequestMapping(value="inputImage")
	public @ResponseBody Integer uploadFilesSpecifyPath(@RequestParam("file") MultipartFile[] file,@RequestParam String openId,@RequestParam String data_type,HttpServletRequest request,HttpServletResponse response) throws Exception {  
		 long startTime=System.currentTimeMillis();   //获取开始时间  
	      /*  if(!file.isEmpty()){  
	            try {  
	                //定义输出流 将文件保存在D盘    file.getOriginalFilename()为获得文件的名字   
	                FileOutputStream os = new FileOutputStream("C:\\Users\\WangJing\\Desktop\\bb\\2\\"+file.getOriginalFilename());  
	                InputStream in = file.getInputStream();  
	                int b = 0;  
	                while((b=in.read())!=-1){ //读取文件   
	                    os.write(b);  
	                }  
	                os.flush(); //关闭流   
	                in.close();  
	                os.close();  
	                  
	            } catch (FileNotFoundException e) {  
	                e.printStackTrace();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }  */
		 
		 
		 if(file!=null&&file.length>0){
	            //组合image名称，“;隔开”
	            List<String> fileName =new ArrayList<String>();
	            String upimage = null;
                System.out.println("length="+file.length);
	            try {
	                for (int i = 0; i < file.length; i++) {
	                    if (!file[i].isEmpty()) {

	                        //上传文件，随机名称，";"分号隔开
	                       // fileName.add(FileUtil.uploadImage(request, "/upload/"+"/", file[i], true));
	                    	upimage=uploadImage(request, "/upload/", file[i],openId,data_type, true);
	                    	fileName.add(upimage);
	                    }
	                }

	                //上传成功
	                if(fileName!=null&&fileName.size()>0&&!upimage.equals("error")){
	                	System.out.println(fileName);
	                    System.out.println("上传成功！");
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
	    
		 
		 
	        long endTime=System.currentTimeMillis(); //获取结束时间  
	        System.out.println("上传文件共使用时间："+(endTime-startTime));  
	        
	        return 0;
 
	} 
	
	
	/**
     * 上传图片
     *  原名称
     * @param request 请求
     * @param path_deposit 存放位置(路径)
     * @param file 文件
     * @param isRandomName 是否随机名称
     * @return 完整文件路径
     */
    public  String uploadImage(HttpServletRequest request,String path_deposit,MultipartFile file,String openId,String data_type,boolean isRandomName) {
        
    	//获得物理路径webapp所在路径  
        String pathRoot = System.getProperty("user.home");
       //存放图片文件的路径
        String path="/mobile/photo/";  
        String filePath=pathRoot+path;  
    	
        //判断是否存在目录
        File savePath = new File(pathRoot+Singleton.filePath);//创建新文件  
        System.out.println("filePath="+Singleton.filePath);
        if (!savePath.exists()) {   
            savePath.mkdir();   
        }  
        
    	//上传
        try {
            String[] typeImg={"gif","png","jpg","jpeg","ico"};

            if(file!=null){
                String origName=file.getOriginalFilename();// 文件原名称
                System.out.println("上传的文件原名称:"+origName);
                // 判断文件类型
                String type=origName.indexOf(".")!=-1?origName.substring(origName.lastIndexOf(".")+1, origName.length()):null;
                    boolean booIsType=false;
                    for (int i = 0; i < typeImg.length; i++) {
                        if (typeImg[i].equals(type.toLowerCase())) {
                            booIsType=true;
                        }
                    }
                    //类型正确
                    if (booIsType) {
                    	//组合名称
                        String fileSrc=""; 
                        //是否随机名称
                        String uuid=UUID.randomUUID().toString();
                        if(isRandomName){
                            origName=uuid+origName.substring(origName.lastIndexOf("."));
                        }
                        //判断是否存在目录
                        File targetFile=new File(savePath,origName);

                        if(!targetFile.exists()){
                            targetFile.mkdirs();//创建目录
                        }
                        //上传
                        try{
                        	file.transferTo(targetFile);
                        	
                        	User_Data user_Data2=new User_Data();
                        	
                        	String[] where={"open_id=",openId,"data_type=",data_type};
                        	user_Data2.setWhere(where);
                        	user_Data2.setCurrently(0);
                        	user_Data2.setAffirm(0);
                        	
                        	userDao.updateUserData(user_Data2);
                        	
                             User_Data user_Data=new User_Data();
                             user_Data.setOpen_id(openId);
                             user_Data.setUuid(uuid.toString());
                             user_Data.setData_type(data_type);
                             user_Data.setType(type);
                             user_Data.setCurrently(1);
                             user_Data.setAffirm(1);
                             user_Data.setDate(new Date());
                             user_Data.setUri(origName);
                             
                             userDao.insertUserData(user_Data);

                        }catch (Exception e) {
							// TODO: handle exception
                        	e.printStackTrace();
						}
                        //完整路径
                        fileSrc=request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+path_deposit+origName;
                        
                        CopyFile.set(Singleton.ROOMINFOIMGPATH2, savePath+"\\"+origName, origName);
                        
                        System.out.println("图片上传成功:"+path+origName);                       
                        System.out.println("图片上传成功2:"+filePath+origName);
                        System.out.println("图片上传成功:"+fileSrc);
                        return fileSrc;
                    }
                }
            return "error";
        }catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
	
    

    
}
