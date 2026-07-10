package org.auleck.first_utils.mmkv;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;

import com.tencent.mmkv.MMKV;
import com.tencent.mmkv.MMKVHandler;
import com.tencent.mmkv.MMKVLogLevel;

import java.util.Set;

/**
 * MMKV 工具类
 *
 * 严格按照腾讯开源 MMKV 官方 Java API 封装（对照 GitHub 源码 / Javadoc 逐一核实签名），
 * 覆盖初始化、多实例（单进程/多进程/加密/自定义目录）、全类型读写、Key 管理、
 * 容量与清理、加密 reKey、SharedPreferences 迁移、备份恢复、日志与内容变更回调等能力。
 *
 * 依赖（build.gradle app 模块）：
 * implementation 'com.tencent:mmkv:2.4.0' // 版本号请以最新 release 为准
 *
 * 使用：
 * 1. Application#onCreate() 中调用 MMKVUtils.init(this);
 * 2. 业务代码直接调用 MMKVUtils.putXxx / getXxx 等静态方法。
 *
 * 注：本类只封装了官方 Java SDK 中明确存在的方法，不同版本的 MMKV 可能新增/移除个别 API，
 * 若编译仍有报红，请对照当前依赖版本的 Javadoc 核对方法是否存在。
 */
public final class MMKVUtils {

    /** 默认实例 */
    private static volatile MMKV sDefaultKV;

    private MMKVUtils() {
        throw new UnsupportedOperationException("u cannot instantiate me");
    }

    // ======================== 一、初始化 ========================

    /** 默认根目录 {FilesDir}/mmkv/ 初始化 */
    public static String init(Context context) {
        String root = MMKV.initialize(context);
        sDefaultKV = MMKV.defaultMMKV();
        return root;
    }

    /** 指定日志级别初始化 */
    public static String init(Context context, MMKVLogLevel logLevel) {
        String root = MMKV.initialize(context, logLevel);
        sDefaultKV = MMKV.defaultMMKV();
        return root;
    }

    /** 指定自定义根目录初始化 */
    public static String init(Context context, String rootDir) {
        String root = MMKV.initialize(context, rootDir);
        sDefaultKV = MMKV.defaultMMKV();
        return root;
    }

    /** 指定自定义根目录 + 日志级别初始化 */
    public static String init(Context context, String rootDir, MMKVLogLevel logLevel) {
        String root = MMKV.initialize(context, rootDir, logLevel);
        sDefaultKV = MMKV.defaultMMKV();
        return root;
    }

    /** 指定自定义根目录 + 三方 so 加载器 + 日志级别初始化（全量参数） */
    public static String init(Context context, String rootDir, MMKV.LibLoader loader, MMKVLogLevel logLevel) {
        String root = MMKV.initialize(context, rootDir, loader, logLevel);
        sDefaultKV = MMKV.defaultMMKV();
        return root;
    }

    /** 获取当前 MMKV 根目录（需先 init） */
    public static String getRootDir() {
        return MMKV.getRootDir();
    }

    /** 运行期动态调整日志级别 */
    public static void setLogLevel(MMKVLogLevel level) {
        MMKV.setLogLevel(level);
    }

    /** App 即将退出时调用（非必须） */
    public static void onExit() {
        MMKV.onExit();
    }

    /** MMKV 当前版本号 */
    public static String version() {
        return MMKV.version();
    }

    /** 设备内存分页大小 */
    public static int pageSize() {
        return MMKV.pageSize();
    }

    // ======================== 二、多实例获取 ========================

    /** 默认实例（懒加载兜底，正常应先调用 init） */
    public static MMKV kv() {
        if (sDefaultKV == null) {
            synchronized (MMKVUtils.class) {
                if (sDefaultKV == null) {
                    sDefaultKV = MMKV.defaultMMKV();
                }
            }
        }
        return sDefaultKV;
    }

    /** 默认实例（自定义 mode + 加密 key） */
    public static MMKV defaultKv(int mode, String cryptKey) {
        return MMKV.defaultMMKV(mode, cryptKey);
    }

    /** 单进程模式下，按 ID 隔离的独立实例 */
    public static MMKV kv(String mmapID) {
        return MMKV.mmkvWithID(mmapID);
    }

    /** 指定 mode（单进程/多进程）的实例 */
    public static MMKV kv(String mmapID, int mode) {
        return MMKV.mmkvWithID(mmapID, mode);
    }

