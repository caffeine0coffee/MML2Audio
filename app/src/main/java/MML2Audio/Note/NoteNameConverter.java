package MML2Audio.Note;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import MML2Audio.Exception.InvalidNoteNameException;
import MML2Audio.Exception.InvalidScaleIndexException;

// ScaleIndex:
//      C:0, C#:1, D:2, D#:3, ... , B: 11

/**
 * scaleIndexとノート名の相互変換を行う。
 */
public class NoteNameConverter {
    /**
     * ノート名とscaleIndexの対応表。
     * 
     * <p>
     * 半音が付加されていない音階のみ対応している。
     * ノート名とは、MML文中に記述されるノート表現から音長を除いた部分である。
     * 例えば、8Eというノート表現に含まれるノート名はEとなる。
     */
    private static HashMap<String, Integer> scaleMap = null;

    /**
     * {@link #scaleMap}のインスタンスを取得する。 
     * 
     * <p>
     * プログラム中で{@link #scaleMap}のインスタンスを参照する場合は、必ずこのメソッドを経由する。
     * プログラム起動後初めてこのメソッドを呼び出した際に{@link #scaleMap}をインスタンス化する。
     * それ以降は{@link #scaleMap}をそのまま返却する。
     * 
     * @return HashMap<String, Integer> {@link #scaleMap}インスタンス
     */
    private static HashMap<String, Integer> getScaleMapInstance() {
        if (NoteNameConverter.scaleMap != null) {
            return NoteNameConverter.scaleMap;
        }
        
        NoteNameConverter.scaleMap = new HashMap<>();
        NoteNameConverter.scaleMap.put("C", 0);
        NoteNameConverter.scaleMap.put("D", 2);
        NoteNameConverter.scaleMap.put("E", 4);
        NoteNameConverter.scaleMap.put("F", 5);
        NoteNameConverter.scaleMap.put("G", 7);
        NoteNameConverter.scaleMap.put("A", 9);
        NoteNameConverter.scaleMap.put("B", 11);
        return NoteNameConverter.scaleMap;
    }

    
    /** 
     * ノート名をscaleIndexに変換する。
     * 
     * @param noteName ノート名
     * @return int scaleIndex
     * @throws InvalidNoteNameException ノート名が誤っている場合に発生する
     */
    public static int noteNameToScaleIndex(String noteName) throws InvalidNoteNameException {
        if (! Pattern.matches("^[A-Ga-gR][#+-]?$", noteName)) {
            throw new InvalidNoteNameException();
        }
        if (Pattern.matches("R", noteName)) {
            return 0;
        }

        String scaleName = String.valueOf(noteName.charAt(0)).toUpperCase();
        String semitoneName = "";
        Pattern pattern = Pattern.compile("^.([#+-])$");
        Matcher matcher = pattern.matcher(noteName);
        if (matcher.find()) {
            semitoneName = matcher.group(1);
        }

        int scaleIndex = NoteNameConverter.getScaleMapInstance().get(scaleName);
        if (semitoneName.equals("-")) {
            scaleIndex -= 1;
        }
        else if (semitoneName.equals("+") || semitoneName.equals("#")) {
            scaleIndex += 1;
        }

        return scaleIndex;
    }

    
    /** 
     * scaleIndexをノート名に変換する。
     * 
     * @param scaleIndex scaleIndex
     * @return String ノート名
     * @throws InvalidScaleIndexException 範囲外のscaleIndexを処理しようとした場合に発生する
     */
    public static String scaleIndexToNoteName(int scaleIndex) throws InvalidScaleIndexException {
        boolean addSemitone = false;

        if (scaleIndex > 11) {
            throw new InvalidScaleIndexException("scale index is above 11");
        }

        if (! NoteNameConverter.getScaleMapInstance().containsValue(scaleIndex)) {
            scaleIndex -= 1;
            scaleIndex %= 12;
            addSemitone = true;
        }
        for (HashMap.Entry<String, Integer> ent: NoteNameConverter.getScaleMapInstance().entrySet()) {
            if (ent.getValue() == scaleIndex) {
                return ent.getKey() + (addSemitone ? "#" : "");
            }
        }

        throw new InvalidScaleIndexException();
    }
}