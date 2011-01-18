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
package net.oauth.example.consumer.servlet;

import net.oauth.example.consumer.ExoOAuthMessage;
import net.oauth.example.consumer.ExoOAuthUtils;
import net.oauth.example.consumer.service.ExoOAuth2LeggedConsumerService;

import java.io.IOException;

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
public class GateIn2LeggedConsumerServlet extends HttpServlet
{
   @Override
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      String consumer = "gatein2";
      String restEndpointUrl = "http://localhost:8080/exo-oauth-provider/rest/SocialRest/infos";      
      ExoOAuth2LeggedConsumerService oauthService = new ExoOAuth2LeggedConsumerService();
      
      try {        
         ExoOAuthMessage result = oauthService.send(consumer, restEndpointUrl, request, response);
         ExoOAuthUtils.copyResponse(result, response);
      } catch (Exception e) {
         oauthService.handleException(e, request, response, consumer);
      }
   }
   private static final long serialVersionUID = 1L;
}
