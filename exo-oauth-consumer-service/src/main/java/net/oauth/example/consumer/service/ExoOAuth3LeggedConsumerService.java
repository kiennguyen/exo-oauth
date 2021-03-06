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
package net.oauth.example.consumer.service;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.ParameterStyle;
import net.oauth.example.consumer.CookieMap;
import net.oauth.example.consumer.ExoOAuth3LeggedCallback;
import net.oauth.example.consumer.ExoOAuthConsumerStorage;
import net.oauth.example.consumer.ExoOAuthMessage;
import net.oauth.example.consumer.ExoOAuthUtils;
import net.oauth.example.consumer.RedirectException;
import net.oauth.server.OAuthServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class operate as service of GateIn, it is compliant OAuth 2.0 specification, it can be called OAuth three legged
 * it used to send OAuth request to endpoint to get data
 * There are many steps for handshake, authentication, authorization between this service and OAuth provider. 
 * See OAuth 2.0 specification for more detail 
 * 
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Kien
 *          nguyenanhkien2a@gmail.com
 * Dec 3, 2010  
 */
public class ExoOAuth3LeggedConsumerService extends ExoOAuth2LeggedConsumerService
{
   public ExoOAuth3LeggedConsumerService() {}
   
   /**
    * Send a request to REST endpoint
    * 
    * @param consumerName name of consumer that was stored in database, service will use this name
    * to query consumer information as key, secret, signature method, OAuth request url, 
    * OAuth authorization url, OAuth access url
    * @param restEndpoint the service url to get data
    * @param request the http servlet request
    * @param response the http servlet response
    * @thows IOException, OAuthException, URISyntaxException
    */
   @Override
   public ExoOAuthMessage send(String consumerName, String restEndpoint, HttpServletRequest request,
      HttpServletResponse response) throws OAuthException, IOException, URISyntaxException
   {
      OAuthConsumer consumer = ExoOAuthConsumerStorage.getConsumer(consumerName);
      OAuthAccessor accessor = getAccessor(request, response, consumer);
      OAuthMessage message = accessor.newRequestMessage(OAuthMessage.GET, restEndpoint, null);

      OAuthMessage responseMessage =
         ExoOAuth2LeggedConsumerService.CLIENT.invoke(message, ParameterStyle.AUTHORIZATION_HEADER);
      return (new ExoOAuthMessage(consumerName, responseMessage));
   }

   /**
    * Send a request to REST endpoint
    * 
    * @param requestMessage An ExoOAuthMessage object that contains neccessary information of request 
    * such as name of consumer that was stored in database, REST endpoint, http request method, etc.
    * @param request the http servlet request
    * @param response the http servlet response
    * @thows IOException, OAuthException, URISyntaxException
    * @return ExoOAuthMessage object
    */
   @Override
   public ExoOAuthMessage send(ExoOAuthMessage requestMessage, HttpServletRequest request,
         HttpServletResponse response) throws OAuthException, IOException, URISyntaxException
   {
      String consumerName = requestMessage.getConsumerName();
      OAuthConsumer consumer = ExoOAuthConsumerStorage.getConsumer(consumerName);
      OAuthAccessor accessor = getAccessor(request, response, consumer);
      OAuthMessage message = accessor.newRequestMessage(requestMessage.getHttpMethod(), requestMessage.getRestEndpoint(), requestMessage.getParameters());

      OAuthMessage responseMessage =
         ExoOAuth2LeggedConsumerService.CLIENT.invoke(message, ParameterStyle.AUTHORIZATION_HEADER);
      return (new ExoOAuthMessage(consumerName, responseMessage));
   }
   
   /**
    * Get the access token and token secret for the given consumer. Get them
    * from cookies if possible; otherwise obtain them from the service
    * provider. In the latter case, throw RedirectException.
    * @throws IOException 
    * @throws URISyntaxException 
    */
   public static OAuthAccessor getAccessor(HttpServletRequest request, HttpServletResponse response,
      OAuthConsumer consumer) throws OAuthException, IOException, URISyntaxException
   {
      CookieMap cookies = new CookieMap(request, response);
      OAuthAccessor accessor = ExoOAuthUtils.newAccessor(consumer, cookies);
      if (accessor.accessToken == null)
      {
         getAccessToken(request, cookies, accessor);
      }
      return accessor;
   }

   /** Remove all the cookies that contain accessors' data. */
   public static void removeAccessors(CookieMap cookies)
   {
      List<String> names = new ArrayList<String>(cookies.keySet());
      for (String name : names)
      {
         if (name.endsWith(".requestToken") || name.endsWith(".accessToken") || name.endsWith(".tokenSecret"))
         {
            cookies.remove(name);
         }
      }
   }

