package com.yuewen.utils;

import com.yuewen.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wangshiyang
 * @since 2022/2/14
 **/
@Component
public class HtmlParseUtil {
//    public static void main(String[] args) throws IOException {
//        parseJD("码出高效").forEach(System.out::println);
//    }  不使用main方法运行的化，就直接丢到Spring中就可以使用了

    public static List<Content> parseJD(String keyWords) throws IOException {
        // 获取请求：http://search.jd.com/Search?keyword=java
        // 前提：需要联网

        String url = "http://search.jd.com/Search?keyword="+keyWords;
        // 解析网页。（Jsoup 返回的Document就是浏览器Document对象）
        Document document = Jsoup.parse(new URL(url), 30000);
        // 所有在Js中可以使用的方法这里都可以使用
        Element element = document.getElementById("J_goodsList");
//        System.out.println(element);
        // 获取所有的li元素
        Elements elements = element.getElementsByTag("li");
        // 获取元素中的内容
        // 创建一个list
        ArrayList<Content> goodsList = new ArrayList<>();
        for (Element el : elements) {
            // 对于图片特别多的网站，所有的图片都是延迟加载的
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            // 对于爬取出来的数据如何处理呢？  直接封装一个对象即可
            Content content = new Content();
            content.setImg(img);
            content.setPrice(price);
            content.setTitle(title);
            goodsList.add(content);
        }
        return goodsList;
    }
}
