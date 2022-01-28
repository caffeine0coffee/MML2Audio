package MML2Audio;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import MML2Audio.Channel.Channel;
import MML2Audio.Channel.ChannelBuilder;
import MML2Audio.Util.Log;

/**
 * MMLの文法を解釈し、アプリケーション内のデータを構築する。
 *
 * <p>
 * このクラスでは、.mmlファイルの中身を読み取り、MML文として解釈を行う。
 * 演奏データは{@link Task}オブジェクトの集合として構築される。<br>
 * MML2AudioにおけるMMLの文法は以下の通り ({@literal <number>}は整数値、{@literal <string>}は文字列を表す)
 * 
 * <ul>
 *  <li> {@literal :<string>}　指定したチャンネル名でチャンネルの開始を宣言する
 *  <ul>
 *      <li> 次のチャンネル宣言、若しくはファイル終端までがチャンネル定義範囲となる
 *  </ul>
 *  <li> T{@literal <number>}　曲のテンポをBPM値で指定する
 *  <ul>
 *      <li> 一番最後に記述されたT命令が全体に反映される
 *      <li> デフォルトは100
 *  </ul>
 *  <li> {@literal <length><scale><semitone>}
 *  <ul>
 *      <li> ノート表現
 *      <li> length: 1 ~ 64の整数　音楽的な表記に準ずる　省略するとデフォルト音長(初期値4)になる
 *      <li> scale: [A-Ga-gR]　音階の英語表記に従うがRは休符を表す
 *      <li> semitone: [#+-]　#と+は半音上、-は半音下を表す　省略可能
 *      <li> 例: 8G# -> 8分音符のソ#
 *  </ul>
 *  <li> {@literal O<number>}　これ以降のオクターブを指定する
 *  <li> {@literal <, >}　それぞれオクターブを1つ上げる, 下げる
 *  <li> L{@literal <number>}　これ以降のデフォルト音長を指定する
 *  <li> {@literal @(<string>)}　それ以降の音色をwaveGeneratorIdで指定する
 *  <ul>
 *      <li> waveGeneratorIdの一覧
 *      <li> sin      : sin波
 *      <li> square   : 矩形波
 *      <li> sawtooth : のこぎり波
 *      <li> noise    : ノイズ (ノートの音階に影響を受けない)
 *  </ul>
 *  <li> {@literal V<number>}　それ以降の音量を指定　0~255の整数
 *  <li> {@literal (<number>}　指定した分だけ音量を上げる
 *  <li> {@literal )<number>}　指定した分だけ音量を下げる
 *  <li> |　可視性の為に使う　生成される曲には影響を及ぼさない
 *  <li> {@literal /* * /}　コメント文　行を跨ぐことも可能
 *  <ul>
 *      <li> javadocには記述できないが、コメント終端の * と / の間にスペースは入れない
 *  </ul>
 *  
 */
public class MmlReader {
    /**
    * MML文一行に対して実行する命令とインデックスを保持する。
    * 
    * <p>
    * MML命令を正しい順序で実行する為に使用する。
    * このオブジェクトの優先順位はMMLの出現位置によって決定される。
    * 
    * <p>
    * 実行する命令は{@link Runnable}として保持する。
    * 引数を要する関数は引数なしのラムダ式でラップする。
    * その際、引数に使用する変数は予めDeep Copyする必要がある。
    */
    private static class Task implements Comparable<Task> {
        /**
         * MML文中のMML命令の位置
         */
        private Integer index;
        /**
         * MML命令に対して実行する関数
         */
        private Runnable task;

        protected Task(int index, Runnable task) {
            this.index = index;
            this.task = task;
        }

        @Override
        public int compareTo(Task t) {
            return this.index.compareTo(t.index);
        }

        /**
         * 登録した命令を実行する
         */
        protected void run() {
            this.task.run();
        }
    }

    /**
     * MML命令のパターンとそれに対する命令を定義する。
     * 
     * <p>
     * keyとして{@link Pattern}を保持する。
     * keyにmatchした場合の処理を{@link BiConsumer}として定義する。
     * BiConsumerの第1引数は関数オブジェクト内で加工され、第2引数である{@link ChannelBuilder}のインスタンスメソッドに渡される。
     */
    private static LinkedHashMap<Pattern, BiConsumer<String, ChannelBuilder>> operationMap;

