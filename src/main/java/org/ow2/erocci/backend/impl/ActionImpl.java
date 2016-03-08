/**
 * Copyright (c) 2015-2017 Linagora
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

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.freedesktop.dbus.Variant;
import org.occiware.clouddesigner.occi.Entity;
import org.ow2.erocci.backend.action;
import org.ow2.erocci.model.ConfigurationManager;

/**
 * Implementation of OCCI action
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
	 * @param id , represent entityId ex: compute/vm1
	 * @param action_id, represent action scheme + term ex: http://schemas.ogf.org/occi/infrastructure/compute/action#start 
	 * @param attributes, the attributes of the action, may be empty.
	 */
	@Override
	public void Action(String id, String action_id, Map<String, Variant> attributes) {
		
		// TODO : Warning, this is vital to have owner in parameter, elsewere, an action will be executed on all relative path (id) referenced on all owners.
		
		logger.info("id " + id + " >-- action_id: " + action_id + " --< attributes=" + Utils.convertVariantMap(attributes));
		
		if (action_id == null) {
			// TODO : return fail or no state.
			return;
		}
		
		// Launch the action if found on kind.
		Map<String, Entity> entities = ConfigurationManager.findEntityAction(id, action_id); 
		if (!entities.isEmpty()) {
			// Launch action.
			logger.info("Launching the action... " + action_id + " on entity " + id);
			// TODO : Cloudesigner Connector to the real infra and command pattern linked to this action.
			// ConnectorDelegate.launchAction(entities, action_id).
			logger.info("Action " + action_id + " launched !");
			// return success; (or state)
		} else {
			logger.info("Action : " + action_id + " doesnt exist for entity : " + id);
			// return failed; (or state)
		}
		
		
		// TODO : Give a return value to this method (ex: a state or if the action command succeed or not).
		
	}
	

}
