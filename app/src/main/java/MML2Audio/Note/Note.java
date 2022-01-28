package MML2Audio.Note;

import MML2Audio.Exception.InvalidNoteNameException;
import MML2Audio.Exception.InvalidScaleIndexException;

/**
 * 曲中の1音を表す。
 * 
 * <p>
 * 音の周波数や長さなどの情報を保持する。
 */
public class Note {
    /**
     * 音量の最大値。
     */
    public static final int MAX_VOLUME = 255;

    /**
     * 音の周波数。
     */
    private double freq;
    /**
     * 音の音階番号。
     * 
     * <p>
     * 0がC(ド)、1がC#(ド#)、...、11がB(シ)に対応している。
     */
    private int scaleIndex;
    /**
     * 音のオクターブ数。
     * 
     * <p>
     * 初期値では4。
     * オクターブ4のA(ラ)の周波数が440Hz
     */
    private int octave;
    /**
     * 音の長さ。
     * 
     * <p>
     * 長さの値は音楽的な表記に準ずる。
     * 例えば、4分音符であれば4、16分音符であれば16となる。
     * 数値が小さいほど音の持続時間が長くなる。
     */
    private int toneLength;
    /**
     * 音の音量。
     * 
     * <p>
     * 0~{@Link #MAX_VOLUME}までの数値で指定する。
     */
    private int volume;  // 0 ~ MAX_VOLUME
    /**
     * 音の音色。
     * 
     * <p>
     * 音の音色をwaveGeneratorIdで指定する。
     * waveGeneratorIdによって音の波形が変化し、音色が変化する。
     */
    private String waveGeneratorId;
    /**
     * 音の表記。
     * 
     * <p>
     * MML中のノート表現に準拠した文字列が格納される。
     */
    private String noteName;

    public Note(int toneLength, int octave, int volume, String waveGeneratorId) {
        this.octave = octave;
        this.toneLength = toneLength;
        this.volume = volume;
        this.waveGeneratorId = waveGeneratorId;
    }

    /**
     * scaleIndexを指定して{@link Note}を生成する。
     * 
     * @param scaleIndex scaleIndex
     * @param toneLength 音長
     * @param octave オクターブ
     * @param volume 音量
     * @param waveGeneratorId waveGeneratorId
     */
    public Note(int scaleIndex, int toneLength, int octave, int volume, String waveGeneratorId) {
        this(toneLength, octave, volume, waveGeneratorId);

        try {
            this.noteName = NoteNameConverter.scaleIndexToNoteName(scaleIndex);
        } catch (InvalidScaleIndexException e) {
            e.printStackTrace();
            System.exit(1);
        }

        this.scaleIndex = scaleIndex;
        calcFreq();
    }

    /**
     * ノート表現の文字列から{@link Note}を生成する。
     * 
     * @param noteName ノート表現
     * @param toneLength 音長
     * @param octave オクターブ
     * @param volume 音量
     * @param waveGeneratorId waveGeneratorId
     */
    public Note(String noteName, int toneLength, int octave, int volume, String waveGeneratorId) {
        this(toneLength, octave, volume, waveGeneratorId);

        try {
            this.scaleIndex = NoteNameConverter.noteNameToScaleIndex(noteName);
            if (this.scaleIndex < 0) {
                this.octave -= 1;
                this.scaleIndex = 12 + this.scaleIndex;
            }
            else if (this.scaleIndex > 11) {
                this.octave += 1;
                this.scaleIndex %= 12;
            }
        } catch (InvalidNoteNameException e) {
            e.printStackTrace();
            System.exit(1);
        }

        this.noteName = noteName;
        calcFreq();

        if (noteName.equals("R")) {
            this.volume = 0;
        }
    }

    /**
     * scaleIndexとオクターブから音の周波数を計算する。
     */
    private void calcFreq() {
        int delta = this.octave - 4;
        int cIndex = this.scaleIndex - 9;
        this.freq = 440 * Math.pow(2, 1/12.0 * (cIndex + delta*12));
    }

    /** 
     * 音の周波数を取得する。
     * 
     * @return double 周波数
     */
    public double getFreq() {
        return this.freq;
    }

    /** 
     * ノート表現の文字列を取得する。
     * 
     * @return String ノート表現
     */
    public String getNoteName() {
        return this.noteName;
    }

    /** 
     * 音の長さを取得する。
     * 
     * @return int 音長
     */
    public int getToneLength() {
        return toneLength;
    }

    /**
     * 音の音量を取得する。
     *  
     * @return int 音量
     */
    public int getVolume() {
        return volume;
    }

    /** 
     * 音の音色を取得する。
     * 
     * @return String waveGeneratorId
     */
    public String getWaveGeneratorId() {
        return this.waveGeneratorId;
    }

    @Override
    public String toString() {
        return "[ "
            + "'" + this.noteName + "', "
            + "index=" + this.scaleIndex + ", "
            + "octave=" + this.octave + ", "
            + "toneLength=" + this.toneLength + ", "
            + "freq=" + this.freq + ", "
            + "wave=" + this.waveGeneratorId + " "
            + "]";
    }

    @Override
    public boolean equals(Object obj) {
        Note n = (Note)obj;
        return (
            this.scaleIndex == n.scaleIndex &&
            this.toneLength == n.toneLength &&
            this.octave == n.octave &&
            this.volume == n.volume &&
            this.waveGeneratorId.equals(n.waveGeneratorId)
        );
    }
}
