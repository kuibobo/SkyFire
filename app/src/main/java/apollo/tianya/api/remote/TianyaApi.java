package apollo.tianya.api.remote;


import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apollo.tianya.AppConfig;
import apollo.tianya.AppContext;
import apollo.tianya.R;
import apollo.tianya.api.ApiHttpClient;
import apollo.tianya.bean.Section;
import apollo.tianya.bean.Thread;
import apollo.tianya.util.DateTime;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Texel on 2016/5/27.
 */
public class TianyaApi {

    private static String TAG = "TianyaApi";

    private static DisplayImageOptions mOptions = new DisplayImageOptions.Builder()
                                                        .cacheInMemory(true)
                                                        .cacheOnDisk(true)
                                                        .bitmapConfig(Bitmap.Config.RGB_565)
                                                        .displayer(new FadeInBitmapDisplayer(300))
                                                        .build();
    /**
     * 登录
     * @param username
     * @param password
     * @param handler
     */
    public static void login(String username, String password, String captcha, Header cookie_header,
                             final AsyncHttpResponseHandler handler) {
        RequestParams params = null;
        String loginurl = null;
        AsyncHttpResponseHandler _hld = null;
        Header[] headers = null;
        Header header_referer = null;

        params = new RequestParams();
        params.put("vwriter", username);
        params.put("vpassword", password);
        params.put("vc", captcha);
        params.put("rmflag", 1);
        params.put("__sid", "1#4#1.0#a0b0eb92-404d-4ea1-a45c-ad5bdbc439bf");
        params.put("action", "b2.1.1102|7341eb362f75554bf2e0a56029769c43|7b774effe4a349c6dd82ad4f4f21d34c|Mozilla/5.0 (Windows NT 10.0; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0|0|3|v2.2");

        header_referer = new ApiHttpClient.HttpHeader("Referer", "http://www.tianya.cn");
        if (cookie_header == null)
            headers = new Header[] {header_referer};
        else
            headers = new Header[] {header_referer, cookie_header};

        loginurl = "https://passport.tianya.cn/login?from=index&_goto=login&returnURL=http://www.tianya.cn";

        _hld = new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] responseHeaders, byte[] responseBody) {
                String body = null;
                String querys = null;
                String url = null;
                Pattern pattern = null;
                Matcher matcher = null;
                Header[] headers = null;

                body = new String(responseBody);
                pattern = Pattern.compile("&t=(.*)");
                matcher = pattern.matcher(body);
                if(matcher.find()) {
                    querys = matcher.group();
                    querys = querys.substring(1, querys.length() - 2);
                } else {
                    handler.sendFailureMessage(statusCode, responseHeaders, responseBody, null);
                    return;
                }

                headers = new Header[1];
                headers[0] = new ApiHttpClient.HttpHeader("Referer", "http://passport.tianya.cn/online/loginSuccess.jsp?fowardurl=http%3A%2F%2Fwww.tianya.cn%2F1749397&userthird=index&regOrlogin=%E7%99%BB%E5%BD%95%E4%B8%AD......&" + querys);

                url = "http://passport.tianya.cn/online/domain.jsp?" + querys + "&domain=tianya.cn";
                ApiHttpClient.get(url, headers, null, handler);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                handler.sendFailureMessage(statusCode, headers, responseBody, error);
            }
        };
        ApiHttpClient.post(loginurl, headers, params, _hld);
    }

    /**
     * 获取验证码
     * @param handler
     */
    public static void getCaptcha(AsyncHttpResponseHandler handler) {
        Header[] headers = null;
        String url = "https://passport.tianya.cn/staticHttps/validateImgProxy.jsp";

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Referer", "https://passport.tianya.cn/login");

        ApiHttpClient.post(url, headers, handler);
    }

    /**
     * 获取用户id
     * @param name
     * @param handler
     */
    public static void getUserId(String name, final AsyncHttpResponseHandler handler) {
        String transf_url = "http://my.tianya.cn/info/" + name;

        ApiHttpClient.get(transf_url, handler);
    }

    /**
     * 获取用户头像
     * @param userId
     * @param handler
     */
    public static void displayAvatar(int userId, ImageView image) {
        String url = "http://tx.tianyaui.com/logo/" + userId;

        displayImage(url, image);
    }

    /**
     * 获取相册内容
     * @param url
     * @param handler
     */
    public static void displayImage(String url, ImageView image) {
//        Header[] headers = null;
//
//        headers = new Header[1];
//        headers[0] = new ApiHttpClient.HttpHeader("Referer", "https://www.tianya.cn");
//
//        ApiHttpClient.get(url, headers, handler);

        ImageLoader.getInstance().displayImage(url, image, mOptions);
    }

    /**
     * 获取推荐内容
     * @param handler
     */
    public static void getRecommendThread(AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/m/find/index.shtml";

        ApiHttpClient.get(url, handler);
    }

    /**
     * 获取热贴内容
     * @param handler
     */
    public static void getHotThread(int pageIndex, AsyncHttpResponseHandler handler) {
        String url = "http://bbs.tianya.cn/m/hotArticle.jsp?pageNum=" + pageIndex;

        ApiHttpClient.get(url, handler);
    }

    /**
     * 获取栏目内容 
     * @param channel
     * @param handler
     */
    public static void getChannel(String channel, AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/m/find/" + channel + "/index.shtml";

        ApiHttpClient.get(url, handler);
    }

    /**
     * 获取收藏
     * @param pageIndex
     * @param handler
     */
    public static void getBookMarks(int pageIndex, AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/api/tw?method=bbsArticleMark.select&params.pageSize=20&params.pageNo=" + pageIndex;
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    /**
     * 获取收藏的板块
     * @param handler
     */
    public static void getSectionBookMarks(AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/api/tw?method=userblock.ice.selectItems";
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    /**
     * 获取用户发表的帖子
     * @param userId
     * @param publicNextId
     * @param techNextId
     * @param cityNextId
     * @param handler
     */
    public static void getUserThreads(int userId, int publicNextId, int techNextId, int cityNextId, AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/api/tw?method=userinfo.ice.getUserTotalArticleList&params.userId="
                + userId + "&params.pageSize=20&params.bMore=true&params.publicNextId="
                + publicNextId + "&params.techNextId="
                + techNextId + "&params.cityNextId="
                + cityNextId;

        ApiHttpClient.get(url, handler);
    }

    /**
     * 获取用户回复的帖子
     * @param userId
     * @param publicNextId
     * @param techNextId
     * @param cityNextId
     * @param handler
     */
    public static void getUserPosts(int userId, int publicNextId, int techNextId, int cityNextId, AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/api/tw?method=userinfo.ice.getUserTotalReplyList&params.userId="
                + userId + "&params.pageSize=20&params.bMore=true&params.publicNextId="
                + publicNextId + "&params.techNextId="
                + techNextId + "&params.cityNextId="
                + cityNextId;
        ApiHttpClient.get(url, handler);
    }

    /**
     * 获取用户访问历史
     * @param handler
     */
    public static void getUserHistory(AsyncHttpResponseHandler handler) {
        String url = "http://bbs.tianya.cn/view_art.jsp";
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }
    /**
     * 获取一个主题的所有帖子
     * @param sectionId 板块Id
     * @param threadId 主题Id
     * @param pageIndex 页码
     * @param handler
     */
    public static void getPosts(String sectionId, String threadId, int pageIndex, AsyncHttpResponseHandler handler) {
        String post_url = "http://bbs.tianya.cn/m/post-" + sectionId + "-" + threadId + "-" + pageIndex + ".shtml";

        getPosts(post_url, handler);
    }

    /**
     * 读取某一个主题的帖子
     * @param thread
     * @param handler
     */
    public static void getPosts(Thread thread, AsyncHttpResponseHandler handler) {
        String post_url = "http://bbs.tianya.cn/m/post-" + thread.getSectionId() + "-" + thread.getGuid() + "-1.shtml";

        getPosts(post_url, handler);
    }
    /**
     * 获取一个主题的所有帖子
     * @param post_url 主题的URL
     * @param handler
     */
    public static void getPosts(String post_url, AsyncHttpResponseHandler handler) {
        ApiHttpClient.get(post_url, handler);
    }

    public static void createPost(String sectionId, String threadId, String title, String content, AsyncHttpResponseHandler handler) {
        String url = null;
        String referer = null;
        RequestParams params = new RequestParams();
        Header[] headers = null;

        if (TextUtils.isEmpty(threadId)) {
            url = "http://bbs.tianya.cn/api?method=bbs.ice.compose";
            referer = "http://bbs.tianya.cn/post-" + sectionId + "-" + threadId + "-1.shtml";
        } else {
            url = "http://bbs.tianya.cn/api?method=bbs.ice.reply";
            referer = "http://bbs.tianya.cn/post-" + sectionId + "-" + threadId + "-1.shtml";
        }
        headers = new Header[11];
        headers[0] = new ApiHttpClient.HttpHeader("Referer", referer);
        headers[1] = new ApiHttpClient.HttpHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; rv:18.0) Gecko/20100101 Firefox/18.0");
        headers[2] = new ApiHttpClient.HttpHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        headers[3] = new ApiHttpClient.HttpHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        headers[4] = new ApiHttpClient.HttpHeader("Accept-Encoding", "gzip, deflate");
        headers[5] = new ApiHttpClient.HttpHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers[6] = new ApiHttpClient.HttpHeader("X-Requested-With", "XMLHttpRequest");
        headers[7] = new ApiHttpClient.HttpHeader("Connection", "keep-alive");
        headers[8] = new ApiHttpClient.HttpHeader("Pragma", "no-cache");
        headers[9] = new ApiHttpClient.HttpHeader("Cache-Control", "no-cache");
        headers[10] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        content += AppContext.getInstance().getResources().getString(R.string.post_content_tail);
        params.put("params.action", "");
        params.put("params.appBlock", sectionId);
        params.put("params.appId", "bbs");
        params.put("params.artId", threadId);
        params.put("params.bScore", "true");
        params.put("params.bWeibo", "false");
        params.put("params.content", content);
        params.put("params.item", sectionId);
        params.put("params.postId", threadId);
        params.put("params.prePostTime", Long.toString(DateTime.now().getTime()));
        params.put("params.preTitle", title);
        params.put("params.preUrl", "");
        params.put("params.preUserId", "");
        params.put("params.preUserName", "");
        params.put("params.sourceName", "iTianya");
        params.put("params.title", title);
        params.put("params.appId", "3");

        ApiHttpClient.post(url, headers, params, handler);
    }

    public static void getNotices(AsyncHttpResponseHandler handler) {
        String url = "http://bbs.tianya.cn/api?method=bbs.api.getUserNoticeCount";
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    public static void getNoticesEx(AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/api/tw?method=messagecount.ice.select";
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    /**
     * 清理通知
     * @param type 1-回复, 2-评论, 3-@
     * @param handler
     */
    public static void clearNotice(int type, AsyncHttpResponseHandler handler) {
        String url = "http://bbs.tianya.cn/api?method=bbs.api.clearUserNoticeCount&params.type=" + type;
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    public static void getMessages(int pageIndex, int pageSize, AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/api/msg?method=messagenew.selectmessage&params.isAll=1&params.isFollow=0&params.pageSize=" + pageSize + "&params.pageNo=" + pageIndex;
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    public static void getNotifications(int pageIndex, int pageSize, AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/api/msg?method=messagesys.selectmessage&params.pageSize=" + pageSize + "&params.pageNo=" + pageIndex;
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    public static void getReplies(int pageIndex, int pageSize, AsyncHttpResponseHandler handler) {
        String url = "http://bbs.tianya.cn/api?method=bbs.api.getUserReplyNoticeList&params.pageNum=" + pageIndex + "&params.pageSize=" + pageSize;
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    public static void getComments(int pageIndex, int pageSize, AsyncHttpResponseHandler handler) {
        String url = "http://bbs.tianya.cn/api?method=bbs.api.getUserCommentNoticeList&params.pageNum=" + pageIndex + "&params.pageSize=" + pageSize;
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    public static void getFollows(int pageIndex, int pageSize, AsyncHttpResponseHandler handler) {
        String url = "http://bbs.tianya.cn/api?method=bbs.api.getUserAttentionNoticeList&params.pageNum=" + pageIndex + "&params.pageSize=" + pageSize;
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    /**
     * 将帖子加入到书签
     * @param thread
     * @param handler
     */
    public static void addBookmark(Thread thread, AsyncHttpResponseHandler handler) {
        String url = null;
        Header[] headers = null;
        int userId = 0;

        userId = AppContext.getInstance().getLoginUserId();
        url = "http://www.tianya.cn/api/tw?method=bbsArticleMark.insert&header.userId=" + userId
                + "&params.blockId=" + thread.getSectionId()
                + "&params.articleId=" + thread.getId()
                + "&params.title=" + thread.getTitle()
                + "&params.authorId=" + thread.getAuthorId() + "&params.authorName=" + thread.getAuthor() + "&params.markResId=0&params.markFloorId=0";
        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    /**
     * 将帖子移除书签
     * @param thread
     * @param handler
     */
    public static void removeBookmark(Thread thread, AsyncHttpResponseHandler handler) {
        String url = null;
        Header[] headers = null;
        int userId = 0;
        userId = AppContext.getInstance().getLoginUserId();
        url = "http://www.tianya.cn/api/tw?method=bbsArticleMark.delete&header.userId=" + userId
                + "&params.blockId=" + thread.getSectionId()
                + "&params.articleId=" + thread.getId()
                + "&params.title=" + thread.getTitle()
                + "&params.authorId=" + thread.getAuthorId() + "&params.authorName=" + thread.getAuthor() + "&params.markResId=0&params.markFloorId=0";
        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    /**
     * 将板块加入书签
     * @param section
     * @param handler
     */
    public static void addBookmark(Section section, AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/api/tw?method=userBlock.ice.addWeilun&params.blockId=" + section.getGuid();
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    /**
     * 将板块移出书签
     * @param section
     * @param handler
     */
    public static void removeBookmark(Section section, AsyncHttpResponseHandler handler) {
        String url = "http://www.tianya.cn/api/tw?method=userBlock.ice.quitWeilun&params.blockId=" + section.getGuid();
        Header[] headers = null;

        headers = new Header[1];
        headers[0] = new ApiHttpClient.HttpHeader("Cookie", AppContext.getInstance().getProperty(AppConfig.CONF_COOKIE));

        ApiHttpClient.get(url, headers, handler);
    }

    public static void getThreads(String section, long nextId, AsyncHttpResponseHandler handler) {
        String url = "http://bbs.tianya.cn/list.jsp?item=" + section + "&nextid=" + nextId;

        ApiHttpClient.get(url, handler);
    }

    /**
     * 获取板块信息
     * @param sectionId
     * @param handler
     */
    public static void getSection(int sectionId, AsyncHttpResponseHandler handler) {
        String url = "http://bbs.tianya.cn/m/list-" + sectionId + "-1.shtml";

        ApiHttpClient.get(url, handler);
    }

    public static void searchThreads(String word, int pageIndex, AsyncHttpResponseHandler handler) {
        String url = "http://search.tianya.cn/bbs?q=" + URLEncoder.encode(word) + "&pn=" + pageIndex;

        ApiHttpClient.get(url, handler);
    }
}
