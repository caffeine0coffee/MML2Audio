package MML2Audio.Channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import MML2Audio.Note.Note;
import MML2Audio.Util.Log;

/**
 * MMLのコンテキストを保持し、{@link Channel}を構築する。
 * 
 * <p>
 * オクターブや音量など、MML文の位置によって変化するコンテキストを保持する。
 * MML命令に対応したコンテキストの設定関数とノート追加関数を提供する。
 * 正しい順序で正しく設定された{@link Note}オブジェクトを{@link Channel}に追加し、提供することが目的。
 */
public class ChannelBuilder {
    /**
     * 構築対象の{@link Channel}オブジェクト
     */
    private Channel channel;
    /**
     * チャンネル構築中に変化する音量値を保持する
     */
    private int currentVolume;
    /**
     * チャンネル構築中に変化するオクターブ値を保持する
     */
    private int currentOctave;
    /**
     * チャンネル構築中に変化するデフォルト音長値を保持する
     */
    private int currentDefaultToneLength;
    /**
     * チャンネル構築中に変化するwaveGeneratorIdを保持する
     */
    private String currentWaveGeneratorId;

    /**
     * 構築対象の{@link Channel}オブジェクトを設定し、コンテキストを初期化する。
     * 
     * @param channel 構築対象のチャンネル
     */
    public ChannelBuilder(Channel channel) {
        this.channel = channel;
        this.currentOctave = 4;
        this.currentVolume = 200;
        this.currentDefaultToneLength = 4;
        this.currentWaveGeneratorId = "sin";
    }

    /** 
     * 構築対象の{@link Channel}オブジェクトを設定する。
     * 
     * @param channel 構築対象のチャンネル
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /** 
     * チャンネル構築中の音量値を設定する。
     * 
     * @param currentVolume 音量値
     */
    public void setCurrentVolume(int currentVolume) {
        this.currentVolume = currentVolume;
        Log.log("set Volume to: " + this.currentVolume);
    }

    /** 
     * チャンネル構築中の音量値に加算する。
     * 
     * @param amount 加算する音量値
     */
    public void addToCurrentVolume(int amount) {
        this.currentVolume += amount;
        Log.log("Volume added " + amount + ", current Volume: " + this.currentVolume);
    }

    /** 
     * 構築中のオクターブ値を設定する。
     * 
     * @param currentOctave オクターブ値
     */
    public void setCurrentOctave(int currentOctave) {
        this.currentOctave = currentOctave;
        Log.log("set Octave to: " + this.currentOctave);
    }

    /**
     * 構築中のオクターブに加算する。
     * 
     * @param amount 加算するオクターブ値
     */
    public void addToCurrentOctave(int amount) {
        this.currentOctave += amount;
        Log.log("Octave added " + amount + ", current Octave: " + this.currentOctave);
    }

    /** 
     * 構築中のデフォルト音長値を設定する。
     * 
     * @param currentDefaultToneLength デフォルト音長値
     */
    public void setCurrentDefaultToneLength(int currentDefaultToneLength) {
        this.currentDefaultToneLength = currentDefaultToneLength;
        Log.log("set Default Tone Length to: " + this.currentDefaultToneLength);
    }

    
    /** 
     * 構築中のwaveGeneratorIdを設定する
     * 
     * @param id waveGeneratorId
     */
    public void setCurrentWaveGeneratorId(String id) {
        this.currentWaveGeneratorId = id;
    }

    
    /** 
     * 構築した{@link Channel}オブジェクトを取得する。
     * 
     * @return Channel 構築した{@link Channel}オブジェクト
     */
    public Channel getChannel() {
        return this.channel;
    }
    
    
    /** 
     * 構築中のコンテキストに従って{@link Note}オブジェクトをチャンネルに追加する。
     * 
     * @param noteExpr ノート表現の文字列
     */
    public void addNote(String noteExpr) {
        Pattern noteLengthPattern = Pattern.compile("(\\d+)(.*)");
        Matcher noteLengthMatcher = noteLengthPattern.matcher(noteExpr);
        Note note = null;

        if (noteLengthMatcher.find()) {
            note = new Note(noteLengthMatcher.group(2), Integer.parseInt(noteLengthMatcher.group(1)), this.currentOctave, this.currentVolume, this.currentWaveGeneratorId);
        }
        else {
            note = new Note(noteExpr, this.currentDefaultToneLength, this.currentOctave, this.currentVolume, this.currentWaveGeneratorId);
        }
        this.channel.addNote(note);

        Log.log("Note added: " + note);
    }
}
