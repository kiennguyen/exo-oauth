/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package net.oauth.example.consumer;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.example.consumer.service.ExoOAuth3LeggedConsumerService;
import net.oauth.server.OAuthServlet;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Kien
 *          nguyenanhkien2a@gmail.com
 * Dec 3, 2010 
 */
public class ExoOAuth3LeggedCallback extends HttpServlet {

    public static final String PATH = "/OAuth/ExoOAuth3LeggedCallback";

    protected final Logger log = Logger.getLogger(getClass().getName());
    
    private final ExoOAuth3LeggedConsumerService oauthService = new ExoOAuth3LeggedConsumerService();

    /**
     * Exchange an OAuth request token for an access token, and store the latter
     * in cookies.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        OAuthConsumer consumer = null;
        final OAuthMessage requestMessage = OAuthServlet.getMessage(
              request, null);
        final String consumerName = requestMessage.getParameter("consumer");
        try {
            requestMessage.requireParameters("consumer");
            consumer = ExoOAuthConsumerStorage.getConsumer(consumerName);
            final CookieMap cookies = new CookieMap(request, response);
            final OAuthAccessor accessor = ExoOAuthUtils.newAccessor(consumer,
                    cookies);
            final String expectedToken = accessor.requestToken;
            String requestToken = requestMessage.getParameter(OAuth.OAUTH_TOKEN);
            if (requestToken == null || requestToken.length() <= 0) {
                log.warning(request.getMethod() + " "
                        + OAuthServlet.getRequestURL(request));
                requestToken = expectedToken;
                if (requestToken == null) {
                    OAuthProblemException problem = new OAuthProblemException(OAuth.Problems.PARAMETER_ABSENT);
                    problem.setParameter(OAuth.Problems.OAUTH_PARAMETERS_ABSENT, OAuth.OAUTH_TOKEN);
                    throw problem;
                }
            } else if (!requestToken.equals(expectedToken)) {
                OAuthProblemException problem = new OAuthProblemException("token_rejected");
                problem.setParameter("oauth_rejected_token", requestToken);
                problem.setParameter("oauth_expected_token", expectedToken);
                throw problem;
            }
            List<OAuth.Parameter> parameters = null;
            String verifier = requestMessage.getParameter(OAuth.OAUTH_VERIFIER);
            if (verifier != null) {
                parameters = OAuth.newList(OAuth.OAUTH_VERIFIER, verifier);
            }
            OAuthMessage result = ExoOAuth3LeggedConsumerService.CLIENT.getAccessToken(accessor, null, parameters);
            if (accessor.accessToken != null) {
                String returnTo = requestMessage.getParameter("returnTo");
                if (returnTo == null) {
                    returnTo = request.getContextPath(); // home page
                }
                cookies.remove(consumerName + ".requestToken");
                cookies
                        .put(consumerName + ".accessToken",
                                accessor.accessToken);
                cookies
                        .put(consumerName + ".tokenSecret",
                                accessor.tokenSecret);
                throw new RedirectException(returnTo);
            }
            OAuthProblemException problem = new OAuthProblemException(OAuth.Problems.PARAMETER_ABSENT);
            problem.setParameter(OAuth.Problems.OAUTH_PARAMETERS_ABSENT, OAuth.OAUTH_TOKEN);
            problem.getParameters().putAll(result.getDump());
            throw problem;
        } catch (Exception e) {
            oauthService.handleException(e, request, response, consumerName);
        }
    }

    private static final long serialVersionUID = 1L;

}
