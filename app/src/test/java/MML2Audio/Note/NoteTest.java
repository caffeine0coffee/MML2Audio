package MML2Audio.Note;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NoteTest {
    @Test
    @DisplayName("equals()のテスト")
    void testEquals() {
        // メンバの違いによるテスト
        Note note1 = new Note(0, 8, 5, 200, "square");

        Note note1ExpectedEquals     = new Note(0, 8 , 5, 200, "square");
        Note note1ExpectedNotEquals1 = new Note(1, 8 , 5, 200, "square"); // scaleIndex
        Note note1ExpectedNotEquals2 = new Note(0, 16, 5, 200, "square"); // toneLength
        Note note1ExpectedNotEquals3 = new Note(0, 8 , 6, 200, "square"); // octave
        Note note1ExpectedNotEquals4 = new Note(0, 8 , 5, 150, "square"); // volume
        Note note1ExpectedNotEquals5 = new Note(0, 8 , 5, 200, "noise" ); // waveGeneratorId

        assertEquals(note1, note1ExpectedEquals);
        assertNotEquals(note1, note1ExpectedNotEquals1);
        assertNotEquals(note1, note1ExpectedNotEquals2);
        assertNotEquals(note1, note1ExpectedNotEquals3);
        assertNotEquals(note1, note1ExpectedNotEquals4);
        assertNotEquals(note1, note1ExpectedNotEquals5);

        // 大文字小文字の違いによるテスト
        Note note2Upper = new Note("A", 8, 5, 200, "sin");
        Note note2Lower = new Note("a", 8, 5, 200, "sin");

        assertEquals(note2Upper, note2Lower);

        // 半音記号の違いによるテスト
        Note noteCPlus  = new Note("G+", 8, 5, 200, "sin");
        Note noteCSharp = new Note("G#", 8, 5, 200, "sin");

        assertEquals(noteCPlus, noteCSharp);
    }

    @Test
    @DisplayName("scaleIndexとnoteNameの等価性のテスト")
    void testEqualsBetweenScaleIndexAndNoteName() {
        Note noteScaleIndex = new Note(0  , 8, 5, 200, "sin");
        Note noteNoteName   = new Note("C", 8, 5, 200, "sin");

        assertEquals(noteScaleIndex, noteNoteName);
    }

    @Test
    @DisplayName("半音によるscaleIndexの繰り上げ/繰り下げのテスト")
    void testScaleIndexMoveUpAndMoveDown() {
        // B(シ)の半音上はC(ド)なので繰り上げが発生
        Note note1       = new Note(0   , 8, 5, 200, "sin");
        Note note1MoveUp = new Note("B#", 8, 4, 200, "sin");

        assertEquals(note1, note1MoveUp);

        // C(ド)の半音下はB(シ)なので繰り下げが発生
        Note note2         = new Note(11  , 8, 4, 200, "sin");
        Note note2MoveDown = new Note("C-", 8, 5, 200, "sin");

        assertEquals(note2, note2MoveDown);
    }
}
