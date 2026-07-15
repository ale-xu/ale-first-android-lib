package org.auleck.first_dialogs.dialogs.date;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.auleck.first_dialogs.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 *  使用方法
 *
 *             HsuDateSelectorDialog dialog = new HsuDateSelectorDialog(this);
 *             dialog.setOnConfirmListener((year, month, day) -> {
 *                 String dateStr = year + "-" + month + "-" + day;
 *                 Toast.makeText(this, dateStr, Toast.LENGTH_SHORT).show();
 *             });
 *
 *             dialog.show();
 *
 * */


public class DateSelectorAaDialog extends AppCompatDialog {

    private static final String TAG = "HsuDateSelectorDialog";
    private static final int MIN_YEAR = 1949;
    private static final long ANIMATION_DURATION = 200;

    private final Context context;
    private OnDateConfirmListener onConfirmListener;
    private OnDateCancelListener onCancelListener;

    private int minYear = MIN_YEAR;
    private int maxYear = 0;

    private final List<Integer> years = new ArrayList<>();
    private final List<Integer> months = new ArrayList<>();
    private final List<Integer> days = new ArrayList<>();

    private int selectedYearIndex = 0;
    private int selectedMonthIndex = 0;
    private int selectedDayIndex = 0;

    private int selectedYear = MIN_YEAR;
    private int selectedMonth = 1;
    private int selectedDay = 1;

    private DateSelectorAaAdapter yearAdapter;
    private DateSelectorAaAdapter monthAdapter;
    private DateSelectorAaAdapter dayAdapter;

    private View maskView;
    private View contentView;
    private TextView tvYear;
    private TextView tvMonth;
    private TextView tvDay;
    private ListView lvYear;
    private ListView lvMonth;
    private ListView lvDay;
    private Button btnCancel;
    private Button btnConfirm;
    private TextView tvCancelTop;
    private TextView tvConfirmTop;

    private boolean isDismissing = false;

    public interface OnDateConfirmListener {
        void onConfirm(int year, int month, int day);
    }

    public interface OnDateCancelListener {
        void onCancel();
    }

    public DateSelectorAaDialog(@NonNull Context context) {
        super(context, R.style.HsuDateSelectorDialog);
        this.context = context;
    }

    public void setOnConfirmListener(OnDateConfirmListener listener) {
        this.onConfirmListener = listener;
    }

    public void setOnCancelListener(OnDateCancelListener listener) {
        this.onCancelListener = listener;
    }

    public void setMinYear(int minYear) {
        this.minYear = minYear;
    }

