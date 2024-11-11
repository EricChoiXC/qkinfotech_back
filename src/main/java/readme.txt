					<compilerArgs>
						<arg>--add-opens=jdk.compiler/com.sun.tools.javac.api=qkinfotech.hibernate</arg>
						<arg>--add-opens=jdk.compiler/com.sun.tools.javac.processing=qkinfotech.hibernate</arg>
						<arg>--add-opens=jdk.compiler/com.sun.tools.javac.tree=qkinfotech.hibernate</arg>
						<arg>--add-opens=jdk.compiler/com.sun.tools.javac.util=qkinfotech.hibernate</arg>
						<arg>--add-exports=jdk.compiler/com.sun.tools.javac.api=qkinfotech.hibernate</arg>
						<arg>--add-exports=jdk.compiler/com.sun.tools.javac.processing=qkinfotech.hibernate</arg>
						<arg>--add-exports=jdk.compiler/com.sun.tools.javac.tree=qkinfotech.hibernate</arg>
						<arg>--add-exports=jdk.compiler/com.sun.tools.javac.util=qkinfotech.hibernate</arg>
					</compilerArgs>
										<compilerArgs>
						<arg>--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
						<arg>--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
						<arg>--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
						<arg>--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
					</compilerArgs>
					<annotationProcessorPaths>
						<path>
							<groupId>com.qkinfotech</groupId>
							<artifactId>hibernate-extension</artifactId>
							<version>0.0.1-SNAPSHOT</version>
						</path>
					</annotationProcessorPaths>
					<annotationProcessors>
    					com.qkinfotech.extension.CompositionProcessor
  					</annotationProcessors>



框架：

1. 统一日志
2. 文档扩展机制
3. 微服务、分布式，单机
4. 缓存机制
5. 权限机制
6. 数据过滤机制
7. 存储机制
8. 接口
9. 导入导出

引擎：
1. 任务引擎
2. 流程引擎
3. 规则引擎
4. 业务引擎
5. 脚本引擎
6. 版本管理

QTask 任务调度服务说明

1. 三张表

   任务队列：存放 即时任务，普通定时任务，系统定时任务
   
   历史任务执行日志：任务队列执行后，迁移数据。
    
   		主要字段 fdId，fdTaskId（任务队列的ID，但是，不做外键关联，所有信息复制到本表）
   
   任务执行结果：作为历史任务执行日志的子表，存放执行时的输出，异常，返回值 
   		
   		主要字段 fdId，fdTaskExecId（历史任务执行日志表的ID，做外键关联）
   
2. 调度器

   任务的调度，通过 IQTaskQueue#take，获取可执行的任务。 
   
3. 执行器

   任务执行的方式。目前支持：Bean执行器，类执行器，HTTP执行器，WebService执行器。可扩展，如：支持微服务的服务调用
   
   任务类型：
   	
   		即时任务：执行返回、异常即视为任务执行完成。自动更新任务状态。
   		
   		异步任务：执行时，先返回执行节点标识，通过开启线程执行。需要在任务完成后，通过发送任务正常、异常结束消息更新任务状态
   
4. 对外接口、服务(Rest)
   
   注册任务，执行任务，删除任务，查找任务，
   
5. 消息

   发送：开始调度，停止调度，任务分发，调度激活
   
   接收：调度激活，任务开始执行，任务正常结束，任务异常结束
   
6. 根据 application.properties 中 Eureka 配置，自动注册对外服务(参看 4. 对外服务）
   
   扩展支持 nacos, zookeeper
   
   参考：
   	通过配置切换微服务客户端 
   		https://blog.csdn.net/weixin_45493694/article/details/122298587
   	同时注册nacos 和 ueraka
   		https://blog.csdn.net/lmchhh/article/details/125034678?spm=1001.2101.3001.6650.8&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-8-125034678-blog-122298587.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-8-125034678-blog-122298587.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=9
   	
   
6. 支持单机，集群，微服务三种方式

   单机：使用 Spring ApplicationEvent 实现消息推送

   集群、微服务：使用 RabbitMQ 发送，接收消息，并使用 Spring ApplicationEvent 实现单节点的消息推送

7. 管理界面

   支持对任务的增、删，改，查，执行等任务
   
   支持对节点调度器的监控（集群，微服务时，可集中监控）