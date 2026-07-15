package org.auleck.first_dialogs.dialogs.address;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.auleck.first_dialogs.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
*
* 使用方法
*
* HsuAddressSelectorDialog dialog = new HsuAddressSelectorDialog(this);
* dialog.setOnConfirmListener((province, city, district) -> {
*         String addressStr = province + "-" + city + "-" + district;
*         Toast.makeText(this, addressStr, Toast.LENGTH_SHORT).show();
* });
* dialog.show();
*
* */

/**
 * HsuAddressSelectorDialog - 底部滑动地址选择器
 *
 * ==== 功能说明 ====
 * 1. 三级联动：省 → 市 → 区
 * 2. Tab 路径头实时显示当前选中项
 * 3. 加载中 / 加载失败（可重试）状态
 * 4. 确认/取消双按钮 + 顶部快捷操作
 * 5. 底部弹出动画，与 HarmonyOS 版本视觉一致
 *
 * ==== 数据格式 ====
 * province.json: [{name, city: [{name, area: [string]}]}]
 */
public class AddressSelectorAaDialog extends AppCompatDialog {

    private static final String TAG = "HsuAddressSelectorDialog";
    private static final String ASSET_FILE_NAME = "province.json";
    private static final long ANIMATION_DURATION = 250;

    private final Context context;

    /** 确认回调 */
    private OnAddressConfirmListener onConfirmListener;
    /** 取消回调 */
    private OnAddressCancelListener onCancelListener;

    // ---- 数据源 ----
    private final List<ProvinceItem> provinces = new ArrayList<>();

    // ---- 选中索引 ----
    private int selectedProvinceIndex = 0;
    private int selectedCityIndex = 0;
    private int selectedDistrictIndex = 0;

    // ---- 适配器 ----
    private AddressSelectorAaAdapter provinceAdapter;
    private AddressSelectorAaAdapter cityAdapter;
    private AddressSelectorAaAdapter districtAdapter;

    // ---- 视图 ----
    private View contentView;
    private TextView tvProvince;
    private TextView tvCity;
    private TextView tvDistrict;
    private ListView lvProvince;
    private ListView lvCity;
    private ListView lvDistrict;
    private Button btnCancel;
    private Button btnConfirm;
    private TextView tvCancelTop;
    private TextView tvConfirmTop;
    private LinearLayout columnsContainer;
    private LinearLayout loadingContainer;
    private LinearLayout errorContainer;
    private TextView tvRetry;

    private boolean isDismissing = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ==================== 回调接口 ====================

    public interface OnAddressConfirmListener {
        void onConfirm(String province, String city, String district);
    }

    public interface OnAddressCancelListener {
        void onCancel();
    }

    // ==================== 数据模型 ====================

    private static class ProvinceItem {
        String name;
        List<CityItem> city = new ArrayList<>();
    }

    private static class CityItem {
        String name;
        List<String> area = new ArrayList<>();
    }

    // ==================== 构造 ====================

    public AddressSelectorAaDialog(@NonNull Context context) {
        super(context, R.style.HsuAddressSelectorDialog);
        this.context = context;
    }

    public void setOnConfirmListener(OnAddressConfirmListener listener) {
        this.onConfirmListener = listener;
    }

    public void setOnCancelListener(OnAddressCancelListener listener) {
        this.onCancelListener = listener;
    }

