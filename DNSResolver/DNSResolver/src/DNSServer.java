import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class DNSServer {
    DatagramSocket clientSocket_;
    byte[] receiveBuffer_ = new byte[512];
    DatagramPacket receivePacket_;

    public DNSServer() throws IOException {
        clientSocket_ = new DatagramSocket(8053);
    }

    public static void main(String[] args) throws IOException {
        DNSServer server = new DNSServer();
        DNSCache cache = new DNSCache();
        ArrayList<DNSRecord> responses = new ArrayList<>();
        byte[] google = new byte[512];

        while (true) {
            server.receivePacket_ = new DatagramPacket(server.receiveBuffer_, server.receiveBuffer_.length);
            try {
                server.clientSocket_.receive(server.receivePacket_);
                server.receiveBuffer_ = server.receivePacket_.getData();
                DNSMessage messageReq = DNSMessage.decodeMessage(server.receiveBuffer_);

                    if (cache.containsRequest(messageReq.questions_.get(0))) {
                        System.out.println("request in CACHE!");
                        DNSRecord answer = cache.getRecordFromCache(messageReq.questions_.get(0));
                        responses.add(answer);
                    } else {
                        System.out.println("request not in cache need to request from google");
                        DatagramPacket googleRequestPacket = new DatagramPacket(server.receiveBuffer_, server.receiveBuffer_.length, InetAddress.getByName("8.8.8.8"), 53);
                        server.clientSocket_.send(googleRequestPacket);

                        DatagramPacket googleResponsePacket = new DatagramPacket(google, google.length);
                        server.clientSocket_.receive(googleResponsePacket);
                        google = googleResponsePacket.getData();
                        System.out.println("Google response packet received!");

                        // now to decode stuff from google
                        DNSMessage googleMessage = DNSMessage.decodeMessage(google);
                        System.out.println("Google message decoded!");
                         //now that we decoded the google message we can check if we have responses
                        if (googleMessage.answers_.size() > 0) {
                            responses.add(googleMessage.answers_.get(0)); // add to responses
                            cache.addToCache(messageReq.questions_.get(0), googleMessage.answers_.get(0)); // add to cache
                        }
                        DatagramPacket clientPacket = new DatagramPacket(google, google.length, server.receivePacket_.getAddress(),server.receivePacket_.getPort());
                        server.clientSocket_.send(clientPacket);
                }

                DNSMessage msgResponse = DNSMessage.buildResponse(messageReq, responses);
                DatagramPacket responsePacket = new DatagramPacket(msgResponse.toBytes(), msgResponse.toBytes().length, server.receivePacket_.getAddress(), server.receivePacket_.getPort());
                server.clientSocket_.send(responsePacket); // send response back to client socket

            } catch (IOException e) {
                System.out.println("Error: " + e);
                break;
            }
        }
        server.clientSocket_.close();
    }
}
