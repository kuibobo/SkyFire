package apollo.tianya.fragment;

import android.view.View;

import java.util.List;

import apollo.tianya.adapter.RecyclerBaseAdapter;
import apollo.tianya.adapter.ThreadAdapter;
import apollo.tianya.api.TianyaParser;
import apollo.tianya.api.remote.TianyaApi;
import apollo.tianya.base.BaseListFragment;
import apollo.tianya.bean.Constants;
import apollo.tianya.bean.DataSet;
import apollo.tianya.bean.Thread;
import apollo.tianya.ui.DetailActivity;
import apollo.tianya.util.UIHelper;

/**
 * Created by Texel on 2016/8/12.
 */
public class ThreadsFragment extends BaseListFragment<Thread> {

    private String mSectionId;
    private long mNextId;

    @Override
    protected RecyclerBaseAdapter getListAdapter() {
        return new ThreadAdapter();
    }

    @Override
    protected DataSet<Thread> parseList(byte[] datas) {
        String body = null;
        DataSet<Thread> dataset = null;

        body = new String(datas);
        dataset = TianyaParser.parseThreads(body);
        return dataset;
    }

    @Override
    protected void executeOnLoadDataSuccess(DataSet<Thread> datas) {
        if (datas != null && datas.getObjects().size() > 0) {
            List<Thread> list = datas.getObjects();
            Thread thread = list.get(list.size() - 1);

            mNextId = thread.getPostDate().getTime();

            DetailActivity activity = (DetailActivity) getActivity();
            activity.setTitle(thread.getSectionName());
        }

        super.executeOnLoadDataSuccess(datas);
    }

    @Override
    public void onRefresh() {
        if (mState == STATE_REFRESH)
            return;

        setSwipeRefreshLoadingState();
        mState = STATE_REFRESH;
        mNextId = 0;
        mAdapter.clear();
        requestData(true);
    }

    @Override
    protected void sendRequestData() {
        TianyaApi.getThreads(mSectionId, mNextId, mHandler);
    }

    @Override
    protected String getCacheKeyPrefix() {
        return "threads_" + mSectionId + "_" + mNextId;
    }

    @Override
    public void onItemClick(View view, int position) {
        Thread thread = mAdapter.getItem(position);

        if (TianyaParser.parseThreadUrl(thread.getUrl()) != null)
            UIHelper.showPostDetail(view.getContext(), thread);
    }

    @Override
    public void initView(View view) {
        mSectionId = getActivity().getIntent().getStringExtra(Constants.BUNDLE_KEY_SECTION_ID);

        super.initView(view);
    }

}
