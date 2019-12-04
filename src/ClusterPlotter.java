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

    class Cluster {
        int clusterId;
        Color clusterColor;
        List<Tuple> clusterArray;

        public Cluster() {
        }

        public Cluster(int id, Color col) {
            this.clusterId = id;
            this.clusterColor = col;
            this.clusterArray = new ArrayList<>();
        }

        public void setClusterId(int id) {
            this.clusterId = id;
        }

        public void setClusterColor(Color col) {
            this.clusterColor = col;
        }

        public void add(Tuple tup) {
            clusterArray.add(tup);
        }

        public int size() {
            return clusterArray.size();
        }

        public Tuple get(int i) {
            return this.clusterArray.get(i);
        }

        public List<Tuple> getList() {
            return this.clusterArray;
        }

        Tuple find_center() {
            double total_x = 0;
            double total_y = 0;

            //Find average x and y of the cluster
            for(Tuple item : clusterArray) {
                total_x += item.x;
                total_y += item.y;
            }

            return new Tuple(total_x/clusterArray.size(), total_y/clusterArray.size());
        }
    }

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
        List<Cluster> clusters = new ArrayList<Cluster>(k);
        for(int i = 0; i<k; i++) {
            Color randomColor = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            clusters.add(new Cluster(i, randomColor));
        }

        for(Tuple item : data) {
            double minDist = Double.POSITIVE_INFINITY;
            Cluster cluster = new Cluster();
            //For each item, calculate the distance to all centers
            //and mark the closest center's cluster
            for(int i = 0; i < k; i++) {
                if(item.dist(centers.get(i)) < minDist) {
                    //We found a new minimum. Mark it and repeat
                    minDist = item.dist(centers.get(i));
                    cluster = clusters.get(i);
                }
            }
            //Add the item to its closest cluster center
            cluster.add(item);
        }

        //Console output of clusters for debugging
        for(Cluster cluster : clusters) {
            System.out.println("[");
            for(Tuple item : cluster.getList()) {
                item.print();
            }
            System.out.println("\n],\nReal Center:");
            cluster.find_center().print();
        }

        //Calculate error level
        //TODO
        /*
        for(List<Tuple> cluster : clusters) {
            for (Tuple item : data) {

            }
        }*/

        //Draw a pretty picture
        ImagePlotter plotter = new ImagePlotter();
        plotter.setWidth(width);
        plotter.setHeight(height);
        plotter.setDimensions(-400,400,-400,400);
        //For each cluster, plot each point as the cluster color
        for(Cluster cluster : clusters) {
            for(int j = 0; j < cluster.size(); j++) {
                System.out.print("Adding point");
                cluster.get(j).print();
                plotter.addPoint((int) cluster.get(j).x, (int) cluster.get(j).y, cluster.clusterColor);
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
