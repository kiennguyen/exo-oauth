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

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.example.consumer.ExoOAuthConsumerStorage;
import net.oauth.example.consumer.ExoOAuthMessage;
import net.oauth.example.consumer.RedirectException;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class operate as service of GateIn, it can send OAuth signing request to endpoint to get data.
 * It is compliant as OAuth 1.0 specification, it can be called: OAuth two legged, signing request, 
 * Signed Fetch, call home, phone home, etc.
 * In request, this service automatically attachs some parameters for authentication as:
 * - oauth_consumer_key string know as consumer key
 * - oauth_signature_method algorithm used to sign, support three algorithms: PLAINTEXT, HMAC_SYMMETRIC (HMAC_SHA1), RSA_PRIVATE (RSA-SHA1)
 * - oauth_timestamp time stamp to avoid 
 * - oauth_nonce salt string create by service
 * - oauth_version version of OAuth specification (1.0, 2.0)
 * - oauth_signature signature that signed this request
 * All request also is signed by this service, use consumer key and secret
 * See OAuth 1.0 specification for more detail
 * 
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Kien
 *          nguyenanhkien2a@gmail.com
 * Dec 3, 2010 
 */
public class ExoOAuth2LeggedConsumerService
{
   public static final OAuthClient CLIENT = new OAuthClient(new HttpClient4());

   public ExoOAuth2LeggedConsumerService() {}

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
      OAuthAccessor accessor = new OAuthAccessor(consumer);
      return accessor;
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
