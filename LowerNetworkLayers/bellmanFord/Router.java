package msd.benjones;

import java.util.HashMap;
import java.util.Set;

public class Router {

    private HashMap<Router, Integer> distances;
    private String name;
    public Router(String name) {
        this.distances = new HashMap<>();
        this.name = name;
    }

    public void onInit() throws InterruptedException {

		//TODO: IMPLEMENT ME
		//As soon as the network is online,
		//fill in your initial distance table and broadcast it to your neighbors

        for (Neighbor neighbor : Network.getNeighbors(this)) {
            distances.put(neighbor.router, neighbor.cost);
        }

        for (Neighbor neighbor : Network.getNeighbors(this)) {
            Network.sendDistanceMessage(new Message(this, neighbor.router, distances));
        }
    }

    public void onDistanceMessage(Message message) throws InterruptedException {
		//update your distance table and broadcast it to your neighbors if it changed
        boolean changed = false;
        int distanceToSender = distances.get(message.sender);
        for (Neighbor neighbor : Network.getNeighbors(this)) {
            Integer distanceSenderToNeighbor = message.distances.get(neighbor.router);
            if (distanceSenderToNeighbor == null) {
                continue;
            }
            if ((distanceToSender + distanceSenderToNeighbor) < distances.get(neighbor.router)) {
                distances.put(neighbor.router, (distanceToSender + distanceSenderToNeighbor));
                changed = true;
            }
        }

        if (changed) {
            for (Neighbor neighbor : Network.getNeighbors(this)) {
                Network.sendDistanceMessage(new Message(this, neighbor.router, distances));
            }
        }

    }


    public void dumpDistanceTable() {
        System.out.println("router: " + this);
        for(Router r : distances.keySet()){
            System.out.println("\t" + r + "\t" + distances.get(r));
        }
    }

    @Override
    public String toString(){
        return "Router: " + name;
    }
}
