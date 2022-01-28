package MML2Audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat.Type;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import MML2Audio.Util.Log;

/**
 * このアプリケーションのエントリーポイントとなるクラス。
 * 
 * 実行時、コマンドライン引数によって以下に示すオプションを指定することができる。<br>
 * <br>
 * -q: 実行時に表示を行わない<br>
 * -v: 実行時に詳細情報の出力を行う<br>
 * -o <output file>: 出力ファイルをwavファイルで指定する<br>
 * <br>
 * また、第1コマンドライン引数として入力ファイルを指定する必要がある。<br>
 * 使用例:<br>
 * {@code input.mml -q -o result.wav}
 */
public class Main {
    /**
     * 出力ファイルパスを保持する。
     */
    @Option(name = "-o", metaVar = "outputFile", usage = "output file path")
    public static String outputFile;

    /**
     * trueの場合、実行中に詳細な表示を行う。
     */
    @Option(name = "-v", metaVar = "verboseFlag", usage = "print verbose information")
    public static Boolean verboseFlag=false;

    /**
     * trueの場合、実行中の表示を行わない。
     */
    @Option(name = "-q", metaVar = "quietFlag", usage = "do not print anything")
    public static Boolean quietFlag=false;

    /**
     * 入力ファイルパスを保持する。
     */
    @Argument(index = 0, required = true, metaVar = "inputFile")
    public static String inputFile;

    
    /** 
     * このアプリケーションのエントリーポイント。
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(new Main());
        try {
            parser.parseArgument(args);
        }
        catch(CmdLineException e) {
            System.out.print("Usage: ");
            parser.printSingleLineUsage(System.out);
            System.out.println();
            parser.printUsage(System.out);
            return;
        }

        Log.setInfoFlag(!Main.quietFlag);
        Log.setLogFlag(Main.verboseFlag);

        Music music = new Music();

        Log.info("Compiling...");
        MmlReader.mmlCompiler(Main.inputFile, music);
        Log.info("Done");

        Log.info("Outputting audio file...");
        byte[] buffer = music.generateAudioBuffer();
        ByteArrayInputStream binput = new ByteArrayInputStream(buffer);
        AudioInputStream audioInputStream = new AudioInputStream(binput, music.getAudioFormat(), buffer.length);

        if (Main.outputFile ==  null) {
            Main.outputFile = Path.of("output.wav").toAbsolutePath().toString();
        }
        AudioSystem.write(audioInputStream, Type.WAVE, new File(Main.outputFile));

        Log.info("Done");

        audioInputStream.close();
        binput.close();
    }
}
