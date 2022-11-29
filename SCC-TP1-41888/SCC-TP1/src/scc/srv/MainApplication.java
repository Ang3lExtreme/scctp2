package scc.srv;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import main.resources.AuctionResource;
import main.resources.BidResource;
import main.resources.CacheResource;
import main.resources.ControlResource;
import main.resources.MediaResource;
import main.resources.QuestionResource;
import main.resources.UserResource;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(UserResource.class);
		resources.add(AuctionResource.class);
		resources.add(BidResource.class);
		resources.add(QuestionResource.class);
		resources.add(CacheResource.class);
		singletons.add( new MediaResource());
		singletons.add(new UserResource());
		singletons.add(new AuctionResource());
		singletons.add(new BidResource());
		singletons.add( new QuestionResource());
		singletons.add( new CacheResource());
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
