package com.geagle.io;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.geagle.mode.ChessBoard;
import com.geagle.mode.ChessItem;
import com.geagle.mode.ChessType;
import com.geagle.mode.Record;

public class PgnReader {
  private static class ChessStep {
    public int from;
    public int to;
    public ChessType type;
    public int move;
    public String string;
  }


  public Record read(File file) throws IOException {
    String content = getContent(file);
    return parse(content);
  }

  public String getContent(File file) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gbk"));
    String line = in.readLine();
    String cont = "";
    while (line != null) {
      cont += line;
      line = in.readLine();
    }
    in.close();
    return cont;
  }


  private Record parse(String content) {
    Record record = new Record();

    Pattern p = Pattern.compile("\\[([^\\]]+)\\]");
    Matcher m = p.matcher(content);
    while (m.find()) {
      this.parseTag(m.group(1), record);
    }
    ChessBoard board = new ChessBoard();
    board.reset(record.getStartStatus(), record.isRedFirst());
    if (!this.parseSteps(m.replaceAll(""), board, record)) {
      return null;
    }
    return record;
  }

  // TODO: Parse sub steps.
  private boolean parseSteps(String stepString, ChessBoard board, Record record) {
    String headRe = "\\d+\\.";
    String stepRe = "[^\\s]{4}+";
    String commentRe = "\\{[^\\}]*\\}";
    String itemRe = "(" + stepRe + ")(\\s*" + commentRe + ")?";
    Pattern p = Pattern.compile(headRe + "\\s*" + itemRe + "(\\s+" + itemRe + ")?");
    Matcher m = p.matcher(stepString);

    while (m.find()) {
      if (m.group(1) != null) {
        ChessStep step = new ChessStep();
        if (!this.parseStep(m.group(1), step, true)
            || !this.addStepToRecord(step, true, board, record)) {
          return false;
        }
      }
      if (m.group(4) != null) {
        ChessStep step = new ChessStep();
        if (!this.parseStep(m.group(4), step, false)
            || !this.addStepToRecord(step, false, board, record)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean addStepToRecord(ChessStep step, boolean isRed, ChessBoard board, Record record) {
    String info =
        "Step : " + step.string + "[" + step.type + "," + step.from + "," + step.move + ","
            + step.to + "]";
    ArrayList<ChessItem> cs = board.findChessesInColumn(step.type, isRed);
    if (cs == null || cs.size() <= 0) {
      System.err.println("Invalid chess step: " + step.string + ". Can not find chess of type "
          + step.type);
      return false;
    }

    ArrayList<ChessItem> src = new ArrayList<>();
    if (step.from < 0) {
      if (cs.size() != 2) {
        System.err.println("Invalid chess step: " + step.string + ". " + cs.size()
            + " chesses of the aim type exist on board.");
        return false;
      }
      if (cs.get(0).getPos().x != cs.get(1).getPos().x) {
        System.err.println("Invalid chess step: " + step.string + ". "
            + "The chesses are not in the same column: " + cs.get(0).getPos() + ","
            + cs.get(1).getPos());
        return false;
      }
      int max = cs.get(0).getPos().y > cs.get(1).getPos().y ? 0 : 1;
      int min = 1 - max;
      if ((isRed && step.from == -2) || (!isRed && step.from == -1)) {
        src.add(cs.get(max));
      } else {
        src.add(cs.get(min));
      }
    } else {
      int x = isRed ? ChessBoard.BOARD_WIDTH - step.from : step.from - 1;
      for (ChessItem c : cs) {
        if (c.getPos().x == x) {
          src.add(c);
        }
      }
      if (src.size() <= 0) {
        System.err.println("Invalid chess step: " + step.string + ". No chess is with x : " + x);
        return false;
      }
    }

    if (isRed) {
      step.move *= -1;
    }

    Record.Step aim = null;
    for (int i = src.size() - 1; i >= 0; i--) {
      ChessItem chess = src.get(i);
      // TODO: Merge all rules to a single file.
      Point to = null;
      int x = isRed ? ChessBoard.BOARD_WIDTH - step.to : step.to - 1;
      if (step.type == ChessType.JU || step.type == ChessType.PAO || step.type == ChessType.BING
          || step.type == ChessType.JIANG) {
        if (step.move == 0) {
          to = new Point(x, chess.getPos().y);
        } else {
          System.out.println(chess.getPos() + "," + step.move + "," + step.to);
          to = new Point(chess.getPos().x, chess.getPos().y + step.move * step.to);
        }
      } else {
        if (step.move == 0) {
          System.err.println("Invalid chess step: " + step.string + ". " + "Type " + step.type
              + " couldn't move horizontally.");
          return false;
        }
        int deltaX = Math.abs(x - chess.getPos().x);
        if (step.type == ChessType.MA) {
          if (deltaX != 2 && deltaX != 1) {
            System.err.println("Invalid chess step: " + step.string + " to calculate destination");
          }
          to = new Point(x, chess.getPos().y + step.move * (3 - deltaX));
        } else if (step.type == ChessType.XIANG) {
          if (deltaX != 2) {
            System.err.println("Invalid chess step: " + step.string + " to calculate destination");
          }
          to = new Point(x, chess.getPos().y + step.move * 2);
        } else if (step.type == ChessType.SHI) {
          if (deltaX != 1) {
            System.err.println("Invalid chess step: " + step.string + " to calculate destination");
          }
          to = new Point(x, chess.getPos().y + step.move);
        }
      }
      Record.Step s = new Record.Step(step.type, chess.getPos(), to, step.string);
      if (!board.setSelectedChess(chess)) {
        System.err.println("Invalid chess step: Selecte chess in step " + step.string + " failed.");
        return false;
      }
      if (!board.canMoveTo(to)) {
        src.remove(i);
        continue;
      }
      aim = s;
    }
    if (src.size() == 0) {
      System.err.println("Invalid chess step: Cann't move to position in step " + step.string);
      return false;
    } else if (src.size() > 1) {
      System.err.println("Invalid chess step: " + step.string + ". " + src.size() + " chesses match it.");
      return false;
    }
    board.setSelectedChess(src.get(0));
    board.moveTo(aim.to);
    board.draw();
    record.addStep(aim);
    System.out.println(info + " => " + aim.from + "," + aim.to);
    return true;
  }

  private boolean parseStep(String string, ChessStep step, boolean isRed) {
    char[] cs = string.toCharArray();
    if (cs.length != 4) {
      System.err.println("Invalid step: " + string);
      return false;
    }

    ChessType type;
    int from;
    if (cs[0] == '前' || cs[0] == '后') {
      type = this.getChessTypeByName(cs[1]);
      from = cs[0] == '前' ? -1 : -2; // TODO: Magic number
    } else {
      type = this.getChessTypeByName(cs[0]);
      from = this.getChessPosition(cs[1]);
    }
    if (type == null && from == -3) {
      System.err.println("Invalid chess type or from position: " + string);
      return false;
    }

    int move;
    switch (cs[2]) {
      case '进':
        move = 1;
        break;
      case '平':
        move = 0;
        break;
      case '退':
        move = -1;
        break;
      default:
        System.err.println("Invalid chess move: " + string);
        return false;
    }
    int to = getChessPosition(cs[3]);
    if (to == -3) {
      System.err.println("Invalid chess to position: " + string + "," + cs[3]);
      return false;
    }

    step.type = type;
    step.from = from;
    step.to = to;
    step.move = move;
    step.string = string;
    return true;
  }


  private int getChessPosition(char pos) {
    if (pos > '\uFF00' && pos < '\uFF5F') {
      pos -= 65248;
    }
    if (pos > '0' && pos <= '9') {
      return pos - '0';
    }
    String num = "一二三四五六七八九十";
    int idx = num.indexOf(pos);
    if (idx < 0) {
      return -3; // TODO: Magic number.
    }
    return idx + 1;
  }

  private ChessType getChessTypeByName(char name) {
    switch (name) {
      case '车':
        return ChessType.JU;
      case '马':
        return ChessType.MA;
      case '象':
      case '相':
        return ChessType.XIANG;
      case '士':
      case '仕':
        return ChessType.SHI;
      case '将':
      case '帅':
        return ChessType.JIANG;
      case '炮':
      case '砲':
        return ChessType.PAO;
      case '兵':
      case '卒':
        return ChessType.BING;
      default:
        return null;
    }
  }

  private boolean parseTag(String tagString, Record record) {
    String[] token = tagString.split(" ", 2);
    if (token.length < 2) {
      System.err.println("Invalid tag string : " + tagString);
      return false;
    }
    if (token[0].equals("FEN") && !this.parseFen(token[1], record)) {
      return false;
    }
    return true;
  }

  private boolean parseFen(String fenString, Record record) {
    String[] token = fenString.replaceAll("\"", "").split(" ");
    if (token.length != 6) {
      System.err.println("Invalid Fen: " + fenString);
      return false;
    }
    return this.parseStartStatus(token[0], record);
  }

  private boolean parseStartStatus(String statusString, Record record) {
    System.out.println(statusString);
    String[] lines = statusString.split("/");
    if (lines.length != ChessBoard.BOARD_HEIGHT) {
      System.err.println("Invalid Start status: " + statusString);
      return false;
    }
    ChessItem[][] status = new ChessItem[ChessBoard.BOARD_HEIGHT][];
    for (int i = 0; i < lines.length; i++) {
      status[i] = new ChessItem[ChessBoard.BOARD_WIDTH];
      char[] cs = lines[i].toCharArray();
      int j = 0;
      for (int k = 0; k < cs.length; k++) {
        boolean isRed = Character.isUpperCase(cs[k]);
        cs[k] = Character.toLowerCase(cs[k]);
        try {
          switch (cs[k]) {
            case 'r':
              status[i][j] = new ChessItem(ChessType.JU, isRed, new Point(j, i));
              j++;
              break;
            case 'n':
              status[i][j] = new ChessItem(ChessType.MA, isRed, new Point(j, i));
              j++;
              break;
            case 'b':
              status[i][j] = new ChessItem(ChessType.XIANG, isRed, new Point(j, i));
              j++;
              break;
            case 'a':
              status[i][j] = new ChessItem(ChessType.SHI, isRed, new Point(j, i));
              j++;
              break;
            case 'k':
              status[i][j] = new ChessItem(ChessType.JIANG, isRed, new Point(j, i));
              j++;
              break;
            case 'c':
              status[i][j] = new ChessItem(ChessType.PAO, isRed, new Point(j, i));
              j++;
              break;
            case 'p':
              status[i][j] = new ChessItem(ChessType.BING, isRed, new Point(j, i));
              j++;
              break;
            default:
              if (cs[k] > '0' && cs[k] <= '9') {
                for (int m = '0'; m < cs[k]; m++) {
                  j++;
                }
              } else {
                System.err.println("Unknown character '" + cs[k] + "' in start status string:"
                    + statusString);
                return false;
              }
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          System.err.println("Line is too long in status string item: " + lines[i]);
          return false;
        }
      }
    }
    record.setStartStatus(status);
    return true;
  }

  public static void main(String[] args) throws IOException {
    Record r = new PgnReader().read(new File("/Users/geagle/Desktop/def.pgn"));
    for (int i = 0; i < r.stepCount(); i++) {
      System.out.println(r.step(i).from + "," + r.step(i).to);
    }
  }
}
