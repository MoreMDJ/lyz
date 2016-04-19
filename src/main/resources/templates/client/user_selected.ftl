<!DOCTYPE html>
<html lang="zh-CN" class="bgc-f3f4f6">
    <head>
        <meta charset="UTF-8">
        <meta name="keywords" content="">
        <meta name="copyright" content="" />
        <meta name="description" content="">
        <meta name="viewport" content="width=device-width,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no" />
        <title>乐易装</title>
        <!-- css -->
        <link rel="stylesheet" type="text/css" href="/client/css/my_base.css"/>
        <link rel="stylesheet" type="text/css" href="/client/css/x_common.css"/>
        <link rel="stylesheet" type="text/css" href="/client/css/x_order_manage.css"/>
        <!-- js -->
        <script src="/client/js/jquery-1.11.0.js" type="text/javascript"></script>
        <script src="/client/js/user_selected.js" type="text/javascript"></script>
        <script src="/client/js/seller_order.js" type="text/javascript"></script>
    </head>
    <body class="bgc-f3f4f6">
        <#-- 引入警告提示样式 -->
        <#include "/client/common_warn.ftl">
        <#-- 引入等待提示样式 -->
        <#include "/client/common_wait.ftl">
        <#-- 引入会员用户信息列表 -->
        <#include "/client/order_user_list.ftl">
        <!-- 头部 -->
        <header>
            <a class="back" href="<#if history??>${history }<#else> javascript:history.go(-1);</#if>"></a>
            <p>我的已选</p>
            <a id="info" class="btn-edit">编辑</a>
        </header>
        <!-- 头部 END -->
        
        <!-- 我的已选 -->
        <div id="my_selected">
            <#include "/client/selected_goods_and_color.ftl">
        </div>
        <!-- 我的已选 END -->
        <div class="clear h50"></div>
        <!-- 底部 -->
        <footer>
        	<a class="btn-clearing" href="javascript:void(0);" style="width:50%;float:left;background:#ffaa00">查看赠品</a>
            <a class="btn-clearing" href="javascript:seller.checkCart();" style="width:50%;float:left;">去结算</a>
        </footer>
        <!-- 底部 END -->
    </body>
</html>