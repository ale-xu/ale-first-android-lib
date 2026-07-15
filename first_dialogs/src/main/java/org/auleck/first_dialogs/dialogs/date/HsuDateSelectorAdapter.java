package org.auleck.first_dialogs.dialogs.date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.auleck.first_dialogs.R;

import java.util.ArrayList;
import java.util.List;

public class HsuDateSelectorAdapter extends BaseAdapter {

    private final Context context;
    private final String suffix;
    private List<Integer> dataList = new ArrayList<>();
    private int selectedIndex = 0;

    public HsuDateSelectorAdapter(Context context, List<Integer> dataList, String suffix) {
        this.context = context;
        this.suffix = suffix;
        this.dataList = new ArrayList<>(dataList);
    }

    /**
     * 用新数据替换内部列表，并设置选中项，仅触发一次刷新
     */
    public void updateData(List<Integer> newData, int selectedIndex) {
        this.dataList = new ArrayList<>(newData);
        this.selectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.hsu_date_selector_item, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.hdsd_item_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(dataList.get(position) + suffix);

        if (position == selectedIndex) {
            holder.textView.setTextColor(0xFF1677ff);
            holder.textView.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.textView.setBackgroundColor(0x0D1677ff);
        } else {
            holder.textView.setTextColor(0xD9333333);
            holder.textView.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.textView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView textView;
    }
}
