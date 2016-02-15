package org.ow2.erocci.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.freedesktop.dbus.Variant;
import org.occiware.clouddesigner.occi.Action;
import org.occiware.clouddesigner.occi.AttributeState;
import org.occiware.clouddesigner.occi.Configuration;
import org.occiware.clouddesigner.occi.Kind;
import org.occiware.clouddesigner.occi.Link;
import org.occiware.clouddesigner.occi.Mixin;
import org.occiware.clouddesigner.occi.OCCIFactory;
import org.occiware.clouddesigner.occi.OCCIPackage;
import org.occiware.clouddesigner.occi.OCCIRegistry;
import org.occiware.clouddesigner.occi.Resource;
import org.occiware.clouddesigner.occi.util.OCCIResourceFactoryImpl;
import org.ow2.erocci.backend.impl.Utils;
import org.occiware.clouddesigner.occi.Entity;

/**
 * Manage configurations (OCCI Model).
 * 
 * @author cgourdin
 *
 */
public class ConfigurationManager {

	static {
		// Init EMF to dealt with OCCI files.
		Registry.INSTANCE.getExtensionToFactoryMap().put("occie", new OCCIResourceFactoryImpl());
		Registry.INSTANCE.getExtensionToFactoryMap().put("occic", new OCCIResourceFactoryImpl());
		Registry.INSTANCE.getExtensionToFactoryMap().put("*", new OCCIResourceFactoryImpl());

		// Register the OCCI package into EMF.
		OCCIPackage.eINSTANCE.toString();

		// Register OCCI extensions.
		OCCIRegistry.getInstance().registerExtension("http://schemas.ogf.org/occi/core#", "model/core.occie");
		OCCIRegistry.getInstance().registerExtension("http://schemas.ogf.org/occi/infrastructure#",
				"model/infrastructure.occie");
	}

	private static Logger logger = Logger.getLogger("ConfigurationManager");

	/**
	 * Used for now when no owner defined (on dbus methods find for example or no owner defined).
	 * Will be removed when the owner parameter will be always defined on input dbus method.
	 */
	public static final String DEFAULT_OWNER = "anonymous";
	
	/**
	 * This map reference all occi configurations by users. The first ref string
	 * is the user uuid. To be updated for multiusers and multiconfigs.
	 */
	protected static Map<String, Configuration> configurations = new HashMap<>();

	/**
	 * Obtain the factory to create OCCI objects.
	 */
	protected static OCCIFactory occiFactory = OCCIFactory.eINSTANCE;

	/**
	 * Get a configuration from the configuration's map.
	 * 
	 */
	public static Configuration getConfigurationForOwner(final String owner) {
		return configurations.get(owner);
	}

	/**
	 * Create a new configuration (empty ==> without any resources and link and
	 * extension) for the user.
	 * 
	 * @param owner
	 * @return a new configuration for the user.
	 */
	public static Configuration createConfiguration(final String owner) {
		// Obtain the factory to create OCCI objects.
		OCCIFactory factory = OCCIFactory.eINSTANCE;

		// Create an empty OCCI configuration.
		Configuration configuration = factory.createConfiguration();

		// Update reference configuration map.
		configurations.put(owner, configuration);

		logger.info("Configuration for user " + owner + " created");

		return configuration;
	}

	/**
	 * Remove a configuration from the configuration's map.
	 * 
	 * @param configuration
	 */
	public static void removeConfiguration(final Configuration configuration) {
		configurations.remove(configuration);
	}

	/**
	 * Update referenced configuration map with a configuration object updated.
	 * this will overwrite previously ref configuration.
	 * 
	 * @param owner
	 * @param configuration
	 */

	public static void updateConfiguration(final String owner, final Configuration configuration) {
		configurations.put(owner, configuration);
	}

