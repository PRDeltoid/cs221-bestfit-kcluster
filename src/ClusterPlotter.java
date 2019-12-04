import java.awt.*;
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

        public void print() {
            System.out.print("(" + this.x +", "+ this.y +") ");
        }
    };

    public void plot(String datafile, String outfile, int k, int width, int height) {

        // Read data file
        List<Tuple> data = readData(datafile);

        //Generate our initial (random) centers
        Random rand = new Random();
        List<Tuple> centers = new ArrayList<>(k);
        for(int i = 0; i<k; i++) {
            centers.add(data.get(rand.nextInt(data.size())));
        }

        System.out.println("Centers:");
        for(Tuple center : centers) {
            center.print();
        }
        System.out.println("\n"); //newline

        //Organize data into clusters based on centers
        List<List<Tuple>> clusters =  new ArrayList<List<Tuple>>(k);
        List<Color> clusterColors = new ArrayList<Color>(k);
        for(int i = 0; i<k; i++) {
            clusters.add(new ArrayList<Tuple>());
            clusterColors.add(new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
        }

        for(Tuple item : data) {
            double minDist = Double.POSITIVE_INFINITY;
            int clusterId = -1;
            //For each item, calculate the distance to all centers
            //and mark the closest center's clusterId (our "clusters" index)
            for(int i = 0; i < k; i++) {
                if(item.dist(centers.get(i)) < minDist) {
                    //We found a new minimum. Mark it and repeat
                    minDist = item.dist(centers.get(i));
                    clusterId = i;
                }
            }
            //Add our item to the closest cluster
            clusters.get(clusterId).add(item);
        }

        //Console output of clusters for debugging
        for(List<Tuple> cluster : clusters) {
            System.out.println("[");
            for(Tuple item : cluster) {
                item.print();
            }
            System.out.println("\n],\nReal Center:");
            find_center(cluster).print();
        }

        //Calculate error level
        /*
        for(List<Tuple> cluster : clusters) {
            for (Tuple item : data) {

            }
        }*/

        //Draw a pretty picture
        ImagePlotter plotter = new ImagePlotter();
        plotter.setWidth(width);
        plotter.setHeight(height);
        for(int i = 0; i < k; i++) {
            for(int j = 0; j < clusters.get(i).size(); j++) {
                plotter.addPoint((int) clusters.get(i).get(j).x, (int) clusters.get(i).get(j).y); //, clusterColors.get(i));
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

    Tuple find_center(List<Tuple> cluster) {
        double total_x = 0;
        double total_y = 0;

        //Find average x and y of the cluster
        for(Tuple item : cluster) {
            total_x += item.x;
            total_y += item.y;
        }

        return new Tuple(total_x/cluster.size(), total_y/cluster.size());
    }
}
