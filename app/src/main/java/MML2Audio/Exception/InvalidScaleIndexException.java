package MML2Audio.Exception;

/**
 * 範囲外のscaleIndexを処理しようとした場合に発生させる。
 */
public class InvalidScaleIndexException extends Exception {
    public InvalidScaleIndexException(String msg) {
        super(msg);
    }

    public InvalidScaleIndexException() {
        super();
    }
}
