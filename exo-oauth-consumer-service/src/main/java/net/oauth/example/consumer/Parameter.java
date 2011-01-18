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

import java.util.Map;

import net.oauth.OAuth;

/**
 * Represent request's parameter object, it contains two properties: key, value
 * 
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Kien
 *          nguyenanhkien2a@gmail.com
 * Jan 18, 2011  
 */
public class Parameter implements Map.Entry<String, String>
{
   public Parameter(String key, String value) {
      this.key = key;
      this.value = value;
   }

   private final String key;

   private String value;

   public String getKey() {
      return key;
   }

   public String getValue() {
      return value;
   }

   public String setValue(String value) {
      try {
          return this.value;
      } finally {
          this.value = value;
      }
   }

   @Override
   public String toString() {
      return OAuth.percentEncode(getKey()) + '=' + OAuth.percentEncode(getValue());
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
          return true;
      if (obj == null)
          return false;
      if (getClass() != obj.getClass())
          return false;
      final Parameter that = (Parameter) obj;
      if (key == null) {
          if (that.key != null)
              return false;
      } else if (!key.equals(that.key))
          return false;
      if (value == null) {
          if (that.value != null)
              return false;
      } else if (!value.equals(that.value))
          return false;
      return true;
   }
}
