package org.elasticsearch.plugin.kbrdemo.rest;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
//import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.plugin.kbrdemo.utils.UnAuthMessage;
import org.elasticsearch.plugin.kbrdemo.wiring.ThreadLocalRepo;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.RestRequest.Method;
import org.elasticsearch.rest.action.RestToXContentListener;

import java.io.IOException;

public class HelloWorldHandler extends BaseRestHandler implements RestHandler {
	final org.apache.logging.log4j.Logger logger = Loggers.getLogger(getClass());
	
	@Inject
	public HelloWorldHandler(Settings settings, RestController controller) {
		super(settings);
		//将该Handler绑定到某访问路径  
        controller.registerHandler(Method.GET, "/hello/", this);
	}

	protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient nodeClient) throws IOException {
		//bypass action auth filter, just do rest without any auth
		ThreadLocalRepo.channel.set(null);
		logger.info("start prepare request ... ");
		return channel -> {
			nodeClient.execute(MyRestAction.INSTANCE,
					new MyRestRequest(),
					new RestToXContentListener<MyRestResponse>(channel));
		};
    }
}
