<%@page pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.fckeditor.net" prefix="FCK"%> 
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String imgPath = basePath + "image/";
%>
<!DOCTYPE html>
<html>
      <head>
             <title>日向blog</title>
             <meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
             <link type="text/css" rel="stylesheet" href="<%=basePath %>css/fixed_background.css" media="all" />
             <script type="text/javascript" src="<%=basePath %>Jquery/jquery-1.3.1.js"></script>
             <script type="text/javascript" src="<%=basePath %>Jquery/jquery.form.js"></script>    
             <script type="text/javascript">
 			  $(document).ready(function() { 
				  $('#articleForm').ajaxForm({ 
			             dataType:      'json',
						 beforeSubmit:  validate,   
						 success:       successFunc
			   	  }); 
		       });
			   function validate(formData, jqForm, options) {
			       for(var i=0; i < formData.length; i++) {
			        	if(!formData[i].value) {
			        		return false;
			        	}
			        } 
			        var queryString = $.param(formData);
                    return true; 
				}
				function successFunc(data) {
					if (data.success) {
						location.href = "/RiXiang_blog/space/list.form";
					}
					else {
						alert(data.info);
					}
				}
               </script>
      </head>
      <body>
             <div id="scene"> 
                  <img src="<%=imgPath%>mainPageBanner.png" ALT=""/> 
             </div>
              <div id = "article_list">
                   <h2>添加文章</h2>
                   <form id="articleForm" action="writeArticle.form" method="post">
                               <label>标题：</label>
                              <input type="text" name="title" style="height:25px;width:150px;"/><br>
                               <label>内容：</label>
                               <FCK:editor instanceName="articleContent" basePath="/fckeditor" toolbarSet="myToolbar" height="400"></FCK:editor>
                               &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
                               <input name="提交" type="submit" class="button" style="height:30px;width:100px;background:black;color:white" value="写完了（￣ c￣）y" />
                  </form>
			  </div>

              <div id="col_right">
                    <div id="menu">
                          <h2>主页导航</h2>
                          <ul>
                              <li><a href ="/RiXiang_blog/article/list.form">主页</a></li>
                              <li><c:if test="${!empty userName}"><a href ="/RiXiang_blog/space/list.form">个人空间 - ${userName}</a></c:if></li>
                              <li><a href ="/RiXiang_blog/game/snake.form">贪吃蛇</a></li>
                              <li><a href ="/RiXiang_blog/sonne/sonne.form">作者-博客开发记录</a></li>
                          </ul>
                    </div>
              </div>
      </body>
</html>