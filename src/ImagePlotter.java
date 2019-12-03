import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * This class is a plotter of data onto an image.
 * It provides operations to add points, lines and
 * circles to draw on the image.
 *
 * It is also possible to set the size of the image to
 * be created, along with the range of the data that
 * is provided to it.
 */
public class ImagePlotter {
  private List<Integer> points;
  private List<Color> pointColors;
  private List<Integer> lines;
  private List<Color> lineColors;
  private List<Integer> circles;
  private List<Color> circleColors;
  private int xmin;
  private int xmax;
  private int ymin;
  private int ymax;
  private final int pointSize;
  private int width;
  private int height;

  public ImagePlotter() {
    reset();
    pointSize = 3;
    width = height = 500;
  }

  /**
   * Add a point to be drawn on the image
   * @param x
   * @param y
   */
  public void addPoint(int x, int y) {
    points.add(x);
    points.add(y);
    pointColors.add(Color.BLACK);
  }

  /**
   * Add a point to be drawn on the image with the specific color
   * @param x
   * @param y
   * @param col
   */
  public void addPoint(int x, int y,Color col) {
    points.add(x);
    points.add(y);
    pointColors.add(col);
  }

  /**
   * Add a line to be drawn on the image
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   */
  public void addLine(int x1, int y1, int x2, int y2) {
    lines.add(x1);
    lines.add(y1);
    lines.add(x2);
    lines.add(y2);
    lineColors.add(Color.RED);
  }

  /**
   * Add a line to be drawn on to the image with the specified color
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @param col
   */
  public void addLine(int x1, int y1, int x2, int y2,Color col) {
    lines.add(x1);
    lines.add(y1);
    lines.add(x2);
    lines.add(y2);
    lineColors.add(col);
  }

  /**
   * Add a circle to be drawn on to the image
   * @param x
   * @param y
   * @param radius
   */
  public void addCircle(int x, int y, int radius) {
    circles.add(x);
    circles.add(y);
    circles.add(radius);
    circleColors.add(Color.GREEN);
  }

  /**
   * Add a circle to be drawn on to the image with the specified color
   * @param x
   * @param y
   * @param radius
   * @param col
   */
  public void addCircle(int x, int y, int radius,Color col) {
    circles.add(x);
    circles.add(y);
    circles.add(radius);
    circleColors.add(col);
  }

  /**
   * Set the range in which all the added points, circles and lines lie. This
   * provides the range of the data as added to this plotter
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   */
  public void setDimensions(int xmin, int xmax, int ymin, int ymax) {
    this.xmin = xmin;
    this.xmax = xmax;
    this.ymin = ymin;
    this.ymax = ymax;

    //modify it to retain aspect ratio
    double aspectRatio = (double)width/height;
    double w = xmax-xmin;
    double h = ymax-ymin;
    if (h*aspectRatio < w) {
      h = w/aspectRatio;
    }
    else {
      w = h*aspectRatio;
    }
    this.xmin = (int)(0.5*(xmin+xmax) - 0.5*w);
    this.xmax = (int)(0.5*(xmin+xmax) + 0.5*w);
    this.ymin = (int)(0.5*(ymin+ymax) - 0.5*h);
    this.ymax = (int)(0.5*(ymin+ymax) + 0.5*h);

  }

  /**
   * Draw all the shapes added thus far to an image and save it to the
   * specific path
   * @param path
   * @throws IOException
   */
  public void write(String path) throws IOException {
    BufferedImage image = new BufferedImage(width,height,BufferedImage
            .TYPE_INT_ARGB);

    Graphics2D g2d = (Graphics2D) image.getGraphics();

    g2d.setColor(Color.WHITE);
    g2d.fillRect(0,0,width,height);
    AffineTransform mat = new AffineTransform();
    mat.concatenate(AffineTransform.getTranslateInstance(0, this.height));
    mat.concatenate(AffineTransform.getScaleInstance(1, -1));

    mat.concatenate(AffineTransform
            .getScaleInstance(
            (double) this.width / (xmax - xmin),
            (double) this.height / (ymax - ymin)));
    mat.concatenate(
            AffineTransform.getTranslateInstance(-xmin, -ymin));


    g2d.setTransform(mat);

    for (int i = 0; i < points.size(); i += 2) {
      g2d.setColor(pointColors.get(i/2));
      g2d.fillOval(points.get(i) - pointSize, points.get(i + 1) - pointSize,
              2 * pointSize, 2 * pointSize);
    }


    for (int i = 0; i < lines.size(); i += 4) {
      g2d.setColor(lineColors.get(i/4));
      g2d.drawLine(lines.get(i), lines.get(i + 1), lines.get(i + 2), lines.get(i + 3));
    }


    for (int i = 0; i < circles.size(); i += 3) {
      int size = circles.get(i + 2);
      g2d.setColor(circleColors.get(i/3));
      g2d.drawOval(circles.get(i) - size, circles.get(i + 1) - size, 2 * size,
              2 * size);
    }

    String imageformat = path.substring(path.indexOf(".")+1);
      ImageIO.write(
              image,
              imageformat,
              new FileOutputStream(path));

  }

  /**
   * Reset this plotter. All shapes are deleted as a result of resetting.
   */
  public void reset() {
    points = new ArrayList<Integer>();
    lines = new ArrayList<Integer>();
    circles = new ArrayList<Integer>();
    pointColors = new ArrayList<Color>();
    lineColors = new ArrayList<Color>();
    circleColors = new ArrayList<Color>();
  }

  /**
   * Set the width of the image that is created by this plotter
   * @param w
   */
  public void setWidth(int w) {
    width = w;
  }

  /**
   * Set the height of the image that is created by this plotter
   * @param h
   */

  public void setHeight(int h) {
    height = h;
  }
}
