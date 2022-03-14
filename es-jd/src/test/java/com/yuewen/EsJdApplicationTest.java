package com.yuewen;

import com.alibaba.fastjson.JSON;
import com.yuewen.pojo.User;
import com.yuewen.utils.EScons;
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
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author wangshiyang
 * @since 2022/2/14
 **/
@SpringBootTest
public class EsJdApplicationTest {
    RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                    new HttpHost("127.0.0.1", 9200, "http")
            ));

    // 后边有时间可以探究一下 ElasticsearchTemplate 的使用
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    // 测试索引的创建
    @Test
    public void testCreateIndex() throws IOException {
        // 创建索引请求 (所有的请求都是基于request进行创建的) PUT wang_index
        CreateIndexRequest request = new CreateIndexRequest("wang_index");
        // 客户端执行创建请求  获得请求后的响应
        CreateIndexResponse createIndexResponse =
                client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    // 获取索引
    @Test
    public void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("wang_index");
        // 这里只是创建请求并没有执行，需要通过client客户端来执行
        boolean response = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }
    // 删除索引
    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("wang_index");
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

    // 添加文档
    @Test
    public void testAddDocument() throws IOException {
        // 创建对象
        User user = new User("wang2", 18);
        // 创建请求
        IndexRequest request = new IndexRequest("wang_index", "_doc");
        // 规则： PUT /wang_index/_doc/1
        request.id("2");
        // 下边两种超时时间的设置是一样的
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");
        // 将我们的数据放入请求  这里放进去就可以了，不需要拿到这个请求
        // 将对象转换为JSON 放入到请求中去即可
        request.source(JSON.toJSONString(user), XContentType.JSON);

        // 客户端发送请求  获取响应结果
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
        System.out.println(response.status());  // 输出对应命令返回的状态
    }

    // 获取文档  判断是否存在
    @Test
    public void testDocIsExists() throws IOException {
        GetRequest request = new GetRequest("wang_index", "_doc", "1");
        // 不获取返回的 _source的上下文了
        // 这里的request对象可以.出来很多的方法，自己可以进行测试
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");

        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 获取文档的信息
    @Test
    public void testGetDoc() throws IOException {
        GetRequest request = new GetRequest("wang_index", "_doc", "1");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
        // 这里的source是包含在response里边的，主要是文档内容，也就是我们的数据，可以直接通过API获取出来
        // 对于response里边的 _index, _type, _id, _source等都可以直接通过API获取
        System.out.println(response.getSourceAsString());
    }

    // 更新文档的信息
    @Test
    public void testUpdateDoc() throws IOException {
        UpdateRequest request = new UpdateRequest("wang_index", "_doc", "1");
        request.timeout("1s");

        User user = new User("流浪码工", 20);
        // 和 kibana里边使用的方法是一致的，在doc里边放入具体的内容
        // 往请求中加入内容
        request.doc(JSON.toJSONString(user), XContentType.JSON);

        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
    }

    // 删除文档记录
    @Test
    public void testDeleteRequest() throws IOException {
        DeleteRequest request = new DeleteRequest("wang_index", "_doc", "3");
        request.timeout("1s");
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
    }

    // 特殊的，真实的项目一般都会有批量插入数据的操作
    // Es 的数据一般都是从数据库中进行导入或者从消息队列中获取然后导入进去
    // 所以数据不可能一条一条的往里边放，要是写一个多线程的方法来放也比较麻烦
    @Test
    public void testBulkRequest() throws IOException {
        BulkRequest request = new BulkRequest();
        request.timeout("10s");
        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("wang1", 17));
        userList.add(new User("wang11", 17));
        userList.add(new User("wang111", 17));
        userList.add(new User("wang12", 17));
        userList.add(new User("wang122", 17));
        userList.add(new User("wang13", 17));
        userList.add(new User("wang133", 17));
        userList.add(new User("wang14", 17));
        userList.add(new User("wang144", 17));
        userList.add(new User("wang15", 17));
        userList.add(new User("wang155", 17));

        // 将数据批量加入到请求中，不直接执行，后边使用client进行执行
        for (int i = 0; i < userList.size(); i++) {
            // 批量添加和批量删除只需要在这里修改对应的请求就可以了
            request.add(
                    new IndexRequest("wang_index")
                        .type("_doc")  //
                        .id("" + (i+1))  // 给插入的数据设置 ID 否则就会生成一个随机ID
                        .source(JSON.toJSONString(userList.get(i)), XContentType.JSON)
            );
        }
        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(response.hasFailures());
    }

    // 查询
    // SearchRequest 搜索请求
    // SearchSourceBuilder 条件构造
    // 对应不同功能的实现，如高亮，精确查询，查询全部等都需要构建对应的***Builder
    // 通常对于索引名和type名都是会设置对应的常量，一般不会写这样的变量，写错就坏了
    @Test
    public void testSearch() throws IOException {
        SearchRequest request = new SearchRequest(EScons.ES_INDEX);
        // 构建搜索条件  高亮/排序/分页搜索等
        // 获取查询构造器    对应的知识点是  设计模式--构造器模式
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 查询条件  具体的查询方式和使用kibana基本一致，通过 . 的方式查看具体的方法
        // 这里使用QueryBuilders工具类来实现  比较方便
        // QueryBuilders.termQuery  精确匹配
        // QueryBuilders.matchAllQuery  匹配所有

        // 配置查询条件
        TermQueryBuilder query = QueryBuilders.termQuery("name", "wang1");
        // MatchAllQueryBuilder query = QueryBuilders.matchAllQuery();

        // 将查询条件放入到构造器中
        sourceBuilder.query(query);
        // 到这里 sourceBuilder 就构建完成了
        // 如果想要构建分页  有默认值
        // sourceBuilder.from();
        // sourceBuilder.size();
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // 将构建得到的条件放入到请求中去
        request.source(sourceBuilder);
        // 执行请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(response.getHits()));
        System.out.println("------------下边遍历显示结果-----------------");
        for (SearchHit documentFields : response.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }
    }

}
