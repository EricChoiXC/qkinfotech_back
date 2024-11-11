package com.qkinfotech.app.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.file.FileManager;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.task.ITask;
import com.qkinfotech.core.task.TaskBuilder;
import com.qkinfotech.core.task.TaskDispatcher;
import com.qkinfotech.core.task.TaskLogger;
import com.qkinfotech.core.task.model.TaskMain;
import com.qkinfotech.core.org.model.OrgAuth;
import com.qkinfotech.core.org.model.OrgDept;
import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.org.model.OrgTitle;


//@Component
public class TestUserTask implements ITask, ApplicationRunner {

	@Override
	public void execute(TaskLogger logger, JSONObject parameter) {
		System.out.println("task begin:" + TestUserTask.class.getName());
		try {
			logger.write("hello world");
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
		}
		System.out.println("task finished:" + TestUserTask.class.getName());
	}

	@Autowired
	TaskDispatcher taskDispatcher;	
	
	@Autowired
	SimpleService<TaskMain> taskMainService;
	
	@Autowired
	FileManager fileManager;

	
	@Autowired
	SimpleService<OrgAuth> orgAuthService;

	@Autowired
	SimpleService<OrgPerson> orgPersonService;

	@Autowired
	SimpleService<OrgTitle> orgTitleService;
	
//	@Autowired
//	SimpleService<OrgDept> orgDeptService;
	
	
	@Override
	@Transactional
	public void run(ApplicationArguments args) throws Exception {
		
		(new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(5*1000);

					TaskBuilder.builder().beanName("testUserTask").build().add();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		})).start();
		
		//fileManager.create(new File("E:/MiniPC/UltraISO/ultraiso.chm"));
//		try(SysFileInputStream in = fileManager.getInputStream("01HVQTCDRT41FTX5V2DZE3H7VZ")){
//			long length = 206795;
//			FileOutputStream fos = new FileOutputStream("E:\\storage\\2024\\04\\18\\aaa.chm");
//			byte[] buffer = new byte[1024];
//	        int read;
//	        while(length > 0) {
//	        	if(length > 1024) {
//	        		read = in.read(buffer, 0, 1024);
//	        	} else {
//	        		read = in.read(buffer, 0, (int)length);
//	        	}
//	            fos.write(buffer, 0, read);
//	            length -= read;
//	        }
//			in.close();
//			fos.close();
//		}
		
		
//		List<OrgAuth> list = orgAuthService.findAll();
//		System.out.println("All Auth:" + list.size());
//		for(OrgAuth auth : list ) {
//			System.out.println(auth.getfName() + ":" +  auth.getfElements().size());
//		}
	
		
//		OrgTitle title = new OrgTitle();
//		title.setfLevel(9);
//		title.setfName("程序员");
//		title.setfValid(true);
//		title.getfId();
//		orgTitleService.save(title);
		
//		OrgPerson person = new OrgPerson();
//		person.setfGender("male");
//		person.setfName("张三");
//		person.setfValid(true);
//		orgPersonService.save(person);
		
//		List<OrgPerson> persons = orgPersonService.findAll();
//		for(person :persons) {
//			System.out.println(person.getfId() + person.getfName());
//		}
		
		/*
		 * OrgTitle orgTitle1 = orgTitleService.getById("01HW6YT23A4V3CXGXAPT98EHD9");
		 * orgTitle1.setfLevel(0);
		 * 
		 * OrgTitle orgTitle2 = orgTitleService.getById("01HW6YT23A4V3CXGXAPT98EHD9");
		 * 
		 * System.out.println(orgTitle1.getfLevel()+","+orgTitle2.getfLevel());
		 */
		
	}
}
