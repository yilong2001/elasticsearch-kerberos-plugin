package org.elasticsearch.plugin.kbrdemo.filter;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.kbrdemo.validation.RequestValidationContext;
import org.elasticsearch.plugin.kbrdemo.utils.SettingsHandler;
import org.elasticsearch.plugin.kbrdemo.utils.ReponseUtil;
import org.elasticsearch.plugin.kbrdemo.validation.ValidationOkListener;
import org.elasticsearch.plugin.kbrdemo.validation.ValidatorCheckResult;
import org.elasticsearch.plugin.kbrdemo.wiring.ThreadLocalRepo;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;

/**
 * Created by yilong on 2017/4/19.
 */
public class AuthActionFilter extends AbstractComponent implements ActionFilter {
    private final ThreadPool threadPool;
    private final IndexNameExpressionResolver indexResolver;
    private ClusterService clusterService;
    private SettingsHandler conf;

    @Inject
    public AuthActionFilter(Settings settings, SettingsHandler conf,
                            ClusterService clusterService, ThreadPool threadPool,
                            IndexNameExpressionResolver indexResolver) {
        super(settings);
        this.conf = conf;
        this.clusterService = clusterService;
        this.threadPool = threadPool;
        this.indexResolver = indexResolver;
    }


    @Override
    public int order() {
        return 0;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> void apply(
            Task task,
            String action,
            Request request,
            ActionListener<Response> listener,
            ActionFilterChain<Request, Response> chain) {
        RestChannel channel = ThreadLocalRepo.channel.get();

        boolean chanNull = channel == null;

        final RestRequest req;
        if (!chanNull) {
            req = channel.request();
        } else {
            req = null;
        }

        boolean reqNull = req == null;

        //logger.info("start to index action filter: "+(reqNull?"null":"notnull")+", "+(chanNull?"null":"notnull")+", "+Thread.currentThread().getName());

        // This was not a REST message
        if (reqNull && chanNull) {
            //logger.info(" >>>>>>>>>>>>>>>> not rest message ");
            chain.proceed(task, action, request, listener);
            return;
        }

        logger.info(req.getHeaders().toString());
        logger.info(req.path());
        logger.info(req.rawPath());
        logger.info(req.getLocalAddress());
        logger.info(req.getRemoteAddress());
        logger.info(" start 5000 ms : " + Thread.currentThread().getName());

        RequestValidationContext rc = new RequestValidationContext(channel, action, request, req, threadPool, conf.settings, conf.kerbUtil);
        ValidatorCheckResult vcr = new ValidatorCheckResult();

        conf.validatorsRepo.check(rc, vcr)
                .exceptionally(throwable -> {
                    System.out.println();
                    logger.info("forbidden request: " + rc + " Reason: " + throwable.getMessage());
                    throwable.printStackTrace();

                    ReponseUtil.sendNotAuthResponse(channel, req, conf.validatorsRepo.onFailResponse(rc, request));
                    return null;
                })
                .thenApply(isOk -> {
                if (isOk) {
                    try {
                        @SuppressWarnings("unchecked")
                        ActionListener<Response> validationListener =
                                (ActionListener<Response>) new ValidationOkListener(request,
                                        (ActionListener<ActionResponse>)listener, rc, conf.validatorsRepo, vcr);
                        chain.proceed(task, action, request, validationListener);
                        return null;
                    } catch (Throwable e) {
                        e.printStackTrace();
                        logger.error(e);
                    }

                    chain.proceed(task, action, request, listener);
                    return null;
                } else {
                    logger.info("forbidden request: " + rc + " Reason: validation failed...... ");
                    ReponseUtil.sendNotAuthResponse(channel, req, conf.validatorsRepo.onFailResponse(rc,request));
                    return null;
                }
            }
        );

        return;
    }
}
