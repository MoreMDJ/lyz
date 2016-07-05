package com.ynyes.lyz.controller.management;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ynyes.lyz.entity.TdAgencyFund;
import com.ynyes.lyz.entity.TdCity;
import com.ynyes.lyz.entity.TdDiySite;
import com.ynyes.lyz.entity.TdGathering;
import com.ynyes.lyz.entity.TdGoodsInOut;
import com.ynyes.lyz.entity.TdManager;
import com.ynyes.lyz.entity.TdManagerDiySiteRole;
import com.ynyes.lyz.entity.TdManagerRole;
import com.ynyes.lyz.entity.TdReturnReport;
import com.ynyes.lyz.entity.TdSalesDetail;
import com.ynyes.lyz.entity.TdWareHouse;
import com.ynyes.lyz.service.TdAgencyFundService;
import com.ynyes.lyz.service.TdCityService;
import com.ynyes.lyz.service.TdDiySiteRoleService;
import com.ynyes.lyz.service.TdDiySiteService;
import com.ynyes.lyz.service.TdGatheringService;
import com.ynyes.lyz.service.TdGoodsInOutService;
import com.ynyes.lyz.service.TdManagerRoleService;
import com.ynyes.lyz.service.TdManagerService;
import com.ynyes.lyz.service.TdReturnReportService;
import com.ynyes.lyz.service.TdSalesDetailService;
import com.ynyes.lyz.service.TdUserService;
import com.ynyes.lyz.service.TdWareHouseService;
import com.ynyes.lyz.util.SiteMagConstant;

@Controller
@RequestMapping("/Verwalter/statement")
public class TdManagerStatementController extends TdManagerBaseController {
	
	@Autowired
	TdGoodsInOutService tdGoodsInOutService;
	@Autowired
	TdManagerService tdManagerService;
	@Autowired
	TdManagerRoleService tdManagerRoleService;
	@Autowired
	TdWareHouseService tdWareHouseService;
	@Autowired
	TdUserService tdUserService;
	@Autowired
	TdDiySiteService tdDiySiteService;
	@Autowired
	TdCityService tdCityService;
	@Autowired
	TdAgencyFundService tdAgencyFundService;
	@Autowired
	TdGatheringService tdGatheringService;
	@Autowired
	TdSalesDetailService tdSalesDetailService;
	@Autowired
	TdReturnReportService tdReturnReportService;
	@Autowired
	TdDiySiteRoleService tdDiySiteRoleService;
	
	
    /*
	 * 报表下载
	 */
	@RequestMapping(value = "/downdata",method = RequestMethod.GET)
	@ResponseBody
	public String dowmDataGoodsInOut(HttpServletRequest req,ModelMap map,String begindata,String enddata,HttpServletResponse response,String diyCode,String cityName,Long statusId)
	{
		//检查登录
		String username = (String) req.getSession().getAttribute("manager");
		if (null == username) {
			return "redirect:/Verwalter/login";
		}
		
		//检查权限
		TdManager tdManager = tdManagerService.findByUsernameAndIsEnableTrue(username);
		TdManagerRole tdManagerRole = null;
		if (null != tdManager && null != tdManager.getRoleId())
		{
			tdManagerRole = tdManagerRoleService.findOne(tdManager.getRoleId());
		}
		if (tdManagerRole == null)
		{
			return "redirect:/Verwalter/login";
		}
		
		Date begin = stringToDate(begindata,null);
		Date end = stringToDate(enddata,null);
		//设置默认时间
		if(null==begin){
			begin=getStartTime();
		}
		if(null==end){
			end=getEndTime();
		}
		//门店管理员只能查询归属门店
		if (tdManagerRole.getTitle().equalsIgnoreCase("门店")) 
			{
	        	diyCode=tdManager.getDiyCode();
	        	cityName=null;
			}
		
		//获取到导出的excel
		HSSFWorkbook wb=acquireHSSWorkBook(statusId, begin, end, diyCode, cityName, username,tdDiySiteRoleService.userRoleDiyId(tdManagerRole, tdManager));
		

		String exportAllUrl = SiteMagConstant.backupPath;
        download(wb, exportAllUrl, response,acquireFileName(statusId));
        return "";
	}
	
