package org.sakaiproject.nakamura.postprocessors;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.scr.annotations.Reference;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;

import edu.nyu.XythosRemote;

/**
 * @scr.component immediate="true" label="GroupMembershipEventsPostProcessor"
 *                description="handle events when group memberships are modified"
 * @scr.service interface="org.osgi.service.event.EventHandler"
 * @scr.property name="service.description"
 *               value="Provides a place to respond when sites are created and memberships updated"
 * @scr.property name="event.topics" value="org/apache/sling/jackrabbit/usermanager/event/part"
 */
public class GroupMembershipEventsPostProcessor implements EventHandler {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(GroupMembershipEventsPostProcessor.class);
  
  @Reference
  XythosRemote xythosService;
  
  public void handleEvent(Event event) {
    String principalName = (String) event.getProperty("principal_name");
    if ((principalName == null) || (! principalName.matches("g-.*(-collaborators|-viewers)"))) {
    	return;
    }
    User user = (User) event.getProperty("user");
    String siteId = principalName.replaceAll("g-", "");
    siteId = siteId.replaceAll("-collaborators", "");
    siteId = siteId.replaceAll("-viewers", "");
    try {
      String userId = user.getID();
      xythosService.toggleMember(siteId, userId);
    } catch (Exception e1) {
      LOGGER.warn("failed to create Xythos group when creating site: " + e1.getMessage());
    }
  }

}