    // ==================== 生命周期 ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hsu_address_selector_dialog);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        initWindow();
        initViews();
        initListeners();
        loadData();
    }

    private void initWindow() {
        android.view.Window window = getWindow();
        if (window != null) {
            android.view.WindowManager.LayoutParams params = window.getAttributes();
            params.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
            params.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.75);
            params.gravity = Gravity.BOTTOM;
            window.setAttributes(params);
        }
    }

    private void initViews() {
        contentView = findViewById(R.id.hasd_content);
        tvProvince = findViewById(R.id.hasd_tv_province);
        tvCity = findViewById(R.id.hasd_tv_city);
        tvDistrict = findViewById(R.id.hasd_tv_district);
        lvProvince = findViewById(R.id.hasd_lv_province);
        lvCity = findViewById(R.id.hasd_lv_city);
        lvDistrict = findViewById(R.id.hasd_lv_district);
        btnCancel = findViewById(R.id.hasd_btn_cancel);
        btnConfirm = findViewById(R.id.hasd_btn_confirm);
        tvCancelTop = findViewById(R.id.hasd_tv_cancel_top);
        tvConfirmTop = findViewById(R.id.hasd_tv_confirm_top);
        columnsContainer = findViewById(R.id.hasd_columns_container);
        loadingContainer = findViewById(R.id.hasd_loading_container);
        errorContainer = findViewById(R.id.hasd_error_container);
        tvRetry = findViewById(R.id.hasd_tv_retry);

        // 适配器占位初始化（数据加载完成后更新）
        provinceAdapter = new AddressSelectorAaAdapter(context);
        cityAdapter = new AddressSelectorAaAdapter(context);
        districtAdapter = new AddressSelectorAaAdapter(context);

        lvProvince.setAdapter(provinceAdapter);
        lvCity.setAdapter(cityAdapter);
        lvDistrict.setAdapter(districtAdapter);
    }

    private void initListeners() {
        lvProvince.setOnItemClickListener((parent, view, position, id) -> onProvinceChanged(position));
        lvCity.setOnItemClickListener((parent, view, position, id) -> onCityChanged(position));
        lvDistrict.setOnItemClickListener((parent, view, position, id) -> onDistrictChanged(position));

        btnCancel.setOnClickListener(v -> cancelSelection());
        btnConfirm.setOnClickListener(v -> confirmSelection());
        tvCancelTop.setOnClickListener(v -> cancelSelection());
        tvConfirmTop.setOnClickListener(v -> confirmSelection());
        tvRetry.setOnClickListener(v -> {
            showLoading();
            loadData();
        });
    }

    // ==================== 数据加载 ====================

    private void loadData() {
        showLoading();
        new Thread(() -> {
            try {
                List<ProvinceItem> result = parseAddressData();
                mainHandler.post(() -> {
                    if (result != null && !result.isEmpty()) {
                        provinces.clear();
                        provinces.addAll(result);
                        onDataLoaded();
                    } else {
                        showError();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "加载地址数据失败", e);
                mainHandler.post(this::showError);
            }
        }).start();
    }

    /**
     * 从 assets/province.json 读取并解析省市区数据
     */
    private List<ProvinceItem> parseAddressData() throws Exception {
        InputStream is = context.getAssets().open(ASSET_FILE_NAME);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        JSONArray jsonArray = new JSONArray(sb.toString());
        List<ProvinceItem> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject provinceObj = jsonArray.optJSONObject(i);
            if (provinceObj == null) continue;

            ProvinceItem province = new ProvinceItem();
            province.name = provinceObj.optString("name", "");

            JSONArray cityArray = provinceObj.optJSONArray("city");
            if (cityArray != null) {
                for (int j = 0; j < cityArray.length(); j++) {
                    JSONObject cityObj = cityArray.optJSONObject(j);
                    if (cityObj == null) continue;

                    CityItem city = new CityItem();
                    city.name = cityObj.optString("name", "");

                    JSONArray areaArray = cityObj.optJSONArray("area");
                    if (areaArray != null) {
                        for (int k = 0; k < areaArray.length(); k++) {
                            String area = areaArray.optString(k, "");
                            if (!area.isEmpty()) {
                                city.area.add(area);
                            }
                        }
                    }
                    province.city.add(city);
                }
            }
            list.add(province);
        }
        return list;
    }

    /**
     * 数据加载成功后初始化适配器与默认选中
     */
    private void onDataLoaded() {
        showColumns();

        // 重置选中索引
        selectedProvinceIndex = 0;
        selectedCityIndex = 0;
        selectedDistrictIndex = 0;

        updateAllColumns();
    }

    // ==================== 状态切换 ====================

    private void showLoading() {
        loadingContainer.setVisibility(View.VISIBLE);
        errorContainer.setVisibility(View.GONE);
        columnsContainer.setVisibility(View.GONE);
    }

    private void showError() {
        loadingContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);
        columnsContainer.setVisibility(View.GONE);
    }

    private void showColumns() {
        loadingContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        columnsContainer.setVisibility(View.VISIBLE);
    }

    // ==================== 级联联动 ====================

    /**
     * 获取当前省份下的城市列表
     */
    private List<CityItem> getCurrentCities() {
        if (provinces.isEmpty()) return new ArrayList<>();
        int idx = clamp(selectedProvinceIndex, provinces.size());
        return provinces.get(idx).city;
    }

    /**
     * 获取当前城市下的区县列表
     */
    private List<String> getCurrentDistricts() {
        List<CityItem> cities = getCurrentCities();
        if (cities.isEmpty()) return new ArrayList<>();
        int idx = clamp(selectedCityIndex, cities.size());
        return cities.get(idx).area;
    }

    private String getSelectedProvinceName() {
        if (provinces.isEmpty()) return "";
        return provinces.get(clamp(selectedProvinceIndex, provinces.size())).name;
    }

    private String getSelectedCityName() {
        List<CityItem> cities = getCurrentCities();
        if (cities.isEmpty()) return "";
        return cities.get(clamp(selectedCityIndex, cities.size())).name;
    }

    private String getSelectedDistrictName() {
        List<String> districts = getCurrentDistricts();
        if (districts.isEmpty()) return "";
        return districts.get(clamp(selectedDistrictIndex, districts.size()));
    }

    private static int clamp(int index, int size) {
        if (size <= 0) return 0;
        if (index < 0) return 0;
        return Math.min(index, size - 1);
    }

    /**
     * 统一更新所有列：刷新数据 + 选中状态 + 头部文字 + 滚动定位
     */
    private void updateAllColumns() {
        // 1. 提取省名列表
        List<String> provinceNames = new ArrayList<>();
        for (ProvinceItem p : provinces) {
            provinceNames.add(p.name);
        }

        // 2. 提取市名列表
        List<CityItem> cities = getCurrentCities();
        List<String> cityNames = new ArrayList<>();
        for (CityItem c : cities) {
            cityNames.add(c.name);
        }

        // 3. 提取区名列表
        List<String> districtNames = getCurrentDistricts();

        // 4. 更新三个适配器
        provinceAdapter.updateData(provinceNames, selectedProvinceIndex);
        cityAdapter.updateData(cityNames, selectedCityIndex);
        districtAdapter.updateData(districtNames, selectedDistrictIndex);

        // 5. 更新头部 Tab 文字
        String provinceName = getSelectedProvinceName();
        String cityName = getSelectedCityName();
        String districtName = getSelectedDistrictName();

        tvProvince.setText(provinceName.isEmpty() ? "请选择省" : provinceName);
        tvProvince.setTextColor(provinceName.isEmpty() ?
                ContextCompat.getColor(context, R.color.hsu_address_text_hint) :
                ContextCompat.getColor(context, R.color.hsu_primary));
        tvProvince.setTypeface(null, provinceName.isEmpty() ?
                android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);

        tvCity.setText(cityName.isEmpty() ? "请选择市" : cityName);
        tvCity.setTextColor(cityName.isEmpty() ?
                ContextCompat.getColor(context, R.color.hsu_address_text_hint) :
                ContextCompat.getColor(context, R.color.hsu_primary));
        tvCity.setTypeface(null, cityName.isEmpty() ?
                android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);

        tvDistrict.setText(districtName.isEmpty() ? "请选择区" : districtName);
        tvDistrict.setTextColor(districtName.isEmpty() ?
                ContextCompat.getColor(context, R.color.hsu_address_text_hint) :
                ContextCompat.getColor(context, R.color.hsu_primary));
        tvDistrict.setTypeface(null, districtName.isEmpty() ?
                android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);

        // 6. 滚动到选中位置
        lvProvince.setSelection(selectedProvinceIndex);
        lvCity.setSelection(selectedCityIndex);
        lvDistrict.setSelection(selectedDistrictIndex);
    }

    /**
     * 省份变化：重置市/区索引 → 更新所有列
     */
    private void onProvinceChanged(int position) {
        if (position < 0 || position >= provinces.size()) return;
        if (position == selectedProvinceIndex) return;
        Log.d(TAG, "onProvinceChanged: position=" + position + " name=" + provinces.get(position).name);
        selectedProvinceIndex = position;
        selectedCityIndex = 0;
        selectedDistrictIndex = 0;
        updateAllColumns();
    }

    /**
     * 城市变化：重置区索引 → 更新所有列
     */
    private void onCityChanged(int position) {
        List<CityItem> cities = getCurrentCities();
        if (position < 0 || position >= cities.size()) return;
        if (position == selectedCityIndex) return;
        Log.d(TAG, "onCityChanged: position=" + position + " name=" + cities.get(position).name);
        selectedCityIndex = position;
        selectedDistrictIndex = 0;
        updateAllColumns();
    }

    /**
     * 区县变化：仅更新选中状态与头部
     */
    private void onDistrictChanged(int position) {
        List<String> districts = getCurrentDistricts();
        if (position < 0 || position >= districts.size()) return;
        if (position == selectedDistrictIndex) return;
        Log.d(TAG, "onDistrictChanged: position=" + position + " name=" + districts.get(position));
        selectedDistrictIndex = position;
        updateAllColumns();
    }

    // ==================== 确认/取消 ====================

    private void confirmSelection() {
        if (provinces.isEmpty()) return;
        String province = getSelectedProvinceName();
        String city = getSelectedCityName();
        String district = getSelectedDistrictName();
        if (onConfirmListener != null) {
            onConfirmListener.onConfirm(province, city, district);
        }
        dismissWithAnimation();
    }

    private void cancelSelection() {
        if (onCancelListener != null) {
            onCancelListener.onCancel();
        }
        dismissWithAnimation();
    }

    // ==================== 动画 ====================

    @Override
    public void show() {
        super.show();
        if (contentView != null) {
            contentView.setTranslationY(0);
            contentView.setAlpha(1f);
        }
    }

    private void dismissWithAnimation() {
        if (isDismissing) return;
        isDismissing = true;

        if (contentView != null) {
            contentView.animate()
                    .translationY(contentView.getHeight())
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        }

        if (contentView != null) {
            contentView.postDelayed(() -> {
                isDismissing = false;
                AddressSelectorAaDialog.super.cancel();
            }, ANIMATION_DURATION);
        } else {
            isDismissing = false;
            AddressSelectorAaDialog.super.cancel();
        }
    }

    @Override
    public void cancel() {
        dismissWithAnimation();
    }
}
