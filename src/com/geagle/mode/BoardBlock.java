package com.geagle.mode;

import java.awt.Point;
import java.util.ArrayList;

public class BoardBlock {

  public static interface ChessListener {
    public void setChess(ChessItem chess);

    public void setSelected(boolean isSelected);
  }

  ArrayList<ChessListener> listener = new ArrayList<>();

  ChessItem chess;
  
  Point pos;

  boolean isSelected = false;

  public BoardBlock(Point pos) {
    this.pos = pos;
  }

  public boolean isBlank() {
    return this.chess == null;
  }

  public void setChess(ChessItem chess) {
    this.chess = chess;
    if (chess != null) {
      this.chess.setPos(this.pos);
    }
    this.notifyChessChange();
  }

  public ChessItem getChess() {
    return this.chess;
  }

  public void setSelected(boolean isSelected) {
    this.isSelected = isSelected;
    this.notifySelectionChange();
  }

  public boolean isSelected() {
    return this.isSelected;
  }

  public Point getPos() {
    return this.pos;
  }
  
  private void notifySelectionChange() {
    for (ChessListener l : listener) {
      l.setSelected(this.isSelected);
    }
  }
  
  private void notifyChessChange() {
    for (ChessListener l : listener) {
      l.setChess(this.chess);
    }
  }

  public void addListener(ChessListener l) {
    this.listener.add(l);
  }
}