    /** 多进程模式实例的快捷方法 */
    public static MMKV kvMultiProcess(String mmapID) {
        return MMKV.mmkvWithID(mmapID, MMKV.MULTI_PROCESS_MODE);
    }

    /** 加密实例（AES-128，cryptKey 长度建议 <= 16 字节），默认单进程 */
    public static MMKV kvEncrypted(String mmapID, String cryptKey) {
        return MMKV.mmkvWithID(mmapID, MMKV.SINGLE_PROCESS_MODE, cryptKey);
    }

    /** 加密 + 多进程 组合实例 */
    public static MMKV kvEncryptedMultiProcess(String mmapID, String cryptKey) {
        return MMKV.mmkvWithID(mmapID, MMKV.MULTI_PROCESS_MODE, cryptKey);
    }

    /** 自定义存储目录的实例（默认单进程、不加密） */
    public static MMKV kvWithCustomPath(String mmapID, String rootPath) {
        return MMKV.mmkvWithID(mmapID, rootPath);
    }

    /** 全量参数实例：mode + 加密 key + 自定义目录 */
    public static MMKV kv(String mmapID, int mode, String cryptKey, String rootPath) {
        return MMKV.mmkvWithID(mmapID, mode, cryptKey, rootPath);
    }

    /** 打开一份备份目录下的实例（只读校验用，见官方 API backedUpMMKVWithID） */
    public static MMKV backedUpKv(String mmapID, int mode, String cryptKey, String backupRootDir) {
        return MMKV.backedUpMMKVWithID(mmapID, mode, cryptKey, backupRootDir);
    }

    /** 基于匿名共享内存(ashmem)的实例，不落盘 */
    public static MMKV kvAshmem(Context context, String mmapID, int size, int mode, String cryptKey) {
        return MMKV.mmkvWithAshmemID(context, mmapID, size, mode, cryptKey);
    }

    // ======================== 三、基础读写（默认实例） ========================

    public static boolean putBoolean(String key, boolean value) {
        return kv().encode(key, value);
    }

    public static boolean getBoolean(String key) {
        return kv().decodeBool(key);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return kv().decodeBool(key, defValue);
    }

    public static boolean putInt(String key, int value) {
        return kv().encode(key, value);
    }

    public static int getInt(String key) {
        return kv().decodeInt(key);
    }

    public static int getInt(String key, int defValue) {
        return kv().decodeInt(key, defValue);
    }

    public static boolean putLong(String key, long value) {
        return kv().encode(key, value);
    }

    public static long getLong(String key) {
        return kv().decodeLong(key);
    }

    public static long getLong(String key, long defValue) {
        return kv().decodeLong(key, defValue);
    }

    public static boolean putFloat(String key, float value) {
        return kv().encode(key, value);
    }

    public static float getFloat(String key) {
        return kv().decodeFloat(key);
    }

    public static float getFloat(String key, float defValue) {
        return kv().decodeFloat(key, defValue);
    }

    public static boolean putDouble(String key, double value) {
        return kv().encode(key, value);
    }

    public static double getDouble(String key) {
        return kv().decodeDouble(key);
    }

    public static double getDouble(String key, double defValue) {
        return kv().decodeDouble(key, defValue);
    }

    public static boolean putString(String key, String value) {
        return kv().encode(key, value);
    }

    public static String getString(String key) {
        return kv().decodeString(key);
    }

    public static String getString(String key, String defValue) {
        return kv().decodeString(key, defValue);
    }

    public static boolean putBytes(String key, byte[] value) {
        return kv().encode(key, value);
    }

    public static byte[] getBytes(String key) {
        return kv().decodeBytes(key);
    }

    public static byte[] getBytes(String key, byte[] defValue) {
        return kv().decodeBytes(key, defValue);
    }

    public static boolean putStringSet(String key, Set<String> value) {
        return kv().encode(key, value);
    }

    public static Set<String> getStringSet(String key) {
        return kv().decodeStringSet(key);
    }

    public static Set<String> getStringSet(String key, Set<String> defValue) {
        return kv().decodeStringSet(key, defValue);
    }

    public static Set<String> getStringSet(String key, Set<String> defValue, Class<? extends Set> cls) {
        return kv().decodeStringSet(key, defValue, cls);
    }

