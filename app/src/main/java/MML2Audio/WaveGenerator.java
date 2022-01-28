package MML2Audio;

import java.util.HashMap;
import java.util.Random;
import java.util.function.BiFunction;

import MML2Audio.Exception.InvalidGeneratorId;

/**
 * 音声波生成関数を提供する。
 * 
 * <p>
 * 第1引数に周波数[Hz]、第2引数に時間[s]を受け取り、その時間における波形の値を返却する音声波生成関数を提供する。
 * また、MML文に記述するwaveGeneratorIdと音声波生成関数を対応付けた{@link #waveGeneratorMap}を提供する。
 */
public class WaveGenerator {
    /**
     * waveGeneratorIdと音声波生成関数の対応表。
     * 
     * <p>
     * keyとしてwaveGeneratorId、valueとして対応する音声波生成関数の関数オブジェクトが格納される。
     */
    private static HashMap<String, BiFunction<Double, Double, Double>> waveGeneratorMap;

    /** 
     * サイン波を生成する。
     * 
     * @param freq 周波数[Hz]
     * @param t 時間[s]
     * @return double 値[-1.0 : 1.0]
     */
    public static double sin(double freq, double t) {
        return Math.sin(freq * t * Math.PI * 2);
    }

    /**
     * 矩形波を生成する。 
     * 
     * @param freq 周波数[Hz]
     * @param t 時間[s]
     * @return double 値[-1.0 : 1.0]
     */
    public static double square(double freq, double t) {
        double T = 1.0 / freq;
        int sw = (int)(Math.floor(t/(0.5*T)) + 1) % 2;
        return sw==1 ? 1 : -1;
    }

    /** 
     * のこぎり波を生成する。
     * 
     * @param freq 周波数[Hz]
     * @param t 時間[s]
     * @return double 値[-1.0 : 1.0]
     */
    public static double sawtooth(double freq, double t) {
        double T = 1.0 / freq;
        return ((t % T) / T)*2 - 1;
    }

    /** 
     * ノイズを生成する。
     * 
     * <p>
     * 周波数と時間に関わらずランダムな値を返却する。
     * 
     * @param freq 周波数[Hz]
     * @param t 時間[s]
     * @return double 値[-1.0 : 1.0]
     */
    public static double noise(double freq, double t) {
        Random rand = new Random();
        return rand.nextDouble() * 2 - 1;
    }

    /** 
     * 音声波生成関数を取得する。
     * 
     * @param generatorId
     * @return BiFunction<Double, Double, Double>
     * @throws InvalidGeneratorId 未定義のwaveGeneratorIdが渡された場合に発生する
     */
    public static BiFunction<Double, Double, Double>  getWaveGenerator(String generatorId) throws InvalidGeneratorId {
        HashMap<String, BiFunction<Double, Double, Double>> wgm;
        wgm = WaveGenerator.getWaveGeneratorMapInstance();

        if (wgm.containsKey(generatorId)) {
            return wgm.get(generatorId);
        }

        throw new InvalidGeneratorId();
    }

    /** 
     * {@link #waveGeneratorMap}のインスタンスを取得する。
     * 
     * <p>
     * プログラム中で{@link #waveGeneratorMap}のインスタンスを参照する場合は、必ずこのメソッドを経由する。
     * プログラム起動後初めてこのメソッドを呼び出した際に{@link #waveGeneratorMap}をインスタンス化する。
     * それ以降は{@link #waveGeneratorMap}をそのまま返却する。
     * 
     * @return HashMap<String, BiFunction<Double, Double, Double>> @{link #waveGeneratorMap}インスタンス
     */
    private static HashMap<String, BiFunction<Double, Double, Double>> getWaveGeneratorMapInstance() {
        if (WaveGenerator.waveGeneratorMap != null) {
            return WaveGenerator.waveGeneratorMap;
        }

        WaveGenerator.waveGeneratorMap = new HashMap<String, BiFunction<Double, Double, Double>>();
        WaveGenerator.waveGeneratorMap.put("sin", WaveGenerator::sin);
        WaveGenerator.waveGeneratorMap.put("square", WaveGenerator::square);
        WaveGenerator.waveGeneratorMap.put("sawtooth", WaveGenerator::sawtooth);
        WaveGenerator.waveGeneratorMap.put("noise", WaveGenerator::noise);

        return WaveGenerator.waveGeneratorMap;
    }
}
