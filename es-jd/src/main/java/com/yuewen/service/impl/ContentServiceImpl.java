package com.yuewen.service.impl;

import com.alibaba.fastjson.JSON;
import com.yuewen.pojo.Content;
import com.yuewen.service.ContentService;
import com.yuewen.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
//import org.elasticsearch.search.sourceBuilder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wangshiyang
 * @since 2022/2/14
 **/
@Service
public class ContentServiceImpl implements ContentService {
    private final RestHighLevelClient restHighLevelClient;

    public ContentServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    // 1、解析数据  放入到 es 的索引中
    public Boolean parseContent(String keywords) throws IOException {
        // 通过关键字进行解析  解析后得到的是一个集合
        List<Content> contents = HtmlParseUtil.parseJD(keywords);
        // 把查到的数据放入到es中  然后进行入库操作  批量添加
        BulkRequest request = new BulkRequest();
        request.timeout("2m");

        for (Content content : contents) {
            // 将数据批量的放入到请求中
            // 这里没有设置ID 那么使用的就是随机 ID
            request.add(
                    new IndexRequest("jd_goods", "_doc")
                            .source(JSON.toJSONString(content), XContentType.JSON)
            );
        }

        // 客户端执行批量插入的方法，将放入到请求中的数据批量放入到ES中
        BulkResponse bulk = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    // 获取这些数据实现搜索功能
    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        if (pageNo < 1) pageNo = 1;
        // 条件搜索
        SearchRequest request = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 分页
        sourceBuilder.from((pageNo-1)*pageSize);
        sourceBuilder.size(pageSize);

        // 精准匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQuery);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 执行搜索
        request.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        // 解析结果
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit documentFields : response.getHits().getHits()) {
            // 将所有的结果遍历出来，封装到list里边返回出去
            list.add(documentFields.getSourceAsMap());
        }

        return list;
    }

    // 获取这些数据实现搜索高亮功能
    public List<Map<String, Object>> searchPageHighlightBuilder(String keyword, int pageNo, int pageSize) throws IOException {
        if (pageNo < 1) pageNo = 1;
        // 条件搜索
        SearchRequest request = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 分页
        sourceBuilder.from((pageNo-1)*pageSize);
        sourceBuilder.size(pageSize);

        // 精准匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQuery);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 设置高亮字段
        highlightBuilder.field("title");
        // 关闭多个高亮的设置
        highlightBuilder.requireFieldMatch(false);
        // 设置前后缀
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        // 执行搜索
        request.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        // 解析结果
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {

            // 解析高亮的字段  实际上就是将原来的字段置换为我们设置的高亮字段即可
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            // 高亮前的结果
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 置换高亮字段
            if (title != null){
                Text[] fragments = title.fragments();
                String newTitle = "";
                for (Text text : fragments) {
                    newTitle += (text);
                }
                sourceAsMap.put("title", newTitle);  // 高亮字段替换掉原来的内容
            }
            list.add(sourceAsMap);
        }

        return list;
    }

}