    public static boolean putParcelable(String key, Parcelable value) {
        return kv().encode(key, value);
    }

    public static <T extends Parcelable> T getParcelable(String key, Class<T> tClass) {
        return kv().decodeParcelable(key, tClass);
    }

    public static <T extends Parcelable> T getParcelable(String key, Class<T> tClass, T defValue) {
        return kv().decodeParcelable(key, tClass, defValue);
    }

    // ======================== 四、指定实例读写（多实例场景） ========================

    public static boolean putBoolean(MMKV target, String key, boolean value) {
        return target.encode(key, value);
    }

    public static boolean getBoolean(MMKV target, String key, boolean defValue) {
        return target.decodeBool(key, defValue);
    }

    public static boolean putInt(MMKV target, String key, int value) {
        return target.encode(key, value);
    }

    public static int getInt(MMKV target, String key, int defValue) {
        return target.decodeInt(key, defValue);
    }

    public static boolean putLong(MMKV target, String key, long value) {
        return target.encode(key, value);
    }

    public static long getLong(MMKV target, String key, long defValue) {
        return target.decodeLong(key, defValue);
    }

    public static boolean putString(MMKV target, String key, String value) {
        return target.encode(key, value);
    }

    public static String getString(MMKV target, String key, String defValue) {
        return target.decodeString(key, defValue);
    }

    public static boolean putBytes(MMKV target, String key, byte[] value) {
        return target.encode(key, value);
    }

    public static byte[] getBytes(MMKV target, String key, byte[] defValue) {
        return target.decodeBytes(key, defValue);
    }

    // ======================== 五、Key 管理 ========================

    public static boolean containsKey(String key) {
        return kv().containsKey(key);
    }

    public static boolean containsKey(MMKV target, String key) {
        return target.containsKey(key);
    }

    /** 获取默认实例的全部 key，可能为 null */
    public static String[] allKeys() {
        return kv().allKeys();
    }

    public static String[] allKeys(MMKV target) {
        return target.allKeys();
    }

    /** 默认实例中已存储的 key 数量 */
    public static long count() {
        return kv().count();
    }

    /** 某个 key 对应 value 的实际大小消耗（字节，含内部开销） */
    public static int getValueSize(String key) {
        return kv().getValueSize(key);
    }

    /** 某个 key 对应 value 的原始长度（字符串长度/字节数组长度等） */
    public static int getValueActualSize(String key) {
        return kv().getValueActualSize(key);
    }

    /** 移除单个 key */
    public static void remove(String key) {
        kv().removeValueForKey(key);
    }

    public static void remove(MMKV target, String key) {
        target.removeValueForKey(key);
    }

    /** 批量移除 key */
    public static void removeAll(String... keys) {
        if (keys != null && keys.length > 0) {
            kv().removeValuesForKeys(keys);
        }
    }

    public static void removeAll(MMKV target, String... keys) {
        if (keys != null && keys.length > 0) {
            target.removeValuesForKeys(keys);
        }
    }

    // ======================== 六、清理、容量与生命周期 ========================

    /** 清空默认实例全部数据 */
    public static void clearAll() {
        kv().clearAll();
    }

    public static void clearAll(MMKV target) {
        target.clearAll();
    }

    /** 清理多余空间，释放存储文件占用（大量删除后建议调用） */
    public static void trim() {
        kv().trim();
    }

    public static void trim(MMKV target) {
        target.trim();
    }

    /** 底层文件大小（对齐磁盘块，一般 4K 的整数倍） */
    public static long totalSize() {
        return kv().totalSize();
    }

    public static long totalSize(MMKV target) {
        return target.totalSize();
    }

    /** 实际已使用的数据大小 */
    public static long actualSize() {
        return kv().actualSize();
    }

    public static long actualSize(MMKV target) {
        return target.actualSize();
    }

    /** 同步落盘（一般无需手动调用，MMKV 自动持久化） */
    public static void sync() {
        kv().sync();
    }

    public static void async() {
        kv().async();
    }

    /** 清空内存缓存（收到内存告警时可调用，下次访问会重新从文件加载） */
    public static void clearMemoryCache(MMKV target) {
        target.clearMemoryCache();
    }

    /** 实例不再使用时调用，释放 mmap；调用后不要再操作同 ID 的实例 */
    public static void close(MMKV target) {
        target.close();
    }

