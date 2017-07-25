package org.molgenis.oneclickimporter.service;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.oneclickimporter.model.DataCollection;

public interface EntityService
{
	/**
	 * Create one {@link EntityType} from a {@link DataCollection}
	 * Uses file name to create a package
	 *
	 * @param dataCollection
	 * @param packageName
	 * @return a newly created {@link EntityType}
	 */
	EntityType createEntityType(DataCollection dataCollection, String packageName);
}
