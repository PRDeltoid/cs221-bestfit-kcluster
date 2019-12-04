public class Main {

    public static void main(String[] args) {
        BestFitLinePlotter myBestFitLinePlotter = new BestFitLinePlotter();
        //myBestFitLinePlotter.plot("./data/linedata-1.txt","best_fit_line.png", 400, 400);

        ClusterPlotter myClusterPlotter = new ClusterPlotter();
        myClusterPlotter.plot("./data/clusterdata-2.txt","clusterplot.png",3, 400, 400);
    }

}
