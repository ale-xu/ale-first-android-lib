# TextPopupFirstAlert 使用指南

## 目录

- [简介](#简介)
- [功能特性](#功能特性)
- [快速开始](#快速开始)
  - [基本用法](#基本用法)
  - [完整示例](#完整示例)
- [Builder API 详解](#builder-api-详解)
  - [titleText](#titletextstring-title)
  - [contentText](#contenttextstring-content)
  - [autoCancel](#autocancelboolean-autocancel)
  - [onCancelClick](#oncanclicktextpopupfirstalertimpl-impl)
- [实例方法](#实例方法)
  - [open()](#open)
  - [close()](#close)
- [回调接口](#回调接口)
  - [TextPopupFirstAlertImpl](#textpopupfirstalertimpl)
- [布局结构](#布局结构)
- [自定义主题](#自定义主题)
- [注意事项](#注意事项)

---

## 简介

`TextPopupFirstAlert` 是一个基于 Android `Dialog` 封装的文本弹窗组件，采用 **Builder 模式** 构建。适用于需要向用户展示简短文本信息并提供关闭操作的场景，例如提示通知、操作确认、信息展示等。

弹窗界面包含：
- 顶部标题栏（可选，含关闭按钮）
- 中间内容文本区域
- 圆角卡片式背景

---

## 功能特性

| 特性 | 说明 |
|------|------|
| Builder 模式 | 链式调用，构建简洁 |
| 标题可隐藏 | 标题为空时自动隐藏标题栏 |
| 自动取消 | 支持点击外部区域或返回键关闭弹窗 |
| 关闭回调 | 点击关闭按钮时触发回调，便于业务处理 |
| 防重复显示 | `open()` 方法内部判断，避免重复弹出 |
| 透明背景 | 去除系统默认白色背景，自定义圆角卡片生效 |

---

## 快速开始

### 基本用法

最简单的用法，只需 Context 和内容文本：

```java
new TextPopupFirstAlert.Builder(context)
    .contentText("这是一条提示信息")
    .onCancelClick(() -> {
        // 点击关闭按钮后的回调
    })
    .show();
```

### 完整示例

```java
new TextPopupFirstAlert.Builder(this)
    .titleText("提示")
    .contentText("操作已完成，感谢您的使用！")
    .autoCancel(true)
    .onCancelClick(() -> {
        // 处理关闭逻辑
        Log.d("Alert", "弹窗已关闭");
    })
    .show();
```

如果需要先创建再手动控制显示：

```java
TextPopupFirstAlert alert = new TextPopupFirstAlert.Builder(this)
    .titleText("警告")
    .contentText("确定要执行此操作吗？")
    .autoCancel(false)  // 禁止点击外部关闭
    .onCancelClick(() -> {
        // 处理关闭逻辑
    })
    .create();

// 手动显示
alert.open();

// 手动关闭
alert.close();
```

---

## Builder API 详解

### `titleText(String title)`

设置弹窗标题文本。

| 参数 | 类型 | 说明 |
|------|------|------|
| `title` | `String` | 标题内容 |

**行为说明：**
- 当 `title` 为空字符串或仅包含空白字符时，标题区域（包括标题文本和关闭按钮所在的整行）会自动隐藏（`View.GONE`）。
- 当 `title` 有实际内容时，标题居中显示，关闭按钮位于右侧。

```java
// 有标题
.titleText("系统提示")

// 无标题（标题栏隐藏）
.titleText("")
```

---

### `contentText(String content)`

设置弹窗主体内容文本。

| 参数 | 类型 | 说明 |
|------|------|------|
| `content` | `String` | 弹窗显示的正文内容 |

**行为说明：**
- 内容文本居中显示，支持多行。
- 行间距为 1.3 倍，提升阅读体验。

```java
.contentText("这是一段较长的说明文本，\n支持换行显示。")
```

---

### `autoCancel(Boolean autoCancel)`

设置是否允许通过点击弹窗外部区域或按返回键关闭弹窗。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `autoCancel` | `Boolean` | `true` | `true` 允许外部关闭，`false` 禁止 |

```java
// 允许点击外部关闭（默认）
.autoCancel(true)

// 禁止点击外部关闭，只能通过关闭按钮关闭
.autoCancel(false)
```

---

### `onCancelClick(TextPopupFirstAlertImpl impl)`

设置关闭按钮的点击回调。

| 参数 | 类型 | 说明 |
|------|------|------|
| `impl` | `TextPopupFirstAlertImpl` | 关闭按钮点击回调接口 |

**行为说明：**
- 点击关闭按钮时，先触发回调，然后自动关闭弹窗。
- 回调为必传项，若不设置可能导致空指针异常。

```java
.onCancelClick(() -> {
    // 执行关闭前的逻辑，如埋点、状态更新等
})
```

---

## 实例方法

### `open()`

显示弹窗。内部已做防重复判断，多次调用不会重复弹出。

```java
alert.open();  // 安全调用，若已显示则忽略
```

---

### `close()`

关闭弹窗。内部已做判断，若弹窗未显示则忽略。

```java
alert.close();  // 安全调用，若未显示则忽略
```

---

## 回调接口

### `TextPopupFirstAlertImpl`

```java
public interface TextPopupFirstAlertImpl {
    void onCancelClick();
}
```

| 方法 | 说明 |
|------|------|
| `onCancelClick()` | 点击右上角关闭按钮时触发，触发后弹窗自动关闭 |

支持 Lambda 表达式简化写法：

```java
.onCancelClick(() -> {
    // 回调逻辑
})
```

---

## 布局结构

弹窗对应的布局文件为 `text_popup_first_alert.xml`，结构如下：

```
FrameLayout (根布局, 透明背景, padding=24dp)
└── CardView (白色背景, 圆角20dp, 无阴影)
    └── LinearLayout (垂直排列)
        ├── RelativeLayout (标题栏区域)
        │   ├── TextView [text_popup_first_alert_titleTv]  -- 标题文本, 居中
        │   └── CardView [text_popup_first_alert_cancelCard] -- 关闭按钮, 右上角
        │       └── ImageView (关闭图标, tint=#757575)
        └── TextView [text_popup_first_alert_contentTv]  -- 内容文本, 居中
```

**关键布局参数：**

| 元素 | 属性 | 值 |
|------|------|-----|
| 根布局 padding | 四周 | 24dp |
| CardView 圆角 | cardCornerRadius | 20dp |
| CardView 阴影 | cardElevation | 0dp |
| 标题文字大小 | textSize | 18sp |
| 标题文字颜色 | textColor | #212121 |
| 内容文字大小 | textSize | 15sp |
| 内容文字颜色 | textColor | #616161 |
| 内容行间距 | lineSpacingMultiplier | 1.3 |
| 关闭按钮尺寸 | 宽高 | 28dp x 28dp |

---

## 自定义主题

可通过构造函数传入自定义主题：

```java
// 使用自定义主题样式
TextPopupFirstAlert alert = new TextPopupFirstAlert(context, R.style.MyDialogTheme);
```

或通过 Builder 创建后修改 Window 属性：

```java
TextPopupFirstAlert alert = new TextPopupFirstAlert.Builder(context)
    .titleText("自定义")
    .contentText("内容")
    .create();

// 自定义 Window 动画等
Window window = alert.getWindow();
if (window != null) {
    window.setWindowAnimations(R.style.MyDialogAnimation);
    WindowManager.LayoutParams params = window.getAttributes();
    params.dimAmount = 0.5f;  // 背景变暗程度
    window.setAttributes(params);
}

alert.show();
```

---

## 注意事项

1. **回调必传**：`onCancelClick()` 必须设置，否则点击关闭按钮时会抛出 `NullPointerException`。

2. **标题为空自动隐藏**：传入空字符串或纯空白字符串时，整个标题栏区域（含关闭按钮）都会隐藏。如果仍需显示关闭按钮，请传入至少一个非空白字符的标题。

3. **`create()` vs `show()`**：
   - `create()` 仅创建弹窗实例，不显示，需手动调用 `open()` 或 `show()`。
   - `show()` 创建并立即显示弹窗。

4. **Context 类型**：Builder 需要传入 `Activity` 的 Context，不能使用 Application Context，否则可能导致主题异常或崩溃。

5. **线程安全**：弹窗的创建和显示必须在主线程（UI 线程）中执行。

6. **内存泄漏**：在 Activity/Fragment 销毁前务必关闭弹窗，避免持有 Activity 引用导致内存泄漏。

   ```java
   @Override
   protected void onDestroy() {
       super.onDestroy();
       if (alert != null && alert.isShowing()) {
           alert.close();
       }
   }
   ```
