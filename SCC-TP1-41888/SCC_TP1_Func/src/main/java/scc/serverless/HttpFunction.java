package scc.serverless;

import java.util.*;

import com.microsoft.azure.functions.annotation.*;

import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;

import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger. These functions can be accessed at:
 * {Server_URL}/api/{route}
 * Complete URL appear when deploying functions.
 */
public class HttpFunction {
	@FunctionName("http-info")
	public HttpResponseMessage info(@HttpTrigger(name = "req", 
										methods = {HttpMethod.GET }, 
										authLevel = AuthorizationLevel.ANONYMOUS,
										route = "serverless/info") 
			HttpRequestMessage<Optional<String>> request,
			final ExecutionContext context) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Headers:\n");
		request.getHeaders().forEach( (k,v) -> { buffer.append( k + "->" + v + "\n");});
		return request.createResponseBuilder(HttpStatus.OK).body(buffer.toString()).build();
	}

	@FunctionName("echo")
	public HttpResponseMessage echo(@HttpTrigger(name = "req", 
										methods = {HttpMethod.GET }, 
										authLevel = AuthorizationLevel.ANONYMOUS, 
										route = "serverless/echo/{text}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("text") String txt, 
				final ExecutionContext context) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:http");
		}
		return request.createResponseBuilder(HttpStatus.OK).body(txt).build();
	}

	@FunctionName("echo-simple")
	public HttpResponseMessage echoSimple(@HttpTrigger(name = "req", 
											methods = {HttpMethod.GET }, 
											authLevel = AuthorizationLevel.ANONYMOUS, 
											route = "serverless/echosimple/{text}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("text") String txt, 
				final ExecutionContext context) {
		return request.createResponseBuilder(HttpStatus.OK).body(txt).build();
	}
}
