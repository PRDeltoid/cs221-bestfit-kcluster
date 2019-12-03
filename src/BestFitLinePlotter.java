import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class BestFitLinePlotter {
    class Tuple {
        double x;
        double y;

        public Tuple() {

        }
        public Tuple(double x, double y) {
            this.x = x;
            this.y = y;
        }
    };

    class Line {
        double a;
        double b;
        double c;

        public Line () {

        }
        public Line(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    };

    List<Tuple> readData(String datafile) {
        List<Tuple> data = new ArrayList<Tuple>();
        try {
            //Read data
            FileReader fileReader = new FileReader(datafile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String [] split = line.split(" ");
                Tuple tuple = new Tuple(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
                data.add(tuple);
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println(e);
            exit(1);
        }
       return data;
    }

    public void plot(String datafile, String outfile, int width, int height) {
        // Init plotter
        ImagePlotter plotter = new ImagePlotter();
        plotter.setWidth(width);
        plotter.setHeight(height);

        // Read data file
        List<Tuple> data = readData(datafile);

        // Find best fit line
        Line myLine = findBestFitLine(data);

        // Plot the line
        drawLine(myLine, plotter);

        // Plot the points
        plotter.setDimensions(-400,400,-400,400);
        for(Tuple item : data) {
            plotter.addPoint((int) item.x, (int) item.y);
        }
        try {
            plotter.write(outfile);
        } catch (IOException e) {}

        }

        void drawLine(Line line, ImagePlotter plotter) {
            double x1 = -400;
            double x2 = 400;

            double y1 = -1*(line.a/line.b)*x1+(-1*(line.c/line.b));//-1*(line.a*x1+line.c)/line.b;
            double y2 = -1*(line.a/line.b)*x2+(-1*(line.c/line.b));     //-1*(line.a*x2+line.c)/line.b;

            System.out.println("Drawing line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");

            plotter.addLine((int) x1, (int) y1, (int) x2, (int) y2);
        }

        Line findBestFitLine(List<Tuple> data) {
            //get averages
            double xbar = 0, ybar = 0;
            for(Tuple item : data) {
                xbar += item.x;
                ybar += item.y;
            }
            xbar = xbar / data.size();
            ybar = ybar / data.size();

            //Find second derivs
            double syy = 0;
            double sxx = 0;
            double sxy = 0;
            for(Tuple item : data) {
                syy += (item.y - ybar) * (item.y - ybar);
                sxx += (item.x - xbar) * (item.x - xbar);
                sxy += (item.x - xbar) * (item.y - ybar);
            }

            //Find d
            double d = (2*sxy)/(sxx-syy);

            //Find theta
            double theta = Math.atan(d);

            //Adjust theta
            if ((syy - sxx) * Math.cos(theta) - (2 * sxy * Math.sin(theta)) < 0) {
                theta = theta + 180;
            }

            //Find a
            double a = Math.cos(theta/2);
            double b = Math.sin(theta/2);
            double c = (-1 * (a) * xbar) - (b*ybar);

            System.out.println("Best Fit Line: " + a + "x + " + b +"y + " + c + " = 0\n");
            System.out.println("theta: " + theta + " xbar: " + xbar + " ybar:" + ybar + " syy:" + syy + " sxx:" + sxx + " sxy: " + sxy);

            return new Line(a,b,c);
        }
}
