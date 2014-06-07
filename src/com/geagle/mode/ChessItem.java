package com.geagle.mode;

import java.awt.Point;


public class ChessItem implements Cloneable {
  Point pos;
  ChessType type;
  boolean isRed;

  public ChessItem(ChessType type, boolean isRed, Point pos) {
    this.type = type;
    this.isRed = isRed;
    this.pos = (Point) pos.clone();
  }
  
  public void setPos(Point pos) {
    this.pos = (Point) pos.clone();
  }
  
  public Point getPos() {
    return this.pos;
  }
  
  public ChessType getType() {
    return this.type;
  }
  
  public boolean isRed() {
    return this.isRed;
  }
  
  @Override
  public String toString() {
    return "[" + this.type + "," + this.isRed + ",(" + this.pos.x + "," + this.pos.y + ")]";
  }
  
  public String encode() {
    return "" + this.type + this.isRed;
  }
  
  @Override
  public Object clone() {
    return new ChessItem(this.type, this.isRed, (Point) this.pos.clone());
  }
  

  public int blockChessInLine(Point from, Point to, ChessBoard chessBoard) {
    int block = 0;
    // If from and to are the same point, or don't in a same line, return
    // infinite block count.
    if (from.equals(to) || (from.x != to.x && from.y != to.y)) {
      return Integer.MAX_VALUE;
    }
    if (from.x == to.x) {
      int min = this.pos.y < to.y ? this.pos.y : to.y;
      int max = this.pos.y + to.y - min;
      for (int i = min + 1; i < max; i++) {
        System.out.println(chessBoard.getChess(this.pos.x, i));
        if (chessBoard.getChess(this.pos.x, i) != null) {
          block++;
        }
      }
      System.out.println("from " + this.pos + " to " + to + ", " + this.isRed + ", " + block);
      return block;
    } else { // this.pos.y == to.y
      int min = this.pos.x < to.x ? this.pos.x : to.x;
      int max = this.pos.x + to.x - min;
      for (int i = min + 1; i < max; i++) {
        System.out.println(chessBoard.getChess(i, this.pos.y));
        if (chessBoard.getChess(i, this.pos.y) != null) {
          block++;
        }
      }
      System.out.println("from " + this.pos + " to " + to + ", " + this.isRed + ", " + block);
      return block;
    }
  }

  public boolean isInPalace(Point p) {
    Point dis = new Point(p.x - 4, p.y - (this.isRed ? 8 : 1));
    return Math.abs(dis.x) <= 1 && Math.abs(dis.y) <= 1;
  }

  public boolean canMoveTo(Point to, ChessBoard chessBoard) {
    if (this.pos.equals(to)) {
      return false;
    }
    if (to.x < 0 || to.x >= ChessBoard.BOARD_WIDTH || to.y < 0 || to.y >= ChessBoard.BOARD_HEIGHT) {
      return false;
    }
    if (this.type == ChessType.BING) {
      Point dis = new Point(Math.abs(to.x - this.pos.x), to.y - this.pos.y);
      boolean passedRiver = this.pos.y > 4;
      if (this.isRed) {
        dis.y = -dis.y;
        passedRiver = !passedRiver;
      }
      System.out.println("from " + this.pos + " to " + to + ", " + this.isRed + ", " + dis);
      if (dis.x == 0 && dis.y == 1) {
        return true;
      }
      if (passedRiver && dis.x == 1 && dis.y == 0) {
        return true;
      }
      return false;
    } else if (this.type == ChessType.JU) {
      return this.blockChessInLine(this.pos, to, chessBoard) == 0;
    } else if (this.type == ChessType.MA) {
      Point dis = new Point(to.x - this.pos.x, to.y - this.pos.y);

      if (!(Math.abs(dis.x) == 1 && Math.abs(dis.y) == 2)
          && !(Math.abs(dis.x) == 2 && Math.abs(dis.y) == 1)) {
        return false;
      }
      // Is blocked
      if (chessBoard.getChess(this.pos.x + dis.x / 2, this.pos.y + dis.y / 2) != null) {
        return false;
      }
      System.out.println(this.toString() + " to " + to + ", " + this.isRed + ", " + dis);
      return true;
    } else if (this.type == ChessType.PAO) {
      int block = this.blockChessInLine(this.pos, to, chessBoard);
      if (chessBoard.getChess(to) == null && block == 0) {
        return true;
      }
      if (chessBoard.getChess(to) != null && block == 1) {
        return true;
      }
      return false;
    } else if (this.type == ChessType.XIANG) {
      Point dis = new Point(to.x - this.pos.x, to.y - this.pos.y);
      if (Math.abs(dis.x) != 2 || Math.abs(dis.y) != 2) {
        return false;
      }
      // The 'to' position is in the other side of river.
      if (to.y < 5 == this.isRed) {
        return false;
      }
      // Is blocked.
      if (chessBoard.getChess(this.pos.x + dis.x / 2, this.pos.y + dis.y / 2) != null) {
        return false;
      }
      System.out.println(this.toString() + " to " + to + ", " + this.isRed + ", " + dis);
      return true;
    } else if (this.type == ChessType.SHI) {
      if (!this.isInPalace(to)) {
        return false;
      }
      Point dis = new Point(to.x - this.pos.x, to.y - this.pos.y);
      return Math.abs(dis.x) == 1 && Math.abs(dis.y) == 1;
    } else if (this.type == ChessType.JIANG) {
      if (!this.isInPalace(to)) {
        return false;
      }
      return this.pos.distance(to) == 1;
    }
    return false;
  }

}
