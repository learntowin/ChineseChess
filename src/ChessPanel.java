import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.geagle.mode.BoardBlock;
import com.geagle.mode.ChessBoard;
import com.geagle.mode.Record;
import com.geagle.mode.Record.Step;


public class ChessPanel extends JFrame {
  private static final long serialVersionUID = 3261030676511048428L;

  private ChessBoard board = new ChessBoard();

  private JPanel mainPanel = new JPanel();

  private Record record = null;
  private int recordStep = 0;

  private BoardBlockLabel[][] labels;

  private static ChessPanel instance = null;

  private BoardBlockLabel[] moveMark = new BoardBlockLabel[2];

  public static ChessPanel getInstance() {
    if (instance == null) {
      instance = new ChessPanel();
    }
    return instance;
  }

  public ChessBoard getBoard() {
    return this.board;
  }

  public ChessPanel() {
    super();

    this.mainPanel.setLayout(null);

    this.setContentPane(this.mainPanel);
    this.initBackground(this.mainPanel);
    this.initBlockLabel(this.mainPanel);

    this.mainPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Component item = mainPanel.getComponentAt(e.getPoint());
        if (item == null || !(item instanceof BoardBlockLabel)) {
          return;
        }
        BoardBlock block = ((BoardBlockLabel) item).getMode();
        if (board.getSelectedBlock() != null) {
          board.moveTo(block.getPos());
        } else if (!block.isBlank()) {
          board.setSelectedBlock(block);
        }
      }
    });

    this.board.addListener(new ChessBoard.BoardListener() {
      @Override
      public void reset(ChessBoard board) {
        repaint();
      }

      @Override
      public void move(Point from, Point to) {
        if (moveMark[0] != null) {
          moveMark[0].setMarked(false);
        }
        moveMark[0] = labels[from.y][from.x];
        if (moveMark[1] != null) {
          moveMark[1].setMarked(false);
        }
        moveMark[1] = labels[to.y][to.x];
        
        moveMark[0].setMarked(true);
        moveMark[1].setMarked(true);
      }
    });
    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        char c = e.getKeyChar();
        if (c == 'r') {
          loadRecord(record);
        }
        if (c == 'd') {
          nextStepInRecord();
        }
        if (c == 'a') {
          stepBack();
        }
      }
    });
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.pack();
    this.setLocationRelativeTo(null);
  }

  private void initBackground(JPanel panel) {
    panel.setPreferredSize(new Dimension(300, 300));
    panel.setLayout(null);
    panel.setSize(300, 300);
    BufferedImage border = Source.getBoardImage();
    JLabel label = new JLabel(new ImageIcon(border));
    Dimension size = label.getPreferredSize();
    label.setBounds(0, 0, size.width, size.height);
    panel.add(label);
    panel.setPreferredSize(label.getPreferredSize());
    panel.setSize(panel.getPreferredSize());
  }

  private void initBlockLabel(JPanel panel) {
    this.labels = new BoardBlockLabel[ChessBoard.BOARD_HEIGHT][];
    for (int i = 0; i < ChessBoard.BOARD_HEIGHT; i++) {
      this.labels[i] = new BoardBlockLabel[ChessBoard.BOARD_WIDTH];
      for (int j = 0; j < ChessBoard.BOARD_WIDTH; j++) {
        this.labels[i][j] = new BoardBlockLabel(this.board.getBlock(j, i));
        panel.add(this.labels[i][j], 0);
      }
    }
  }

  public void loadRecord(Record record) {
    // try {
    // Record r = RecordIO.red(file);
    // this.board.reset(RecordIO.read(new File("/Users/geagle/Desktop/def.pgn")));
    // } catch (IOException e) {
    // e.printStackTrace();
    // this.board.reset();
    // }
    this.record = record;
    if (this.record != null) {
      this.board.reset(this.record.getStartStatus(), this.record.isRedFirst());
    } else {
      this.board.reset();
    }
    this.recordStep = 0;
  }

  public void nextStepInRecord() {
    if (this.record == null || this.recordStep >= this.record.stepCount()) {
      System.err.println("null record" + this.recordStep);
      return;
    }
    Step s = this.record.step(this.recordStep);
    BoardBlock block = this.board.getBlock(s.from);
    this.board.setSelectedBlock(block);
    this.board.moveTo(s.to);
    this.recordStep++;
  }
  
  public void stepBack() {
    if (this.record != null && this.recordStep > 0) {
      this.recordStep--;
    }
    this.board.stepBack();
  }
}