	/**
	 * Add a new resource entity to a configuration and update the
	 * configuration's map accordingly.
	 * 
	 * @param id
	 *            (entity id : "term/title")
	 * @param kind
	 *            (scheme#term)
	 * @param mixins
	 *            (ex:
	 *            mixins=[http://schemas.ogf.org/occi/infrastructure/network#
	 *            ipnetwork])
	 * @param attributes
	 *            (ex: attributes={occi.network.vlan=12,
	 *            occi.network.label=private, occi.network.address=10.1.0.0/16,
	 *            occi.network.gateway=10.1.255.254})
	 * @return the updated configuration, can't return null
	 */
	public static Configuration addResourceToConfiguration(String id, String kind, List<String> mixins,
			Map<String, Variant> attributes, String owner) {

		if (owner == null) {
			// Assume if owner is not used to a default user uuid "anonymous".
			owner = "anonymous";
		}

		Configuration configuration = getConfigurationForOwner(owner);
		if (configuration == null) {
			configuration = createConfigurationForUser(owner);
		}

		// Assign a new resource to configuration, if configuration has resource
		// existed, inform by logger but overwrite existing one.
		// Create an OCCI resource.
		Resource resource = occiFactory.createResource();

		// TODO : Check if erocci send an entityUuid on the other form, cause
		// here i have a pattern with term/title and not an uuid.
		resource.setId(id);

		Kind occiKind = createKindWithValues(id, kind);

		// Add a new kind to resource (title, scheme, term).
		resource.setKind(occiKind);

		// Add the attributes...
		addAttributesToEntity(resource, attributes);

		// Add the mixins if any.
		addMixinsToEntity(resource, mixins);

		// Add resource to configuration.

		// check before if the resource exist in configuration, if this is the
		// case, the new resource will overwrite existing one.
		Iterator<Resource> it = configuration.getResources().iterator();
		while (it.hasNext()) {
			Resource resourceCheck = it.next();
			if (resourceCheck.getId().equals(id)) {
				logger.warning("resource already exist, overwriting...");
				it.remove();
				break;
			}
		}

		configuration.getResources().add(resource);

		logger.info("Added Resource " + resource.getId() + " to configuration object.");

		return configuration;

	}

	/**
	 * Add a new link entity to a configuration and update the configuration's
	 * map accordingly.
	 * 
	 * @param id
	 * @param kind
	 * @param mixins
	 * @param src
	 * @param target
	 * @param attributes
	 * @param owner
	 * @return a configuration updated.
	 */
	public static Configuration addLinkToConfiguration(String id, String kind, java.util.List<String> mixins,
			String src, String target, Map<String, Variant> attributes, String owner) {

		if (owner == null) {
			// Assume if owner is not used to a default user uuid "anonymous".
			owner = "anonymous";
		}

		Configuration configuration = getConfigurationForOwner(owner);

		if (configuration == null) {
			configuration = createConfigurationForUser(owner);
		}

		Link link = occiFactory.createLink();
		link.setId(id);
		Kind occiKind = createKindWithValues(id, kind);
		link.setKind(occiKind);

		Resource resourceSrc = findResource(configuration, src);

		Resource resourceDest = findResource(configuration, target);

		link.setSource(resourceSrc);
		link.setTarget(resourceDest);

		addAttributesToEntity(link, attributes);

		addMixinsToEntity(link, mixins);
		
		
		// check before if the resource exist in configuration, if this is the
		// case, the new resource will overwrite existing one.
		
		
		Iterator<Link> it = resourceSrc.getLinks().iterator();
		while (it.hasNext()) {
			Link linkCheck = it.next();
			if (linkCheck.getId().equals(id)) {
				logger.warning("link already exist, overwriting...");
				
				// TODO : Check if linkCheck.getTarget().getLinks().remove(linkCheck) is to do.
				//  it's bidirectionnal normally, it made the trick.
				it.remove();
				
				break;
			}
		}
		
		resourceSrc.getLinks().add(link);
		
		return configuration;

	}
	
	
	/**
	 * Search for a resource in a configuration with it's id.
	 * @param configuration
	 * @param id
	 * @return an OCCI resource if found, null if not found.
	 */
	public static Resource findResource(Configuration configuration, String id) {
		Resource resFound = null;
		if (configuration == null) {
			return resFound;
		}
		
		for (Resource resource : configuration.getResources()) {
			if (resource.getId().equals(id)) {
				resFound = resource;
				// Resource found.
				break;
			}
		}
		
		return resFound;
	}
	
	/**
	 * Find a resource for owner and entity Id.
	 * @param owner
	 * @param id
	 * @return an OCCI resource.
	 */
	public static Resource findResource(final String owner, final String id) {
		Resource resFound = null;
		Configuration configuration = configurations.get(owner);
		if (configuration == null) {
			return resFound;
		}
		
		for (Resource resource : configuration.getResources()) {
			if (resource.getId().equals(id)) {
				resFound = resource;
				// Resource found.
				break;
			}
		}
		return resFound;
	}
	