    public void setMaxYear(int maxYear) {
        this.maxYear = maxYear;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hsu_date_selector_dialog);
        setCancelable(true);
        initWindow();
        initViews();
        initData();
        setDefaultToToday();
        initListeners();
    }

    private void initWindow() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.75);
            params.gravity = Gravity.BOTTOM;
            window.setAttributes(params);
        }
    }

    private void initViews() {
        maskView = findViewById(R.id.hdsd_mask);
        contentView = findViewById(R.id.hdsd_content);
        tvYear = findViewById(R.id.hdsd_tv_year);
        tvMonth = findViewById(R.id.hdsd_tv_month);
        tvDay = findViewById(R.id.hdsd_tv_day);
        lvYear = findViewById(R.id.hdsd_lv_year);
        lvMonth = findViewById(R.id.hdsd_lv_month);
        lvDay = findViewById(R.id.hdsd_lv_day);
        btnCancel = findViewById(R.id.hdsd_btn_cancel);
        btnConfirm = findViewById(R.id.hdsd_btn_confirm);
        tvCancelTop = findViewById(R.id.hdsd_tv_cancel_top);
        tvConfirmTop = findViewById(R.id.hdsd_tv_confirm_top);
    }

    // ==================== 数据初始化 ====================

    private void initData() {
        if (maxYear <= 0) {
            maxYear = Calendar.getInstance().get(Calendar.YEAR);
        }
        validateConfig();
        buildYearList();
        buildMonthList();
        buildDayList();
        setupAdapters();
    }

    private void validateConfig() {
        if (minYear < 1) {
            throw new IllegalArgumentException("最小年份不能小于1，当前: " + minYear);
        }
        if (maxYear < minYear) {
            throw new IllegalArgumentException("最大年份(" + maxYear + ")不能小于最小年份(" + minYear + ")");
        }
    }

    private void buildYearList() {
        years.clear();
        for (int y = maxYear; y >= minYear; y--) {
            years.add(y);
        }
    }

    private void buildMonthList() {
        months.clear();
        for (int m = 1; m <= 12; m++) {
            months.add(m);
        }
    }

    /**
     * 根据 selectedYear 和 selectedMonth 重建日期列表
     * 关键：用 calendar.clear() 避免 Calendar 实例残留状态污染天数计算
     */
    private void buildDayList() {
        int dayCount = getDaysInMonth(selectedYear, selectedMonth);
        days.clear();
        for (int d = 1; d <= dayCount; d++) {
            days.add(d);
        }
        Log.d(TAG, "buildDayList: year=" + selectedYear + " month=" + selectedMonth + " days=" + dayCount);

        // 如果当前选中日超出该月天数，修正为最后一天
        if (selectedDay > dayCount) {
            selectedDay = dayCount;
        }
        if (selectedDay < 1) {
            selectedDay = 1;
        }
        selectedDayIndex = selectedDay - 1;
    }

    /**
     * 获取指定年月的天数
     * 2月按闰年规则显式判断：闰年29天，平年28天
     * 其余月份固定天数
     */
    private int getDaysInMonth(int year, int month) {
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                return 31;
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                return isLeapYear(year) ? 28 : 29;
            default:
                return 30;
        }
    }

    /**
     * 判断是否为闰年
     * 规则：能被4整除且不能被100整除，或能被400整除
     */
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    private void setupAdapters() {
        yearAdapter = new DateSelectorAaAdapter(context, years, "年");
        monthAdapter = new DateSelectorAaAdapter(context, months, "月");
        dayAdapter = new DateSelectorAaAdapter(context, days, "日");

        lvYear.setAdapter(yearAdapter);
        lvMonth.setAdapter(monthAdapter);
        lvDay.setAdapter(dayAdapter);
    }

    private void setDefaultToToday() {
        Calendar today = Calendar.getInstance();
        int targetYear = today.get(Calendar.YEAR);
        int targetMonth = today.get(Calendar.MONTH) + 1;
        int targetDay = today.get(Calendar.DAY_OF_MONTH);

        // 定位年份
        selectedYearIndex = 0;
        for (int i = 0; i < years.size(); i++) {
            if (years.get(i) == targetYear) {
                selectedYearIndex = i;
                break;
            }
        }
        selectedYear = years.get(selectedYearIndex);

        // 定位月份
        selectedMonthIndex = targetMonth - 1;
        selectedMonth = targetMonth;

        // 先设好 selectedDay 再 buildDayList，buildDayList 内部会修正越界
        selectedDay = targetDay;
        buildDayList();

        // 重新查找日期索引（buildDayList 已设置 selectedDayIndex，但 targetDay 可能被修正）
        updateAllColumns();
    }

    // ==================== 核心联动逻辑 ====================

    /**
     * 统一更新所有列：刷新数据 + 刷新选中状态 + 刷新头部文字 + 滚动定位
     * 每次点击任意列后都调用此方法，确保三列完全同步
     */
    private void updateAllColumns() {
        // 1. 更新三个 adapter 的选中索引
        yearAdapter.setSelectedIndex(selectedYearIndex);
        monthAdapter.setSelectedIndex(selectedMonthIndex);
        dayAdapter.updateData(days, selectedDayIndex);

        // 2. 更新头部文字
        tvYear.setText(selectedYear + "年");
        tvMonth.setText(selectedMonth + "月");
        tvDay.setText(selectedDay + "日");

        // 3. 滚动到选中位置
        lvYear.setSelection(selectedYearIndex);
        lvMonth.setSelection(selectedMonthIndex);
        lvDay.setSelection(selectedDayIndex);
    }

    /**
     * 年份变化时：更新年份 → 重建日期列表 → 更新所有列
     */
    private void onYearChanged(int position) {
        if (position < 0 || position >= years.size()) {
            return;
        }
        Log.d(TAG, "onYearChanged: position=" + position + " year=" + years.get(position));
        selectedYearIndex = position;
        selectedYear = years.get(position);
        buildDayList();
        updateAllColumns();
    }

    /**
     * 月份变化时：更新月份 → 重建日期列表 → 更新所有列
     */
    private void onMonthChanged(int position) {
        if (position < 0 || position >= months.size()) {
            return;
        }
        Log.d(TAG, "onMonthChanged: position=" + position + " month=" + months.get(position));
        selectedMonthIndex = position;
        selectedMonth = months.get(position);
        buildDayList();
        updateAllColumns();
    }

    /**
     * 日期变化时：更新日期 → 更新所有列（日期列表本身不变，但选中状态和头部需要刷新）
     */
    private void onDayChanged(int position) {
        if (position < 0 || position >= days.size()) {
            return;
        }
        Log.d(TAG, "onDayChanged: position=" + position + " day=" + days.get(position));
        selectedDayIndex = position;
        selectedDay = days.get(position);
        updateAllColumns();
    }

    // ==================== 事件绑定 ====================

    private void initListeners() {
        maskView.setOnClickListener(v -> cancelSelection());

        lvYear.setOnItemClickListener((parent, view, position, id) -> onYearChanged(position));
        lvMonth.setOnItemClickListener((parent, view, position, id) -> onMonthChanged(position));
        lvDay.setOnItemClickListener((parent, view, position, id) -> onDayChanged(position));

        btnCancel.setOnClickListener(v -> cancelSelection());
        btnConfirm.setOnClickListener(v -> confirmSelection());
        tvCancelTop.setOnClickListener(v -> cancelSelection());
        tvConfirmTop.setOnClickListener(v -> confirmSelection());
    }

    // ==================== 确认/取消 ====================

    private void confirmSelection() {
        if (!isValidDate(selectedYear, selectedMonth, selectedDay)) {
            return;
        }
        if (onConfirmListener != null) {
            onConfirmListener.onConfirm(selectedYear, selectedMonth, selectedDay);
        }
        dismissWithAnimation();
    }

    private void cancelSelection() {
        if (onCancelListener != null) {
            onCancelListener.onCancel();
        }
        dismissWithAnimation();
    }

    private boolean isValidDate(int year, int month, int day) {
        if (month < 1 || month > 12) {
            return false;
        }
        if (day < 1 || day > getDaysInMonth(year, month)) {
            return false;
        }
        return true;
    }

    // ==================== 动画 ====================

    @Override
    public void show() {
        super.show();
        if (contentView != null) {
            contentView.setTranslationY(0);
            contentView.setAlpha(1f);
        }
        if (maskView != null) {
            maskView.setAlpha(1f);
        }
    }

    private void dismissWithAnimation() {
        if (isDismissing) {
            return;
        }
        isDismissing = true;

        if (contentView != null) {
            contentView.animate()
                    .translationY(contentView.getHeight())
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        }

        if (maskView != null) {
            maskView.animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        }

        contentView.postDelayed(() -> {
            isDismissing = false;
            DateSelectorAaDialog.super.cancel();
        }, ANIMATION_DURATION);
    }

    @Override
    public void cancel() {
        dismissWithAnimation();
    }
}
