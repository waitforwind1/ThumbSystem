package com.usst.thumbs.listener;

import com.usst.thumbs.Repository.BlogRepository;
import com.usst.thumbs.common.constant.BlogIndexEventConstant;
import com.usst.thumbs.common.constant.RabbitMQConstant;
import com.usst.thumbs.common.exception.BusinessException;
import com.usst.thumbs.mapper.ConsumeRecordsMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.BlogIndexEvent;
import com.usst.thumbs.model.es.BlogEsDoc;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.BlogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.usst.thumbs.common.constant.RabbitMQConstant.BLOG_INDEX_CONSUMER_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogIndexEventConsumer {

    private final BlogService blogService;
    private final BlogRepository blogRepository;
    private final ConsumeRecordsMapper consumeRecordsMapper;

    @RabbitListener(queues = RabbitMQConstant.BLOG_INDEX_QUEUE)
    @Transactional (rollbackFor = BusinessException.class)
    public void blogIndexEventConsumer(final BlogIndexEvent event) {
        int records = consumeRecordsMapper.insertConsumeRecords(event.getEventId(), BLOG_INDEX_CONSUMER_NAME);
        if(records == 0) {
            log.info("事件{}+{}已经消费过了 不可重复消费",event.getEventId(),BLOG_INDEX_CONSUMER_NAME);
            return;
        }
        if (BlogIndexEventConstant.DELETE_ACTION.equals(event.getOperation())) {
            try {
                blogRepository.deleteById(event.getBlogId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }

        Blog blog = blogService.getById(event.getBlogId());
        if (blog == null) {
            throw new BusinessException(ResultType.NOT_FOUND, "文章不存在");
        }

        BlogEsDoc blogEsDoc = new BlogEsDoc();
        BeanUtils.copyProperties(blog, blogEsDoc);
        try {
            blogRepository.save(blogEsDoc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
