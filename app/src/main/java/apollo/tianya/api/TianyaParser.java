package apollo.tianya.api;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apollo.tianya.bean.DataSet;
import apollo.tianya.bean.Entity;
import apollo.tianya.bean.Post;
import apollo.tianya.bean.Thread;
import apollo.tianya.util.DateTime;
import apollo.tianya.util.Transforms;

/**
 * Created by Texel on 2016/6/3.
 */
public class TianyaParser {

    /**
     * 解析推荐的帖子 (http://www.tianya.cn/m/find/index.shtml)
     *
     * @param source HTML string
     * @return
     */
    public static DataSet<Thread> parseRecommendThread(String source) {
        DataSet<Thread> datas = null;
        List<Thread> list = null;
        Thread thread = null;
        Pattern pattern = null;
        Matcher matcher = null;
        Matcher sub_matcher = null;
        String item = null;
        String match_content = null;

        list = new ArrayList<>();
        // 解析回帖信息
        pattern = Pattern.compile("(?s)<li>(.*?)</li>");
        matcher = pattern.matcher(source);
        while (matcher.find()) {
            item = matcher.group(1);
            thread = new Thread();
            list.add(thread);

            // 解析URL
            pattern = Pattern.compile("(?s)<a href=\"(.*?)\"[^>]*>");
            sub_matcher = pattern.matcher(item);
            if (sub_matcher.find()) {
                match_content = sub_matcher.group(1);
                thread.setUrl(match_content);
            }

            // 解析标题
            pattern = Pattern.compile("<h3 class=\"look-title\">(.*?)</h3>");
            sub_matcher = pattern.matcher(item);
            if (sub_matcher.find()) {
                match_content = sub_matcher.group(1);
                match_content = Transforms.stripHtmlXmlTags(match_content);
                thread.setTitle(match_content);
            }

            // 解析作者
            pattern = Pattern.compile("<span class=\"look-author\">文/(.*?)</span>");
            sub_matcher = pattern.matcher(item);
            if (sub_matcher.find()) {
                match_content = sub_matcher.group(1);
                thread.setAuthor(match_content);
            }

            // 解析板块名
            pattern = Pattern.compile("<span class=\"look-item\">(.*?)</span>");
            sub_matcher = pattern.matcher(item);
            if (sub_matcher.find()) {
                match_content = sub_matcher.group(1);
                thread.setSection(match_content);
            }

            // 解析访问量
            pattern = Pattern.compile("<span class=\"look-v-num\">(.*?)</span>");
            sub_matcher = pattern.matcher(item);
            if (sub_matcher.find()) {
                match_content = sub_matcher.group(1);
                match_content = Transforms.stripHtmlXmlTags(match_content);
                if (!TextUtils.isEmpty(match_content))
                    thread.setViews(Integer.parseInt(match_content));
            }
        }
        datas = new DataSet<Thread>();
        datas.setObjects(list);
        datas.setTotalRecords(list.size());
        return datas;
    }

    public static DataSet<Thread> parseHotThread(String source) {
        DataSet<Thread> datas = null;
        List<Thread> list = null;
        Thread thread = null;
        Pattern pattern = null;
        Matcher matcher = null;
        Matcher sub_matcher = null;
        String item = null;
        String match_content = null;

        list = new ArrayList<>();
        // 解析回帖信息
        pattern = Pattern.compile("(?s)<li>(.*?)</li>");
        matcher = pattern.matcher(source);
        while (matcher.find()) {
            item = matcher.group(1);
            thread = new Thread();
            list.add(thread);

            // 解析URL
            pattern = Pattern.compile("(?s)<a href=\"(.*?)\"[^>]*>");
            sub_matcher = pattern.matcher(item);
            if (sub_matcher.find()) {
                match_content = sub_matcher.group(1);
                thread.setUrl(match_content);
            }

            // 解析标题
            pattern = Pattern.compile("<p class=\"title\">(.*?)</p>");
            sub_matcher = pattern.matcher(item);
            if (sub_matcher.find()) {
                match_content = sub_matcher.group(1);
                match_content = Transforms.stripHtmlXmlTags(match_content);
                thread.setTitle(match_content);
            }
        }
        datas = new DataSet<Thread>();
        datas.setObjects(list);
        datas.setTotalRecords(list.size());
        return datas;
    }

