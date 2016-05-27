package apollo.tianya.util;

import android.content.Context;
import android.content.Intent;

import apollo.tianya.LoginActivity;

/**
 * 页面帮助类
 *
 * Created by Texel on 2016/5/26.
 */
public class UIHelper {

    /**
     * 显示登录界面
     * @param context
     */
    public static void showLoginActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }
}
