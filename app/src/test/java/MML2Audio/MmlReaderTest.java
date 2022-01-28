package MML2Audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import MML2Audio.Note.Note;
import MML2Audio.Channel.Channel;

public class MmlReaderTest {
    @Test
    @DisplayName("contertMmlToChannelのテスト")
    void testConvertMmlToChannel() {
        String mmlString = "";
        mmlString += "L8 V100 @(sawtooth)";
        mmlString += "C(10E(20G";
        mmlString += "L4 V200 < D)10F)10A";
        mmlString += "@(square)";
        mmlString += "16B 4A# 2A | O2D";

        ArrayList<Note> noteListExpected = new ArrayList<>();
        // L8 V100 @(sawtooth) octave=4(default)
        noteListExpected.add(new Note("C" , 8 , 4, 100, "sawtooth")); // 0
        // (10
        noteListExpected.add(new Note("E" , 8 , 4, 110, "sawtooth")); // 1
        // (20
        noteListExpected.add(new Note("G" , 8 , 4, 130, "sawtooth")); // 2
        // L4 V200 <
        noteListExpected.add(new Note("D" , 4 , 5, 200, "sawtooth")); // 3
        // )10
        noteListExpected.add(new Note("F" , 4 , 5, 190, "sawtooth")); // 4
        // )10
        noteListExpected.add(new Note("A" , 4 , 5, 180, "sawtooth")); // 5
        // @(square)
        noteListExpected.add(new Note("B" , 16, 5, 180, "square"  )); // 6
        noteListExpected.add(new Note("A#", 4 , 5, 180, "square"  )); // 7
        noteListExpected.add(new Note("A" , 2 , 5, 180, "square"  )); // 8
        // O2
        noteListExpected.add(new Note("D" , 4 , 2, 180, "square"  )); // 9

        Channel channelActual = MmlReader.convertMmlToChannel(mmlString);

        assertEquals(noteListExpected.get(0), channelActual.getNoteList().get(0));
        assertEquals(noteListExpected.get(1), channelActual.getNoteList().get(1));
        assertEquals(noteListExpected.get(2), channelActual.getNoteList().get(2));
        assertEquals(noteListExpected.get(3), channelActual.getNoteList().get(3));
        assertEquals(noteListExpected.get(4), channelActual.getNoteList().get(4));
        assertEquals(noteListExpected.get(5), channelActual.getNoteList().get(5));
        assertEquals(noteListExpected.get(6), channelActual.getNoteList().get(6));
        assertEquals(noteListExpected.get(7), channelActual.getNoteList().get(7));
        assertEquals(noteListExpected.get(8), channelActual.getNoteList().get(8));
        assertEquals(noteListExpected.get(9), channelActual.getNoteList().get(9));
    }

    @Test
    @DisplayName("readLineIgnoreComment()のテスト")
    void testReadLineIgnoreComment()
        throws NoSuchMethodException,
        InvocationTargetException,
        IllegalAccessException,
        IllegalArgumentException
    {
        // private mathodの取得
        Method method = MmlReader.class.getDeclaredMethod("readLineIgnoreComment", BufferedReader.class);
        method.setAccessible(true);

        // 単一行のコメント除去
        String expected1 = "CCCDDD";

        try (
            BufferedReader bufReader1 = new BufferedReader(new FileReader("src/test/resources/test1.mml"))
        ) {
            String actual1 = (String)method.invoke(null, bufReader1);
            assertEquals(expected1, actual1);
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        // 複数行のコメント除去
        String expected2 = "CDEFGA";

        try (
            BufferedReader bufReader2 = new BufferedReader(new FileReader("src/test/resources/test2.mml"))
        ) {
            String actual2 = (String)method.invoke(null, bufReader2);
            assertEquals(expected2, actual2);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
