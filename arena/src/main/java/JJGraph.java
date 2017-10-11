/**
 * Copyright: Copyright (c) Tim Strazny<p>
 * @author Tim Strazny
 * @version 1.0
 */
import java.awt.*;

public class JJGraph extends Frame {
  private static final String pre_title = "Result Graph of ";
  private static final String suf_title = " (red = single, green = double, blue = team)";
  private static final int graph_dist = 2;
  private static final int step = 20;

  private Choice choice = new Choice();
  private Canvas can = new Canvas();
  private String[] modeTitle = {"Single ", "Double ", "Team "};
  private Checkbox[] mode = {new Checkbox(modeTitle[0]+"(0)"), new Checkbox(modeTitle[1]+"(0)"), new Checkbox(modeTitle[2]+"(0)")};
  private Label zoomLabel = new Label("x1");
  private Label intervalLabel = new Label("[0, .., 0]");
  private Label percLabel = new Label("0% 0% 0%");
  private java.awt.List list;

  private int max_width = 600;
  private Image offscreenImage;
  private int w = 0, h = 0;
  private int trans = 0;
  private String pString = "0% 0% 0%";
  private int number = -1;
  private int count;
  private int max = 0;

  private String name = "[none]";
  private String[][] graph;
  private String[] jn;
  private byte zoom = 1;

  public JJGraph(String[] jn, String[][] graph, String[] names) {
    super();
    this.setTitle(pre_title + name + suf_title);
    this.jn = jn;
    this.graph = graph;
    this.setLayout(new BorderLayout());
    Panel p = new Panel();
    p.setLayout(new BorderLayout());
    this.add("Center", p);
    p.add("Center", can);
    Panel n = new Panel();
    n.setLayout(new GridLayout(3, 4));
    this.add("North", n);
    n.add(choice);
    n.add(mode[0]);
    n.add(new Button("Zoom in"));
    n.add(new Button("Zoom out"));

    n.add(new Button("Redraw"));
    n.add(mode[1]);
    n.add(new Button("Move right"));
    n.add(new Button("Move left"));

    n.add(zoomLabel);
    n.add(mode[2]);
    n.add(percLabel);
    n.add(intervalLabel);
    for (int i = 0; i < 3; i++)
      mode[i].setState(true);
    for (int i = 0; i < names.length; i++)
      choice.addItem(names[i]);
    can.setBackground(Color.white);
    can.resize(200, 102);
    max_width = can.getToolkit().getScreenSize().width;
    this.setResizable(false);
    this.pack();
  }

  private int getY(String s, int i) {
    try {
      return 99-Integer.parseInt(s.substring(2*i, 2*i+2));
    } catch (Exception e) {
      e.printStackTrace(); return 0;
    }
  }

  private void drawG(int modeN, int maximum, Graphics g) {
    int s = (zoom > 2) ? zoom*(count++)/graph_dist : 0;
    int lasty = ((graph[number][modeN].length()/2 > trans) ? zoom*getY(graph[number][modeN], trans) +1 : zoom*100);
    int lastx = s;
    int max = graph[number][modeN].length()/2;
    mode[modeN].setLabel(modeTitle[modeN] + "(" + max + ")");
    max = Math.min(this.max, max);
    for (int i = trans+1; i < max; i++) {
      try {
        g.drawLine(lastx, lasty, lastx = zoom*(i-trans) + s,
                   lasty = zoom*getY(graph[number][modeN], i)+1);
      } catch(Exception e) { e.printStackTrace(); }
    }
  }

  private void updateNumber() {
    int i = 0;
    while ((i < jn.length) && (!jn[i].substring(2, jn[i].indexOf('_', 3)).equals(name))) i++;
    this.number = i;
  }

  public void updateGraph() { updateGraph(name); }
  private void updateGraph(String name) {
    this.name = name;
    updateNumber();
    if (number < jn.length) {
      this.setTitle(pre_title + name + suf_title);
      count = 0;

      int n = ((mode[0].getState())?1:0) + ((mode[1].getState())?1:0) + ((mode[2].getState())?1:0) -1;

      max = Math.max(Math.max(graph[number][0].length()/2, graph[number][1].length()/2), graph[number][2].length())/2;
      int w0 = Math.min(zoom*(max+2), max_width);
      int h0 = zoom*100 + 2;
      boolean change;
      if (change = (w != w0)) w = w0;
      if (h != h0) { h = h0; change = true; }
      if (change) {
        can.resize(w, h);
        this.pack();
      }

      max = Math.min(w0 / zoom + trans - 2, max);
      intervalLabel.setText("["+trans+", .., "+max+"]");
      pString = "";
      try {
        for (int j = 0; j < 3; j++)
          pString += ((graph[number][j].length() > 1) ? (Integer.parseInt(graph[number][j].substring(graph[number][j].length()-2))+1) : 0) + "% ";
      } catch(Exception e) { e.printStackTrace(); }
      percLabel.setText(pString);

      offscreenImage = createImage(w, h);
      Graphics g = offscreenImage.getGraphics();
      g.setColor(Color.white);
      g.fillRect(0, 0, w, h);

      g.setColor(Color.lightGray);
      for (int x = 0; x <= 4; x++)
        g.drawLine(0, x*25*zoom+1, w, x*25*zoom+1);

      if (mode[0].getState()) {
        g.setColor(Color.red);
        drawG(0, max, g);
      }
      if (mode[1].getState()) {
        g.setColor(Color.green.darker());
        drawG(1, max, g);
      }
      if (mode[2].getState()) {
        g.setColor(Color.blue);
        drawG(2, max, g);
      }
      repaint();
    }
  }

  public boolean action(Event evt, Object what) {
    Object obj = evt.target;
    if(obj instanceof Button) {
      String label = ((Button)obj).getLabel();
      if (label.equals("Zoom in")) {
        if (zoom < 4) {
          zoom++;
          zoomLabel.setText("x"+zoom);
          updateGraph();
        }
      } else if (label.equals("Zoom out")) {
        if (zoom > 1) {
          zoom--;
          zoomLabel.setText("x"+zoom);
          updateGraph();
        }
      } else if (label.equals("Move left")) {
        if (trans < max-step) {
          trans += step;
          updateGraph();
        }
      } else if (label.equals("Move right")) {
        if (trans > 0) {
          trans -= step;
          updateGraph();
        }
      } else if (label.equals("Redraw")) {
        updateGraph(choice.getSelectedItem());
      }
    } else if (obj instanceof Checkbox) {
      updateGraph();
    }
    return false;
  }

  public boolean mouseMove(Event e, int x, int y) {
    if ((number > -1) && (number < jn.length) && (e.target == can)) {
      updateNumber();
      int i = x / zoom+trans-1;
      String s = "";
      try {
        for (int j = 0; j < 3; j++)
          s += (i < graph[number][j].length()/2) ? (Integer.parseInt(graph[number][j].substring(2*i, 2*i+2))+1)+"% " : "0% ";
      } catch(Exception ex) { ex.printStackTrace(); }
      if (i <= max) s += " (#" + i + ")";
      percLabel.setText(s);
    }
    return false;
  }

  public boolean mouseExit(Event e, int x, int y) {
    if (e.target == can)
      percLabel.setText(pString);
    return false;
  }

  public void update(Graphics g) { paint(g); }
  public void paint(Graphics g) {
    if (offscreenImage != null) can.getGraphics().drawImage(offscreenImage,0,0,this);
  }
}
