package MML2Audio;

import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;

import MML2Audio.Channel.Channel;
import MML2Audio.Exception.InvalidGeneratorId;
import MML2Audio.Note.Note;

/**
 * 曲データ全体を管理する。
 * 
 * <p>
 * 曲を構成するデータを管理している。
 * BPMのような曲全体の共通したデータ、オーディオフォーマット等の出力音声に関する情報、チャンネルを一括して管理するリストを保持する。
 * また、曲データから音声バッファへの変換も行う。
 * 
 */
public class Music {
    /**
     * 出力音声のフォーマット。
     */
    private AudioFormat audioFormat;
    /**
     * 曲を構成する全てのチャンネルを保持する。
     */
    private ArrayList<Channel> channelList = new ArrayList<>();
    /**
     * 曲のテンポ。
     */
    private int bpm;
    /**
     * 音声バッファにおける1フレームの最大値。
     */
    private int maxVolumeValue;

    /**
     * 音長の最小値。
     * 
     * <p>
     * 音楽的な音長表記(4分音符や16分音符など)に準ずる。
     */
    public static final int MIN_TONE_LENGTH = 64;
    /**
     * 出力音声のサンプリングレート。
     */
    public static final double SAMPLE_RATE = 44100.0;

    /**
     * 各変数の初期化を行う。
     */
    public Music() {
        this.audioFormat = new AudioFormat(
        (int)Music.SAMPLE_RATE,
            8,      // サンプルサイズ [bit]
            1,      // オーディオフォーマットにおけるチャンネル数 (mono / stereo)
            true,   // is signed
            true    // is Big Endian
        );
        this.maxVolumeValue = (int) Math.pow(2, this.audioFormat.getSampleSizeInBits()) - 1;
        this.bpm = 100;
    }

    /**
     * {@link Channel}のリストを直接指定して初期化する。
     * 
     * @param channelList {@link Channel}のリスト
     */
    public Music(ArrayList<Channel> channelList) {
        this();
        this.channelList = channelList;
    }

    /** 
     * 曲の長さを秒数で計算する。
     * 
     * @return double 値[-1.0 : 1.0] 曲の長さ [s]
     */
    private double calcLengthOfMusicInSecond() {
        double maxLength = -1;
        for (Channel c: this.channelList) {
            double length = 0;
            for (Note n: c.getNoteList()) {
                length += (4.0 / n.getToneLength()) * (60.0 / this.bpm);
            }
            maxLength = Math.max(maxLength, length);
        }

        return maxLength;
    }

    /** 
     * {@link #channelList}を取得する。
     * 
     * @return ArrayList<Channel>
     */
    public ArrayList<Channel> getChannelList() {
        return this.channelList;
    }

    /** 
     * 曲のテンポをBPM値で指定する。
     * 
     * @param bpm BPM値
     */
    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    
    /** 
     * 曲にチャンネルを追加する。
     * 
     * @param channel {@link Channel}
     */
    public void addChannel(Channel channel) {
        this.channelList.add(channel);
    }

    
    /** 
     * 曲に含まれるチャンネル数を取得する。
     * 
     * @return int チャンネル数
     */
    public int getNumberOfChannel() {
        return this.channelList.size();
    }

    
    /** 
     * 曲データを音声バッファに変換する。
     * 
     * @return byte[] 音声バッファ
     */
    public byte[] generateAudioBuffer() {
        int numOfByte = (int)Music.SAMPLE_RATE * (int)this.calcLengthOfMusicInSecond();
        numOfByte += Music.SAMPLE_RATE * 2.0;

        byte[] audioBuffer = new byte[numOfByte];
        Arrays.fill(audioBuffer, (byte)0);
        int attackTime = (int) (Music.SAMPLE_RATE * 0.01);
        int decreaseTime = (int) (Music.SAMPLE_RATE * 0.1);

        for (int c=0; c<this.getNumberOfChannel(); c++) {
            int phase = 0; // 波の位相
            int count = 0;
            int noteIndex = -1;
            double preampValue = 0;
            double amp = 0;
            Note note = null;
            for (int i=0; i<audioBuffer.length; i++) {
                if (count <= 0) {
                    noteIndex++;
                    if (noteIndex < channelList.get(c).getNoteList().size()) {
                        note = channelList.get(c).getNoteList().get(noteIndex);
                        count = (int) ((4.0 / note.getToneLength()) * (60.0 / bpm) * Music.SAMPLE_RATE);
                        phase = 0;
                    }
                    else {
                        break;
                    }
                }

                amp = this.maxVolumeValue * 0.5 * (note.getVolume() / (double)Note.MAX_VOLUME);
                amp /= (double) this.getNumberOfChannel();
                // 音のアタックを付ける
                if (phase < attackTime) {
                    amp *= (phase / (double)attackTime);
                }
                // 音の終端で減衰させる
                if (count < decreaseTime) {
                    amp *= (count / (double)decreaseTime);
                }

                // value = (byte) (WaveGenerator.sin(note.getFreq(), phase/Music.SAMPLE_RATE) * amp);
                try {
                    preampValue = WaveGenerator.getWaveGenerator(note.getWaveGeneratorId()).apply(
                        note.getFreq(), phase/Music.SAMPLE_RATE);
                }
                catch (InvalidGeneratorId e) {
                    System.err.println("Invalid waveGeneratorId");
                    e.printStackTrace();
                }

                // value = (byte) (amp * Math.sin((phase/waveLength)*Math.PI*2));
                audioBuffer[i] += (byte) (preampValue * amp);

                count--;
                phase++;
            }
        }

        return audioBuffer;
    }

    
    /** 
     * 出力音声のフォーマットを取得する。
     * 
     * @return AudioFormat フォーマット
     */
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }
}