    /** 
     * MML文を読み込み、コメント文の除去と{@link Channel}オブジェクトの構築を行う。
     * 
     * <p>
     * 曲全体に影響するBPM設定とchannel宣言の読み込みを行う。
     * それ以外のMML命令の実行は{@link convertMmlToChannel}にて行う。
     * mmlファイル中のコメント文は全てこの関数内で除去する。
     * 
     * @param filePath 入力MMLファイルのパス
     * @param music 出力先の{@link Music}オブジェクト
     */
    public static void mmlCompiler(String filePath, Music music) {
        try {
            BufferedReader bufReader = new BufferedReader(new FileReader(filePath));
            String line;
            Pattern bpmConfigPattern = Pattern.compile("T(\\d+)");
            Matcher bpmConfigMatcher = null;
            Pattern channelDefPattern = Pattern.compile(":\\w*");
            Matcher channelDefMatcher = null;
            while (true) {
                line = MmlReader.readLineIgnoreComment(bufReader);
                if (line == null) {
                    break;
                }

                // BPMの設定
                bpmConfigMatcher = bpmConfigPattern.matcher(line);
                if (bpmConfigMatcher.find()) {
                    int bpm = Integer.parseInt(bpmConfigMatcher.group(1));
                    music.setBpm(bpm);
                }

                // チャンネル宣言
                channelDefMatcher = channelDefPattern.matcher(line);
                if (channelDefMatcher.find()) {
                    while (true) {
                        if (line == null) {
                            break;
                        }
                        String channelMml = "";
                        while (true) {
                            line = MmlReader.readLineIgnoreComment(bufReader);
                            if (line == null) {
                                break;
                            }
                            channelDefMatcher = channelDefPattern.matcher(line);
                            if (channelDefMatcher.find()) {
                                break;
                            }

                            channelMml += line + "\n";
                        }
                        music.addChannel(MmlReader.convertMmlToChannel(channelMml));
                    }
                }
            }

            bufReader.close();
        }
        catch (FileNotFoundException e) {
            System.err.println("file not found");
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("IO error");
            e.printStackTrace();
            System.exit(1);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /** 
     * チャンネル内のMML文から{@link Channel}オブジェクトを生成します。
     * 
     * <p>
     * チャンネル定義範囲内のMML文を文字列として受け取る。
     * 受け取る文字列にはコメント文を含んではいけない。
     * 
     * @param channelMml MML文を格納した文字列
     * @return Channel 生成された{@link Channel}オブジェクト
     */
    public static Channel convertMmlToChannel(String channelMml) {
        ChannelBuilder builder = new ChannelBuilder(new Channel());

        try (BufferedReader bufReader = new BufferedReader(new StringReader(channelMml));) {
            boolean[] matchedMask = null;
            String line;

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }

                matchedMask = new boolean[line.length()];
                Arrays.fill(matchedMask, false);
                PriorityQueue<Task> taskQueue = new PriorityQueue<>();
                for (HashMap.Entry<Pattern, BiConsumer<String, ChannelBuilder>> ent: MmlReader.getOperationMapInstance().entrySet()) {
                    boolean finish = false;
                    Matcher matcher = ent.getKey().matcher(line);

                    while (!finish) {
                        finish = true;
                        if (matcher.find()) {
                            boolean matched = false;
                            for (int i=matcher.start(); i<matcher.end(); i++) {
                                if (matchedMask[i]) {
                                    matched = true;
                                }
                            }
                            if (matched) {
                                finish=false;
                                continue;
                            }

                            Arrays.fill(matchedMask, matcher.start(), matcher.end(), true);
                            String matcherGroup = matcher.groupCount()>0 ? matcher.group(1) : "";
                            String arg = new String(matcherGroup);
                            int start = matcher.start();
                            taskQueue.add(new Task(start,
                                ()->{ ent.getValue().accept(arg, builder); }));

                            Log.log("matched Pattern: " + ent.getKey());
                            Log.log("  argument: " + matcherGroup);
                            Log.log("  start index: " + start);

                            finish = false;
                            if (matcher.hitEnd()) {
                                break;
                            }
                        }
                    }
                }

                while(!taskQueue.isEmpty()) {
                    taskQueue.poll().run();
                }
            }
        }
        catch (IOException e) {
            System.err.println("IO error");
            e.printStackTrace();
            System.exit(1);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return builder.getChannel();
    }

    /** 
     * BufferReaderから1行読み込み、MMLのコメント表現を除去する。
     * 
     * <p>
     * 読み取った内容にコメント表現が無ければ読み取った内容をそのまま返却する。
     * もし単一行のコメントがあった場合、コメントを取り除いた内容を返却する。
     * もし複数行のコメントがあった場合、最初の行の非コメント部分と最後の行の非コメント文を結合した内容を返却する。
     * 
     * @param bufReader 読み込み元オブジェクト
     * @return String コメント文を除去したMML文
     * @throws IOException {@link BufferedReader#readLine}によって発生し得る
     */
    private static String readLineIgnoreComment(BufferedReader bufReader) throws IOException {
        String line;
        Pattern commentBeginPattern = Pattern.compile("/\\*");
        Pattern commentEndPattern   = Pattern.compile("\\*/");
        Matcher commentBeginMatcher, commentEndMatcher;

        line = bufReader.readLine();
        if (line == null) {
            return null;
        }

        // 単一行のコメントを除去する
        line = line.replaceAll("/\\*.*\\*/", "");

        // 複数行コメントを除去する
        commentBeginMatcher = commentBeginPattern.matcher(line);
        if (commentBeginMatcher.find()) {
            String notCommentLine = line.substring(0, commentBeginMatcher.start());

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }

                commentEndMatcher = commentEndPattern.matcher(line);
                if (commentEndMatcher.find()) {
                    line = line.substring(commentEndMatcher.end(), line.length());
                    line = notCommentLine + line;
                    break;
                }
            }
        }

