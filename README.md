monkey
======
	一个基于Zookeeper的分布式调度系统,可批量执行任务,并返回状态,可邮件通知及短信通知用户
	
目录结构:
		.
|-- Makefile    编译方法
|-- childId.bak 程序运行的ID(切勿手动删除)
|-- conf				常用配置文件
|   |-- log4j.properties	日志输出配置文件
|   |-- monkey						执行的脚本
|   |-- monkey.yaml				调度配置文件
|   `-- updown						小工具,用于上载与下载常用文件(需要搭建nginx环境,修改IP等)
|-- jar
|   `-- monkey.jar				调度的核心jar包
|-- monkey.spec						打包rpm的spec文件
`-- state									状态文件夹,收集日志记录信息
    |-- monkey.log				用户级别的日志输出
    `-- system.log				系统级别的日志输出,主要用于收集zookeeper自身的日志文件
	
用法:
	客户端:
		monkey start					启动客户端(IP及端口配置在monkey.yaml文件中)
		monkey stop						关闭monkey客户端
		monkey restart				重新启动
		monkey log						查看monkey.log文件
	服务端:
		目前服务端采用一次任务一次启动的方式,使用方法有多种格式如下:
		1）、monkey server cmd			命令如果有空格,需要用""号引住,且中间若有特殊符号,需要转义
				cmd 格式: 
					monkey --only hostnames -c"cmd"
					monkey --only hostnames -c"cmd" --conf total=num1 less=num2 timeout=num3(单位为s)
				如:
				monkey server "monkey --only hostname1,hostname2,hostname3 -c\"ls -al /\" " 或
				monkey server "monkey --only hostname1,hostname2,hostname3 -c\"ls -al /\" " --conf total=3 less=3 timeout=30
				
				--conf 后的配置含义:
					<total> 总共多少设备将执行任务
					<less>	最少完成多少算此次任务成功
					<timeout> 一次任务超时时间
				如果不指定,默认配置见monkey.yaml
				
				以上执行的命令,使用的是启动客户端的用户,如果需要用其他用户,可如下执行:
					monkey --only hostnames -c"su - user -c\"cmd\""
					monkey --only hostnames -c"su - user -c\"cmd\"" --conf total=num1 less=num2 timeout=num3(单位为s)
				
		2）、monkey server user hostname cmd <user>客户端执行命令的用户 <hostname>执行命令的设备 <cmd> 待执行的命令,符合linux 行命令格式即可,且用引号引住,如"ls -al /"
		切记:
				命令格式2是为简化操作,进行的简单封装,使用如:
				monkey server root hostname1,hostname2 "ls -al /"
				
		3）、monkey hive user hostname cmd localOutFile remoteIP remoteOutFile
				<localOutFile> 客户端hive输出重定向的文件
				<remoteIP> 远程的IP,用于将不同地方的文件输出到一个机器的目录里,目前采用的传输为netty传输,故格式为:<IP:端口>
				<remoteOutFile>最终机器的目录
				执行的方式有三种:
					monkey hive user hostname cmd		查询后,查看
					monkey hive user hostname cmd localOutFile	查询后重定向到文件
					monkey hive user hostname cmd localOutFile remoteIP remoteOutFile	查询后重定向到文件且传输到一个设备中
					
				如:
					monkey hive root hostname1 "show databases;"
					monkey hive root hostname1 "show databases;" /root/a.out
					monkey hive root hostname1 "show databases;" /root/a.out 127.1.1.0:8888 /root/all/