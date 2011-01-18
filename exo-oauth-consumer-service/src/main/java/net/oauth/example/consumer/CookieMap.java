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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents the set of cookies for the client of an HTTP request. Map-like
 * operations enable examining and modifying cookies.
 * 
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Kien
 *          nguyenanhkien2a@gmail.com
 * Dec 3, 2010 
 */
public class CookieMap {

    public CookieMap(HttpServletRequest request, HttpServletResponse response) {
        this.response = response;
        this.path = request.getContextPath();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie != null) {
                    name2value.put(cookie.getName(), cookie.getValue());
                }
            }
        }
    }

    private final HttpServletResponse response;

    private final String path;

    private final Map<String, String> name2value = new HashMap<String, String>();

    public String get(String name) {
        return name2value.get(name);
    }

    public void put(String name, String value) {
        if (value == null) {
            remove(name);
        } else if (!value.equals(name2value.get(name))) {
            Cookie c = new Cookie(name, value);
            c.setPath(path);
            response.addCookie(c);
            name2value.put(name, value);
        }
    }

    public void remove(String name) {
        if (name2value.containsKey(name)) {
            Cookie c = new Cookie(name, "");
            c.setMaxAge(0);
            c.setPath(path);
            response.addCookie(c);
            name2value.remove(name);
        }
    }

    public Set<String> keySet() {
        Set<String> set = Collections.unmodifiableSet(name2value.keySet());
        return set;
    }

    public String toString() {
        return name2value.toString();
    }

}