        return line;
    }

    /** 
     * {@link #operationMap} のインスタンスを取得する。
     * 
     * <p>
     * プログラム中で{@link #operationMap}のインスタンスを参照する場合は、必ずこのメソッドを経由する。
     * プログラム起動後初めてこのメソッドを呼び出した際に{@link #operationMap}をインスタンス化する。
     * それ以降は{@link #operationMap}をそのまま返却する。
     * 
     * @return HashMap<Pattern, BiConsumer<String, ChannelBuilder>> {@link #operationMap}インスタンス
     */
    private static HashMap<Pattern, BiConsumer<String, ChannelBuilder>> getOperationMapInstance() {
        if (MmlReader.operationMap != null) {
            return MmlReader.operationMap;
        }

        MmlReader.operationMap = new LinkedHashMap<Pattern, BiConsumer<String, ChannelBuilder>>();
        // 音色
        MmlReader.operationMap.put(Pattern.compile("@\\((\\w+)\\)"),  // @(<string>)
            (String arg, ChannelBuilder cb)->{
                cb.setCurrentWaveGeneratorId(arg);
            });
        // 音量
        MmlReader.operationMap.put(Pattern.compile("V(\\d+)"),  //  V<number>
            (String arg, ChannelBuilder cb)->{
                cb.setCurrentVolume(Integer.parseInt(arg));
            });
        MmlReader.operationMap.put(Pattern.compile("\\((\\d+)"),    // (<number>
            (String arg, ChannelBuilder cb)->{
                cb.addToCurrentVolume(Integer.parseInt(arg));;
            });
        MmlReader.operationMap.put(Pattern.compile("\\)(\\d+)"),    // )<number>
            (String arg, ChannelBuilder cb)->{
                cb.addToCurrentVolume(- Integer.parseInt(arg));;
            });
        // オクターブ
        MmlReader.operationMap.put(Pattern.compile("O(\\d+)"),  // O<number>
            (String arg, ChannelBuilder cb)->{
                cb.setCurrentOctave(Integer.parseInt(arg));
            });
        MmlReader.operationMap.put(Pattern.compile("<"),    // <
            (String arg, ChannelBuilder cb)->{
                cb.addToCurrentOctave(1);
            });
        MmlReader.operationMap.put(Pattern.compile(">"),    // >
            (String arg, ChannelBuilder cb)->{
                cb.addToCurrentOctave(-1);
            });
        // デフォルト音長
        MmlReader.operationMap.put(Pattern.compile("L(\\d+)"),  //  L<number>
            (String arg, ChannelBuilder cb)->{
                cb.setCurrentDefaultToneLength(Integer.parseInt(arg));
            });
        // ノート表現
        MmlReader.operationMap.put(Pattern.compile("(\\d*[A-Ga-gR][#+-]?)"), // 8A 16F など
            (String arg, ChannelBuilder cb)->{
                cb.addNote(arg);
            });
        MmlReader.operationMap.put(Pattern.compile("([A-Ga-gR][#+-]?)"), // A F など
            (String arg, ChannelBuilder cb)->{
                cb.addNote(arg);
            });

        // template
        // MmlReader.operationMap.put(Pattern.compile("regex"),
        //     (String arg, ChannelBuilder cb)->{
        //         // Do something
        //     });

        return MmlReader.operationMap;
    }
}
