<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="../../jessma-base.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="${__base}">
    
    <title>测试 Bean 装配 - 1</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<link rel="icon" href="favicon.ico" type="image/x-icon">
  </head>
  
  <body>
  <br>
  <div align="right">
  	<a href="index.action">首&nbsp;页</a>
  </div>
    <br><br><br><br>
    <div align="center">
    	<form action="test/checkBean_1.action" method="post">
    		First Name: <input type="text" name="firstName" value="丑">
    		<br>
    		Last Name: <input type="text" name="lastName" value="怪兽">
    		<br>
    		Birthday: <input type="text" name="birthday" value="1978-11-03">
    		<br>
    		Gender: 男 <input type="radio" name="gender" value="false">
    			&nbsp;女 <input type="radio" name="gender" value="true" checked="checked">
    		<br>
    		Working age: <select name="working-Age">
    			<option value="-1">-请选择-</option>
    			<option value="3">三年</option>
    			<option value="5" selected="selected">五年</option>
    			<option value="10">十年</option>
    			<option value="20">二十年</option>
    		</select>
    		<br>
    		Interest: 游泳 <input type="checkbox" name="its" value="1" checked="checked">
	    		&nbsp;打球 <input type="checkbox" name="its" value="2" checked="checked">
	    		&nbsp;下棋 <input type="checkbox" name="its" value="3">
	    		&nbsp;打麻将 <input type="checkbox" name="its" value="4">
	    		&nbsp;看书 <input type="checkbox" name="its" value="5" checked="checked">
    		<br>
			<br>
			<input type="submit" value="确 定">&nbsp;&nbsp;<input type="reset" value="重 置">
    	</form>    	
    </div>
  </body>
</html>
