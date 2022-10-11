package edu.utah.kmm.va.cdshooks.demo.km;

import org.hl7.fhir.r4.model.Observation;
import org.opencds.hooks.engine.api.CdsHooksEvaluationContext;
import org.opencds.hooks.engine.api.CdsHooksExecutionEngine;
import org.opencds.hooks.model.request.CdsRequest;
import org.opencds.hooks.model.response.*;

import java.util.UUID;

public class LungCancerSDMHook implements CdsHooksExecutionEngine {

    @Override
    public CdsResponse evaluate(
            CdsRequest cdsRequest,
            CdsHooksEvaluationContext cdsHooksEvaluationContext) {
        CdsResponse response = new CdsResponse();
        Card card = new Card();
        card.setDetail("CDSHook Demo for LCS SDM");
        card.setIndicator(Indicator.WARNING);
        card.setUuid(newUUID());
        response.addCard(card);

        Suggestion suggestion = new Suggestion();
        suggestion.setUuid(newUUID());
        card.addSuggestion(suggestion);

        Action action = new Action();
        Observation obs = new Observation();
        action.setResource(obs);
        action.setType(ActionType.CREATE);
        suggestion.addAction(action);

        return response;
    }

    private String newUUID() {
        return UUID.randomUUID().toString();
    }

}