   /**
    * Get a fresh access token from the service provider.
    * @throws IOException 
    * @throws URISyntaxException 
    * 
    * @throws RedirectException
    *             to obtain authorization
    */
   private static void getAccessToken(HttpServletRequest request, CookieMap cookies, OAuthAccessor accessor)
      throws OAuthException, IOException, URISyntaxException
   {
      final String consumerName = (String)accessor.consumer.getProperty("name");
      final String callbackURL = getCallbackURL(request, consumerName);
      List<OAuth.Parameter> parameters = OAuth.newList(OAuth.OAUTH_CALLBACK, callbackURL);
      // Google needs to know what you intend to do with the access token:
      Object scope = accessor.consumer.getProperty("request.scope");
      if (scope != null)
      {
         parameters.add(new OAuth.Parameter("scope", scope.toString()));
      }
      OAuthMessage response = CLIENT.getRequestTokenResponse(accessor, null, parameters);
      cookies.put(consumerName + ".requestToken", accessor.requestToken);
      cookies.put(consumerName + ".tokenSecret", accessor.tokenSecret);
      String authorizationURL = accessor.consumer.serviceProvider.userAuthorizationURL;
      if (authorizationURL.startsWith("/"))
      {
         authorizationURL =
            (new URL(new URL(request.getRequestURL().toString()), request.getContextPath() + authorizationURL))
               .toString();
      }
      authorizationURL = OAuth.addParameters(authorizationURL //
         , OAuth.OAUTH_TOKEN, accessor.requestToken);
      if (response.getParameter(OAuth.OAUTH_CALLBACK_CONFIRMED) == null)
      {
         authorizationURL = OAuth.addParameters(authorizationURL //
            , OAuth.OAUTH_CALLBACK, callbackURL);
      }
      throw new RedirectException(authorizationURL);
   }

   private static String getCallbackURL(HttpServletRequest request, String consumerName) throws IOException
   {
      URL base = new URL(new URL(request.getRequestURL().toString()), //
         request.getContextPath() + ExoOAuth3LeggedCallback.PATH);
      return OAuth.addParameters(base.toExternalForm() //
         , "consumer", consumerName //
         , "returnTo", getRequestPath(request) //
         );
   }

   /** Reconstruct the requested URL path, complete with query string (if any). */
   private static String getRequestPath(HttpServletRequest request) throws MalformedURLException
   {

      URL url = new URL(OAuthServlet.getRequestURL(request));
      StringBuilder path = new StringBuilder(url.getPath());
      String queryString = url.getQuery();
      if (queryString != null)
      {
         path.append("?").append(queryString);
      }
      return path.toString();
   }

   /**
    * The names of problems from which a consumer can recover by getting a
    * fresh token.
    */
   protected static final Collection<String> RECOVERABLE_PROBLEMS = new HashSet<String>();
   static
   {
      RECOVERABLE_PROBLEMS.add("token_revoked");
      RECOVERABLE_PROBLEMS.add("token_expired");
      RECOVERABLE_PROBLEMS.add("permission_unknown");
      // In the case of permission_unknown, getting a fresh token
      // will cause the Service Provider to ask the User to decide.
   }

   /**
    * Handle an exception that occurred while processing an HTTP request.
    * Depending on the exception, either send a response, redirect the client
    * or propagate an exception.
    */
   public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response,
      String consumerName) throws IOException, ServletException
   {
      if (e instanceof RedirectException)
      {
         RedirectException redirect = (RedirectException)e;
         String targetURL = redirect.getTargetURL();
         if (targetURL != null)
         {
            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            response.setHeader("Location", targetURL);
         }
      }
      else if (e instanceof OAuthProblemException)
      {
         OAuthProblemException p = (OAuthProblemException)e;
         String problem = p.getProblem();
         if (consumerName != null && RECOVERABLE_PROBLEMS.contains(problem))
         {
            try
            {
               CookieMap cookies = new CookieMap(request, response);
               OAuthConsumer consumer = ExoOAuthConsumerStorage.getConsumer(consumerName);
               OAuthAccessor accessor = ExoOAuthUtils.newAccessor(consumer, cookies);
               getAccessToken(request, cookies, accessor);
               // getAccessToken(request, consumer,
               // new CookieMap(request, response));
            }
            catch (Exception e2)
            {
               handleException(e2, request, response, null);
            }
         }
         else
         {
            try
            {
               StringWriter s = new StringWriter();
               PrintWriter pw = new PrintWriter(s);
               e.printStackTrace(pw);
               pw.flush();
               p.setParameter("stack trace", s.toString());
            }
            catch (Exception rats)
            {
            }
            response.setStatus(p.getHttpStatusCode());
            response.resetBuffer();
            request.setAttribute("OAuthProblemException", p);
            request.getRequestDispatcher //
               ("/OAuthProblemException.jsp").forward(request, response);
         }
      }
      else if (e instanceof IOException)
      {
         throw (IOException)e;
      }
      else if (e instanceof ServletException)
      {
         throw (ServletException)e;
      }
      else if (e instanceof RuntimeException)
      {
         throw (RuntimeException)e;
      }
      else
      {
         throw new ServletException(e);
      }
   }

}
