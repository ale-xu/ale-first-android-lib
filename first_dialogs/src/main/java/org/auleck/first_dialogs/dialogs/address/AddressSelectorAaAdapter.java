package org.auleck.first_dialogs.dialogs.address;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import org.auleck.first_dialogs.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 地址选择器列适配器
 * 用于展示省/市/区列表项，高亮当前选中项
 */
public class AddressSelectorAaAdapter extends BaseAdapter {

    private final Context context;
    private List<String> dataList = new ArrayList<>();
    private int selectedIndex = 0;

    public AddressSelectorAaAdapter(Context context) {
        this.context = context;
    }

    public AddressSelectorAaAdapter(Context context, List<String> dataList, int selectedIndex) {
        this.context = context;
        this.dataList = new ArrayList<>(dataList);
        this.selectedIndex = selectedIndex;
    }

    /**
     * 用新数据替换内部列表，并设置选中项，仅触发一次刷新
     */
    public void updateData(List<String> newData, int selectedIndex) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.hsu_address_selector_item, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.hasd_item_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(dataList.get(position));

        if (position == selectedIndex) {
            holder.textView.setTextColor(0xFF1677FF);
            holder.textView.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.textView.setBackgroundColor(0x0F1677FF);
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
