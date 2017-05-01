package org.elasticsearch.plugin.kbrdemo.wiring;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.*;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.plugin.kbrdemo.utils.SettingsHandler;
import org.elasticsearch.plugin.kbrdemo.filter.AuthActionFilter;
import org.elasticsearch.plugin.kbrdemo.rest.HelloWorldHandler;
import org.elasticsearch.plugin.kbrdemo.rest.MyRestAction;
import org.elasticsearch.plugin.kbrdemo.rest.MyRestTransportAction;
import org.elasticsearch.plugins.*;

//import org.elasticsearch.rest.RestModule;
import org.elasticsearch.rest.*;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;

public class MyKerberosPluginDemo extends Plugin implements ScriptPlugin, ActionPlugin, IngestPlugin, NetworkPlugin {
	final org.apache.logging.log4j.Logger logger = Loggers.getLogger(getClass());
	private final Settings settings;

	public MyKerberosPluginDemo(Settings settings) {
		super();
		this.settings = settings;
	}

	public CompletableFuture<Integer> configFuture() {
		CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> (1));
		return future;
	}

	@Override
	public List<Setting<?>> getSettings() {
		return SettingsHandler.allowedSettings();
	}

	@Override
	public List<Class<? extends ActionFilter>> getActionFilters() {
		return Collections.singletonList(AuthActionFilter.class);
	}

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Collections.singletonList(new ActionHandler(MyRestAction.INSTANCE,
                MyRestTransportAction.class,
                new Class[0]));
    }

    @Override
	public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool,
											   ResourceWatcherService resourceWatcherService, ScriptService scriptService,
											   NamedXContentRegistry xContentRegistry) {

		Collection<Object> fromSup = super.createComponents(client, clusterService, threadPool, resourceWatcherService,
				scriptService, xContentRegistry
		);
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					logger.debug("[==================] checking index..");
					SettingsHandler.getInstance(settings, client).updateSettingsFromIndex(client);
					logger.info("Cluster-wide settings found, overriding elasticsearch.yml");
					executor.shutdown();
				} catch (ElasticsearchException ee) {
					logger.info("[==================] settings not found, Will keep on using elasticearch.yml.");
					executor.shutdown();
				} catch (Throwable t) {
					logger.debug("[==================] index not ready yet..");
					executor.schedule(this, 200, TimeUnit.MILLISECONDS);
				}
			}
		};
		executor.schedule(task, 200, TimeUnit.MILLISECONDS);
		return fromSup;
	}

	@Override
	public List<RestHandler> getRestHandlers(
			Settings settings, RestController restController, ClusterSettings clusterSettings,
			IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter,
			IndexNameExpressionResolver indexNameExpressionResolver, Supplier<DiscoveryNodes> nodesInCluster) {
		return Collections.singletonList(new HelloWorldHandler(settings, restController));
	}

	@Override
	public UnaryOperator<RestHandler> getRestHandlerWrapper(ThreadContext threadContext) {
		return restHandler -> new RestHandler() {
			@Override
			public void handleRequest(RestRequest request, RestChannel channel, NodeClient client) throws Exception {
				// Need to make sure we've fetched cluster-wide configuration at least once. This is super fast, so NP.
				SettingsHandler.getInstance(settings, client);
				ThreadLocalRepo.channel.set(channel);

				logger.info("getRestHandlerWrapper.handleRequest is called... begin, " + Thread.currentThread().getName());
				restHandler.handleRequest(request, channel, client);

				logger.info("getRestHandlerWrapper.handleRequest is called... end");
			}
		};
	}
}
