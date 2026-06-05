package com.usst.thumbs.controller;

import com.usst.thumbs.model.vo.MessageVO;
import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.service.MessageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/message")
@RestController
public class MessageController {
    @Resource
    private MessageService messageService;

    @GetMapping("/unread/count")
    public Result<Long> getUnReadCount(HttpServletRequest request){
        return ResultUtils.success(ResultType.SUCCESS, messageService.getUnReadCount(request), "查询成功");
    }
    @GetMapping("/page")
    public Result<List<MessageVO>> pageRes(@RequestParam(defaultValue = "1") Integer pageNo,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           HttpServletRequest request){
        return ResultUtils.success(ResultType.SUCCESS,messageService.pageMessage(pageNo,pageSize,request),"查询成功");
    }

    @PostMapping("/read/{messageId}")
    public Result<Boolean> read(HttpServletRequest request,@PathVariable("messageId") Long messageId){
        return ResultUtils.success(ResultType.SUCCESS, messageService.readMessage(request, messageId), "消息已读");
    }

    @PostMapping("/readall")
    public Result<Boolean> readAll(HttpServletRequest request){
        return ResultUtils.success(ResultType.SUCCESS, messageService.readAll(request), "全部已读");
    }

    @DeleteMapping("/{messageId}")
    public Result<Boolean> delete(@PathVariable Long messageId, HttpServletRequest request) {
        return ResultUtils.success(messageService.deleteMessage(messageId,request));
    }
}
