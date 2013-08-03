package io.github.eddieringle.android.apps.passwordmaker.ui.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public abstract class BaseListAdapter<T> extends BaseAdapter {

    protected LayoutInflater mInflater;

    private ArrayList<T> mData;

    private HashMap<Integer, Boolean> mSelectionMap = new HashMap<Integer, Boolean>();

    private boolean mNotifyOnChange = true;

    private Context mContext;

    public BaseListAdapter(Context context) {
        super();

        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mData = new ArrayList<T>();
    }

    public Context getContext() {
        return mContext;
    }

    public void clear() {
        mData.clear();
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public ArrayList<T> getAll() {
        return mData;
    }

    public void addAll(Collection<? extends T> collection) {
        mData.addAll(collection);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }

    protected void fillWithItems(List<T> data, boolean append) {
        if (!append) {
            clear();
        }
        addAll(data);
    }

    public void fillWithItems(List<T> data) {
        fillWithItems(data, false);
    }

    public void appendWithItems(List<T> data) {
        fillWithItems(data, true);
    }

    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    public void setNewSelection(int position, boolean value) {
        mSelectionMap.put(position, value);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        Boolean result = mSelectionMap.get(position);
        return result == null ? false : result;
    }

    public Set<Integer> getCurrentCheckedPosition() {
        return mSelectionMap.keySet();
    }

    public void removeSelection(int position) {
        if (mSelectionMap.remove(position) != null) {
            notifyDataSetChanged();
        }
    }

    public void clearSelection() {
        mSelectionMap.clear();
        notifyDataSetChanged();
    }
}
