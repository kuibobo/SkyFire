package apollo.tianya.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import apollo.tianya.bean.Entity;

/**
 * Created by Texel on 2016/6/2.
 */
public abstract class RecyclerBaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    public interface OnItemClickListener {
        void onItemClick(View view,int postion);
    }

    public interface DisplayFloorHandle<T> {
        void setFloor(TextView view, T t, int position);
    }

    protected List<T> mItems;
    private LayoutInflater mInflater;
    private LinearLayout mFooterView;

    protected OnItemClickListener mItemClickListener;
    protected DisplayFloorHandle mDisplayFloorHandle;

    public abstract VH getViewHolder(ViewGroup viewGroup);

    public RecyclerBaseAdapter() {
        mItems = new ArrayList<T>();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    protected boolean hasFooterView(){
        return true;
    }

    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        final VH vh = getViewHolder(parent);

        vh.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int position = vh.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && mItemClickListener != null)
                    mItemClickListener.onItemClick(view, position);
            }
        });

        return vh;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mItemClickListener = l;
    }

    public void setDisplayFloor(DisplayFloorHandle<T> callBack) {
        mDisplayFloorHandle = callBack;
    }

    protected LayoutInflater getLayoutInflater(Context context) {
        if (mInflater == null) {
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        return mInflater;
    }

    public void setData(List<T> items) {
        mItems = items;

        notifyDataSetChanged();
    }

    public void addItems(List<T> items) {
        if (items != null && !items.isEmpty()) {
            mItems.addAll(items);
            notifyDataSetChanged();
        }
    }

    public void addItem(T i) {
        mItems.add(i);
        notifyDataSetChanged();
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        return mItems.get(position);
    }
}