    /** 校验某个 mmapID 对应的文件是否有效（默认根目录下） */
    public static boolean isFileValid(String mmapID) {
        return MMKV.isFileValid(mmapID);
    }

    /** 校验自定义目录下某个 mmapID 对应的文件是否有效 */
    public static boolean isFileValid(String mmapID, String rootPath) {
        return MMKV.isFileValid(mmapID, rootPath);
    }

    // ======================== 七、加密相关 ========================

    /** 获取当前实例的加密 key（未加密返回 null） */
    public static String cryptKey(MMKV target) {
        return target.cryptKey();
    }

    /**
     * 变更加密 key：
     * - newKey 为 null：加密实例转明文
     * - newKey 非空：明文转加密，或更换现有加密 key
     */
    public static boolean reKey(MMKV target, String newKey) {
        return target.reKey(newKey);
    }

    /**
     * 多进程场景下，当其他进程已执行过 reKey 后，本进程调用此方法同步新 key
     * （只重置 key 本身，不做任何加解密操作）
     */
    public static void checkReSetCryptKey(MMKV target, String cryptKey) {
        target.checkReSetCryptKey(cryptKey);
    }

    // ======================== 八、SharedPreferences 迁移 / 兼容 ========================

    /**
     * 一键将系统 SharedPreferences 数据迁移至默认 MMKV 实例，返回迁移的 key-value 数量
     */
    public static int importFromSharedPreferences(SharedPreferences sp) {
        return kv().importFromSharedPreferences(sp);
    }

    public static int importFromSharedPreferences(MMKV target, SharedPreferences sp) {
        return target.importFromSharedPreferences(sp);
    }

    /**
     * MMKV 本身实现了 SharedPreferences 接口，老项目替换时可直接把默认实例当 SharedPreferences 用
     */
    public static SharedPreferences asSharedPreferences() {
        return kv();
    }

    public static SharedPreferences.Editor edit() {
        return kv().edit();
    }

    // ======================== 九、备份与恢复 ========================

    /**
     * 备份指定 mmapID 的实例到 dstDir
     * @param rootPath 该实例原本所在的自定义目录，默认目录传 null
     */
    public static boolean backupOne(String mmapID, String dstDir, String rootPath) {
        return MMKV.backupOneToDirectory(mmapID, dstDir, rootPath);
    }

    /**
     * 从 srcDir 恢复指定 mmapID 的实例（会覆盖当前数据）
     * @param rootPath 恢复到的自定义目录，默认目录传 null
     */
    public static boolean restoreOne(String mmapID, String srcDir, String rootPath) {
        return MMKV.restoreOneMMKVFromDirectory(mmapID, srcDir, rootPath);
    }

    /** 备份根目录下的全部实例，返回成功备份的数量 */
    public static long backupAll(String dstDir) {
        return MMKV.backupAllToDirectory(dstDir);
    }

    /** 从 srcDir 恢复全部实例，返回成功恢复的数量 */
    public static long restoreAll(String srcDir) {
        return MMKV.restoreAllFromDirectory(srcDir);
    }

    // ======================== 十、多进程互斥锁 ========================

    /** 独占进程间锁，阻塞直到获取成功（单进程模式下无效果） */
    public static void lock(MMKV target) {
        target.lock();
    }

    public static void unlock(MMKV target) {
        target.unlock();
    }

    /** 非阻塞尝试加锁，成功返回 true */
    public static boolean tryLock(MMKV target) {
        return target.tryLock();
    }

    // ======================== 十一、日志与内容变更回调 ========================

    /** 注册全局日志重定向 / 错误处理回调 */
    public static void registerHandler(MMKVHandler handler) {
        MMKV.registerHandler(handler);
    }

    public static void unregisterHandler() {
        MMKV.unregisterHandler();
    }

    /**
     * 手动检查是否被其他进程修改过内容（多进程模式配合 registerContentChangeNotify 使用）
     */
    public static void checkContentChangedByOuterProcess(MMKV target) {
        target.checkContentChangedByOuterProcess();
    }

    // ======================== 十二、进程模式校验开关（调试用） ========================

    /** 手动开启进程模式校验（Debug 包默认开启，Release 默认关闭） */
    public static void enableProcessModeChecker() {
        MMKV.enableProcessModeChecker();
    }

    public static void disableProcessModeChecker() {
        MMKV.disableProcessModeChecker();
    }
}