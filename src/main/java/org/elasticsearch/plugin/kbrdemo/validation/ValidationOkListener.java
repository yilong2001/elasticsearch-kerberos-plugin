package org.elasticsearch.plugin.kbrdemo.validation;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.logging.Loggers;

import java.util.Map;

/**
 * Created by yilong on 2017/4/23.
 */
public class ValidationOkListener implements ActionListener<ActionResponse> {
    private final Logger logger = Loggers.getLogger(getClass());

    private final ActionListener<ActionResponse> baseListener;
    private final ActionRequest request;
    private final RequestValidationContext rc;
    private final ValidatorsRepo validatorsRepo;
    private final ValidatorCheckResult validatorCheckResult;

    public ValidationOkListener(ActionRequest request,
                                ActionListener<ActionResponse> baseListener,
                                RequestValidationContext rc,
                                ValidatorsRepo vs,
                                ValidatorCheckResult vcr) {
        this.request = request;
        this.baseListener = baseListener;
        this.rc = rc;
        this.validatorsRepo = vs;
        this.validatorCheckResult = vcr;
    }

    @Override
    public void onResponse(ActionResponse response) {
        logger.info("ValidationOkListener, on reponse ... begin ");
        boolean shouldContinue = true;
        Map<String, String> headers = validatorsRepo.onOkResponse(rc, request, response, validatorCheckResult);
        rc.addReponseHeader(headers);

        logger.info("ValidationOkListener, on reponse ... end ");

        baseListener.onResponse(response);
    }

    @Override
    public void onFailure(Exception e) {
        boolean shouldContinue = true;
        baseListener.onFailure(e);
    }
}
