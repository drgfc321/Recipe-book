package com.recipebook.views;

import com.recipebook.service.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Route("oauth-callback")
@PageTitle("Signing in... | Recipe Book")
public class OAuthCallbackView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthCallbackView.class);

    private final AuthService authService;

    public OAuthCallbackView(AuthService authService) {
        this.authService = authService;
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Paragraph loading = new Paragraph(getTranslation("oauth.signing_in", "Signing in..."));
        add(loading);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();

        List<String> errorParams = params.get("error");
        if (errorParams != null && !errorParams.isEmpty()) {
            LOG.warn("OAuth callback received error: {}", errorParams.get(0));
            event.forwardTo(LoginView.class);
            return;
        }

        List<String> tokenParams = params.get("token");
        if (tokenParams == null || tokenParams.isEmpty()) {
            LOG.warn("OAuth callback: no token provided");
            event.forwardTo(LoginView.class);
            return;
        }

        try {
            String token = tokenParams.get(0);
            authService.loginWithOAuthToken(token);
            LOG.info("OAuth login successful, redirecting to dashboard");
            UI.getCurrent().getPage().getHistory().replaceState(null, "oauth-callback");
            event.forwardTo(DashboardView.class);
        } catch (Exception e) {
            LOG.error("OAuth callback failed: {}", e.getMessage());
            event.forwardTo(LoginView.class);
        }
    }
}
