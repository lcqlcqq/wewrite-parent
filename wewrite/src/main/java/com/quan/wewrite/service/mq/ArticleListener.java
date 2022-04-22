package com.quan.wewrite.service.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 监听消息，设置主题，消费者组。消息持久化，不会丢失
 * 删除缓存
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "blog-deleteArticle-failed",consumerGroup = "blog-deleteFailed-group")
public class ArticleListener implements RocketMQListener<Set<String>> {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Override
    public void onMessage(Set<String> message) {
        log.info("缓存删除失败：{}",message);
        //重新删除缓存
        Long aLong = redisTemplate.delete(message);
        log.info(String.valueOf(aLong));
        if (aLong <= 0) {
            log.info("MQ删除缓存失败，keys：{}",message);
        }
    }
}
