package com.ycglj.manage.service;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ycglj.manage.model.Sellers;


public interface SellerService {

	public Sellers selectByCampusAdmin(String campusAdmin);
	
	public Sellers selectByCampusId(String campusAdmin);

	public void updateLastLoginTime(Date date, String campusAdmin);

	public void addASeller(Sellers seller);
	
	public List<Sellers> getCampusAdmin(String campusAdmin);
	
	public List<Sellers> getAllCampusAdmin();
	
	public int getCampusAdminCount(String campusAdmin);
		
	public int selectMaxCityId();
	
	public int selectRepeatAdmin(String campusAdmin);
	
	int insertSellective(Sellers seller);

	int updateSellective(Sellers sellers);
	
	int selectCountCampusAdmin(String campusAdmin);
	
	int deleteSellective(String campusAdmin);
}
