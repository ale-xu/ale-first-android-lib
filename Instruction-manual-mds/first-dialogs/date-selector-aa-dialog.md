# DateSelectorAaDialog 使用指南

## 目录

- [简介](#简介)
- [功能特性](#功能特性)
- [快速开始](#快速开始)
  - [基本用法](#基本用法)
  - [完整示例](#完整示例)
- [配置方法](#配置方法)
  - [setMinYear](#setminyearint-minyear)
  - [setMaxYear](#setmaxyearint-maxyear)
- [回调接口](#回调接口)
  - [OnDateConfirmListener](#ondateconfirmlistener)
  - [OnDateCancelListener](#ondatecancellistener)
- [默认行为](#默认行为)
- [级联联动逻辑](#级联联动逻辑)
  - [年份切换](#年份切换)
  - [月份切换](#月份切换)
  - [日期切换](#日期切换)
- [闰年处理](#闰年处理)
- [界面结构](#界面结构)
  - [标题栏](#标题栏)
  - [Tab 路径头](#tab-路径头)
  - [三列选择列表](#三列选择列表)
  - [底部按钮栏](#底部按钮栏)
  - [遮罩层](#遮罩层)
- [动画效果](#动画效果)
- [布局文件说明](#布局文件说明)
- [适配器说明](#适配器说明)
- [注意事项](#注意事项)
- [常见问题](#常见问题)

---

## 简介

`DateSelectorAaDialog` 是一个底部弹出的日期选择器，基于 `AppCompatDialog` 封装。适用于需要用户选择年月日的场景，如生日选择、日期筛选等。

选择器从屏幕底部弹出，包含年、月、日三列联动列表，顶部显示当前选中路径，底部提供确认/取消按钮。默认选中当天日期。

---

## 功能特性

| 特性 | 说明 |
|------|------|
| 三级联动 | 年 → 月 → 日 自动级联，日期根据年月自动计算 |
| Tab 路径头 | 实时显示当前选中的年/月/日路径 |
| 默认选中今天 | 打开时自动定位到当前日期 |
| 年份范围配置 | 支持设置最小/最大年份 |
| 闰年支持 | 自动处理闰年2月天数（28/29天） |
| 月份天数自适应 | 根据年月自动计算当月天数（28/29/30/31） |
| 日期越界修正 | 切换年月时，若日期超出范围自动修正为最后一天 |
| 双操作入口 | 顶部和底部均有确认/取消按钮 |
| 遮罩点击关闭 | 点击弹窗上方遮罩区域可关闭弹窗 |
| 底部弹出动画 | 平滑的底部滑入/滑出动画效果 |

---

## 快速开始

### 基本用法

```java
DateSelectorAaDialog dialog = new DateSelectorAaDialog(this);
dialog.setOnConfirmListener((year, month, day) -> {
    String dateStr = year + "-" + month + "-" + day;
    Toast.makeText(this, dateStr, Toast.LENGTH_SHORT).show();
});
dialog.show();
```

### 完整示例

```java
DateSelectorAaDialog dialog = new DateSelectorAaDialog(this);

// 可选：设置年份范围
dialog.setMinYear(1949);
dialog.setMaxYear(2050);

// 设置确认回调
dialog.setOnConfirmListener((year, month, day) -> {
    // 处理选中日期
    String fullDate = year + "年" + month + "月" + day + "日";
    Log.d("Date", "选中日期: " + fullDate);
    
    // 更新 UI
    tvDate.setText(fullDate);
});

// 设置取消回调（可选）
dialog.setOnCancelListener(() -> {
    Log.d("Date", "用户取消了日期选择");
});

// 显示弹窗
dialog.show();
```

---

## 配置方法

### setMinYear(int minYear)

设置可选的最小年份。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `minYear` | `int` | `1949` | 最小年份，不能小于1 |

```java
dialog.setMinYear(1900);  // 最小可选1900年
```

**注意：** 必须在 `show()` 之前调用，否则不生效。

---

### setMaxYear(int maxYear)

设置可选的最大年份。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `maxYear` | `int` | 当前年份 | 最大年份，不能小于 minYear |

```java
dialog.setMaxYear(2030);  // 最大可选2030年
```

**注意：** 
- 默认值为当前年份
- 必须在 `show()` 之前调用
- 若设置的值小于 minYear，会抛出 `IllegalArgumentException`

---

## 回调接口

### OnDateConfirmListener

确认按钮点击回调，返回选中的年月日。

```java
public interface OnDateConfirmListener {
    void onConfirm(int year, int month, int day);
}
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `year` | `int` | 选中的年份，如 2024 |
| `month` | `int` | 选中的月份（1-12） |
| `day` | `int` | 选中的日期（1-31，根据月份自动限制） |

**使用示例：**

```java
dialog.setOnConfirmListener((year, month, day) -> {
    // year = 2024
    // month = 3
    // day = 15
    String date = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);
    // date = "2024-03-15"
});
```

---

### OnDateCancelListener

取消按钮点击回调（可选）。

```java
public interface OnDateCancelListener {
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

## 默认行为

| 行为 | 说明 |
|------|------|
| 默认选中日期 | 打开时自动选中当前日期（今天） |
| 年份列表顺序 | 从大到小降序排列（最新年份在顶部） |
| 月份列表 | 1-12月，升序排列 |
| 日期列表 | 1-当月最大天数，升序排列 |
| 弹窗高度 | 屏幕高度的 75% |
| 点击外部 | 支持点击遮罩关闭 |

---

## 级联联动逻辑

选择器实现了完整的三级联动：

### 年份切换

| 操作 | 联动效果 |
|------|----------|
| 切换年份 | 重新计算当月天数，更新日期列表 |

**示例：** 从2024年切换到2023年，若当前是2月，日期列表会自动更新（闰年29天 → 平年28天）

### 月份切换

| 操作 | 联动效果 |
|------|----------|
| 切换月份 | 重新计算当月天数，更新日期列表 |

**示例：** 从1月切换到2月，日期列表从31天变为28/29天

### 日期切换

| 操作 | 联动效果 |
|------|----------|
| 切换日期 | 仅更新选中状态和头部文字 |

**日期越界修正：**

当切换年月导致当前选中日期超出新月份天数时，自动修正为该月最后一天：

```
当前选中：2024年1月31日
切换到2月 → 日期自动修正为2月29日（2024是闰年）
```

---

## 闰年处理

选择器自动处理闰年规则：

**闰年判断规则：**
- 能被4整除且不能被100整除，**或**
- 能被400整除

**示例：**
- 2024年：闰年（能被4整除，不能被100整除）→ 2月29天
- 1900年：平年（能被100整除，不能被400整除）→ 2月28天
- 2000年：闰年（能被400整除）→ 2月29天

**各月天数：**

| 月份 | 天数 |
|------|------|
| 1、3、5、7、8、10、12月 | 31天 |
| 4、6、9、11月 | 30天 |
| 2月（闰年） | 29天 |
| 2月（平年） | 28天 |

---

## 界面结构

### 标题栏

位于弹窗顶部，高度 44dp。

| 元素 | 位置 | 功能 |
|------|------|------|
| 取消按钮 | 左侧 | 关闭弹窗，触发取消回调 |
| 标题文字 | 居中 | 显示"请选择日期" |
| 确定按钮 | 右侧 | 确认选择，触发确认回调 |

---

### Tab 路径头

位于标题栏下方，显示当前选中的年/月/日路径。

```
[ 2024年 ] > [ 3月 ] > [ 15日 ]
```

**样式说明：**
- 文字颜色：主题蓝色（#1677FF）
- 字体样式：加粗
- 单行显示，超长省略

---

### 三列选择列表

中间区域为年、月、日三列并排显示：

```
┌──────────┬──────────┬──────────┐
│   年份   │   月份   │   日期   │
│  列表    │  列表    │  列表    │
└──────────┴──────────┴──────────┘
```

- 每列宽度均分（weight=1）
- 列之间有 0.5dp 分隔线
- 选中项蓝色高亮，背景半透明
- 列表项显示格式：数字+单位（如"2024年"、"3月"、"15日"）

---

### 底部按钮栏

位于弹窗底部，高度 56dp。

| 按钮 | 样式 | 功能 |
|------|------|------|
| 取消 | 灰色边框 | 关闭弹窗 |
| 确定 | 蓝色实心 | 确认选择 |

---

### 遮罩层

弹窗上方的透明区域，点击可关闭弹窗。

```
┌────────────────────────┐
│                        │  ← 遮罩层（可点击关闭）
│                        │
├────────────────────────┤
│   日期选择器内容        │
└────────────────────────┘
```

---

## 动画效果

弹窗采用底部滑入/滑出动画：

| 动画 | 时长 | 插值器 | 效果 |
|------|------|--------|------|
| 显示 | 系统默认 | - | 内容从底部滑入 |
| 关闭 | 200ms | FastOutSlowInInterpolator | 内容向下滑出 + 遮罩淡出 |

关闭时：
1. 内容视图向下滑出屏幕
2. 遮罩层透明度从1变为0
3. 动画结束后真正关闭弹窗

---

## 布局文件说明

### hsu_date_selector_dialog.xml

主布局文件，结构如下：

```
LinearLayout (根布局, 垂直方向)
├── View [hdsd_mask]  -- 遮罩层, 点击关闭弹窗
└── LinearLayout [hdsd_content]  -- 内容容器
    ├── LinearLayout  -- 标题栏
    │   ├── TextView [hdsd_tv_cancel_top]  -- 顶部取消按钮
    │   ├── TextView  -- 标题"请选择日期"
    │   └── TextView [hdsd_tv_confirm_top]  -- 顶部确定按钮
    ├── LinearLayout  -- Tab路径头
    │   ├── TextView [hdsd_tv_year]  -- 年份显示
    │   ├── TextView  -- 分隔符">"
    │   ├── TextView [hdsd_tv_month]  -- 月份显示
    │   ├── TextView  -- 分隔符">"
    │   └── TextView [hdsd_tv_day]  -- 日期显示
    ├── LinearLayout  -- 三列列表容器
    │   ├── ListView [hdsd_lv_year]  -- 年份列表
    │   ├── View  -- 分隔线
    │   ├── ListView [hdsd_lv_month]  -- 月份列表
    │   ├── View  -- 分隔线
    │   └── ListView [hdsd_lv_day]  -- 日期列表
    └── LinearLayout  -- 底部按钮栏
        ├── Button [hdsd_btn_cancel]  -- 底部取消按钮
        └── Button [hdsd_btn_confirm]  -- 底部确定按钮
```

### hsu_date_selector_item.xml

列表项布局，单个日期项：

| 属性 | 值 |
|------|-----|
| 高度 | 42dp |
| 文字大小 | 13sp |
| 对齐方式 | 居中 |
| 单行显示 | 是 |
| 左右内边距 | 6dp |

---

## 适配器说明

### DateSelectorAaAdapter

列表适配器，用于渲染年/月/日三列数据。

**构造方法：**

```java
public DateSelectorAaAdapter(Context context, List<Integer> dataList, String suffix)
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `context` | `Context` | 上下文 |
| `dataList` | `List<Integer>` | 数据列表（年/月/日数值） |
| `suffix` | `String` | 显示后缀（"年"/"月"/"日"） |

**主要方法：**

| 方法 | 说明 |
|------|------|
| `updateData(List<Integer> newData, int selectedIndex)` | 更新数据并设置选中项 |
| `setSelectedIndex(int selectedIndex)` | 仅更新选中项 |

**显示格式：**

列表项显示为 `数值 + 后缀`，例如：
- 年份列表：`2024年`、`2023年`、`2022年`...
- 月份列表：`1月`、`2月`、`3月`...
- 日期列表：`1日`、`2日`、`3日`...

**选中项样式：**

| 状态 | 文字颜色 | 字体 | 背景 |
|------|----------|------|------|
| 选中 | #1677FF | 加粗 | #0D1677FF |
| 未选中 | #D9333333 | 常规 | 透明 |

---

## 注意事项

1. **配置时机**：`setMinYear()` 和 `setMaxYear()` 必须在 `show()` 之前调用。

2. **参数校验**：
   - minYear 不能小于 1
   - maxYear 不能小于 minYear
   - 违反规则会抛出 `IllegalArgumentException`

3. **Context 类型**：必须传入 `Activity` 的 Context，不能使用 Application Context。

4. **弹窗高度**：固定为屏幕高度的 75%，不可配置。

5. **年份列表顺序**：年份从大到小降序排列（最新年份在顶部）。

6. **月份参数**：回调中的月份是 1-12，不是 Calendar 的 0-11。

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

### Q: 如何设置默认选中某个日期？

A: 当前版本默认选中当天日期，不支持自定义默认日期。如需修改，可调整源码中 `setDefaultToToday()` 方法的逻辑。

### Q: 年份范围如何设置？

A: 使用 `setMinYear()` 和 `setMaxYear()` 方法：

```java
dialog.setMinYear(1900);
dialog.setMaxYear(2100);
```

### Q: 如何修改弹窗高度？

A: 修改 `initWindow()` 方法中的高度计算：

```java
params.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.75);
// 修改 0.75 为其他比例
```

### Q: 能否禁用点击遮罩关闭？

A: 在 `onCreate()` 中修改：

```java
setCanceledOnTouchOutside(false);  // 禁用点击外部关闭
```

同时需要移除遮罩层的点击事件：

```java
// 注释掉这行
// maskView.setOnClickListener(v -> cancelSelection());
```

### Q: 如何自定义日期格式？

A: 修改 `DateSelectorAaAdapter` 构造时传入的后缀参数，或修改 `getView()` 方法中的文字拼接逻辑。

### Q: 回调中的月份是从0开始还是从1开始？

A: 从1开始（1-12），符合日常习惯，无需额外 +1 处理。