	/**
	 * 报表 展示
	 * 选择城市 刷新门店列表
	 * @param keywords 订单号
	 * @param page 当前页
	 * @param size 每页显示行数
	 * @param __EVENTTARGET
	 * @param __EVENTARGUMENT
	 * @param __VIEWSTATE
	 * @param map 
	 * @param req
	 * @param orderStartTime 开始时间
	 * @param orderEndTime 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市名称
	 * @param statusid 0:出退货报表 1:代收款报表2：收款报表3：销售明细报表4：退货报表5：领用记录报表
	 * @return
	 */
	@RequestMapping(value = "/goodsInOut/list/{statusId}")
	public String goodsListDialog(String keywords,@PathVariable Long statusId, Integer page, Integer size,
			String __EVENTTARGET, String __EVENTARGUMENT, String __VIEWSTATE,
			ModelMap map, HttpServletRequest req,String orderStartTime,String orderEndTime,String diyCode,String cityName,
			String oldOrderStartTime,String oldOrderEndTime) {
		
		String username = (String) req.getSession().getAttribute("manager");
		//判断是否登陆
		if (null == username)
		{
			return "redirect:/Verwalter/login";
		}

		TdManager tdManager = tdManagerService.findByUsernameAndIsEnableTrue(username);
		TdManagerRole tdManagerRole = null;
		if (null != tdManager && null != tdManager.getRoleId())
		{
			tdManagerRole = tdManagerRoleService.findOne(tdManager.getRoleId());
		}
		//判断是否有权限
		if (tdManagerRole == null)
		{
			return "redirect:/Verwalter/login";
		}
		//查询用户管辖门店权限
    	TdManagerDiySiteRole diySiteRole= tdDiySiteRoleService.findByTitle(tdManagerRole.getTitle());
    	//获取管理员管辖城市
    	List<TdCity> cityList= new ArrayList<TdCity>();
    	//获取管理员管辖门店
    	List<TdDiySite> diyList=new ArrayList<TdDiySite>(); 
    	
    	//管理员获取管辖的城市和门店
    	tdDiySiteRoleService.userRoleCityAndDiy(cityList, diyList, diySiteRole, tdManagerRole, tdManager);
    	
    	
		if (null == page || page < 0) {
			page = 0;
		}

		if (null == size || size <= 0) {
			size = SiteMagConstant.pageSize;
		}

		if (null != __EVENTTARGET) {
			if (__EVENTTARGET.equalsIgnoreCase("btnCancel"))
			{
			} 
			else if (__EVENTTARGET.equalsIgnoreCase("btnConfirm"))
			{
			}
			else if (__EVENTTARGET.equalsIgnoreCase("btnDelete"))
			{
			}
			else if (__EVENTTARGET.equalsIgnoreCase("btnPage")) 
			{
				if (null != __EVENTARGUMENT) 
				{
					page = Integer.parseInt(__EVENTARGUMENT);
				}
			}else if(__EVENTTARGET.equals("btnSearch")){
				page=0;
			}
		}
		
		//修改城市刷新门店列表
		if(StringUtils.isNotBlank(cityName)){
			//需要删除的diy
			List<TdDiySite> diyRemoveList=new ArrayList<TdDiySite>(); 
			for (TdDiySite tdDiySite : diyList) {
				if(!cityName.equals(tdDiySite.getCity())){
					diyRemoveList.add(tdDiySite);
					if(tdDiySite.getStoreCode().equals(diyCode)){
						diyCode=null;
					}
				}
			}
			diyList.removeAll(diyRemoveList);
		}
		
		Date begin=null;
		Date end=null;
		try {//字符串转换为时间
			begin=stringToDate(orderStartTime,null);
			end=stringToDate(orderEndTime,null);
		} catch (Exception e) {
			System.out.println(e);
		}
		if(begin==null){//如果为空设置为默认时间
			begin=getStartTime();
			orderStartTime=dateToString(begin,null);
		}
		if(end==null){//如果为空设置为默认时间
			end=getEndTime();
			orderEndTime=dateToString(end,null);
		}
		
		if(!orderStartTime.equals(oldOrderStartTime) || !orderEndTime.equals(oldOrderEndTime)){
			//调用存储过程查询数据
			callProcedure(statusId, __EVENTTARGET, begin, end, username);
		}
		
		//根据报表类型 查询相应数据到map
		addOrderListToMap(map, statusId, keywords, begin, end, diyCode, cityName, username, size, page,tdDiySiteRoleService.userRoleDiyId(tdManagerRole, tdManager));
	
		
    	//城市和门店信息
    	map.addAttribute("diySiteList",diyList);
		map.addAttribute("cityList", cityList);
		
		// 参数注回
		map.addAttribute("orderNumber", keywords);
		map.addAttribute("orderStartTime", orderStartTime);
		map.addAttribute("orderEndTime", orderEndTime);
		map.addAttribute("diyCode", diyCode);
		map.addAttribute("cityName", cityName);
		map.addAttribute("oldOrderStartTime", orderStartTime);
		map.addAttribute("oldOrderEndTime", orderEndTime);

		map.addAttribute("page", page);
		map.addAttribute("size", size);
		map.addAttribute("keywords", keywords);
		map.addAttribute("statusId", statusId);
		map.addAttribute("__EVENTTARGET", __EVENTTARGET);
		map.addAttribute("__EVENTARGUMENT", __EVENTARGUMENT);
		map.addAttribute("__VIEWSTATE", __VIEWSTATE);

		return "/site_mag/statement_list";
	}

	

	private String changeName(List<TdWareHouse> wareHouses,String number)
	{
//		郑州公司	11	总仓
//		天荣中转仓	1101	分仓
//		五龙口中转仓	1102	分仓
//		东大中转仓	1103	分仓
//		百姓中转仓	1104	分仓
//		主仓库	1105	分仓
		for (TdWareHouse tdWareHouse : wareHouses) {
			if(tdWareHouse.getWhNumber().equals(number)){
				return tdWareHouse.getWhName();
			}
		}
		return number;
		
//		if (name == null || name.equalsIgnoreCase(""))
//		{
//			return "未知";
//		}
//		if (name.equalsIgnoreCase("11"))
//		{
//			return "郑州公司";
//		}
//		else if (name.equalsIgnoreCase("1101"))
//		{
//			return "天荣中转仓";
//		}
//		else if (name.equalsIgnoreCase("1102"))
//		{
//			return "五龙口中转仓";
//		}
//		else if (name.equalsIgnoreCase("1103"))
//		{
//			return "东大中转仓";
//		}
//		else if (name.equalsIgnoreCase("1104"))
//		{
//			return "百姓中转仓";
//		}
//		else if (name.equalsIgnoreCase("1105"))
//		{
//			return "主仓库";
//		}
//		else
//		{
//			return "未知编号：" + name;
//		}
	}
	
