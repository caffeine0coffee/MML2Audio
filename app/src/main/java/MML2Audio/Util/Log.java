package MML2Audio.Util;

/**
 * オプションに応じてログを出力する。
 * 
 * <p>
 * このクラスが提供する関数を用いてログや情報を出力できる。
 * 出力の有無をこのクラスで一括して制御する。
 */
public class Log {
    /**
     * ログを表示するかを決定する。
     */
    private static boolean logOutputFlag = false;
    /**
     * 情報を表示するかを決定する。
     */
    private static boolean infoOutputFlag = false;

    /** 
     * ログを出力する。
     * 
     * <p>
     * {@link #logOutputFlag}と{@link #infoOutputFlag}の値に応じて出力する。
     * {@link #infoOutputFlag}がfalseの場合もログの出力を行わない。
     * 
     * @param msg 表示文字列
     */
    public static void log(String msg) {
        if (Log.logOutputFlag && Log.infoOutputFlag) {
            System.err.println(msg);
        }
    }

    /** 
     * 情報を出力する。
     * 
     * <p>
     * {@link #infoOutputFlag}の値に応じて出力する。
     * 
     * @param msg 表示文字列
     */
    public static void info(String msg) {
        if (Log.infoOutputFlag) {
            System.err.println(msg);
        }
    }

    /** 
     * {@link #infoOutputFlag}を設定する。
     * 
     * @param flag {@link #infoOutputFlag}
     */
    public static void setInfoFlag(boolean flag) {
        Log.infoOutputFlag = flag;
    }

    /**
     * {@link #logOutputFlag}を設定する。
     *  
     * @param flag {@link #logOutputFlag}
     */
    public static void setLogFlag(boolean flag) {
        Log.logOutputFlag = flag;
    }
}
