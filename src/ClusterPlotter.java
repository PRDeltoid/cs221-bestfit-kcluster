import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.System.exit;

public class ClusterPlotter {

    //A group of clustered points and the error value of their clustering
    class Sample {
        List<Cluster> clusterSet;
        double percentError = 0.0;

        public Sample(List<Cluster> clusterSet, double percentError) {
            this.clusterSet = clusterSet;
            this.percentError = percentError;
        }
    }

    //A simple (x,y) tuple used to represent points and centroids on a graph
    class Tuple {
        double x;
        double y;

        public Tuple(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double dist(Tuple to) {
            return Math.sqrt(Math.pow(this.x - to.x,2)+Math.pow(this.y - to.y,2));
        }

        public void print() {
            System.out.print("(" + this.x +", "+ this.y +") ");
        }
    }

    //A grouping of points which this plotter attempts to optimize around a center
    class Cluster {
        Color clusterColor;
        List<Tuple> clusterArray;
        Tuple centroid;

        public Cluster() {
        }

        public Cluster(Color col, Tuple centroid) {
            this.clusterColor = col;
            this.clusterArray = new ArrayList<>();
            this.centroid = centroid;
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

    //Core logic. This will plot the data as an image
    public void plot(String datafile, String outfile, int k, int numSamples, int width, int height) {

        //Our starting (empty) list of RANSAC samples
        List<Sample> samples = new ArrayList<Sample>();

        // Read data file
        List<Tuple> data = readData(datafile);

        //We'll need rand for generating colors and our "seed" centroids
        Random rand = new Random();

        //Gather numSamples amount of sample clusters for RANSAC
        for(int z = 0; z < numSamples; z++) {
            //Organize data into clusters based on randomly chosen centroids
            List<Cluster> clusters = new ArrayList<Cluster>(k);
            for (int i = 0; i < k; i++) {
                //Generate cluster color
                Color randomColor = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
                //Add the cluster to our cluster list
                clusters.add(new Cluster(randomColor, data.get(rand.nextInt(data.size()))));
            }

            /*Debug output of centroids
            System.out.println("Centers:");
            for (Cluster cluster : clusters) {
                cluster.getCentroid().print();
            }
            System.out.println("\n"); */

            //Loop-Persistent error variables
            double percentError = 0;
            double error = Double.POSITIVE_INFINITY;
            Boolean repeat = false;

            //Core k-means cluster loop
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
                //If percentError is NaN, we're on our first past. Just set error=newError and repeat
                if (percentError > 0.1 || Double.isNaN(percentError)) {
                    error = newError;
                    repeat = true;
                }

            } while (repeat == true); //error > 0.1);

            //Add to our RANSAC sack (heh)
            samples.add(new Sample(clusters, error));
        }

        //RANSAC sampling
        Sample winnerSample = samples.get(0);
        for(Sample sample : samples) {
            if(sample.percentError < winnerSample.percentError) {
                winnerSample = sample;
            }
        }

        //Draw a pretty picture
        ImagePlotter plotter = new ImagePlotter();
        plotter.setWidth(width);
        plotter.setHeight(height);
        plotter.setDimensions(-400,400,-400,400);
        //For each cluster, plot each point as the cluster color
        for(Cluster cluster : winnerSample.clusterSet) {
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

    //Helper function for reading the datafile into a list of tuples
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