	/**
	 * 根据报表类型 调用相应的存储过程 插入数据
	 * @param statusId 报表类型
	 * @param __EVENTTARGET
	 * @param begin 开始时间
	 * @param end 结算时间
	 * @param username 当前用户
	 */
	private void callProcedure(Long statusId,String __EVENTTARGET,Date begin,Date end,String username){
		try {//调用存储过程 报错
			if(null != __EVENTTARGET && __EVENTTARGET.equalsIgnoreCase("btnPage")){
				return;
			}else if(statusId==0){//出退货报表
				tdGoodsInOutService.callinsertGoodsInOutInitial(begin, end,username);
			}else if(statusId==1){//代收款报表
				tdAgencyFundService.callInsertAgencyFund(begin, end,username);
			}else if(statusId==2){//收款报表
				tdGatheringService.callInsertGathering(begin, end, username);
			}else if(statusId==3){//销售明细报表
//				tdSalesDetailService.callInsertSalesDetail(begin, end, username);
			}else if(statusId==4){//退货报表
				tdReturnReportService.callInsertReturnReport(begin, end, username);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/**
	 * 根据报表类型 查询相应数据到map
	 * 增加门店id查询
	 * @param map 
	 * @param statusId 报表类型
	 * @param keywords 订单号
	 * @param begin 开始时间
	 * @param end 结算时间
	 * @param diySiteCode 门店编号
	 * @param cityName 城市名称
	 * @param username 当前用户
	 * @param roleDiyIds 门店id集合
	 * @param size
	 * @param page
	 */
	private void addOrderListToMap(ModelMap map,Long statusId,String keywords,Date begin,Date end,String diySiteCode,String cityName,String username,
			int size,int page,List<String> roleDiyIds){
		if(statusId==0){//出退货报表
			map.addAttribute("order_page",tdGoodsInOutService.searchList(keywords,begin, end, diySiteCode, cityName,username, size, page,roleDiyIds));
		}else if(statusId==1){//代收款报表
			map.addAttribute("order_page",tdAgencyFundService.searchList(keywords,begin, end,cityName ,diySiteCode ,username, size, page,roleDiyIds));
		}else if(statusId==2){//收款报表
			map.addAttribute("order_page",tdGatheringService.searchList(keywords,begin, end,cityName ,diySiteCode ,username, size, page,roleDiyIds));
		}else if(statusId==3){//销售明细报表
//			map.addAttribute("order_page",tdSalesDetailService.queryPageList(begin, end, cityName, diySiteCode, roleDiyIds, size, page));
		}else if(statusId==4){//退货报表
			map.addAttribute("order_page",tdReturnReportService.searchList(keywords,begin, end,cityName ,diySiteCode ,username, size, page,roleDiyIds));
		}

	}
	/**
	 * 根据报表类型获取报表名
	 * @param statusId
	 * @return
	 */
	private String acquireFileName(Long statusId){
		String fileName="";
		if(statusId==0){
			fileName="出退货明细报表";
		}else if(statusId==1){
			fileName= "代收款报表";
		}else if(statusId==2){
			fileName= "收款报表";
		}else if(statusId==3){
			fileName= "销售明细报表";
		}else if(statusId==4){
			fileName= "退货报表";
		}else if(statusId==5){
			fileName= "领用记录报表";
		}
		return fileName;
	}
	/**
	 * 根据报表状态 设置相应的报表
	 * @param statusId
	 * @param begin 开始时间
	 * @param end 结算时间
	 * @param diyCode 门店编号
	 * @param cityName 城市名称
	 * @param username 当前用户
	 * @return
	 */
	private HSSFWorkbook acquireHSSWorkBook(Long statusId,Date begin,Date end,String diyCode,String cityName,String username,List<String> roleDiyIds){
		HSSFWorkbook wb= new HSSFWorkbook();  
		if(statusId==0){//出退货明细报表
			wb=goodsInOutWorkBook(begin, end, diyCode, cityName, username,roleDiyIds);
		}else if(statusId==1){//代收款报表
			wb=agencyFundWorkBook(begin, end, diyCode, cityName, username,roleDiyIds);
		}else if(statusId==2){//收款报表
			wb=payWorkBook(begin, end, diyCode, cityName, username,roleDiyIds);
		}else if(statusId==3){//销售明细报表
			wb=salesDetailWorkBook(begin, end, diyCode, cityName, username,roleDiyIds);
		}else if(statusId==4){//退货报表
			wb=returnWorkBook(begin, end, diyCode, cityName, username,roleDiyIds);
		}
		return wb;
	}
	/**
	 * 出退货明细报表
	 * 查询条件增加管理员管辖门店
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市编号
 	 * @param username 当前用户
	 * @return
	 */
	private HSSFWorkbook goodsInOutWorkBook(Date begin,Date end,String diyCode,String cityName,String username,List<String> roleDiyIds){
		// 第一步，创建一个webbook，对应一个Excel文件 
        HSSFWorkbook wb = new HSSFWorkbook();  
        
 
        // 第五步，设置值  
        List<TdGoodsInOut> goodsInOutList=tdGoodsInOutService.searchGoodsInOut(begin, end, cityName, diyCode,username,roleDiyIds);
        List<TdWareHouse> wareHouseList = tdWareHouseService.findAll();
//        long startTimne = System.currentTimeMillis();
        
        //excel单表最大行数是65535
        int maxRowNum = 60000;
        int maxSize=0;
        if(goodsInOutList!=null){
        	maxSize=goodsInOutList.size();
        }
        int sheets = maxSize/maxRowNum+1;
        
		//写入excel文件数据信息
		for(int i=0;i<sheets;i++){
			
			// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet  
	        HSSFSheet sheet = wb.createSheet("第"+(i+1)+"页");  
	        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short  
	        //列宽
	        int[] widths={13,25,25,13,18,18,13,13,13,13,
	        		9,13,13,20,9,9,9,13,13,13,
	        		20,13};
	        sheetColumnWidth(sheet,widths);
	        
	        // 第四步，创建单元格，并设置值表头 设置表头居中  
	        HSSFCellStyle style = wb.createCellStyle();  
	        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
	        style.setWrapText(true);


	       	//设置标题
	        HSSFRow row = sheet.createRow((int) 0); 
	        
	        String[] cellValues={"门店名称","主单号","分单号","订单状态","订单日期","销售日期","客户名称","客户电话","品牌","商品类别",
					"导购","配送方式","产品编号","产品名称","数量","单价","总价","中转仓","配送人员","配送人员电话",
					"地址","客户备注"};
			cellDates(cellValues, style, row);
			
			for(int j=0;j<maxRowNum;j++)
	        {
				if(j+i*maxRowNum>=maxSize){
					break;
				}
				TdGoodsInOut goodsInOut= goodsInOutList.get(j+i*maxRowNum);
	        	row = sheet.createRow((int) j + 1);
	            row.createCell(0).setCellValue(objToString(goodsInOut.getDiySiteName()));//门店名称
	            row.createCell(1).setCellValue(objToString(goodsInOut.getMainOrderNumber()));//主单号
	            row.createCell(2).setCellValue(objToString(goodsInOut.getOrderNumber()));//分单号
	            row.createCell(3).setCellValue(objToString(orderStatus(goodsInOut.getStatusId())));//订单状态
	            row.createCell(4).setCellValue(objToString(dateToString(goodsInOut.getOrderTime(), null)));//订单日期
	            row.createCell(5).setCellValue(objToString(dateToString(goodsInOut.getSalesTime(), null)));//销售日期
	            row.createCell(6).setCellValue(objToString(goodsInOut.getRealName()));//客户名称
	            row.createCell(7).setCellValue(objToString(goodsInOut.getUsername()));//客户电话
	            row.createCell(8).setCellValue(objToString(goodsInOut.getBrandTitle()));//品牌
	            String brand= objToString(goodsInOut.getOrderNumber()).substring(0, 2);
	            if(brand.equals("HR")){
	            	row.createCell(8).setCellValue("华润");
	            }else if(brand.equals("LY")){
	            	row.createCell(8).setCellValue("乐易装");
	            }else if(brand.equals("YR")){
	            	row.createCell(8).setCellValue("莹润");
	            }else{
	            	row.createCell(8).setCellValue("其他");
	            }
	            row.createCell(9).setCellValue(objToString(goodsInOut.getCategoryTitle()));//商品类别
	            row.createCell(10).setCellValue(objToString(goodsInOut.getSellerRealName()));//导购
	            row.createCell(11).setCellValue(objToString(goodsInOut.getDeliverTypeTitle()));//配送方式
	            row.createCell(12).setCellValue(objToString(goodsInOut.getSku()));//产品编号
	            row.createCell(13).setCellValue(objToString(goodsInOut.getGoodsTitle()));//产品名称
	            row.createCell(14).setCellValue(objToString(goodsInOut.getQuantity()));//数量
	            row.createCell(15).setCellValue(objToString(goodsInOut.getPrice()));//单价
	            row.createCell(16).setCellValue(objToString(goodsInOut.getTotalPrice()));//总价
	            row.createCell(17).setCellValue(objToString(changeName(wareHouseList,goodsInOut.getWhNo())));//中转仓
	            row.createCell(18).setCellValue(objToString(goodsInOut.getDeliverRealName()));//配送人员
	            row.createCell(19).setCellValue(objToString(goodsInOut.getDeliverUsername()));//配送人员电话
	            row.createCell(20).setCellValue(objToString(goodsInOut.getShippingAddress()));//地址
	            row.createCell(21).setCellValue(objToString(goodsInOut.getRemarkInfo()));//客户备注
//	            System.out.println("正在生成excel文件的 sheet"+(i+1)+"第"+(j+1)+"行");
			}
//			System.out.println("正在生成excel文件的 sheet"+(i+1));
		}
        
        
//        long endTime = System.currentTimeMillis();
//        System.out.println("用时="+((endTime-startTimne)/1000)+"秒");
        return wb;
        
	}
	
	/**
	 * 代收款报表(1.1修改)
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市编号
 	 * @param username 当前用户
	 * @return
	 */
	private HSSFWorkbook agencyFundWorkBook(Date begin,Date end,String diyCode,String cityName,String username,List<String> roleDiyIds){
		// 第一步，创建一个webbook，对应一个Excel文件 
        HSSFWorkbook wb = new HSSFWorkbook();  
        List<TdAgencyFund> agencyFundList = tdAgencyFundService.searchAgencyFund(begin, end, cityName, diyCode,username,roleDiyIds);
        //excel单表最大行数是65535
        int maxRowNum = 60000;
        int maxSize=0;
        if(agencyFundList!=null){
        	maxSize=agencyFundList.size();
        }
        int sheets = maxSize/maxRowNum+1;
		//写入excel文件数据信息
		for(int i=0;i<sheets;i++){
			
			// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet  
	        HSSFSheet sheet = wb.createSheet("第"+(i+1)+"页");  
	        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short  
	        //列宽
	        int[] widths={13,25,13,13,18,13,13,9,9,9,
	        		9,9,13,13,13,13,13,25,18,18,
	        		25};
	        sheetColumnWidth(sheet,widths);
	        
	        // 第四步，创建单元格，并设置值表头 设置表头居中  
	        HSSFCellStyle style = wb.createCellStyle();  
	        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
	        style.setWrapText(true);


	       	//设置标题
	        HSSFRow row = sheet.createRow((int) 0); 
	        //版本1.1
	        String[] cellValues={"门店名称","主单号","订单状态","导购","订单日期","客户名称","客户电话","需代收金额","实收现金","实收pos",
	        		"实收总金额","欠款","仓库名称","配送人员","配送人电话","收货人","收货人电话","收货人地址","预约配送时间","实际配送时间",
	        		"备注信息"};
			cellDates(cellValues, style, row);
			
			for(int j=0;j<maxRowNum;j++)
	        {
				if(j+i*maxRowNum>=maxSize){
					break;
				}
				TdAgencyFund agencyFund= agencyFundList.get(j+i*maxRowNum);
				
				row = sheet.createRow((int) j + 1);
				//门店名称
				row.createCell(0).setCellValue(objToString(agencyFund.getDiySiteName()));
				//主单号
				row.createCell(1).setCellValue(objToString(agencyFund.getMainOrderNumber()));
				//订单状态
				String statusStr = orderStatus(agencyFund.getStatusId());
				row.createCell(2).setCellValue(objToString(statusStr));
				//导购
				row.createCell(3).setCellValue(objToString(agencyFund.getSellerName()));
				//订单日期
				row.createCell(4).setCellValue(objToString(dateToString(agencyFund.getOrderTime(), null)));
				//客户名称
				row.createCell(5).setCellValue(objToString(agencyFund.getUserName()));
				//客户电话
				row.createCell(6).setCellValue(objToString(agencyFund.getUserPhone()));
				//需代收金额
				row.createCell(7).setCellValue(objToString(agencyFund.getPayPrice()));
				Double payMoney= agencyFund.getPayMoney();
				Double payPos= agencyFund.getPayPos();
				Double payed= agencyFund.getPayed();
				//实收现金
				row.createCell(8).setCellValue(objToString(payMoney));
				//实收pos
				row.createCell(9).setCellValue(objToString(payPos));
				//历史数据没有区分现金和pos这2个字段为空  默认为pos收款
				if((null == payMoney || payMoney==0) && (null ==payPos || payPos==0) && (payed!=null && payed!=0)){
					row.createCell(9).setCellValue(objToString(payed));
				}
				//实收总金额
				row.createCell(10).setCellValue(objToString(payed));
				//欠款
				row.createCell(11).setCellValue(objToString(agencyFund.getOwned()));
				//仓库名称
				row.createCell(12).setCellValue(objToString(agencyFund.getWhName()));
				//配送人员
				row.createCell(13).setCellValue(objToString(agencyFund.getDeliveryName()));
				//配送人电话
				row.createCell(14).setCellValue(objToString(agencyFund.getDeliveryPhone()));
				//收货人
				row.createCell(15).setCellValue(objToString(agencyFund.getShippingName()));
				//收货人电话
				row.createCell(16).setCellValue(objToString(agencyFund.getShippingPhone()));
				//收货人地址
				row.createCell(17).setCellValue(objToString(agencyFund.getShippingAddress()));
				//预约配送时间
				String dayTime = agencyFund.getDeliveryDate();
				dayTime = dayTime + " " + agencyFund.getDeliveryDetailId() + ":30";
				row.createCell(18).setCellValue(objToString(dayTime));
				//实际配送时间
				row.createCell(19).setCellValue(objToString(dateToString(agencyFund.getDeliveryTime(), null)));
				//备注信息
				row.createCell(20).setCellValue(objToString(agencyFund.getRemark()));
	        }
		}
        return wb;
	}
	
	/**
	 * 收款报表
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市编号
 	 * @param username 当前用户

	 */
	private HSSFWorkbook payWorkBook(Date begin,Date end,String diyCode,String cityName,String username,List<String> roleDiyIds){
		// 第一步，创建一个webbook，对应一个Excel文件 
        HSSFWorkbook wb = new HSSFWorkbook();  
        
 
        // 第五步，设置值  
        List<TdGathering> gatheringList = tdGatheringService.searchGathering(begin, end, cityName, diyCode, username,roleDiyIds);
        List<TdWareHouse> wareHouseList = tdWareHouseService.findAll();
//        long startTimne = System.currentTimeMillis();
        
        //excel单表最大行数是65535
        int maxRowNum = 60000;
        int maxSize=0;
        if(gatheringList!=null){
        	maxSize=gatheringList.size();
        }
        int sheets = maxSize/maxRowNum+1;
        
		//写入excel文件数据信息
		for(int i=0;i<sheets;i++){
			
			// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet  
	        HSSFSheet sheet = wb.createSheet("第"+(i+1)+"页");  
	        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short  
	        //列宽
	        int[] widths={8,13,25,25,13,13,13,13,13,13,
	        		13,13,13,13};
	        sheetColumnWidth(sheet,widths);
	        
	        // 第四步，创建单元格，并设置值表头 设置表头居中  
	        HSSFCellStyle style = wb.createCellStyle();  
	        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
	        style.setWrapText(true);


	       	//设置标题
	        HSSFRow row = sheet.createRow((int) 0); 
	        
	        String[] cellValues={"门店名称","门店电话","主单号","分单号","品牌","导购","订单日期","配送方式","使用可提现金额","使用不可提现金额",
					"使用现金劵额度","使用产品券情况","代收款金额","实际代收金额","支付方式","第三方支付金额","欠款","配送人员","配送人电话","收货人","收货人电话",
					"收货人地址","备注信息","订单状态","仓库名称","订单总金额","预约配送时间","实际配送时间"};
			cellDates(cellValues, style, row);
			
			for(int j=0;j<maxRowNum;j++)
	        {
				if(j+i*maxRowNum>=maxSize){
					break;
				}
				TdGathering gathering= gatheringList.get(j+i*maxRowNum);
	        	row = sheet.createRow((int) j + 1);
	        	if (null != gathering.getDiySiteName())
	        	{//门店名称
	            	row.createCell(0).setCellValue(gathering.getDiySiteName());
	    		}
	        	if (null != gathering.getDiySitePhone())
	        	{//门店电话
	            	row.createCell(1).setCellValue(gathering.getDiySitePhone());
	    		}
	        	if (null != gathering.getMainOrderNumber())
	        	{//主单号
	            	row.createCell(2).setCellValue(gathering.getMainOrderNumber());
	    		}
	        	if (null != gathering.getOrderNumber())
	        	{//分单号
	            	row.createCell(3).setCellValue(gathering.getOrderNumber());
	    		}
	        	if (null != gathering.getBrandTitle())
	        	{//品牌
	            	row.createCell(4).setCellValue(gathering.getBrandTitle());
	    		}else{
	    			String brand= gathering.getOrderNumber().substring(0, 2);
	    			if(brand.equals("HR")){
	    				row.createCell(4).setCellValue("华润");
	    			}else if(brand.equals("LY")){
	    				row.createCell(4).setCellValue("乐易装");
	    			}else if(brand.equals("YR")){
	    				row.createCell(4).setCellValue("莹润");
	    			}else{
	    				row.createCell(4).setCellValue("其他");
	    			}
	    		}
	        	if (null != gathering.getSellerRealName())
	        	{//导购
	            	row.createCell(5).setCellValue(gathering.getSellerRealName());
	    		}
	        	if (null != gathering.getOrderTime())
	        	{//订单日期
	        		Date orderTime = gathering.getOrderTime();
	        		String orderTimeStr = orderTime.toString();
	            	row.createCell(6).setCellValue(orderTimeStr);
	    		}
	        	if (null != gathering.getDeliverTypeTitle())
	        	{//配送方式
	            	row.createCell(7).setCellValue(gathering.getDeliverTypeTitle());
	    		}
	        	if (null != gathering.getCashBalanceUsed())
	        	{//使用可提现金额
	            	row.createCell(8).setCellValue(gathering.getCashBalanceUsed());
	    		}
	        	if (null != gathering.getUnCashBalanceUsed())
	        	{//使用不可提现金额
	            	row.createCell(9).setCellValue(gathering.getUnCashBalanceUsed());
	    		}
	        	if (null != gathering.getCashCoupon())
	        	{//使用现金劵金额
	            	row.createCell(10).setCellValue(gathering.getCashCoupon());
	    		}
	        	if (null != gathering.getProductCoupon())
	        	{//使用产品劵情况
	            	row.createCell(11).setCellValue(gathering.getProductCoupon());
	    		}
	        	if (null != gathering.getTotalPrice())
	        	{//代收款金额
	            	row.createCell(12).setCellValue(gathering.getTotalPrice()-(gathering.getUnCashBalanceUsed()==null?0:gathering.getUnCashBalanceUsed())-(gathering.getCashBalanceUsed()==null?0:gathering.getCashBalanceUsed()));
	    		}
	        	if(null!= gathering.getPayed()){//实际代收款金额 
	        			row.createCell(13).setCellValue(gathering.getPayed());
	        			
	        	}
	        	if(null!= gathering.getOwned()){//欠款
	        		row.createCell(16).setCellValue(gathering.getOwned());
	        	}
	        	
	        	if (null != gathering.getPayTypeTitle())
	        	{//支付方式
	            	row.createCell(14).setCellValue(gathering.getPayTypeTitle());
	    		}
	        	if (null != gathering.getOtherPay())
	        	{//第三方支付金额
	            	row.createCell(15).setCellValue(gathering.getOtherPay());
	    		}
	        	if(null!= gathering.getWhNo()){//配送仓库
	        		row.createCell(24).setCellValue(changeName(wareHouseList,gathering.getWhNo()));
	        	}
	        	if(gathering.getRealName() != null){ //配送人员姓名 和电话
	        		row.createCell(17).setCellValue(gathering.getRealName());
	        	}
	        	if(gathering.getUsername() != null){ //配送人员姓名 和电话
	        		row.createCell(18).setCellValue(gathering.getUsername());
	        	}
	        	if (!"门店自提".equals(gathering.getDeliverTypeTitle()))
	        	{
	        		if (null != gathering.getShippingName())
	            	{//收货人姓名
	                	row.createCell(19).setCellValue(gathering.getShippingName());
	        		}
	            	if (null != gathering.getShippingPhone())
	            	{//收获人电话
	                	row.createCell(20).setCellValue(gathering.getShippingPhone());
	        		}
	            	if (null != gathering.getShippingAddress())
	            	{//收货人地址
	                	row.createCell(21).setCellValue(gathering.getShippingAddress());
	        		}
	    		}
	        	if (null != gathering.getRemark())
	        	{//备注信息
	            	row.createCell(22).setCellValue(gathering.getRemark());
	    		}
	        	if (null != gathering.getStatusId())
	        	{//订单状态
	        		String statusStr = orderStatus(gathering.getStatusId());
					row.createCell(23).setCellValue(statusStr);
				}
	        	if (null != gathering.getTotalPrice())
	        	{//订单总金额
					row.createCell(25).setCellValue(gathering.getTotalGoodsPrice());
				}
	        	if (null != gathering.getDeliveryDate()) 
	        	{//预约配送时间
	        		String dayTime = gathering.getDeliveryDate();
	    			dayTime = dayTime + " " + gathering.getDeliveryDetailId() + ":30";
					row.createCell(26).setCellValue(dayTime);
				}
	        	if (null != gathering.getDeliveryTime()) 
	        	{//实际配送时间
					row.createCell(27).setCellValue(gathering.getDeliveryTime().toString());
				}
//	            System.out.println("正在生成excel文件的 sheet"+(i+1)+"第"+(j+1)+"行");
			}
//			System.out.println("正在生成excel文件的 sheet"+(i+1));
		}
        
        
//        long endTime = System.currentTimeMillis();
//        System.out.println("用时="+((endTime-startTimne)/1000)+"秒");
        return wb;
	}
	/**
	 * 销售细报表
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市编号
 	 * @param username 当前用户
	 * @return
	 */
	private HSSFWorkbook salesDetailWorkBook(Date begin,Date end,String diyCode,String cityName,String username,List<String> roleDiyIds){
		// 第一步，创建一个webbook，对应一个Excel文件 
        HSSFWorkbook wb = new HSSFWorkbook();  
        
 
        // 第五步，设置值  
        List<TdSalesDetail> salesDetailList= tdSalesDetailService.queryDownList(begin, end, cityName, diyCode, roleDiyIds);
//      long startTimne = System.currentTimeMillis();

        //excel单表最大行数是65535
        int maxRowNum = 60000;
        int maxSize=0;
        if(salesDetailList!=null){
        	maxSize=salesDetailList.size();
        }
        int sheets = maxSize/maxRowNum+1;
        
		//写入excel文件数据信息
		for(int i=0;i<sheets;i++){
			
			// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet  
	        HSSFSheet sheet = wb.createSheet("第"+(i+1)+"页");  
	        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short  
	        //列宽
	        int[] widths={13,25,25,18,13,18,18,13,13,18,
	        		9,9,9,9,9,13,13,13,18,13,
	        		13,13,13,20,20};
	        sheetColumnWidth(sheet,widths);
	        
	        // 第四步，创建单元格，并设置值表头 设置表头居中  
	        HSSFCellStyle style = wb.createCellStyle();  
	        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
	        style.setWrapText(true);


	       	//设置标题
	        HSSFRow row = sheet.createRow((int) 0); 
	        
	        String[] cellValues={"门店名称","主单号","分单号","下单时间","订单状态","导购","客户名称","客户电话","产品编号","产品名称",
	        		"数量","单价","总价","现金卷","品牌","商品父分类","商品子分类","配送方式","中转仓","配送人员",
	        		"配送人员电话","收货人姓名","收货人电话","收货人地址","订单备注"};
			cellDates(cellValues, style, row);
			
			for(int j=0;j<maxRowNum;j++)
	        {
				if(j+i*maxRowNum>=maxSize){
					break;
				}
				TdSalesDetail salesDetail= salesDetailList.get(j+i*maxRowNum);
	        	row = sheet.createRow((int) j + 1);
	        	//门店名称
	        	row.createCell(0).setCellValue(objToString(salesDetail.getDiySiteName()));

	        	//代付款订单没有主单号  分单号显示到主单号位置
	        	if(salesDetail.getStatusId() != null && salesDetail.getStatusId().equals(2L)){
	        		row.createCell(1).setCellValue(objToString(salesDetail.getOrderNumber()));
	        	}else{
	        		row.createCell(1).setCellValue(objToString(salesDetail.getMainOrderNumber()));
	        		row.createCell(2).setCellValue(objToString(salesDetail.getOrderNumber()));
	        	} 
	        	//下单时间
	        	row.createCell(3).setCellValue(objToString(salesDetail.getOrderTime()));
	        	//订单状态
	        	row.createCell(4).setCellValue(orderStatus(salesDetail.getStatusId()));
	        	//导购
				row.createCell(5).setCellValue(objToString(salesDetail.getSellerRealName()));
				//客户名称
				row.createCell(6).setCellValue(objToString(salesDetail.getRealName()));
				//客户电话
				row.createCell(7).setCellValue(objToString(salesDetail.getUsername()));
				//产品编号
				row.createCell(8).setCellValue(objToString(salesDetail.getSku()));
				//产品名称
				row.createCell(9).setCellValue(objToString(salesDetail.getGoodsTitle()));
				//产品数量
				row.createCell(10).setCellValue(objToString(salesDetail.getQuantity()));
				//产品价格
				row.createCell(11).setCellValue(objToString(salesDetail.getPrice()));
				//产品总价
				row.createCell(12).setCellValue((salesDetail.getTotalPrice()*100)/100);
	        	//现金卷
	            row.createCell(13).setCellValue(objToString(salesDetail.getCashCoupon()));
	          	//品牌
				row.createCell(14).setCellValue(objToString(salesDetail.getBrandTitle()));
	    		//商品父分类
				row.createCell(15).setCellValue(objToString(salesDetail.getGoodsParentTypeTitle()));
				//商品子分类
	        	row.createCell(16).setCellValue(objToString(salesDetail.getGoodsTypeTitle()));
				//配送方式
	        	row.createCell(17).setCellValue(objToString(salesDetail.getDeliverRealName()));
				//中转仓
	            row.createCell(18).setCellValue(objToString(salesDetail.getWhName()));
	    		//配送人员
	        	row.createCell(19).setCellValue(objToString(salesDetail.getDeliverRealName()));
	        	//配送人员电话
	        	row.createCell(20).setCellValue(objToString(salesDetail.getDeliverUsername()));
	        	//收货人姓名
	        	if(!"门店自提".equals(salesDetail.getDeliverTypeTitle())){
	        		row.createCell(21).setCellValue(objToString(salesDetail.getShippingName()));
	        	}
	        	//收货人电话
	        	if(!"门店自提".equals(salesDetail.getDeliverTypeTitle())){
	        		row.createCell(22).setCellValue(objToString(salesDetail.getShippingPhone()));
	        	}
	        	//收货人地址
	        	if(!"门店自提".equals(salesDetail.getDeliverTypeTitle())){
	        		row.createCell(23).setCellValue(objToString(salesDetail.getShippingAddress()));
	        	}
	        	//订单备注
	        	row.createCell(24).setCellValue(objToString(salesDetail.getRemark()));
				
//	            System.out.println("正在生成excel文件的 sheet"+(i+1)+"第"+(j+1)+"行");
			}
//			System.out.println("正在生成excel文件的 sheet"+(i+1));
		}
        
        
//        long endTime = System.currentTimeMillis();
//        System.out.println("用时="+((endTime-startTimne)/1000)+"秒");
        return wb;
        
	}
	/**
	 * 退货报表
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市编号
 	 * @param username 当前用户
	 * @return
	 */
	private HSSFWorkbook returnWorkBook(Date begin,Date end,String diyCode,String cityName,String username,List<String> roleDiyIds){
		// 第一步，创建一个webbook，对应一个Excel文件 
        HSSFWorkbook wb = new HSSFWorkbook();  
        
 
        // 第五步，设置值  
		List<TdReturnReport> returnReportList = tdReturnReportService.searchReturnReport(begin, end, cityName, diyCode, username,roleDiyIds);
		List<TdWareHouse> wareHouseList = tdWareHouseService.findAll();
//        long startTimne = System.currentTimeMillis();
        
        //excel单表最大行数是65535
        int maxRowNum = 60000;
        int maxSize=0;
        if(returnReportList!=null){
        	maxSize=returnReportList.size();
        }
        int sheets = maxSize/maxRowNum+1;
        
		//写入excel文件数据信息
		for(int i=0;i<sheets;i++){
			
			// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet  
	        HSSFSheet sheet = wb.createSheet("第"+(i+1)+"页");  
	        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short  
	        //列宽
	        int[] widths={13,25,20,13,13,13,13,13,13,13};
	        sheetColumnWidth(sheet,widths);
	        
	        // 第四步，创建单元格，并设置值表头 设置表头居中  
	        HSSFCellStyle style = wb.createCellStyle();  
	        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
	        style.setWrapText(true);


	       	//设置标题
	        HSSFRow row = sheet.createRow((int) 0); 
	        
	        String[] cellValues={"退货门店","原订单号","退货单号","退货单状态","品牌","商品类别","导购","订单日期","退货日期","客户名称",
					"客户电话","产品编号","产品名称","退货数量","退货单价","退货金额","客户备注","中转仓","配送人员","配送人电话",
			"退货地址"};
			cellDates(cellValues, style, row);
			
			for(int j=0;j<maxRowNum;j++)
	        {
				if(j+i*maxRowNum>=maxSize){
					break;
				}
				TdReturnReport returnReport= returnReportList.get(j+i*maxRowNum);
	        	row = sheet.createRow((int) j + 1);
	        	if (returnReport.getDiySiteName() != null)
				{//退货门店
					row.createCell(0).setCellValue(returnReport.getDiySiteName());
				}
				if (returnReport.getOrderNumber() != null)
				{//原订单号
					row.createCell(1).setCellValue(returnReport.getOrderNumber());
					row.createCell(9).setCellValue(returnReport.getRealName());//客户名称 
					row.createCell(10).setCellValue(returnReport.getUsername());// 客户电话
					row.createCell(6).setCellValue(returnReport.getSellerRealName());//导购
					//					        	row.createCell(16).setCellValue(returnReport.getCashCoupon());//退现金卷金额
					//					            row.createCell(17).setCellValue(returnReport.getProductCoupon());//退产品卷金额
					row.createCell(20).setCellValue(returnReport.getShippingAddress());//退货地址
					row.createCell(18).setCellValue(returnReport.getDeliverRealName());//配送人员
					row.createCell(19).setCellValue(returnReport.getDeliverUsername());//配送人员电话

				}
				if (returnReport.getReturnNumber() != null)
				{//退货单号
					row.createCell(2).setCellValue(returnReport.getReturnNumber());
				}
				if (returnReport.getStatusId() != null)
				{//退货单状态
					if(returnReport.getStatusId().equals(1L)){
						row.createCell(3).setCellValue("确认退货单");
					}
					if(returnReport.getStatusId().equals(2L)){
						row.createCell(3).setCellValue("通知物流");
					}
					if(returnReport.getStatusId().equals(3L)){
						row.createCell(3).setCellValue("验货确认");
					}
					if(returnReport.getStatusId().equals(4L)){
						row.createCell(3).setCellValue("确认退款");
					}
					if(returnReport.getStatusId().equals(5L)){
						row.createCell(3).setCellValue("已完成");
					}
				}
				if (returnReport.getBrandTitle() != null)
				{//品牌
					row.createCell(4).setCellValue(returnReport.getBrandTitle());
				}
				if (returnReport.getCategoryTitle() != null)
				{//商品类别
					row.createCell(5).setCellValue(returnReport.getCategoryTitle());
				}
				if (returnReport.getOrderTime() != null)
				{//订单日期
					row.createCell(7).setCellValue(returnReport.getOrderTime().toString());
				}
				if (returnReport.getCancelTime() != null)
				{//退货日期
					row.createCell(8).setCellValue(returnReport.getCancelTime().toString());
				}
				if (returnReport.getSku() != null)
				{//产品编号
					row.createCell(11).setCellValue(returnReport.getSku());
				}
				if (returnReport.getGoodsTitle() != null)
				{//产品名称
					row.createCell(12).setCellValue(returnReport.getGoodsTitle());
				}
				if (returnReport.getQuantity() != null)
				{//退货数量
					row.createCell(13).setCellValue(returnReport.getQuantity());
				}
				if (returnReport.getPrice() != null)
				{//退货单价
					row.createCell(14).setCellValue(returnReport.getPrice());
				}

				if (returnReport.getQuantity() != null && returnReport.getPrice() != null)
				{//退货总价
					row.createCell(15).setCellValue(returnReport.getQuantity()*returnReport.getPrice());
				}

				if(returnReport.getRemarkInfo() != null){//客户备注
					row.createCell(16).setCellValue(returnReport.getRemarkInfo());
				}
				if(returnReport.getWhNo() != null){//中转仓
					row.createCell(17).setCellValue(changeName(wareHouseList,returnReport.getWhNo()));
				}
//	            System.out.println("正在生成excel文件的 sheet"+(i+1)+"第"+(j+1)+"行");
			}
//			System.out.println("正在生成excel文件的 sheet"+(i+1));
			
		}
        
        
//        long endTime = System.currentTimeMillis();
//        System.out.println("用时="+((endTime-startTimne)/1000)+"秒");
        return wb;
	}
	
	private String objToString(Object obj){
		if(obj==null){
			return "";
		}
		return obj.toString();
	}
	
	
}
