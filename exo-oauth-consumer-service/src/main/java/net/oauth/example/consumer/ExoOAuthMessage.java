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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.oauth.OAuthMessage;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Kien
 *          nguyenanhkien2a@gmail.com
 * Dec 4, 2010  
 */
/**
 * This class used to contain information for an OAuth request or an OAuth response
 */
public class ExoOAuthMessage
{
   private String consumerName;
   private OAuthMessage message;
   public static final String GET = OAuthMessage.GET;
   public static final String POST = OAuthMessage.POST;
   public static final String PUT = OAuthMessage.PUT;
   public static final String DELETE = OAuthMessage.DELETE;
   
   public ExoOAuthMessage() {}
   
   public ExoOAuthMessage(String consumerName, OAuthMessage message) {
      this.consumerName = consumerName;
      this.message = message;
   }
   
   public ExoOAuthMessage(String consumerName, String restEndpoint, String httpMethod, List<Parameter> parameters) {
      this.consumerName = consumerName;          
      this.message = new OAuthMessage(httpMethod, restEndpoint, parameters);
   }

   public void setConsumerName(String consumerName)
   {
      this.consumerName = consumerName;
   }

   public String getConsumerName()
   {
      return consumerName;
   }
   
   public void setHttpMethod(String httpMethod)
   {
      this.message.method = httpMethod;
   }
   
   public void setRestEndpoint(String restEndpoint)
   {
      this.message.URL = restEndpoint;
   }
   
   public void setMessage(OAuthMessage message)
   {
      this.message = message;
   }
   
   public String getRestEndpoint() {
      return message.URL;
   }
   
   public String getHttpMethod() {
      return message.method;
   }
   
   public List<Map.Entry<String, String>> getParameters() throws IOException {
      return message.getParameters();
   }
   
   public InputStream getBodyAsStream() throws IOException {
      return message.getBodyAsStream();
   }
   
   public String getHeader(String name) {
      return message.getHeader(name);
   }
}
