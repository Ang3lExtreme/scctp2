package scc.serverless;


import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import scc.data.AuctionDAO;
import scc.data.CosmosDBLayer;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */
public class TimerFunction {
	@FunctionName("auction-timer")
	public void cosmosFunction(@TimerTrigger(name = "lowerTimerOnAuctions", schedule = "0 0 */1 * * *") String timerInfo,
			ExecutionContext context) {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosPagedIterable<AuctionDAO> cpi = null;

		cpi = db.getAuctions();

		Iterator<AuctionDAO> it = cpi.iterator();

		while (it.hasNext()) {
			AuctionDAO u = it.next();

			if (u.getStatus() == 1) {

				AuctionDAO a = new AuctionDAO();

				int timeleft = u.getEndTime() - 60;
				
				a.setId(u.getId());
				a.setTitle(u.getTitle());
				a.setDescription(u.getDescription());
				a.setPhotoId(u.getPhotoId());
				a.setOwner(u.getOwner());
				a.setEndTime(timeleft);
				a.setMinimumBid(u.getMinimumBid());
				a.setStatus(u.getStatus());

				if (timeleft <= 0) {
					a.setStatus(0);
				}

				db.upsertAuction(a);
			}

		}
	}
}
