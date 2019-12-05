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
        Tuple centroid;

        public Cluster() {
        }

        public Cluster(int id, Color col, Tuple centroid) {
            this.clusterId = id;
            this.clusterColor = col;
            this.clusterArray = new ArrayList<>();
            this.centroid = centroid;
        }

        public void setClusterId(int id) {
            this.clusterId = id;
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

        public Tuple getCentroid() {
            return this.centroid;
        }

        Tuple findMeanCentroid() {
            double total_x = 0;
            double total_y = 0;

            //Find average x and y of the cluster
            for(Tuple item : clusterArray) {
                total_x += item.x;
                total_y += item.y;
            }

            this.centroid = new Tuple(total_x/clusterArray.size(), total_y/clusterArray.size());
            return new Tuple(total_x/clusterArray.size(), total_y/clusterArray.size());
        }
    }

    public void plot(String datafile, String outfile, int k, int width, int height) {

        // Read data file
        List<Tuple> data = readData(datafile);

        //We'll need rand for generating colors and our "seed" centroids
        Random rand = new Random();

        //Organize data into clusters based on randomly chosen centroids
        List<Cluster> clusters = new ArrayList<Cluster>(k);
        for (int i = 0; i < k; i++) {
            //Generate cluster color
            Color randomColor = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            //Add the cluster to our cluster list
            clusters.add(new Cluster(i, randomColor, data.get(rand.nextInt(data.size()))));
        }

        //Debug output of centroids
        System.out.println("Centers:");
        for (Cluster cluster : clusters) {
            cluster.getCentroid().print();
        }
        System.out.println("\n"); //newline

        //Loop-Persistent error variables
        double percentError = 0;
        double error = Double.POSITIVE_INFINITY;
        Boolean repeat = false;
        do {
            repeat = false;
            //For each item, calculate the distance to all centers
            //and mark the closest center's cluster
            for (Tuple item : data) {
                double minDist = Double.POSITIVE_INFINITY;
                Cluster cluster = new Cluster();
                for (int i = 0; i < k; i++) {
                    Tuple clusterCentroid = clusters.get(i).getCentroid();
                    if (item.dist(clusterCentroid) < minDist) {
                        //We found a new minimum. Mark it and repeat
                        minDist = item.dist(clusterCentroid);
                        cluster = clusters.get(i);
                    }
                }
                //Add the item to its closest cluster center
                cluster.add(item);
            }

            //Console output of clusters for debugging
            for (Cluster cluster : clusters) {
                System.out.println("[");
                for (Tuple item : cluster.getList()) {
                    item.print();
                }
                System.out.println("\n],\nMean Center:");
                cluster.findMeanCentroid().print();
                System.out.println("");
            }

            //Calculate error level
            double distFromCenterTotal = 0;
            int totalNodes = 0;
            for (Cluster cluster : clusters) {
                //this function also changes the Cluster's centroid to the found center
                Tuple clusterCenter = cluster.findMeanCentroid();
                for (Tuple item : cluster.getList()) {
                    distFromCenterTotal += clusterCenter.dist(item);
                    totalNodes += cluster.size();
                }
            }

            //Compute the new error 𝑛𝑒 as the average distance of each data point from the new center of its cluster
            double newError = distFromCenterTotal / totalNodes;

            //Calculate the percentage error as |ne - e| / e
            percentError = Math.abs(newError - error) / error; //produces NaN on first pass??
            //Debug output
            System.out.println("New Error: " + newError + " distFromCenterTotal: " + distFromCenterTotal + " totalNodes: " + totalNodes + " Error: " + error + " Percent Error: " + percentError);

            //If our percent error is greater than a small delta, set error = newError and repeat the loop
            if(percentError > 0.1 || Double.isNaN(percentError)) {
                error = newError;
                repeat = true;
            }

        } while(repeat == true); //error > 0.1);

        //Draw a pretty picture
        ImagePlotter plotter = new ImagePlotter();
        plotter.setWidth(width);
        plotter.setHeight(height);
        plotter.setDimensions(-400,400,-400,400);
        //For each cluster, plot each point as the cluster color
        for(Cluster cluster : clusters) {
            for(int j = 0; j < cluster.size(); j++) {
                //System.out.print("Adding point");
                //cluster.get(j).print();
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
