package edu.utah.kmm.va.cdshooks.demo.km;

import org.hl7.fhir.r4.model.ServiceRequest;
import org.opencds.hooks.engine.api.CdsHooksEvaluationContext;
import org.opencds.hooks.engine.api.CdsHooksExecutionEngine;
import org.opencds.hooks.model.request.CdsRequest;
import org.opencds.hooks.model.response.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.UUID;

public class LungCancerSDMHook implements CdsHooksExecutionEngine {

    @Override
    public CdsResponse evaluate(
            CdsRequest cdsRequest,
            CdsHooksEvaluationContext cdsHooksEvaluationContext) {
        CdsResponse response = new CdsResponse();
        Card card = new Card();
        card.setDetail("CDSHook Demo for LCS SDM");
        card.setSummary("Lung Cancer Screening");
        card.setIndicator(Indicator.WARNING);
        card.setUuid(newUUID());
        response.addCard(card);

        Source source = new Source();
        source.setLabel("Veterans Health Administration");
        source.setIcon(toUrl("https://www.choose.va.gov/_media/img/seals/va-color-seal-black-text.png"));
        source.setUrl(toUrl("https://www.va.gov/health/"));
        card.setSource(source);

        Link link = new Link();
        link.setLabel("Shared Decision Making");
        link.setType(LinkType.SMART);
        link.setUrl(toUrl("https://screenlc.com"));
        card.setLinks(Collections.singletonList(link));

        Suggestion suggestion = new Suggestion();
        suggestion.setUuid(newUUID());
        suggestion.setRecommended(true);
        suggestion.setLabel("Recommend Low Dose CT scan.");
        card.addSuggestion(suggestion);

        Action action = new Action();
        action.setDescription("LDCT");
        ServiceRequest resource = new ServiceRequest();
        action.setResource(resource);
        action.setType(ActionType.CREATE);
        suggestion.addAction(action);

        return response;
    }

    private URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String newUUID() {
        return UUID.randomUUID().toString();
    }

}
