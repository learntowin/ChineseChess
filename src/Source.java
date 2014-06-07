import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.geagle.mode.ChessType;


public class Source {
  public static final int CHESS_SIZE = 35;
  public static final int BOARDER = 2;


  private static BufferedImage border;
  private static BufferedImage items;
  private static BufferedImage selected;
  private static BufferedImage blank;
  private static BufferedImage mark;

  public static void loadSource() throws IOException {
    border = ImageIO.read(new File("source/board.jpg"));
    items = ImageIO.read(new File("source/items.png"));
    selected = ImageIO.read(new File("source/select.png"));
    blank = ImageIO.read(new File("source/blank.png"));
    mark = ImageIO.read(new File("source/mark.png"));
  }

  public static BufferedImage getBoardImage() {
    return border;
  }

  public static BufferedImage getChessImage(ChessType item, boolean isRed) {
    int index = 0;
    switch (item) {
      case JIANG:
        index = 0;
        break;
      case SHI:
        index = 1;
        break;
      case XIANG:
        index = 2;
        break;
      case MA:
        index = 3;
        break;
      case JU:
        index = 4;
        break;
      case PAO:
        index = 5;
        break;
      case BING:
        index = 6;
        break;
      default:
        return getBlankImage();
    }

    int w = items.getWidth() / 14;
    int h = items.getHeight() / 3;

    return items.getSubimage(w * (index + (isRed ? 0 : 7)), 0, w, h);
  }

  public static BufferedImage getSelectedImage() {
    return selected;
  }

  public static BufferedImage getBlankImage() {
    return blank;
  }
  
  public static BufferedImage getMarkedImage() {
    return mark;
  }
}