	/**
	 * Find a link for owner, entity id and source Resource Id.
	 * @param owner
	 * @param id
	 * @param srcResourceId
	 * @return an OCCI Link. May be null if configuration or link doesnt exist anymore.
	 */
	public static Link findLink(final String owner, final String id, final String srcResourceId) {
		Link linkFound = null;
		Configuration configuration = configurations.get(owner);
		if (configuration == null) {
			return linkFound;
		}
		
		Resource resourceSrc = findResource(owner, srcResourceId);
		if (resourceSrc == null) {
			return linkFound;
		}
		for (Link link : resourceSrc.getLinks()) {
			if (link.getId().equals(id)) {
				linkFound = link;
				// link has been found.
				break;
			}
		}
		
		return linkFound;
		
	}
	/**
	 * Find a link on all chains of resources.
	 * @return
	 */
	public static Link findLink(final String owner, final String id) {
		Configuration configuration = configurations.get(owner);
		Link link = null;
		EList<Link> links;
		for (Resource resource : configuration.getResources()) {
			links = resource.getLinks();
			if (!links.isEmpty()) {
				for (Link lnk : links) {
					if (lnk.getId().equals(id)) {
						link = lnk;
						break;
						
					}
				}
				if (link != null) {
					break;
				}
			}
			
		}
		
		
		return link;
		
	}
	
	/**
	 * Search an entity (link or resource) on the current configuration.
	 * @param owner
	 * @param id (entityId)
	 * @return an OCCI Entity, could be null, if entity has is not found. 
	 */
	public static Entity findEntity(final String owner, final String id) {
		Entity entity = null;
		Resource resource = findResource(owner, id);
		
		Link link = null;
		if (resource == null) {
			link = findLink(owner, id);
			entity = link;
		} else {
			entity = resource;
		}
		return entity;
	}
	
	public static Entity findEntityOnAllOwner(String ownerFound, final String id) {
		Set<String> owners = configurations.keySet();
		Entity entity = null;
		for (String owner : owners) {
			entity = findEntity(owner, id);
			ownerFound = owner;
			if (entity != null) {
				break;
			}
		}
		if (ownerFound == null) {
			ownerFound = DEFAULT_OWNER;
		}
		return entity;
	}
	
	/**
	 * Remove referenced configuration for an owner.
	 * @param owner
	 */
	public static void resetForOwner(final String owner) {
		Configuration configuration = getConfigurationForOwner(owner);
		if (configuration != null) {
			removeConfiguration(configuration);
		}
	}
	
	/**
	 * Destroy all configurations for all owners.
	 */
	public static void resetAll() {
		configurations.clear();
	}

	
	/**
	 * Create a new configuration object for the user uuid, if needed.
	 * 
	 * @param userUUID
	 * @return
	 */
	private static Configuration createConfigurationForUser(final String userUUID) {
		// Create an OCCI configuration.
		Configuration configuration = occiFactory.createConfiguration();
		// add to map reference for future access.
		configurations.put(userUUID, configuration);

		return configuration;
	}

	/**
	 * Create a new OCCI Kind with default values (title, term and scheme).
	 * 
	 * @param id
	 *            (entity id)
	 * @param kindTerm
	 *            (scheme#term)
	 * @return a new OCCI Kind with values
	 */
	private static Kind createKindWithValues(final String id, final String kindTerm) {
		Kind occiKind = occiFactory.createKind();

		String[] schemeArr = kindTerm.split("#");

		String scheme = schemeArr[0] + "#";
		String term = schemeArr[1];

		String title = id.split("/")[1];
		occiKind.setScheme(scheme);
		occiKind.setTerm(term);
		occiKind.setTitle(title);
		logger.info("Kind --> Term : " + term + " --< Scheme : " + scheme + " --< title : " + title);
		// Pour debug.
		logger.info("      - actions:");
		for (Action action : occiKind.getActions()) {
			logger.info("        * Action");
			logger.info("          - term: " + action.getTerm());
			logger.info("          - scheme: " + action.getScheme());
			logger.info("          - title: " + action.getTitle());
		}

		return occiKind;
	}

	/**
	 * Create the attributes list for the OCCI Entity and assign them. Please
	 * note that entity.getAttributes() method will return empty list if none
	 * attributes has been assigned.
	 * 
	 * @param entity
	 * @param attributes
	 * @return Be aware the returned list attributes may be null.
	 */
	private static void addAttributesToEntity(Entity entity, final Map<String, Variant> attributes) {

		if (attributes != null && !attributes.isEmpty()) {
			String key;
			String value;
			Map<String, String> mapAttr = Utils.convertVariantMap(attributes);
			
			for (Map.Entry<String, String> entry : mapAttr.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				// Assign key --< value to attributes list.
				AttributeState attrState = occiFactory.createAttributeState();
				attrState.setName(key);
				attrState.setValue(value);

				entity.getAttributes().add(attrState);

				logger.info("Attributes added to entity --> " + entity.getId() + " --> " + attrState.getName() + " <-- "
						+ attrState.getValue());
			}

		}
	}
	
