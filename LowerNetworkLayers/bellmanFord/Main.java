package msd.benjones;

public class Main {

    public static void main(String[] args) throws InterruptedException {

//        Network.makeSimpleNetwork(); //use this for testing/debugging
        Network.makeProbablisticNetwork(1000); //use this for the plotting part
        Network.dump();

        Network.startup();
        Network.runBellmanFord();

        System.out.println("done building tables!");
        for(Router r : Network.getRouters()){
            r.dumpDistanceTable();
        }
        System.out.println("total messages: " + Network.getMessageCount());


    }
}
