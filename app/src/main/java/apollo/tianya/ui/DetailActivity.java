package apollo.tianya.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;

import apollo.tianya.R;
import apollo.tianya.base.BaseActivity;
import apollo.tianya.base.BaseFragment;
import apollo.tianya.fragment.bar.BarBaseFragment;
import apollo.tianya.fragment.bar.InputFragment;
import apollo.tianya.fragment.bar.ToolbarFragment;
import apollo.tianya.fragment.ThreadDetailFragment;
import apollo.tianya.fragment.bar.BarBaseFragment.Action;
import apollo.tianya.fragment.bar.BarBaseFragment.OnActionClickListener;

/**
 * 帖子详情Activity
 * Created by Texel on 2016/6/20.
 */
public class DetailActivity extends BaseActivity implements
        OnActionClickListener, InputFragment.OnSendListener {

    private ToolbarFragment mToolFragment = new ToolbarFragment();
    private InputFragment mInputFragment = new InputFragment();
    private BarBaseFragment mNewFragment = null;
    private InputFragment.OnSendListener mSendListener = null;

    @Override
    protected void init(Bundle savedInstanceState) {
        BaseFragment fragment = null;
        FragmentTransaction trans = null;

        fragment = new ThreadDetailFragment();
        trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.container, fragment);
        trans.commitAllowingStateLoss();

        if (fragment instanceof InputFragment.OnSendListener)
            mSendListener = (InputFragment.OnSendListener) fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_detail;
    }

    @Override
    protected void initView() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.emoji_keyboard, mToolFragment).commit();
        mToolFragment.setOnActionClickListener(this);
        mInputFragment.setOnActionClickListener(this);
        mInputFragment.setOnSendListener(this);
        mNewFragment = mInputFragment;
    }

    @Override
    public void onActionClick(Action action) {
        switch (action) {
            case ACTION_CHANGE:
            case ACTION_WRITE_COMMENT:
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.footer_menu_slide_in,
                                R.anim.footer_menu_slide_out)
                        .replace(R.id.emoji_keyboard, mNewFragment)
                        .commit();

                mNewFragment = mNewFragment == mInputFragment ? mToolFragment : mInputFragment;
                break;
        }
    }

    @Override
    public void onSend(Editable editor) {
        mSendListener.onSend(editor);
    }
}
