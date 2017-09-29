# 动态更改Aliyun DNS解析 Java


* 解决动态IP地址绑定域名问题

	
### 什么情况可以用

* 如果你的域名是在阿里购买可以直接用
* 如果你的域名没在阿里云，你可以使用阿里云的解析，把原始域名的DNS服务器切换到阿里上面


东西很简单，其实就是Ali API的调用


**配置说明 path_confong.js**

```
var configs = {
	"AccessKeyID" : "你的Key",
	"AccessKeySecret" : "你的Secret",
	ipchecker : {
		url : "http://2017.ip138.com/ic.asp",
		header : {},
		charset : "GBK",
		ipreg : "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))"
	},
	configs : [ 
	/* 可以同时跟新很多组 */
	{
		name : "demo.com",
		dns : [ "app", "www"],
		ipchecker : {
			url : "http://2017.ip138.com/ic.asp",
			header : {},
		}
	} ]
}

```

### 自动更新

* 生成为jar包 然后 通过计划任务执行命令
	
	在 cmd 窗口执行  
		
		
		java -jar update_ip.jar path_confong.js
		
	
* 可以部署到web项目里面去，然后通过容器来执行
