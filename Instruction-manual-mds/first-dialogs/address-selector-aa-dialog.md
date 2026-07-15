# AddressSelectorAaDialog 使用指南

## 目录

- [简介](#简介)
- [功能特性](#功能特性)
- [快速开始](#快速开始)
  - [基本用法](#基本用法)
  - [完整示例](#完整示例)
- [回调接口](#回调接口)
  - [OnAddressConfirmListener](#onaddressconfirmlistener)
  - [OnAddressCancelListener](#onaddresscancellistener)
- [数据格式](#数据格式)
  - [province.json 结构](#provincejson-结构)
  - [数据放置位置](#数据放置位置)
- [界面结构](#界面结构)
  - [标题栏](#标题栏)
  - [Tab 路径头](#tab-路径头)
  - [三列级联列表](#三列级联列表)
  - [底部按钮栏](#底部按钮栏)
- [状态管理](#状态管理)
  - [加载中状态](#加载中状态)
  - [加载失败状态](#加载失败状态)
  - [正常显示状态](#正常显示状态)
- [级联联动逻辑](#级联联动逻辑)
- [动画效果](#动画效果)
- [布局文件说明](#布局文件说明)
- [适配器说明](#适配器说明)
- [注意事项](#注意事项)
- [常见问题](#常见问题)

---

## 简介

`AddressSelectorAaDialog` 是一个底部弹出的省市区三级联动地址选择器，基于 `AppCompatDialog` 封装。适用于需要用户选择收货地址、定位区域等场景。

选择器从屏幕底部弹出，包含省、市、区三列联动列表，顶部显示当前选中路径，底部提供确认/取消按钮。

---

## 功能特性

| 特性 | 说明 |
|------|------|
| 三级联动 | 省 → 市 → 区 自动级联，切换上级自动重置下级 |
| Tab 路径头 | 实时显示当前选中的省/市/区路径 |
| 加载状态 | 数据加载中显示进度指示器 |
| 错误重试 | 数据加载失败时显示错误提示，支持点击重试 |
| 双操作入口 | 顶部和底部均有确认/取消按钮 |
| 底部弹出动画 | 平滑的底部滑出动画效果 |
| 点击外部关闭 | 支持点击弹窗外部区域关闭 |
| 选中高亮 | 当前选中项蓝色高亮显示 |

---

## 快速开始

### 基本用法

```java
AddressSelectorAaDialog dialog = new AddressSelectorAaDialog(this);
dialog.setOnConfirmListener((province, city, district) -> {
    String address = province + "-" + city + "-" + district;
    Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
});
dialog.show();
```

### 完整示例

```java
AddressSelectorAaDialog dialog = new AddressSelectorAaDialog(this);

// 设置确认回调
dialog.setOnConfirmListener((province, city, district) -> {
    // 处理选中地址
    String fullAddress = province + city + district;
    Log.d("Address", "选中地址: " + fullAddress);
    
    // 更新 UI
    tvAddress.setText(fullAddress);
});

// 设置取消回调（可选）
dialog.setOnCancelListener(() -> {
    Log.d("Address", "用户取消了地址选择");
});

// 显示弹窗
dialog.show();
```

---

## 回调接口

### OnAddressConfirmListener

确认按钮点击回调，返回选中的省市区名称。

```java
public interface OnAddressConfirmListener {
    void onConfirm(String province, String city, String district);
}
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `province` | `String` | 选中的省份名称 |
| `city` | `String` | 选中的城市名称 |
| `district` | `String` | 选中的区县名称 |

**使用示例：**

```java
dialog.setOnConfirmListener((province, city, district) -> {
    // province = "广东省"
    // city = "深圳市"
    // district = "南山区"
});
```

---

### OnAddressCancelListener

取消按钮点击回调（可选）。

```java
public interface OnAddressCancelListener {
    void onCancel();
}
```

**使用示例：**

```java
dialog.setOnCancelListener(() -> {
    // 用户取消选择，执行清理逻辑
});
```

---

## 数据格式

### province.json 结构

地址数据采用 JSON 格式存储，结构如下：

```json
[
  {
    "name": "省份名称",
    "city": [
      {
        "name": "城市名称",
        "area": [
          "区县名称1",
          "区县名称2",
          "区县名称3"
        ]
      }
    ]
  }
]
```

**完整示例：**

```json
[
  {
    "name": "北京市",
    "city": [
      {
        "name": "北京市",
        "area": [
          "东城区",
          "西城区",
          "朝阳区",
          "海淀区"
        ]
      }
    ]
  },
  {
    "name": "广东省",
    "city": [
      {
        "name": "广州市",
        "area": [
          "天河区",
          "越秀区",
          "海珠区"
        ]
      },
      {
        "name": "深圳市",
        "area": [
          "福田区",
          "南山区",
          "宝安区"
        ]
      }
    ]
  }
]
```

### 数据放置位置

将 `province.json` 文件放置在项目的 `assets` 目录下：

```
app/
└── src/
    └── main/
        └── assets/
            └── province.json
```

---

## 界面结构

### 标题栏

位于弹窗顶部，高度 44dp。

| 元素 | 位置 | 功能 |
|------|------|------|
| 取消按钮 | 左侧 | 关闭弹窗，触发取消回调 |
| 标题文字 | 居中 | 显示"请选择地址" |
| 确定按钮 | 右侧 | 确认选择，触发确认回调 |

---

### Tab 路径头

位于标题栏下方，显示当前选中的省/市/区路径。

```
[  广东省  ] > [  深圳市  ] > [  南山区  ]
```

**状态说明：**

| 状态 | 文字颜色 | 字体样式 |
|------|----------|----------|
| 未选择 | 灰色提示色 | 常规 |
| 已选择 | 主题蓝色 | 加粗 |

---

### 三列级联列表

中间区域为省、市、区三列并排显示：

```
┌──────────┬──────────┬──────────┐
│   省份   │   城市   │   区县   │
│  列表    │  列表    │  列表    │
└──────────┴──────────┴──────────┘
```

- 每列宽度均分（weight=1）
- 列之间有 0.5dp 分隔线
- 选中项蓝色高亮，背景半透明

---

### 底部按钮栏

位于弹窗底部，高度 56dp。

| 按钮 | 样式 | 功能 |
|------|------|------|
| 取消 | 灰色边框 | 关闭弹窗 |
| 确定 | 蓝色实心 | 确认选择 |

---

## 状态管理

弹窗内部有三种显示状态，自动切换：

### 加载中状态

数据加载时显示，包含：
- 圆形进度指示器
- "加载地址数据中..." 文字提示

### 加载失败状态

数据加载异常时显示，包含：
- "数据加载失败" 提示
- "请重试" 可点击文字，点击后重新加载

### 正常显示状态

数据加载成功后显示三列级联列表。

---

## 级联联动逻辑

选择器实现了完整的三级联动：

| 操作 | 联动效果 |
|------|----------|
| 切换省份 | 重置市、区索引为 0，更新所有列 |
| 切换城市 | 重置区索引为 0，更新市、区列 |
| 切换区县 | 仅更新区列选中状态 |

**联动流程：**

```
选择省份
    ↓
重置市索引 = 0
重置区索引 = 0
    ↓
更新省列表选中状态
更新市列表数据
更新区列表数据
    ↓
更新 Tab 路径头
```

---

## 动画效果

弹窗采用底部滑入/滑出动画：

| 动画 | 时长 | 插值器 |
|------|------|--------|
| 显示 | 系统默认 | - |
| 关闭 | 250ms | FastOutSlowInInterpolator |

关闭时内容视图向下滑出屏幕，动画结束后真正关闭弹窗。

---

## 布局文件说明

### hsu_address_selector_dialog.xml

主布局文件，包含以下区域：

| ID | 类型 | 说明 |
|----|------|------|
| `hasd_content` | LinearLayout | 根容器，带圆角背景 |
| `hasd_tv_cancel_top` | TextView | 顶部取消按钮 |
| `hasd_tv_confirm_top` | TextView | 顶部确定按钮 |
| `hasd_tv_province` | TextView | Tab 路径头-省 |
| `hasd_tv_city` | TextView | Tab 路径头-市 |
| `hasd_tv_district` | TextView | Tab 路径头-区 |
| `hasd_columns_container` | LinearLayout | 三列列表容器 |
| `hasd_lv_province` | ListView | 省份列表 |
| `hasd_lv_city` | ListView | 城市列表 |
| `hasd_lv_district` | ListView | 区县列表 |
| `hasd_loading_container` | LinearLayout | 加载状态容器 |
| `hasd_error_container` | LinearLayout | 错误状态容器 |
| `hasd_tv_retry` | TextView | 重试按钮 |
| `hasd_btn_cancel` | Button | 底部取消按钮 |
| `hasd_btn_confirm` | Button | 底部确定按钮 |

### hsu_address_selector_item.xml

列表项布局，单个地址项：

| 属性 | 值 |
|------|-----|
| 高度 | 42dp |
| 文字大小 | 13sp |
| 对齐方式 | 居中 |
| 单行显示 | 是 |

---

## 适配器说明

### AddressSelectorAaAdapter

列表适配器，用于渲染省/市/区三列数据。

**构造方法：**

```java
// 空构造，后续通过 updateData 设置数据
public AddressSelectorAaAdapter(Context context)

// 带数据构造
public AddressSelectorAaAdapter(Context context, List<String> dataList, int selectedIndex)
```

**主要方法：**

| 方法 | 说明 |
|------|------|
| `updateData(List<String> newData, int selectedIndex)` | 更新数据并设置选中项 |
| `setSelectedIndex(int selectedIndex)` | 仅更新选中项 |

**选中项样式：**

| 状态 | 文字颜色 | 字体 | 背景 |
|------|----------|------|------|
| 选中 | #1677FF | 加粗 | #0F1677FF |
| 未选中 | #D9333333 | 常规 | 透明 |

---

## 注意事项

1. **数据文件必须存在**：`assets/province.json` 必须存在，否则会进入加载失败状态。

2. **Context 类型**：必须传入 `Activity` 的 Context，不能使用 Application Context。

3. **线程安全**：数据加载在子线程执行，UI 更新自动切回主线程。

4. **弹窗高度**：固定为屏幕高度的 75%，不可配置。

5. **默认选中**：首次加载完成后，默认选中第一项（索引 0）。

6. **重复显示**：调用 `show()` 前请确保弹窗未处于显示状态。

7. **内存泄漏**：在 Activity 销毁前应关闭弹窗。

   ```java
   @Override
   protected void onDestroy() {
       super.onDestroy();
       if (dialog != null && dialog.isShowing()) {
           dialog.dismiss();
       }
   }
   ```

---

## 常见问题

### Q: 如何自定义地址数据？

A: 替换 `assets/province.json` 文件，按照规定的 JSON 格式组织数据即可。

### Q: 加载失败如何处理？

A: 弹窗内置重试机制，用户可点击"请重试"重新加载。也可在外部监听错误并提示用户。

### Q: 能否默认选中某个地址？

A: 当前版本不支持预设选中，默认选中第一项。如需预设功能，可修改源码中的 `selectedProvinceIndex`、`selectedCityIndex`、`selectedDistrictIndex` 初始值。

### Q: 如何修改弹窗高度？

A: 修改 `initWindow()` 方法中的高度计算：

```java
params.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.75);
// 修改 0.75 为其他比例
```

### Q: 能否禁用点击外部关闭？

A: 在 `onCreate()` 中修改：

```java
setCanceledOnTouchOutside(false);  // 禁用点击外部关闭
```
