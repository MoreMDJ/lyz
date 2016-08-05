package com.ynyes.lyz.interfaces.utils;

public class InterfaceConfigure 
{
	/*
	 * 根据不同的服务器注释不同的部分
	 */
	
	/*-----测试环境 start----*/
	
	
	/**
	 * 在测试服务器抛给WMS的WEBSERVICE接口地址
	 */
	public static String WMS_WS_URL = "http://182.92.160.220:8199/WmsInterServer.asmx?wsdl";
	
	/**
	 * 在测试服务器抛给EBS的WEBSERVICE接口地址
	 */
	public static String EBS_WS_URL = "http://erptest.zghuarun.com:8030/webservices/SOAProvider/plsql/cux_app_webservice_pkg/?wsdl";
	
	/**
	 * 在测试环境的微信回调地址
	 */
	public static String WX_NOTIFY_RETURN_URL = "http://123.57.32.143:8080/pay/wx_notify";
	
	
	/*-----正式环境 start----*/
	
//	public static String WMS_WS_URL = "http://101.200.75.73:8999/WmsInterServer.asmx?wsdl";
//	
//	public static String EBS_WS_URL = "http://erpap.zghuarun.com:8008/webservices/SOAProvider/plsql/cux_app_webservice_pkg/?wsdl";
//	
//	public static String WX_NOTIFY_RETURN_URL = "http://101.200.128.65:8080/pay/wx_notify";
	
	/*-----正式环境    end----*/
}
