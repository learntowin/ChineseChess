package com.geagle.mode;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class ChessBoard {

  private static ChessItem[][] status = null;

  // TODO: Move this to a common file.
  public static final int BOARD_WIDTH = 9;
  public static final int BOARD_HEIGHT = 10;

  public static interface BoardListener {
    public void reset(ChessBoard board);

    public void move(Point from, Point to);
  }

  BoardBlock[][] blocks;

  Map<String, ArrayList<ChessItem>> chesses = new HashMap<>();
  private BoardBlock selectedBlock = null;
  private boolean isRedTurn = true;

  private static class Step {
    public Point from;
    public Point to;
    public ChessItem chess;
    public ChessItem eat;

    public Step(ChessItem chess, Point from, Point to, ChessItem eat) {
      this.chess = chess;
      this.from = (Point) from.clone();
      this.to = (Point) to.clone();
      this.eat = eat;
    }
    
    @Override
    public String toString() {
      return "" + this.chess + " from " + this.from + " to " + this.to + ", eat = " + this.eat;
    }
  }

  private LinkedList<Step> history = new LinkedList<>();

  private ArrayList<BoardListener> listeners = new ArrayList<>();


  public static ChessItem[][] defaultChessStatus() {
    if (status == null) {
      ChessType[][] initType = new ChessType[ChessBoard.BOARD_HEIGHT][];
      for (int i = 0; i < ChessBoard.BOARD_HEIGHT; i++) {
        initType[i] = new ChessType[ChessBoard.BOARD_WIDTH];
      }
      // Tops left is (0, 0).
      initType[0][4] = ChessType.JIANG;
      initType[0][3] = initType[0][5] = ChessType.SHI;
      initType[0][2] = initType[0][6] = ChessType.XIANG;
      initType[0][1] = initType[0][7] = ChessType.MA;
      initType[0][0] = initType[0][8] = ChessType.JU;
      initType[2][1] = initType[2][7] = ChessType.PAO;
      initType[3][0] =
          initType[3][2] = initType[3][4] = initType[3][6] = initType[3][8] = ChessType.BING;

      status = new ChessItem[ChessBoard.BOARD_HEIGHT][];
      for (int i = 0; i < status.length; i++) {
        status[i] = new ChessItem[ChessBoard.BOARD_WIDTH];
        for (int j = 0; j < ChessBoard.BOARD_WIDTH; j++) {
          ChessType type = initType[i >= 5 ? 9 - i : i][j];
          if (type != null) {
            status[i][j] = new ChessItem(type, i >= 5, new Point(j, i));
          }
        }
      }
    }
    return status;
  }

  public ChessBoard() {
    this.blocks = new BoardBlock[BOARD_HEIGHT][];
    for (int i = 0; i < BOARD_HEIGHT; i++) {
      this.blocks[i] = new BoardBlock[BOARD_WIDTH];
      for (int j = 0; j < BOARD_WIDTH; j++) {
        this.blocks[i][j] = new BoardBlock(new Point(j, i));
      }
    }
  }

  public void reset() {
    this.resetBoard(defaultChessStatus());
    this.isRedTurn = true;
    this.setSelectedBlock(null);
    this.notifyReset();
  }

  public void reset(ChessItem[][] status, boolean isRedTurn) {
    this.resetBoard(status);
    this.isRedTurn = isRedTurn;
    this.setSelectedBlock(null);
    this.notifyReset();
  }

  public void addListener(BoardListener l) {
    this.listeners.add(l);
  }

  public void notifyReset() {
    for (BoardListener l : this.listeners) {
      l.reset(this);
    }
  }

  public void notifyMove(Point from, Point to) {
    for (BoardListener l : this.listeners) {
      l.move(from, to);
    }
  }

  public boolean canMoveTo(Point pos) {
    if (this.selectedBlock == null || this.selectedBlock.isBlank()) {
      return false;
    }
    return this.selectedBlock.getChess().canMoveTo(pos, this);
  }

  public boolean moveTo(Point pos) {
    if (this.selectedBlock == null || this.selectedBlock.isBlank()) {
      return false;
    }

    // Swap position
    if (!this.selectedBlock.getChess().canMoveTo(pos, this)) {
      return false;
    }

    // Record history before changing chess status.
    ChessItem eat = this.blocks[pos.y][pos.x].getChess();
    if (eat != null) {
      this.removeFromChesses(eat);
    }
    this.history
        .push(new Step(this.selectedBlock.getChess(), this.selectedBlock.getPos(), pos, eat));

    this.blocks[pos.y][pos.x].setChess(this.selectedBlock.getChess());
    this.selectedBlock.setChess(null);

    this.notifyMove(this.selectedBlock.getPos(), pos);

    // Reset selected chess.
    this.setSelectedBlock(null);
    this.isRedTurn = !this.isRedTurn;
    return true;
  }

  public BoardBlock getBlock(int x, int y) {
    return this.blocks[y][x];
  }

  public BoardBlock getBlock(Point pos) {
    return this.getBlock(pos.x, pos.y);
  }

  public ChessItem getChess(int x, int y) {
    BoardBlock block = this.blocks[y][x];
    return block == null ? null : block.getChess();
  }

  public Object getSelectedBlock() {
    return this.selectedBlock;
  }

  public ChessItem getChess(Point pos) {
    return this.getChess(pos.x, pos.y);
  }

  public boolean setSelectedChess(ChessItem chess) {
    return this.setSelectedBlock(this.getBlock(chess.getPos()));
  }

  public boolean setSelectedBlock(BoardBlock block) {
    // If click other side chess, ignore.
    if (block != null && !block.isBlank() && block.getChess().isRed() != this.isRedTurn) {
      return false;
    }
    if (this.selectedBlock != null) {
      this.selectedBlock.setSelected(false);
      this.selectedBlock = null;
    }
    if (block != null && !block.isBlank() && block.getChess().isRed() == this.isRedTurn) {
      this.selectedBlock = block;
      this.selectedBlock.setSelected(true);
    }
    return true;
  }

  public ArrayList<ChessItem> findChessesInColumn(ChessType type, boolean isRed) {
    String key = new ChessItem(type, isRed, new Point(0, 0)).encode();
    return this.chesses.get(key);
  }

  private void resetBoard(ChessItem[][] status) {
    this.chesses.clear();
    this.history.clear();
    for (int i = 0; i < BOARD_HEIGHT; i++) {
      for (int j = 0; j < BOARD_WIDTH; j++) {
        if (status[i][j] == null) {
          this.blocks[i][j].setChess(null);
        } else {
          ChessItem chess = (ChessItem) status[i][j].clone();
          this.blocks[i][j].setChess(chess);
          this.addToChesses(chess);
        }
      }
    }
  }

  public void stepBack() {
    if (this.history.size() > 0) {
      Step step = this.history.pop();
      this.blocks[step.from.y][step.from.x].setChess(step.chess);
      this.blocks[step.to.y][step.to.x].setChess(step.eat);
      if (step.eat != null) {
        this.addToChesses(step.eat);
      }
      this.notifyMove(step.to, step.from);
      this.isRedTurn = !this.isRedTurn;
    }
  }

  private void removeFromChesses(ChessItem item) {
    if (item == null) {
      return;
    }
    String key = item.encode();
    if (this.chesses.containsKey(key)) {
      this.chesses.get(key).remove(item);
    }
  }

  private void addToChesses(ChessItem item) {
    String key = item.encode();
    if (!this.chesses.containsKey(key)) {
      this.chesses.put(key, new ArrayList<ChessItem>());
    }
    this.chesses.get(key).add(item);

  }

  public void draw() {
    for (int i = 0; i < ChessBoard.BOARD_HEIGHT; i++) {
      String line = "";
      for (int j = 0; j < ChessBoard.BOARD_WIDTH; j++) {
        ChessItem chess = this.blocks[i][j].getChess();
        if (chess == null) {
          line += "---";
        } else {
          line += ts(chess.getType()) + (chess.isRed() ? "*" : "");
        }
      }
      System.out.println(line);
      System.out.println("|  |  |  |  |  |  |  |  |");
    }
  }

  public String ts(ChessType type) {
    switch (type) {
      case JU:
        return "车";
      case MA:
        return "马";
      case PAO:
        return "炮";
      case BING:
        return "兵";
      case SHI:
        return "士";
      case XIANG:
        return "相";
      case JIANG:
        return "帅";
    }
    return "";
  }

}
