package com.yuewen.controller;

import com.yuewen.service.ContentService;
import com.yuewen.service.impl.ContentServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author wangshiyang
 * @since 2022/2/14
 **/
@RestController
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentServiceImpl contentService) {
        this.contentService = contentService;
    }

    // 解析爬取的数据，放入到ES中
    @GetMapping("/parse/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword) throws IOException {
        return contentService.parseContent(keyword);
    }

    // 分页查询
    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> searchPage(@PathVariable("keyword") String keyword,
                                                @PathVariable("pageNo") int pageNo,
                                                @PathVariable("pageSize") int pageSize) throws IOException {
        return contentService.searchPageHighlightBuilder(keyword, pageNo, pageSize);
    }
}
