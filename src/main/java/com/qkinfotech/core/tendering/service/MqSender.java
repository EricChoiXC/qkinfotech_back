package com.qkinfotech.core.tendering.service;

import com.qkinfotech.util.SpringUtil;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Configuration
public class MqSender {

    public void sendMq(String key,String obj) throws Exception {
        Environment ev = SpringUtil.getEnvironment();
        // 1. 获取连接工厂
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
                ev.getProperty("spring.activemq.username"),
                ev.getProperty("spring.activemq.password"),
                ev.getProperty("spring.activemq.broker-url")
        );

        // 2. 获取一个向activeMq的连接
        Connection connection = factory.createConnection();
        // 3. 获取session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // 4.找目的地，获取destination，消费端，也会从这个目的地取消息
//        Queue queue = session.createQueue("SYS_NEWS_FILE_SIZE_LISTENER");
        Queue queue = session.createQueue(key);

        // 5.1 消息创建者
        MessageProducer producer = session.createProducer(queue);

        // 5.2. 创建消息
        /*
         * {
         * 	"newsId": "ekp公告id"
         * 	"signId": "ekp报名信息id",
         * 	"supplierId": "ekp供应商id",
         * 	"size": "5"
         * }
         */
        TextMessage textMessage = session.createTextMessage(obj);
        // 5.3 向目的地写入消息
        producer.send(textMessage);

        // 6.关闭连接
        connection.close();
    }




}
