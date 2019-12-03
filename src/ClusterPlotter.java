import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.System.exit;

public class ClusterPlotter {
    class Tuple {
        double x;
        double y;
        int clusterId; //may not actually be used

        public Tuple() {

        }
        public Tuple(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void setCluster(int clusterId) {
            this.clusterId = clusterId;
        }

        public double dist(Tuple to) {
            return Math.sqrt(Math.pow(this.x - to.x,2)+Math.pow(this.y - to.y,2));
        }
    };

    public void plot(String datafile, String outfile, int k, int width, int height) {
        ImagePlotter plotter = new ImagePlotter();
        plotter.setWidth(width);
        plotter.setHeight(height);

        // Read data file
        List<Tuple> data = readData(datafile);

        Random rand = new Random();

        List<Tuple> centers = new ArrayList<Tuple>();
        for(int i = 0; i<k; i++) {
            centers.add(data.get(rand.nextInt(data.size())));
        }

        for(Tuple item : data) {
            double minDist = 0;
            for(Tuple center : centers) {
                if(item.dist(center) < minDist) {
                    minDist = item.dist(center);
                }
            }
        }

        try {
            plotter.write(outfile);
        } catch (IOException e) {}
    }

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
}
