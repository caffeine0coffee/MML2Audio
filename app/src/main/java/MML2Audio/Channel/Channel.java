package MML2Audio.Channel;

import java.util.ArrayList;

import MML2Audio.Note.Note;

/**
 * 曲における1つのパート・演奏者を表す。
 * 
 * <p>
 * チャンネルとは混声合唱における1つのパート、若しくは演奏者を表すような概念である。
 * MML2Audioにおいて、チャンネルは同時に複数の音を発音することができない。
 * したがって、同時に複数の音を発音するためには複数のチャンネルを定義する必要がある。
 */
public class Channel {
    /**
     * チャンネルに含まれる{@link Note}オブジェクトを管理する。
     */
    private ArrayList<Note> noteList = new ArrayList<>();

    public Channel(ArrayList<Note> noteList) {
        this.noteList = noteList;
    }

    public Channel() { }

    /** 
     * チャンネルに含まれる全ての{@link Note}オブジェクトを取得する。
     * 
     * @return ArrayList<Note>
     */
    public ArrayList<Note> getNoteList() {
        return noteList;
    }

    /** 
     * チャンネルに{@link Note}オブジェクトを追加する。
     * 
     * @param note
     */
    public void addNote(Note note) {
        this.noteList.add(note);
    }
}
