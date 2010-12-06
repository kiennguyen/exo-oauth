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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient3.HttpClient3;
import net.oauth.example.consumer.CookieMap;
import net.oauth.example.consumer.ExoOAuthMessage;
import net.oauth.example.consumer.OAuthConsumerStorage;
import net.oauth.example.consumer.RedirectException;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Kien
 *          nguyenanhkien2a@gmail.com
 * Dec 3, 2010  
 */
public class OAuth2LeggedConsumerService
{
   public static final OAuthClient CLIENT = new OAuthClient(new HttpClient3());
   
   public OAuth2LeggedConsumerService(){}
   
   public ExoOAuthMessage send(String consumerName, String restEndpointUrl, HttpServletRequest request, HttpServletResponse response) 
   throws OAuthException, IOException, URISyntaxException{      
      OAuthConsumer consumer = OAuthConsumerStorage.getConsumer(consumerName);
      OAuthAccessor accessor = getAccessor(request, response, consumer);
      OAuthMessage message = accessor.newRequestMessage(OAuthMessage.GET, restEndpointUrl, null);

      OAuthMessage responseMessage = OAuth2LeggedConsumerService.CLIENT.invoke(message, ParameterStyle.AUTHORIZATION_HEADER);
      return (new ExoOAuthMessage(consumerName, responseMessage));
   }  

   /**
    * Get the access token and token secret for the given consumer. Get them
    * from cookies if possible; otherwise obtain them from the service
    * provider. In the latter case, throw RedirectException.
    * @throws IOException 
    * @throws URISyntaxException 
    */
   public static OAuthAccessor getAccessor(HttpServletRequest request,
           HttpServletResponse response, OAuthConsumer consumer)
           throws OAuthException, IOException, URISyntaxException {
       CookieMap cookies = new CookieMap(request, response);
       OAuthAccessor accessor = newAccessor(consumer, cookies);
       return accessor;
   }

   /**
    * Construct an accessor from cookies. The resulting accessor won't
    * necessarily have any tokens.
    */
   public static OAuthAccessor newAccessor(OAuthConsumer consumer, CookieMap cookies)
           throws OAuthException {
       OAuthAccessor accessor = new OAuthAccessor(consumer);
       String consumerName = (String) consumer.getProperty("name");
       accessor.requestToken = cookies.get(consumerName + ".requestToken");
       accessor.accessToken = cookies.get(consumerName + ".accessToken");
       accessor.tokenSecret = cookies.get(consumerName + ".tokenSecret");
       return accessor;
   }

   /** Remove all the cookies that contain accessors' data. */
   public static void removeAccessors(CookieMap cookies) {
       List<String> names = new ArrayList<String>(cookies.keySet());
       for (String name : names) {
           if (name.endsWith(".requestToken") || name.endsWith(".accessToken")
                   || name.endsWith(".tokenSecret")) {
               cookies.remove(name);
           }
       }
   }
   
   public static void copyResponse(ExoOAuthMessage from, HttpServletResponse into) throws IOException {
       InputStream in = from.getMessage().getBodyAsStream();
       OutputStream out = into.getOutputStream();
       into.setContentType(from.getMessage().getHeader("Content-Type"));
       try {
           OAuth2LeggedConsumerService.copyAll(in, out);
       } finally {
           in.close();
       }
   }

   private static void copyAll(InputStream from, OutputStream into) throws IOException {
       byte[] buffer = new byte[1024];
       for (int len; 0 < (len = from.read(buffer, 0, buffer.length));) {
           into.write(buffer, 0, len);
       }
   }

   /**
    * The names of problems from which a consumer can recover by getting a
    * fresh token.
    */
   protected static final Collection<String> RECOVERABLE_PROBLEMS = new HashSet<String>();
   static {
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
   public static void handleException(Exception e, HttpServletRequest request,
           HttpServletResponse response, String consumerName)
           throws IOException, ServletException {
       if (e instanceof RedirectException) {
           RedirectException redirect = (RedirectException) e;
           String targetURL = redirect.getTargetURL();
           if (targetURL != null) {
               response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
               response.setHeader("Location", targetURL);
           }
       } else if (e instanceof IOException) {
           throw (IOException) e;
       } else if (e instanceof ServletException) {
           throw (ServletException) e;
       } else if (e instanceof RuntimeException) {
           throw (RuntimeException) e;
       } else {
           throw new ServletException(e);
       }
   }
}
