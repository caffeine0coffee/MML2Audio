# MML2Audio

mmlファイルをwavファイルに変換するプログラムです。

## Build

Java Development Kit (JDK)が必要です。
openjdk 16.0.1 及び java 17.0.2 で動作確認済みです。

Linux / Mac
```
./gradlew shadowJar
```

Windows
```
.\gradlew.bat shadowJar
```

## Run

```
java -jar MML2Audio.jar <inputFile> [-o <outputFile>] [-q] [-v]
    -o 出力ファイル指定
    -q quietフラグ
    -v verboseフラグ
```
⚠出力フォーマットは出力ファイル名の指定に依らずwavファイルになります

## MML Syntax

- :\<string>
  - 指定したチャンネル名でチャンネルの開始を宣言する
  - 次のチャンネル宣言、若しくはファイル終端までがチャンネル定義範囲となる
- \<number>
  - 曲のテンポをBPM値で指定する
  - 一番最後に記述されたT命令が全体に反映される
  - デフォルトは100
- \<length>\<scale>\<semitone>
  - ノート表現
  - length: 1 ~ 64の整数
    - 音楽的な表記に準ずる
    - 省略するとデフォルト音長(初期値4)になる
  - scale: [A-Ga-gR] 音階の英語表記に従うがRは休符を表す
  - semitone: [#+-] #と+は半音上、-は半音下を表す(省略可能)
  - 例: 8G# -> 8分音符のソ#
- O\<number>
  - これ以降のオクターブを指定する
- <, >
  - それぞれオクターブを1つ上げる, 下げる
- L\<number>
  - これ以降のデフォルト音長を指定する
- @(\<string>)
  - それ以降の音色をwaveGeneratorIdで指定する
  - waveGeneratorIdの一覧
    - sin : sin波
    - square : 矩形波
    - sawtooth : のこぎり波
    - noise : ノイズ (ノートの音階に影響を受けない)
- V<number>
  - それ以降の音量を指定
  - 0~255の整数
- (<number> 指定した分だけ音量を上げる
- )<number> 指定した分だけ音量を下げる
- | 可視性の為に使う
  - 生成される曲には影響を及ぼさない
- /* */ コメント文
  - 行を跨ぐことも可能
