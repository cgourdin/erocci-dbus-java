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
package org.ow2.erocci.runtime;

import java.util.Map;

import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Extension;
import org.ow2.erocci.model.exception.ExecuteActionException;

public class CloudActionExecutor extends AbstractActionExecutor implements IActionExecutor {

	private CloudActionExecutor() {
        super();
    }
    
    public CloudActionExecutor(Extension extension) {
		super(extension);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void occiPostCreate(Entity entity) throws ExecuteActionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void occiPreDelete(Entity entity) throws ExecuteActionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void occiPostUpdate(Entity entity) throws ExecuteActionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void occiMixinAdded(String mixinId) throws ExecuteActionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void occiMixinDeleted(String mixinId) throws ExecuteActionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(String actionId, Entity entity, String fromMethod) throws ExecuteActionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(String actionId, Map<String, String> actionAttributes, Entity entity, String fromMethod)
			throws ExecuteActionException {
		// TODO Auto-generated method stub
		
	}

    
    public static IActionExecutor getInstance() {
        return CloudActionExecutorHolder.INSTANCE;
    }
    
    private static class CloudActionExecutorHolder {
        private final static CloudActionExecutor INSTANCE = new CloudActionExecutor();
    }


}
