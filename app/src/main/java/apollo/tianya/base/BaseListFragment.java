package apollo.tianya.base;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

import com.loopj.android.http.AsyncHttpResponseHandler;

import apollo.tianya.R;
import apollo.tianya.adapter.RecyclerBaseAdapter;
import apollo.tianya.bean.DataSet;
import apollo.tianya.bean.Entity;
import butterknife.BindView;
import cz.msebera.android.httpclient.Header;

/**
 * Created by kuibo on 2016/6/1.
 */
public abstract class BaseListFragment<T extends Entity> extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener, RecyclerBaseAdapter.OnItemClickListener {

    private String TAG = this.getClass().getName();

    /**
     * HTTP LIST内容解析任务类
     */
    private class ParserTask extends AsyncTask<Void, Void, DataSet<T>> {

        private final byte[] responseData;
        private boolean parserError;

        public ParserTask(byte[] data) {
            this.responseData = data;
        }

        @Override
        protected DataSet<T> doInBackground(Void... voids) {
            DataSet<T> dataset = null;

            try {
                dataset = parseList(responseData);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                parserError = true;
            }

            return dataset;
        }

        @Override
        protected void onPostExecute(DataSet<T> dataset) {
            if (parserError) {

            } else {
                executeOnLoadDataSuccess(dataset);
                executeOnLoadFinish();
            }
        }
    }

    public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";

    private ParserTask mParserTask = null;

    protected AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            if (isAdded()) {
                if (mState == STATE_REFRESH) {
                    onRefreshNetworkSuccess();
                }
                executeParserTask(responseBody);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

        }
    };

    protected abstract RecyclerBaseAdapter<T, RecyclerView.ViewHolder> getListAdapter();

    protected abstract DataSet<T> parseList(byte[] datas);

    protected abstract void sendRequestData();

    protected void onRefreshNetworkSuccess() {
    }

    @BindView(R.id.listview)
    protected RecyclerView mListView;

    @BindView(R.id.swiperefreshlayout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    protected String mCatalog = null;
    protected RecyclerBaseAdapter<T, RecyclerView.ViewHolder> mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pull_refresh_listview;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = this.getArguments();
        if (args != null) {
            mCatalog = args.getString(BUNDLE_KEY_CATALOG);
        }
    }

    @Override
    public void initView(View view) {

        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (mAdapter == null) {
            mAdapter = getListAdapter();
        }
        mAdapter.setOnItemClickListener(this);


        final LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(llm);
        mListView.setAdapter(mAdapter);
        mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mAdapter == null || mAdapter.getItemCount() == 0) {
                    return;
                }
                // 数据已经全部加载，或数据为空时，或正在加载，不处理滚动事件
                if (mState == STATE_LOADMORE || mState == STATE_REFRESH) {
                    return;
                }
                // 判断是否滚动到底部
                boolean scrollEnd = false;
                try {
                    if (llm.findLastVisibleItemPosition() == llm.getItemCount())
                        scrollEnd = true;
                } catch (Exception e) {
                    scrollEnd = false;
                }

                if (mState == STATE_NONE && scrollEnd) {
                    if (mAdapter.getState() == ListBaseAdapter.STATE_LOAD_MORE
                            || mAdapter.getState() == ListBaseAdapter.STATE_NETWORK_ERROR) {
                        mCurrentPage++;
                        mState = STATE_LOADMORE;
                        requestData(false);
                        mAdapter.setFooterViewLoading();
                    }
                }
            }
        });
        requestData(false);
    }

    @Override
    public void onRefresh() {
        requestData(true);
    }

    protected void executeOnLoadDataSuccess(DataSet<T> ds) {
        mAdapter.addItems(ds.getObjects());
    }

    // 完成刷新
    protected void executeOnLoadFinish() {
        setSwipeRefreshLoadedState();
        mState = STATE_NONE;
    }

    /**
     * 设置顶部正在加载的状态
     */
    protected void setSwipeRefreshLoadingState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            // 防止多次重复刷新
            mSwipeRefreshLayout.setEnabled(false);
        }
    }

    protected void requestData(boolean refresh) {
        sendRequestData();
    }

    /**
     * 设置顶部加载完毕的状态
     */
    protected void setSwipeRefreshLoadedState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);
        }
    }

    private void executeParserTask(byte[] data) {
        cancelParserTask();
        mParserTask = new ParserTask(data);
        mParserTask.execute();
    }

    private void cancelParserTask() {
        if (mParserTask != null) {
            mParserTask.cancel(true);
            mParserTask = null;
        }
    }
}
