# MMKVUtils 使用文档

## 目录

1. [概述](#1-概述)
2. [依赖配置](#2-依赖配置)
3. [初始化](#3-初始化)
    - 3.1 最简初始化
    - 3.2 指定日志级别
    - 3.3 自定义存储根目录
    - 3.4 自定义根目录 + 日志级别
    - 3.5 全量参数（含 so 加载器）
    - 3.6 其他初始化相关方法
4. [获取 MMKV 实例](#4-获取-mmkv-实例)
    - 4.1 默认实例（懒加载）
    - 4.2 带 mode 和加密 key 的默认实例
    - 4.3 按 ID 获取独立实例（单进程）
    - 4.4 指定 mode（单进程/多进程）
    - 4.5 快捷获取多进程实例
    - 4.6 加密实例
    - 4.7 加密 + 多进程
    - 4.8 自定义存储目录
    - 4.9 全量参数（mode + 加密 key + 自定义目录）
    - 4.10 只读备份目录实例
    - 4.11 匿名共享内存（ashmem）实例
5. [基础读写（默认实例）](#5-基础读写默认实例)
    - 5.1 Boolean
    - 5.2 Int
    - 5.3 Long
    - 5.4 Float
    - 5.5 Double
    - 5.6 String
    - 5.7 byte[]
    - 5.8 Set<String>
    - 5.9 Parcelable
6. [指定实例的读写](#6-指定实例的读写)
7. [Key 管理](#7-key-管理)
    - 7.1 检查 key 是否存在
    - 7.2 获取所有 key
    - 7.3 获取 key 数量
    - 7.4 获取 value 占用的存储大小
    - 7.5 删除单个 key
    - 7.6 批量删除
8. [清理、容量与生命周期](#8-清理容量与生命周期)
    - 8.1 清空全部数据
    - 8.2 碎片整理
    - 8.3 获取存储文件总大小 & 实际使用大小
    - 8.4 同步落盘
    - 8.5 清空内存缓存
    - 8.6 关闭实例
    - 8.7 校验文件是否有效
9. [加密相关](#9-加密相关)
    - 9.1 获取当前实例的加密 key
    - 9.2 变更加密 key（reKey）
    - 9.3 多进程场景同步新 key
10. [SharedPreferences 迁移](#10-sharedpreferences-迁移)
    - 10.1 导入系统 SharedPreferences 数据
    - 10.2 将 MMKV 实例直接作为 SharedPreferences 使用
11. [备份与恢复](#11-备份与恢复)
    - 11.1 备份单个实例
    - 11.2 恢复单个实例
    - 11.3 备份全部实例
    - 11.4 恢复全部实例
12. [多进程互斥锁](#12-多进程互斥锁)
13. [日志与内容变更回调](#13-日志与内容变更回调)
    - 13.1 注册全局日志/错误回调
    - 13.2 注销回调
    - 13.3 手动检查是否被其他进程修改
14. [进程模式校验开关（调试用）](#14-进程模式校验开关调试用)
15. [完整使用示例](#15-完整使用示例)
    - 15.1 Application 初始化
    - 15.2 在任意地方读写
    - 15.3 多实例使用
16. [注意事项](#16-注意事项)
17. [源码参考](#17-源码参考)

---

## 1. 概述

`MMKVUtils` 是基于腾讯开源 [MMKV](https://github.com/Tencent/MMKV) 封装的 Android 工具类，提供了全面、便捷的 API，覆盖：

- 初始化（支持自定义目录、日志级别、动态加载 so）
- 多实例管理（单进程/多进程、加密、自定义路径、匿名共享内存）
- 全类型数据读写（boolean、int、long、float、double、String、byte[]、Set<String>、Parcelable）
- Key 管理、容量统计、清理同步
- 加密 key 变更
- SharedPreferences 迁移
- 备份与恢复
- 多进程锁与内容变更通知
- 日志重定向与错误回调

所有方法均为静态方法，可直接调用，极大降低使用成本。

---

## 2. 依赖配置

在模块的 `build.gradle` 中添加：

```groovy
dependencies {
    implementation 'com.tencent:mmkv:2.4.0'   // 建议使用最新稳定版
}
```

---

## 3. 初始化

**必须**在 `Application.onCreate()` 中调用初始化方法，之后再进行任何读写操作。

### 3.1 最简初始化（默认根目录）

```java
MMKVUtils.init(context);
```

### 3.2 指定日志级别

```java
MMKVUtils.init(context, MMKVLogLevel.LevelDebug); // 或 LevelInfo, LevelError
```

### 3.3 自定义存储根目录

```java
String customRoot = context.getExternalFilesDir("mmkv").getAbsolutePath();
MMKVUtils.init(context, customRoot);
```

### 3.4 自定义根目录 + 日志级别

```java
MMKVUtils.init(context, customRoot, MMKVLogLevel.LevelDebug);
```

### 3.5 全量参数（含 so 加载器）

```java
MMKVUtils.init(context, customRoot, new MMKV.LibLoader() {
    @Override
    public void loadLibrary(String libName) {
        System.loadLibrary(libName);
    }
}, MMKVLogLevel.LevelDebug);
```

### 3.6 其他初始化相关方法

| 方法 | 说明 |
|------|------|
| `getRootDir()` | 获取当前 MMKV 根目录 |
| `setLogLevel(level)` | 运行时动态调整日志级别 |
| `version()` | 返回 MMKV 版本号 |
| `pageSize()` | 返回设备内存分页大小 |
| `onExit()` | App 退出前调用（非必须） |

---

## 4. 获取 MMKV 实例

初始化后，所有读写操作默认使用 **默认实例**（`defaultMMKV`），也可获取自定义实例。

### 4.1 默认实例（懒加载）

```java
MMKV kv = MMKVUtils.kv();   // 若未初始化则自动 defaultMMKV()
```

### 4.2 带 mode 和加密 key 的默认实例

```java
MMKV kv = MMKVUtils.defaultKv(MMKV.MULTI_PROCESS_MODE, "myCryptKey");
```

### 4.3 按 ID 获取独立实例（单进程）

```java
MMKV kv = MMKVUtils.kv("user_info");
```

### 4.4 指定 mode（单进程/多进程）

```java
MMKV kv = MMKVUtils.kv("config", MMKV.MULTI_PROCESS_MODE);
```

### 4.5 快捷获取多进程实例

```java
MMKV kv = MMKVUtils.kvMultiProcess("shared_data");
```

### 4.6 加密实例（AES-128）

```java
MMKV kv = MMKVUtils.kvEncrypted("secure_data", "encryptKey123");
```

### 4.7 加密 + 多进程

```java
MMKV kv = MMKVUtils.kvEncryptedMultiProcess("secure_shared", "encryptKey123");
```

### 4.8 自定义存储目录

```java
MMKV kv = MMKVUtils.kvWithCustomPath("data", "/sdcard/mmkv/");
```

### 4.9 全量参数（mode + 加密 key + 自定义目录）

```java
MMKV kv = MMKVUtils.kv("myData", MMKV.MULTI_PROCESS_MODE, "key", "/custom/path");
```

### 4.10 只读备份目录实例（`backedUpMMKVWithID`）

```java
MMKV backup = MMKVUtils.backedUpKv("backup_id", MMKV.SINGLE_PROCESS_MODE, null, "/backup/dir");
```

### 4.11 匿名共享内存（ashmem）实例（不落盘）

```java
MMKV ashmem = MMKVUtils.kvAshmem(context, "temp", 1024 * 100, MMKV.SINGLE_PROCESS_MODE, null);
```

---

## 5. 基础读写（默认实例）

所有读写方法均针对默认实例（由 `kv()` 返回）。

### 5.1 Boolean

```java
MMKVUtils.putBoolean("key_bool", true);
boolean val = MMKVUtils.getBoolean("key_bool");          // 默认 false
boolean valWithDef = MMKVUtils.getBoolean("key_bool", false);
```

### 5.2 Int

```java
MMKVUtils.putInt("key_int", 100);
int val = MMKVUtils.getInt("key_int");
int valWithDef = MMKVUtils.getInt("key_int", 0);
```

### 5.3 Long

```java
MMKVUtils.putLong("key_long", System.currentTimeMillis());
long val = MMKVUtils.getLong("key_long");
```

### 5.4 Float

```java
MMKVUtils.putFloat("key_float", 3.14f);
float val = MMKVUtils.getFloat("key_float");
```

### 5.5 Double

```java
MMKVUtils.putDouble("key_double", 3.1415926);
double val = MMKVUtils.getDouble("key_double");
```

### 5.6 String

```java
MMKVUtils.putString("key_str", "Hello MMKV");
String str = MMKVUtils.getString("key_str");
String def = MMKVUtils.getString("key_missing", "default");
```

### 5.7 byte[]

```java
byte[] data = {1, 2, 3};
MMKVUtils.putBytes("key_bytes", data);
byte[] read = MMKVUtils.getBytes("key_bytes");
```

### 5.8 Set<String>

```java
Set<String> set = new HashSet<>(Arrays.asList("a", "b"));
MMKVUtils.putStringSet("key_set", set);
Set<String> result = MMKVUtils.getStringSet("key_set");
// 指定默认值和集合类型
Set<String> defSet = new HashSet<>();
result = MMKVUtils.getStringSet("key_set", defSet, HashSet.class);
```

### 5.9 Parcelable

```java
// 存储
MyParcelable obj = new MyParcelable();
MMKVUtils.putParcelable("key_parcel", obj);
// 读取
MyParcelable readObj = MMKVUtils.getParcelable("key_parcel", MyParcelable.class);
MyParcelable withDef = MMKVUtils.getParcelable("key_missing", MyParcelable.class, new MyParcelable());
```

---

## 6. 指定实例的读写

若使用自定义实例，可传入 `MMKV` 对象进行操作（方法名对应，但多一个 `MMKV` 参数）。

```java
MMKV userKv = MMKVUtils.kv("user");
MMKVUtils.putString(userKv, "name", "张三");
String name = MMKVUtils.getString(userKv, "name", "未知");
```

支持的类型：`boolean`、`int`、`long`、`String`、`byte[]`（其他类型可直接调用 `target.encode()` 系列）。

---

## 7. Key 管理

### 7.1 检查 key 是否存在

```java
boolean exists = MMKVUtils.containsKey("key_int");
// 指定实例
boolean existsInTarget = MMKVUtils.containsKey(userKv, "name");
```

### 7.2 获取所有 key

```java
String[] keys = MMKVUtils.allKeys();   // 可能为 null
// 指定实例
String[] keys2 = MMKVUtils.allKeys(userKv);
```

### 7.3 获取 key 数量

```java
long count = MMKVUtils.count();   // 默认实例
```

### 7.4 获取 value 占用的存储大小

```java
int size = MMKVUtils.getValueSize("key_str");       // 包含内部开销
int actualSize = MMKVUtils.getValueActualSize("key_str"); // 原始长度
```

### 7.5 删除单个 key

```java
MMKVUtils.remove("key_int");
// 指定实例
MMKVUtils.remove(userKv, "name");
```

### 7.6 批量删除

```java
MMKVUtils.removeAll("key1", "key2", "key3");
// 指定实例
MMKVUtils.removeAll(userKv, "keyA", "keyB");
```

---

## 8. 清理、容量与生命周期

### 8.1 清空全部数据

```java
MMKVUtils.clearAll();            // 默认实例
MMKVUtils.clearAll(userKv);      // 指定实例
```

### 8.2 碎片整理（推荐在大量删除后调用）

```java
MMKVUtils.trim();
MMKVUtils.trim(userKv);
```

### 8.3 获取存储文件总大小 & 实际使用大小

```java
long total = MMKVUtils.totalSize();
long actual = MMKVUtils.actualSize();
// 指定实例同上
```

### 8.4 同步落盘（自动，一般无需手动调用）

```java
MMKVUtils.sync();   // 同步落盘
MMKVUtils.async();  // 异步落盘（默认）
```

### 8.5 清空内存缓存（内存不足时调用）

```java
MMKVUtils.clearMemoryCache(userKv);
```

### 8.6 关闭实例（释放 mmap）

```java
MMKVUtils.close(userKv);
// 关闭后不再使用该实例
```

### 8.7 校验文件是否有效

```java
boolean valid = MMKVUtils.isFileValid("user");  // 默认目录
boolean valid2 = MMKVUtils.isFileValid("user", "/custom/path");
```

---

## 9. 加密相关

### 9.1 获取当前实例的加密 key

```java
String key = MMKVUtils.cryptKey(userKv); // 未加密返回 null
```

### 9.2 变更加密 key（reKey）

- `newKey = null`：加密 → 明文
- `newKey` 非空：明文 → 加密，或更换已有加密 key

```java
boolean success = MMKVUtils.reKey(userKv, "newKey123");
```

### 9.3 多进程场景同步新 key（当其他进程已 reKey 后）

```java
MMKVUtils.checkReSetCryptKey(userKv, "newKey123");
```

---

## 10. SharedPreferences 迁移

### 10.1 导入系统 SharedPreferences 数据

```java
SharedPreferences sp = getSharedPreferences("pref_name", MODE_PRIVATE);
int count = MMKVUtils.importFromSharedPreferences(sp);   // 默认实例
int count2 = MMKVUtils.importFromSharedPreferences(userKv, sp);
```

### 10.2 将 MMKV 实例直接作为 SharedPreferences 使用

```java
SharedPreferences sp = MMKVUtils.asSharedPreferences();
String value = sp.getString("key", "");
SharedPreferences.Editor editor = MMKVUtils.edit();
editor.putString("key", "value").apply();
```

---

## 11. 备份与恢复

### 11.1 备份单个实例到指定目录

```java
// 备份默认目录下的 "user" 实例到 /backup/
boolean ok = MMKVUtils.backupOne("user", "/backup/", null);
// 若实例在自定义目录，则传入 rootPath
boolean ok2 = MMKVUtils.backupOne("user", "/backup/", "/custom/path");
```

### 11.2 恢复单个实例

```java
boolean restored = MMKVUtils.restoreOne("user", "/backup/", null);
```

### 11.3 备份全部实例

```java
long count = MMKVUtils.backupAll("/backup/");
```

### 11.4 恢复全部实例

```java
long count = MMKVUtils.restoreAll("/backup/");
```

---

## 12. 多进程互斥锁

适用于多进程模式下保证原子操作。

```java
MMKVUtils.lock(userKv);
try {
        // 临界区操作
        userKv.encode("key", "value");
} finally {
        MMKVUtils.unlock(userKv);
}

// 尝试加锁（非阻塞）
        if (MMKVUtils.tryLock(userKv)) {
        try {
        // ...
        } finally {
        MMKVUtils.unlock(userKv);
    }
            }
```

---

## 13. 日志与内容变更回调

### 13.1 注册全局日志/错误回调

```java
MMKVUtils.registerHandler(new MMKVHandler() {
    @Override
    public void mmkvLog(MMKVLogLevel level, String file, int line, String msg) {
        // 自定义日志输出
    }

    @Override
    public void mmkvContentChangedByOuterProcess(String mmapID) {
        // 其他进程修改了数据，可在回调中刷新 UI
    }

    @Override
    public void mmkvError(String mmapID, int errorCode, String errorMsg) {
        // 错误处理
    }
});
```

### 13.2 注销回调

```java
MMKVUtils.unregisterHandler();
```

### 13.3 手动检查是否被其他进程修改

```java
MMKVUtils.checkContentChangedByOuterProcess(userKv);
```

---

## 14. 进程模式校验开关（调试用）

```java
// Debug 包默认开启，Release 默认关闭
MMKVUtils.enableProcessModeChecker();
MMKVUtils.disableProcessModeChecker();
```

---

## 15. 完整使用示例

### 15.1 Application 初始化

```java
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 使用默认配置初始化
        MMKVUtils.init(this);
        // 或自定义
        // MMKVUtils.init(this, getExternalFilesDir("mmkv").getAbsolutePath(), MMKVLogLevel.LevelDebug);
    }
}
```

### 15.2 在任意地方读写

```java
// 存储用户登录态
MMKVUtils.putBoolean("isLogin", true);
MMKVUtils.putString("userName", "张三");
MMKVUtils.putInt("userId", 10001);

// 读取
boolean isLogin = MMKVUtils.getBoolean("isLogin", false);
String name = MMKVUtils.getString("userName", "");
int id = MMKVUtils.getInt("userId", -1);

// 删除单个
MMKVUtils.remove("userName");

// 清空全部（慎用）
// MMKVUtils.clearAll();
```

### 15.3 多实例使用

```java
// 存储 App 配置（多进程）
MMKV configKv = MMKVUtils.kvMultiProcess("app_config");
MMKVUtils.putBoolean(configKv, "darkMode", true);

// 存储用户信息（加密）
MMKV userKv = MMKVUtils.kvEncrypted("user_info", "mySecret");
MMKVUtils.putString(userKv, "token", "abcdefg");
```

---

## 16. 注意事项

1. **初始化时机**：务必在 `Application.onCreate()` 中先调用 `init`，否则后续操作可能抛出异常。
2. **多进程模式**：如需多进程共享数据，必须使用 `MMKV.MULTI_PROCESS_MODE` 创建实例。
3. **加密 key 长度**：AES-128 加密 key 建议不超过 16 字节，超出部分会被截断。
4. **文件有效性**：备份恢复前建议使用 `isFileValid` 检查文件完整性。
5. **内存释放**：不再使用的实例可调用 `close()` 释放 mmap，避免内存浪费。
6. **线程安全**：MMKV 内部是线程安全的，无需额外同步。
7. **版本兼容**：本工具类基于 MMKV 2.4.0 封装，若升级到更高版本，请对照官方 Javadoc 检查 API 变更。

---

## 17. 源码参考

完整工具类源码请参见 `MMKVUtils.java`，包名为 `org.auleck.first_utils.mmkv`。

---
  
  