package scc.Main;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.BidDAO;
import scc.Data.DTO.Status;
import scc.Database.CosmosAuctionDBLayer;
import scc.Database.CosmosBidDBLayer;

import java.util.Date;

public class Main {

    public static void main(String[] args) {
        //look for every auction that has expired and change its status to closed
        //get all
       CosmosClient cosmosClient = new CosmosClientBuilder()
                .endpoint(System.getenv("COSMOSDB_URL"))
                .key(System.getenv("COSMOSDB_KEY"))
                .buildClient();

        CosmosAuctionDBLayer cosmos =  new CosmosAuctionDBLayer(cosmosClient);
        CosmosPagedIterable<AuctionDAO> auctions = cosmos.getAuctions();
        CosmosBidDBLayer cosmosBid = new CosmosBidDBLayer(cosmosClient);
        //check if auction has expired
        //if expired change status to closed
        //update auction
        int counter = 0;
        for(AuctionDAO auction : auctions){
            //parse AuctionDAO to Auction
            if(auction.getEndTime().before(new Date()) && auction.getStatus() == Status.OPEN){
                auction.setStatus(Status.CLOSED);
                CosmosPagedIterable<BidDAO> bis = cosmosBid.getBids(auction.getId());
                BidDAO winner = null;
                for(BidDAO bid : bis){
                    if(winner == null || bid.getValue() > winner.getValue()){
                        winner = bid;
                    }
                }
                if(winner != null){
                    auction.setWinnerId(winner.getUserId());
                    cosmos.updateAuction(auction);
                    System.out.println("Auction " + auction.getId() + " has a winner");
                }
                System.out.println("Auction " + auction.getId() + " has no winner");
                cosmos.updateAuction(auction);
                counter++;
            }
        }
        

        System.out.println("Timer trigger function executed at: " + System.currentTimeMillis());
        //System.out.println("Closed " + counter + " auctions");
    }
}
