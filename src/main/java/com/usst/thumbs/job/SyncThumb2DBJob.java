package com.usst.thumbs.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.model.enums.ThumbTypeEnum;
import com.usst.thumbs.service.ThumbsService;
import com.usst.thumbs.utils.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// 定时任务 定时将redis中的临时点赞记录同步到数据库中
@Component
@Slf4j
public class SyncThumb2DBJob {
    @Resource
    private ThumbsService thumbsService;

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        log.info("开始执行");

        LocalDateTime nowDate = LocalDateTime.now();

        // 如果秒数为 0~9，则回到上一分钟的 50 秒
        int second = (nowDate.getSecond() / 10 - 1) * 10;

        if (second == -10) {
            second = 50;

            // 回到上一分钟
            nowDate = nowDate.minusMinutes(1);
        }

        String date = nowDate.format(DateTimeFormatter.ofPattern("HH:mm:")) + String.format("%02d", second);

        syncThumb2DBByDate(date);

        log.info("临时数据同步完成");
    }

    public void syncThumb2DBByDate(String date){
        String tempThumbKey = RedisKeyUtil.getTempThumbsKey(date);
        Map<Object,Object>  allTempThumbKey = redisTemplate.opsForHash().entries(tempThumbKey);
        Map<Long,Long> blogThumbCountMap = new HashMap<>();
        ArrayList<Thumbs> thumbsList = new ArrayList<>();
        LambdaQueryWrapper<Thumbs> wrapper = new LambdaQueryWrapper<>();
        boolean needRemove = false;
        // 获取到所有临时记录的KEY-VALUE，每个做更新
        for(Object userIdBlogIdObj: allTempThumbKey.keySet()){
            String userIdBlogId  =(String)userIdBlogIdObj;
            String[] userIdAndblogId = userIdBlogId.split(StringPool.COLON);
            Long userId = Long.valueOf(userIdAndblogId[0]);
            Long blogId = Long.valueOf(userIdAndblogId[1]);
            // 判断点赞记录的类型
            Integer thumbType = Integer.valueOf(allTempThumbKey.get(userIdBlogId).toString());
            // 点赞类型-->同步到数据库
            if(thumbType == ThumbTypeEnum.INCR.getValue()){
                Thumbs thumbs = new Thumbs();
                thumbs.setBlogId(blogId);
                thumbs.setUserId(userId);
                thumbsList.add(thumbs);
            }
            // 取消点赞
            else if(thumbType==ThumbTypeEnum.DECR.getValue()){
                needRemove = true;
                wrapper.or().eq(Thumbs::getUserId,userId).eq(Thumbs::getBlogId,blogId);
            }
            // 无变化
            else {
                if(thumbType!=ThumbTypeEnum.NON.getValue()){
                    log.warn("数据异常：{}",userId+""+blogId+""+thumbType);
                }
                continue;
            }
            blogThumbCountMap.put(blogId,blogThumbCountMap.getOrDefault(blogId,0L)+thumbType);
        }
        thumbsService.saveBatch(thumbsList);
        if(needRemove)
            thumbsService.remove(wrapper);
        if(!blogThumbCountMap.isEmpty())
            blogMapper.batchUpdateThumbCount(blogThumbCountMap);
        Thread.startVirtualThread(()->{
            redisTemplate.delete(tempThumbKey);
        });
    }

}
