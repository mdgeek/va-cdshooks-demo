package edu.utah.kmm.lcsdemo;

import org.opencds.hooks.engine.api.CdsHooksEvaluationContext;
import org.opencds.hooks.engine.api.CdsHooksExecutionEngine;
import org.opencds.hooks.model.request.CdsRequest;
import org.opencds.hooks.model.response.CdsResponse;

public class LungCancerSDMHook implements CdsHooksExecutionEngine {

    @Override
    public CdsResponse evaluate(
            CdsRequest cdsRequest,
            CdsHooksEvaluationContext cdsHooksEvaluationContext) {
        return null;
    }

}
