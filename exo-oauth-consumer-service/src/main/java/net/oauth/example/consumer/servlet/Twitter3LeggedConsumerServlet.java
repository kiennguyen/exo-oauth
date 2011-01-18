/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package net.oauth.example.consumer.servlet;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.example.consumer.ExoOAuthMessage;
import net.oauth.example.consumer.ExoOAuthUtils;
import net.oauth.example.consumer.Parameter;
import net.oauth.example.consumer.service.ExoOAuth3LeggedConsumerService;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Kien
 *          nguyenanhkien2a@gmail.com
 * Jan 18, 2011  
 */
public class Twitter3LeggedConsumerServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      String consumerName = "twitter";
      String restEndpoint = "http://twitter.com/statuses/update.xml"; 
      List<Parameter> parameters = new ArrayList<Parameter>();
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      Date date = new Date();
      parameters.add(new Parameter("status", "Test GateIn OAuth with Twitter " + dateFormat.format(date)));
      ExoOAuthMessage requestMessage = new ExoOAuthMessage(consumerName, restEndpoint, ExoOAuthMessage.POST, parameters);
      ExoOAuth3LeggedConsumerService oauthService = new ExoOAuth3LeggedConsumerService();
      
      try {
         ExoOAuthMessage result = oauthService.send(requestMessage, request, response);
         ExoOAuthUtils.copyResponse(result, response);
      } catch (Exception e) {
         oauthService.handleException(e, request, response, consumerName);
      }
 }
}
