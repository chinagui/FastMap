<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
		<meta http-equiv="content-type" content="text/html" charset="UTF-8" />
		<title>FastMap Monitor</title>
	</head>

	<body bgcolor="#DDDDDD">
		<form id="form" action="/monitor/search/log?" method="get" target="_blank">
			<table width="100" align="center">
				<caption>
					<h1>日志监控中心</h1></caption>
				<tr height="50" bgcolor="azure">
					<td><input style="height:40px; font-size: large;" size="100" id="logId" name="logId" placeholder="请输入查询的日志id" /> </td>
					<td><input style="height:40px; font-size: large;" id="submit"  type="submit" value="搜索"/></td>
				</tr>
			</table>
		</form>

	</body>
</html>