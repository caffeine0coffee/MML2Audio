package MML2Audio.Exception;

/**
 * 正しくないノート表現を処理しようとした場合に発生させる。
 */
public class InvalidNoteNameException extends Exception {
    public InvalidNoteNameException(String msg) {
        super(msg);
    }

    public InvalidNoteNameException() {
        super();
    }
}
