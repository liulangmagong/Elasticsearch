package com.yuewen;

import com.alibaba.fastjson.JSON;
import com.yuewen.pojo.User;
import com.yuewen.utils.EsConstant;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * @author wangshiyang
 * @since 2022/2/14
 **/
@SpringBootTest
public class YuewenEsApiApplicationTest {

    RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
            new HttpHost("127.0.0.1", 9200, "http")
                ));

    // 测试索引的创建  Request
    @Test
    public void testCreateIndex() throws IOException {
        // 1. 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("jd_goods");
        // 2. 客户端执行请求
        CreateIndexResponse createIndexResponse =
                 client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    // 测试获取索引
    @Test
    public void testExistsIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("kuang_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 测试删除索引
    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("kuang_index");
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }

    // 测试添加文档
    @Test
    public void testAddDocument() throws IOException {
        // 创建对象
        User user = new User("狂神说python", 32);
        // 创建请求
        IndexRequest request = new IndexRequest("kuang_index", "_doc");
        // 规则 put /kuang_index/_doc/1
        request.id("3");
        request.timeout("1s");
        // 将我们的数据放入到请求  通过使用kibana可以知道，请求携带进去的是一个json串
        request.source(JSON.toJSONString(user), XContentType.JSON);

        // 客户端发送请求, 获取响应结果
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
        System.out.println(response.status());
    }

    // 获取文档  判断是否存在
    @Test
    public void testIsExists() throws IOException {
        GetRequest request = new GetRequest("kuang_index", "_doc", "1");
        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 获取文档信息
    @Test
    public void testGetDocument() throws IOException {
        GetRequest request = new GetRequest("kuang_index", "_doc", "1");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        // 打印文档内容，后边会转换为map
        System.out.println(response.getSourceAsString());
        System.out.println(response);
    }

    // 更新文档信息
    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("kuang_index", "_doc", "1");
        request.timeout("1s");
        User user = new User("狂神说Java", 18);
        request.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        // 打印文档内容，后边会转换为map
        System.out.println(response.status());
    }

    // 删除文档信息
    @Test
    public void testDeleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("kuang_index", "_doc", "3");
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
    }

    // 批量添加数据
    @Test
    public void testBulkAddDocument() throws IOException {
        BulkRequest request = new BulkRequest();
        request.timeout("10s");

        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("kuang1", 3));
        userList.add(new User("kuang2", 34));
        userList.add(new User("kuang3", 3));
        userList.add(new User("yuewen1", 3));
        userList.add(new User("yuewen2", 3));
        userList.add(new User("yuewen3", 3));

        for (int i = 0; i < userList.size(); i++) {
            request.add(
                    new IndexRequest("kuang_index", "_doc")
                            .id("" + (i+1))  // 不设置ID 就会生成随机的ID
                            .source(JSON.toJSONString(userList.get(i)), XContentType.JSON));
        }

        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(response.hasFailures());
    }

    // 查询
    @Test
    public void testSearch() throws IOException {
        SearchRequest request = new SearchRequest("kuang_index");
        // 构建搜索条件
        SearchSourceBuilder builder = new SearchSourceBuilder();

        // 查询条件  具体的查询方式和使用kibana基本一致，通过 . 的方式查看具体的方法
        // 这里使用QueryBuilders工具类来实现
        // QueryBuilders.termQuery  精确匹配
        // QueryBuilders.matchAllQuery  匹配所有

        TermQueryBuilder query = QueryBuilders.termQuery("name", "yuewen1");
//        MatchAllQueryBuilder query = QueryBuilders.matchAllQuery();
        builder.query(query);
        builder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        request.source(builder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(response.getHits()));
        System.out.println("-----------------------------");
        for (SearchHit documentFields : response.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }
    }

}


