import java.io.File;
import java.io.IOException;

import com.geagle.io.RecordIO;


public class Main {
  public static void main(String[] args) throws IOException {
    Source.loadSource();
    ChessPanel.getInstance().setVisible(true);
    ChessPanel.getInstance().loadRecord(RecordIO.read(new File("/Users/geagle/Desktop/def.pgn")));
  }
}
