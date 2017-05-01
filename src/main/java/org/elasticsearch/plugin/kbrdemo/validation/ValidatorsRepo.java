package org.elasticsearch.plugin.kbrdemo.validation;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.kbrdemo.authentication.KerberosValidator;
import org.elasticsearch.plugin.kbrdemo.utils.FuturesSequencer;
import org.elasticsearch.plugin.kbrdemo.utils.KerbUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by yilong on 2017/4/23.
 */
public class ValidatorsRepo {
    final static org.apache.logging.log4j.Logger logger = Loggers.getLogger(ValidatorsRepo.class);
    private ArrayList<Validator> validators = new ArrayList<>();
    final private KerbUtil kerbUtil;

    public ValidatorsRepo(Settings settings, KerbUtil kerbUtil) {
        this.kerbUtil = kerbUtil;
        validators.add(new KerberosValidator());
    }

    public CompletableFuture<Boolean> check(RequestValidationContext rc, ValidatorCheckResult vcr) {
        logger.debug("checking request:" + rc);
        return FuturesSequencer.runInSeqUntilConditionIsUndone(
                validators.iterator(),
                validator -> validator.check(rc, vcr),
                checkResult -> {
                    return !checkResult;
                },
                nothing -> {
                    return true;
                }
        );
    }

    public Map<String, String> onOkResponse(RequestValidationContext rvc, ActionRequest ar, ActionResponse response, ValidatorCheckResult vcr) {
        Map<String, String> headers = new HashMap<>();
        validators.forEach(v -> v.onOkResponse(rvc, ar, response, vcr, headers));
        return headers;
    }

    public Map<String, String> onFailResponse(RequestValidationContext rvc, ActionRequest ar) {
        Map<String, String> headers = new HashMap<>();
        validators.forEach(v -> v.onFailResponse(rvc, ar,headers));
        return headers;
    }
}