    /**
     * 解析一个主题的所有帖子 (http://bbs.tianya.cn/m/post-no04-2668855-1.shtml)
     * @param source
     * @return
     */
    public static DataSet<Post> parsePosts(String source) {
        DataSet<Post> datas = null;
        List<Post> list = null;
        Post post = null;
        Document doc = null;
        Elements elms = null;
        Element bd = null;
        Element item = null;

        list = new ArrayList<Post>();
        doc = Jsoup.parse(source);
        elms = doc.select("div.content div.item");
        if (elms == null || elms.size() ==0)
            return null;

        for(Element elm:elms) {
            post = new Post();

            // 解析内容
            bd = elm.select(".bd").first();
            if (bd == null)
                continue;

            post.setBody(bd.text());

            // 解析时间
            item = elm.select("a p").first();
            post.setPostDate(DateTime.parse(item.text(), "yyyy-MM-dd HH:mm").getDate());

            // 解析作者
            post.setAuthor(elm.attr("data-user"));
            post.setAuthorId(Integer.parseInt(elm.select("h4").first().attr("data-id")));

            // 解析PostId
            if (elm.hasAttr("data-replyid"))
                post.setId(Integer.parseInt(elm.attr("data-replyid")));

            list.add(post);
        }
        post = list.get(0);

        // 解析标题
        post.setTitle(doc.select("div.title h1").text());

        // 解析帖子访问量
        int views = 0;
        item = doc.select("i.icon-view").first();
        views = Integer.parseInt(item.text());
        post.setViews(views);

        // 解析回帖数量
        int replies = 0;
        item = doc.select("i.icon-reply").first();
        replies = Integer.parseInt(item.text());
        post.setReplies(replies);

        // 解析页码
        int total_pages = 0;

        elms = doc.select(".u-pager span");
        if (elms != null && elms.size() > 2) {
            item = elms.get(1);
            total_pages = Integer.parseInt(item.text());
        }

        datas = new DataSet<Post>();
        datas.setObjects(list);

        if (total_pages < 2)
            datas.setTotalRecords(list.size());
        else
            datas.setTotalRecords(total_pages * 100);
        return datas;
    }

    /**
     * 简单解析一个页面内容
     * @param source
     * @return
     */
    public static Post parsePage(String source) {
        Post post = new Post();
        Document doc = null;

        doc = Jsoup.parse(source);
        post.setBody(doc.select("body").text());
        post.setPostDate(new Date());
        return post;
    }

    /**
     * 从页面上解析出用户Id
     * @param source
     * @return
     */
    public static int parseUserId(String source) {
        int userId = 0;
        Pattern pattern = null;
        Matcher matcher = null;
        String match_content = null;

        pattern = Pattern.compile("(?s)\"http://tx.tianyaui.com/logo/(.*?)\"");
        matcher = pattern.matcher(source);
        if (matcher.find()) {
            match_content = matcher.group(1);
            userId = Integer.parseInt(match_content);
        }
        return userId;
    }

    /**
     * 解析一个主贴的图片内容 (http://bbs.tianya.cn/m/post-no04-2668855-1.shtml)
     * @param source
     * @return
     */
    public static List<String> parseThreadImage(String source) {
        List<String> list = null;
        Document doc = null;
        Elements elms = null;

        doc = Jsoup.parse(source);
        elms = doc.select("div.item-zt img");
        list = new ArrayList<String>();
        for(Element elm:elms) {
            list.add(elm.attr("original"));
        }
        return list;
    }

    /**
     * 解析内容中的第一张图片
     * @param source
     * @return
     */
    public static String parseImage(String source) {
        String src = null;
        Document doc = null;
        Element elm = null;

        doc = Jsoup.parse(source);
        elm = doc.select("img").first();
        if (elm != null)
            src = elm.attr("src");

        return src;
    }
}
