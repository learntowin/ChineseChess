package com.geagle.mode;

import java.awt.Point;
import java.util.ArrayList;

public class Record {
  
  public static class Step {
    public Point from;
    public Point to;
    public ChessType type;
    public String string = "";
    
    public ChessType eat;
    public Step(ChessType type, Point from, Point to, String string) {
      this.type = type;
      this.from = (Point) from.clone();
      this.to = (Point) to.clone();
      this.string = string;
    }
    public Step(ChessType type, Point from, Point to, ChessType eat) {
      this.type = type;
      this.from = (Point) from.clone();
      this.to = (Point) to.clone();
      this.eat = eat;
    }
  }
  
  private ChessItem[][] startStatus = null;
  
  private boolean isRedFirst = true;
  
  private ArrayList<Step> steps = new ArrayList<>();
  
  public void setStartStatus(ChessItem[][] status) {
    this.startStatus = status;
  }
  
  public void addStep(Step step) {
    this.steps.add(step);
  }

  public final ChessItem[][] getStartStatus() {
    return this.startStatus;
  }
  
  public void setFirst(boolean isRed) {
    this.isRedFirst = isRed;
  }
  
  public boolean isRedFirst() {
    return this.isRedFirst;
  }
  
  public int stepCount() {
    return this.steps.size();
  }

  public final Step step(int i) {
    return i >= 0 && i < this.steps.size() ? this.steps.get(i) : null;
  }
}
