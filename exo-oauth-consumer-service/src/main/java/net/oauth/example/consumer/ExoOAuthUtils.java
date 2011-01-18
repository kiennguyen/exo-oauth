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
package net.oauth.example.consumer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Kien
 *          nguyenanhkien2a@gmail.com
 * Jan 17, 2011  
 */
public class ExoOAuthUtils
{   
   /**
    * Construct an accessor from cookies. The resulting accessor won't
    * necessarily have any tokens.
    */
   public static OAuthAccessor newAccessor(OAuthConsumer consumer, CookieMap cookies) throws OAuthException
   {
      OAuthAccessor accessor = new OAuthAccessor(consumer);
      String consumerName = (String)consumer.getProperty("name");
      accessor.requestToken = cookies.get(consumerName + ".requestToken");
      accessor.accessToken = cookies.get(consumerName + ".accessToken");
      accessor.tokenSecret = cookies.get(consumerName + ".tokenSecret");
      return accessor;
   }
   
   /**
    * Copy data from ExoOAuthMessage object to a HttpServletResponse object
    * @param from
    * @param into
    * @throws IOException
    */
   public static void copyResponse(ExoOAuthMessage from, HttpServletResponse into) throws IOException
   {
      InputStream in = from.getBodyAsStream();
      OutputStream out = into.getOutputStream();
      into.setContentType(from.getHeader("Content-Type"));
      try
      {
         ExoOAuthUtils.copyAll(in, out);
      }
      finally
      {
         in.close();
      }
   }

   /**
    * Copy all data from InputStream object to OutputStream object
    * @param from
    * @param into
    * @throws IOException
    */
   private static void copyAll(InputStream from, OutputStream into) throws IOException
   {
      byte[] buffer = new byte[1024];
      for (int len; 0 < (len = from.read(buffer, 0, buffer.length));)
      {
         into.write(buffer, 0, len);
      }
   }
}
