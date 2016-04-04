/**
 * Copyright (c) 2015-2017 Inria - Linagora
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.erocci.backend.impl;

import java.util.Map;
import java.util.logging.Logger;

import org.freedesktop.dbus.Variant;
import org.occiware.clouddesigner.occi.Entity;
import org.ow2.erocci.backend.action;
import org.ow2.erocci.model.ConfigurationManager;
import org.ow2.erocci.model.exception.ExecuteActionException;
import org.ow2.erocci.runtime.ActionExecutorFactory;
import org.ow2.erocci.runtime.IActionExecutor;

/**
 * Implementation of OCCI action
 *
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class ActionImpl implements action {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public boolean isRemote() {

        return false;
    }

    /**
     * Launch an action on a resource or link.
     *
     * @param id , represent entityId ex: compute/vm1
     * @param action_id, represent action scheme + term ex:
     * http://schemas.ogf.org/occi/infrastructure/compute/action#start
     * @param attributes, the attributes of the action, may be empty.
     */
    @Override
    public void Action(String id, String action_id, Map<String, Variant> attributes) {

        logger.info("id " + id + " >-- action_id: " + action_id + " --< attributes=" + Utils.convertVariantMap(attributes));

        if (action_id == null) {
            // TODO : return fail or no state.
            return;
        }
        // TODO : Owner in parameters entry of Action method.
        String owner = ConfigurationManager.DEFAULT_OWNER;
        
        Map<String, String> actionAttributes = Utils.convertVariantMap(attributes);

        Entity entity = ConfigurationManager.findEntity(owner, id);
        if (entity != null) {
          // TODO : Model validator AFTER launching the action, this can cause a lot of problem if constraints aren't respected.
          // Get the executor corresponding on entity kind.
          // Launch the action effectively.
          String entityKind = entity.getKind().getScheme() + entity.getKind().getTerm();
          IActionExecutor actExecutor = ActionExecutorFactory.build(ConfigurationManager.getExtensionForKind(owner, entityKind));
          
          try {
              actExecutor.execute(action_id, actionAttributes, entity, IActionExecutor.FROM_ACTION);
          } catch (ExecuteActionException ex) {
              logger.warning("Action launch error : " + ex.getMessage());
          }
          
        } else {
            logger.info("Entity doesnt exist : " + id);
            // return failed; (or state)
        }

        // TODO : Give a return value to this method (ex: a state or if the action command succeed or not).
    }

}
