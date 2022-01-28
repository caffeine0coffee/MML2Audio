package MML2Audio.Exception;

/**
 * 未定義のwaveGeneratorIdを処理しようとした場合に発生させる。
 */
public class InvalidGeneratorId extends Exception {
    public InvalidGeneratorId() {
        super();
    }

    public InvalidGeneratorId(String msg) {
        super(msg);
    }
}
