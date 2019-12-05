public class Main {

    public static void main(String[] args) {
        //To use BestFitLinePlotter, use the following parameters:
        //  datafile: [String] A link to a text file containing 2-column decimal input
        //  outfile: [String] A link to the image file where the resulting visualization will be saved
        //  width: [int] The width of the resulting outfile image
        //  height: [int] The height of the resulting outfile image

        //Example Usage:
        BestFitLinePlotter myBestFitLinePlotter = new BestFitLinePlotter();
        myBestFitLinePlotter.plot("./data/linedata-1.txt","best_fit_line.png", 400, 400);

        //To use ClusterPlotter, use the following parameters:
        //  datafile: [String] A link to a text file containing 2-column decimal input
        //  outfile: [String] A link to the image file where the resulting visualization will be saved
        //  k: [int] The number of clusters to generate per sampling
        //  numSample: [int] The number of RANSAC samples generate. The lowest error sample will be chosen as the output
        //  width: [int] The width of the resulting outfile image
        //  height: [int] The height of the resulting outfile image

        //Example Usage:
        ClusterPlotter myClusterPlotter = new ClusterPlotter();
        myClusterPlotter.plot("./data/clusterdata-2.txt","clusterplot.png",3, 10, 400, 400);
    }

}
