package com.yuewen.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author wangshiyang
 * @since 2022/2/14
 **/
public interface ContentService {
    Boolean parseContent(String keyword) throws IOException;

    List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException;
    List<Map<String, Object>> searchPageHighlightBuilder(String keyword, int pageNo, int pageSize) throws IOException;
}
