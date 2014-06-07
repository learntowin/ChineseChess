import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.geagle.mode.BoardBlock;
import com.geagle.mode.ChessItem;


public class BoardBlockLabel extends JLabel {
  private static final long serialVersionUID = -7691478402816767345L;

  private BoardBlock mode = null;
  
  private JLabel selected = null;
  
  private JLabel marked = null;

  public BoardBlockLabel(BoardBlock pmode) {
    super();

    this.mode = pmode;
    assert(this.mode != null);
    Point pos = this.mode.getPos();
    this.setBounds(Source.BOARDER + pos.x * Source.CHESS_SIZE, Source.BOARDER
        + pos.y * Source.CHESS_SIZE, Source.CHESS_SIZE, Source.CHESS_SIZE);
    this.updateIcon();
    
    this.mode.addListener(new BoardBlock.ChessListener() {
      @Override
      public void setSelected(boolean isSelected) {
        updateSelection();
      }
      
      @Override
      public void setChess(ChessItem chess) {
        updateIcon();
      }
    });
  }

  private void updateIcon() {
    if (this.mode != null && !this.mode.isBlank()) {
      ChessItem chess = this.mode.getChess();
      this.setIcon(new ImageIcon(Source.getChessImage(chess.getType(), chess.isRed())));
    } else {
      this.setIcon(new ImageIcon(Source.getBlankImage()));
    }
    this.repaintParent();
  }
  
  private void repaintParent() {
    Container parent = this.getParent();
    if (parent != null) {
      Rectangle bounds = this.getBounds();
      parent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
    }
  }
  
  public void updateSelection() {
    this.setSelected(this.mode.isSelected());
  }

  private void setSelected(boolean isSelected) {
    if (isSelected) {
      if (this.selected == null) {
        this.selected = new JLabel(new ImageIcon(Source.getSelectedImage()));
        this.selected.setBounds(0, 0, Source.CHESS_SIZE, Source.CHESS_SIZE);
      }
      if (this.selected.getParent() == this) {
        return;
      }
      this.add(this.selected, 0);
    } else {
      this.remove(this.selected);
    }
    this.repaintParent();
  }
  
  public void setMarked(boolean isMarked) {
    if (isMarked) {
      if (this.marked == null) {
        this.marked = new JLabel(new ImageIcon(Source.getMarkedImage()));
        this.marked.setBounds(0, 0, Source.CHESS_SIZE, Source.CHESS_SIZE);
      }
      if (this.marked.getParent() == this) {
        return;
      }
      this.add(this.marked, 0);
    } else {
      this.remove(this.marked);
    }
    this.repaintParent();
  }

  public BoardBlock getMode() {
    return this.mode;
  }
}