	/**
	 * Update attributes on an entity (link or resource).
	 * @param entity
	 * @param attributes
	 */
	private static void updateAttributesToEntity(Entity entity, final Map<String, Variant> attributes) {
		if (attributes != null && !attributes.isEmpty()) {
			String key;
			String value;
			
			Map<String, String> mapAttr = Utils.convertVariantMap(attributes);
			
			EList<AttributeState> attrStates;
			for (Map.Entry<String, String> entry : mapAttr.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				// Check if this attribute already exist.
				attrStates = entity.getAttributes();
				
				Iterator<AttributeState> it = attrStates.iterator();
				while (it.hasNext()) {
					AttributeState attrState = it.next();
					if (attrState.getName().equals(key)) {
						it.remove();
						break;
					}
				}
				// Assign key --< value to attributes list.
				AttributeState attrState = occiFactory.createAttributeState();
				attrState.setName(key);
				attrState.setValue(value);
				entity.getAttributes().add(attrState);
			}
			
			
			
		}
	}

	/**
	 * Add mixins to an existing entity (resources or links). Ex of mixin string
	 * format : http://schemas.ogf.org/occi/infrastructure/network#ipnetwork
	 * 
	 * @param entity
	 *            (OCCI Entity).
	 * @param mixins
	 *            (List of mixins).
	 */
	public static void addMixinsToEntity(Entity entity, final List<String> mixins) {
		if (mixins != null && !mixins.isEmpty()) {
			String scheme;
			String term;
			// String title;
			
			for (String mixinStr : mixins) {

				term = mixinStr.split("#")[1];
				scheme = mixinStr.split("#")[0] + "#";

				// TODO : How to find the title in this string ?
				Mixin mixin = occiFactory.createMixin();
				// mixin.setTitle(title);
				mixin.setTerm(term);
				mixin.setScheme(scheme);

				entity.getMixins().add(mixin);
				logger.info("Mixin --> Term : " + term + " --< Scheme : " + scheme);

			}
		}
	}

	/**
	 * Search mixin on owner's configuration.
	 * @param owner
	 * @param mixinId
	 * @return a mixin found or null if not found
	 */
	public static Mixin findMixin(final String owner, final String mixinId) {
		Configuration configuration = configurations.get(owner);
		Mixin mixinToReturn = null;
		boolean mixinOk;
		
		for (Resource res : configuration.getResources()) {
			mixinOk = false;
			for (Mixin mixin : res.getMixins()) {
				if ((mixin.getScheme() + mixin.getTerm()).equals(mixinId)) {
					mixinToReturn = mixin;
					mixinOk = true;
					break;
				} 
			}
			
			if (mixinOk) {
				break;
			} else {
				// Recherche dans les links.
				for (Link link : res.getLinks()) {
					for (Mixin mixin : link.getMixins()) {
						if ((mixin.getScheme() + mixin.getTerm()).equals(mixinId)) {
							mixinToReturn = mixin;
							mixinOk = true;
							break;
						}
					}
					if (mixinOk) {
						break;
					}
				}
			}
			if (mixinOk) {
				break;
			}
			
		}
		
		return mixinToReturn;
	}
	
	/**
	 * Associate a list of entities with a mixin, replacing existing list if any.
	 * @param mixinId
	 * @param entityIds
	 */
	public static void saveMixinForEntities(final String owner, final String mixinId, final List<String> entityIds) {
		
		// searching for the mixin to register on entities.
		Mixin mixin = findMixin(owner, mixinId);
		
		for (String entityId : entityIds) {
			Entity entity = findEntity(entityId, owner);
			if (entity != null && !mixin.getEntities().contains(entity)) {
				mixin.getEntities().add(entity);
			} 
		}
		
	}
	


	/**
	 * Update attributes for an entity (resource or link).
	 * @param owner
	 * @param entityId
	 * @param attributes
	 */
	public static void updateAttributesForEntity(String owner, String entityId, Map<String, Variant> attributes) {
		// Searching on resources, if not found searching on link of each resources.
		Configuration configuration = configurations.get(owner);
		EList<Resource> entities = configuration.getResources();
		EList<Link> entitiesLnk;
		Entity entityFound = null;
		
		for (Resource res : entities) {
			if (res.getId().equals(entityId)) {
				// Entity found !
				entityFound = res;
				break;
			} else {
				// searching on his links.
				entitiesLnk = res.getLinks();
				if (!entitiesLnk.isEmpty()) {
					for (Link link : entitiesLnk) {
						if (link.getId().equals(entityId)) {
							entityFound = link;
							break;
						}
					}
					if (entityFound != null) {
						break;
					} 
				}
			}
		}
		if (entityFound != null) {
			// update the attributes.
			updateAttributesToEntity(entityFound, attributes);
			
		} else {
			// TODO : Report an exception, impossible to update entity, it doesnt exist.
			
		}
		
	}

	
	
}