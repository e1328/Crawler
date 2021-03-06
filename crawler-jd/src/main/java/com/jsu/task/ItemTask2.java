package com.jsu.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsu.pojo.Item;
import com.jsu.service.ItemService;
import com.jsu.util.HttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @auther wenlongzhou
 * @date 2019/6/18 19:57
 */

@Component
public class ItemTask2 {

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private ItemService itemService;

    @Autowired
    private static final ObjectMapper MAPPER = new ObjectMapper();

    //当下载任务完成后，间隔多长时间进行下一次的任务
    @Scheduled(fixedDelay = 100 * 1000)
    public void itemTask() throws Exception {
        //声明需要解析的初始地址
        String url = "https://search.yhd.com/c0-0/k%25E7%2594%25B5%25E8%2584%2591/#&sort=1&page=";

        //按照页面对手机的搜索结果进行遍历解析
        for (int i=1; i<=50; i++) {
            String html = httpUtils.doGetHtml(url + i);

            //解析页面，获取商品数据并存储
            this.parse(html);
            System.out.println("第"+i+"页爬取完成");
        }

        System.out.println("数据抓取完成");
    }

    //解析页面，获取商品数据并存储
    private void parse(String html) throws Exception {
        //解析html获取Document
        Document doc = Jsoup.parse(html);

        //获取spu
        Elements itemEles = doc.select("#itemSearchList > div");
        for (Element itemEle : itemEles) {
            //System.out.println(itemEle);
            Item item = new Item();
            Element div = itemEle.select("[pagetype]").first();
            String sku = div.attr("comproid");
            //System.out.println(div);
            String price_str = div.select(".proPrice").first().text();
            int index = price_str.indexOf(".");
            price_str = price_str.substring(2,index+2);
            Double price = Double.parseDouble(price_str);
            item.setPrice(price);
            String title = div.select(".proName a").first().attr("title");
            item.setTitle(title);
            String itemUrl = div.select(".proName a").first().attr("href");
            item.setUrl(itemUrl);
            String itemDoc = this.httpUtils.doGetHtml("https:" + itemUrl);
            Document picDoc = Jsoup.parse(itemDoc);
            String picUrl = "http://img10.360buyimg.com/n1/s360x360_" + picDoc.select("img#J_prodImg").first().attr("original_src");
            this.httpUtils.doGetImage(picUrl);
            item.setPic(picUrl);

            item.setCreated(new Date());
            item.setUpdated(new Date());

            this.itemService.save(item);
        }

//        for (Element spuEle : spuEles) {
//            //获取spu
//            int spu = Integer.parseInt(spuEle.attr("data-spu"));
//
//            //获取sku
//            Elements skuEles = spuEle.select("li.ps-item");
//            for (Element skuEle : skuEles) {
//                //获取sku
//                int sku = Integer.parseInt(skuEle.select("[data-sku]").attr("data-sku"));
//
//                //根据sku查询商品数据
//                Item item = new Item();
//                item.setSku(sku);
//                List<Item> list = this.itemService.findAll(item);
//
//                if (list.size() > 0) {
//                    //如果商品存在，就进行下一个循环，该商品不保存，因为已存在
//                    continue;
//                }
//
//                //设置商品的spu
//                item.setSpu(spu);
//
//                //获取商品的详情的url
//                String itemUrl = "https://item.jd.com/" + sku + ".html";
//                System.out.println(itemUrl);
//                item.setUrl(itemUrl);
//
//                //获取商品的图片
//                String picUrl = "https:" + skuEle.select("img[data-sku]").first().attr("data-lazy-img");
//                picUrl = picUrl.replace("/n9/","/n1/");
//                String picName = this.httpUtils.doGetImage(picUrl);
//                item.setPic(picName);
//
//                //获取商品的价格
//                String priceJson = this.httpUtils.doGetHtml("https://p.3.cn/prices/mgets?skuIds=J_" + sku);
//                Double price = MAPPER.readTree(priceJson).get(0).get("p").asDouble();
//                item.setPrice(price);
//
//                //获取商品的标题
//                String itemInfo = this.httpUtils.doGetHtml(itemUrl);
//                String title = Jsoup.parse(itemInfo).select("div.sku-name").text();
//                item.setTitle(title);
//
//                item.setCreated(new Date());
//
//                item.setUpdated(new Date());
//
//                //保存商品数据到数据库中
//                this.itemService.save(item);
//            }
//        }
    }

}
