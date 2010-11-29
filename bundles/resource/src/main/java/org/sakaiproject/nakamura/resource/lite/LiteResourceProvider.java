/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.resource.lite;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.resource.lite.SparseContentResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

/**
 * Resource provider to Sling for accessing sparse map content.
 */
@Component(immediate = true, metatype = true)
@Service
@Property(name = ResourceProvider.ROOTS, value = "/test")
public class LiteResourceProvider implements ResourceProvider {
  private static final Logger logger = LoggerFactory
      .getLogger(LiteResourceProvider.class);

  @Reference
  private Repository repository;

  // ---------- ResourceProvider interface ----------
  /**
   * {@inheritDoc}
   *
   * @see org.apache.sling.api.resource.ResourceProvider#getResource(org.apache.sling.api.resource.ResourceResolver,
   *      java.lang.String)
   */
  public Resource getResource(ResourceResolver resourceResolver, String path) {
    Resource retRes = null;
    Session session = null;
    try {
      javax.jcr.Session jcrSession = resourceResolver.adaptTo(javax.jcr.Session.class);
      // get login information
      String userId = jcrSession.getUserID();
      session = repository.loginAdministrative(userId);
      ContentManager cm = session.getContentManager();
      Content content = cm.get(path);
      if (content != null) {
        retRes = new SparseContentResource(content, cm, new LiteResourceResolver(
            repository, userId));
      }
    } catch (ClientPoolException e) {
      logger.error(e.getMessage(), e);
    } catch (StorageClientException e) {
      logger.error(e.getMessage(), e);
    } catch (AccessDeniedException e) {
      logger.error(e.getMessage(), e);
    } finally {
      try {
        session.logout();
      } catch (ClientPoolException e) {
        logger.debug(e.getMessage(), e);
      }
    }
    return retRes;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.sling.api.resource.ResourceProvider#getResource(org.apache.sling.api.resource.ResourceResolver,
   *      javax.servlet.http.HttpServletRequest, java.lang.String)
   */
  public Resource getResource(ResourceResolver resourceResolver,
      HttpServletRequest request, String path) {
    return getResource(resourceResolver, path);
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.sling.api.resource.ResourceProvider#listChildren(org.apache.sling.api.resource.Resource)
   */
  public Iterator<Resource> listChildren(Resource parent) {
    return null;
  }
}
