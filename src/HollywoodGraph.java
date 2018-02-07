import java.util.*;

/**
 * Joey Ferguson
 * 7 February 2018
 */

public class HollywoodGraph {
    private SymbolGraph SG;                         // initializes symbol graph
    private CC CC;                                  // initializes connected components
    private Stack<String> components;

    private HollywoodGraph() {
        SG = new SymbolGraph("movies.txt","/");     // sets symbol graph to txt file
        CC = new CC(SG.G());                        // sets connected components the graph of the symbol graph

        components = new Stack<>();
        int[] connected = new int[CC.count()];

        RedBlackBST<String, Integer> RBT = new RedBlackBST<>();

        String[] lines = (new In("movies.txt")).readAllLines();
        for (String line : lines) {
            String[] parts = line.split("/");
            String movie = null;
            for (String part : parts) {
                if (movie == null) {
                    movie = part;
                } else {
                    RBT.put(part, 0);
                }
            }
        }

        for (String actorString : RBT.keys()) {
            if (connected[CC.id(SG.index(actorString))] == 0) {
                components.push(actorString);
                connected[CC.id(SG.index(actorString))] = 1;
            }
        }
    }

    private class Actor implements HollywoodActor {
        String name;                                // initializes name
        BreadthFirstPaths BFP;                      // initializes BFP
        double[] distArray;                         // keeps track of average, maximum, and actor maximum
        LinkedQueue<Integer> actorQueue;            // iterates through actors

        private Actor(String name) {
            this.name = name;
            BFP = new BreadthFirstPaths(SG.G(), SG.index(name));
            actorQueue = new LinkedQueue<>();

            for (int v = 0; v < SG.G().V(); v++) {
                if (CC.connected(v, SG.index(name))) {
                    actorQueue.enqueue(v);
                }
            }
            distArray = calcDist();
        }

        private double[] calcDist() {
            double distArray[] = new double[3];

            double avg, max, accum, actorMax, dist;
            max = 0.0; accum = 0.0; actorMax = 0.0;

            for (int i : actorQueue){
                String actor = SG.name(i);
                dist = BFP.distTo(i);
                dist -= dist/2;
                if (dist > max) {
                    max = dist;
                    actorMax = (double) SG.index(actor);
                }
                accum += dist;
            }
            avg = accum / actorQueue.size();

            distArray[0] = avg;
            distArray[1] = max;
            distArray[2] = actorMax;
            return distArray;
        }

        public String name() { return name; }

        public Iterable<String> movies() {
            Stack<String> movies = new Stack<>();

            int actor = SG.index(name);
            for (int i : SG.G().adj(actor)) {
                movies.push(SG.name(i));
            }
            return movies;
        }

        public double distanceAverage() {
            return distArray[0];
        }

        public double distanceMaximum() {
            return distArray[1];
        }

        public String actorMaximum() {
            return SG.name((int)distArray[2]);
        }

        public Iterable<String> actorPath(String name) {
            int actorIndex = SG.index(name);
            LinkedQueue<String> actorQueue = new LinkedQueue<>();

            if (BFP.hasPathTo(actorIndex)) {
                boolean flag = true;
                for (int i : BFP.pathTo(actorIndex)) {
                    if (flag) {
                        actorQueue.enqueue(SG.name(i));
                        flag = false;
                    }
                    else flag = true;
                }
            } else return null;
            return actorQueue;
        }

        public double actorPathLength(String name) {
            if (BFP.hasPathTo(SG.index(name))) {
                double dist = BFP.distTo(SG.index(name));
                return (dist - dist/2);
            }
            else return Double.POSITIVE_INFINITY;
        }

        public Iterable<String> moviePath(String name) {
            int actorIndex = SG.index(name);
            LinkedQueue<String> movieQueue = new LinkedQueue<>();

            if (BFP.hasPathTo(actorIndex)) {
                for (int i : BFP.pathTo(actorIndex)) {
                    movieQueue.enqueue(SG.name(i));
                }
            }
            else return null;
            return movieQueue;
        }
    }

    private HollywoodActor getActorDetails(String name) {
        return new Actor(name);
    }

    private Iterable<String> connectedComponents() {
        return components;
    }

    private int connectedComponentsCount() {
        return CC.count();
    }

    private int connectedActorsCount(String name) {
        if (name == null) { throw new NoSuchElementException("Name cannot be null"); }
        return CC.size(SG.index(name))/2 + 1;
    }

    private double hollywoodNumber(String name) {
        if (name == null) { throw new NoSuchElementException("Name cannot be null"); }
        return (new Actor(name)).distanceAverage();
    }

    static public void main(String[] args) {
        HollywoodGraph HG = new HollywoodGraph();

        // Prints out tests for functions
        StdOut.println(HG.getActorDetails("Bacon, Kevin").name());
        StdOut.println(HG.getActorDetails("Bacon, Kevin").movies());
        StdOut.println(HG.getActorDetails("Bacon, Kevin").distanceAverage());
        StdOut.println(HG.getActorDetails("Bacon, Kevin").distanceMaximum());
        StdOut.println(HG.getActorDetails("Bacon, Kevin").actorMaximum());
        StdOut.println(HG.getActorDetails("Ford, Harrison (I)").distanceMaximum());
        StdOut.println(HG.getActorDetails("Fisher, Carrie").distanceMaximum());
        StdOut.println(HG.getActorDetails("Hamill, Mark (I)").distanceMaximum());
        StdOut.println(HG.getActorDetails("Chan, Jackie (I)").actorMaximum());
        StdOut.println(HG.getActorDetails("Chan, Jackie (I)").actorPath("Bacon, Kevin"));
        StdOut.println(HG.getActorDetails("Bacon, Kevin").actorPathLength("Lloyd, Christopher (I)"));
        StdOut.println(HG.getActorDetails("Ford, Harrison (I)").actorPathLength("Lloyd, Christopher (I)"));
        StdOut.println(HG.getActorDetails("Fisher, Carrie").actorPathLength("Lloyd, Christopher (I)"));
        StdOut.println(HG.getActorDetails("Hamill, Mark (I)").actorPathLength("Lloyd, Christopher (I)"));
        StdOut.println(HG.getActorDetails("Kidman, Nicole").moviePath("Bacon, Kevin"));

        StdOut.println(HG.connectedComponents());
        StdOut.println(HG.connectedComponentsCount());
        StdOut.println(HG.connectedActorsCount("Bacon, Kevin"));
        StdOut.println(HG.connectedActorsCount("Fisher, Carrie"));

        StdOut.println(HG.hollywoodNumber("Bacon, Kevin"));
        StdOut.println(HG.hollywoodNumber("Ford, Harrison (I)"));
        StdOut.println(HG.hollywoodNumber("Fisher, Carrie"));
        StdOut.println(HG.hollywoodNumber("Hamill, Mark (I)"));
        StdOut.println(HG.hollywoodNumber("Damon, Matt"));
        StdOut.println(HG.hollywoodNumber("Hanks, Tom"));
        StdOut.println(HG.hollywoodNumber("Depp, Johnny"));
        StdOut.println(HG.hollywoodNumber("Freeman, Morgan (I)"));
        StdOut.println(HG.hollywoodNumber("Jackson, Samuel L."));

        // Prints out how many times it gets actor details in 1 minute
        Stopwatch timer = new Stopwatch();
        int count = 0;
        while ( timer.elapsedTime() <= 10 ) {
            HG.getActorDetails("Bacon, Kevin");
            count++;
        }
        StdOut.println(count*6);
    }
}
