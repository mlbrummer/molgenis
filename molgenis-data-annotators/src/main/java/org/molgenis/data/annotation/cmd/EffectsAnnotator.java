package org.molgenis.data.annotation.cmd;

import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.impl.RepositoryAnnotatorImpl;

public class EffectsAnnotator extends RepositoryAnnotatorImpl {
    public EffectsAnnotator(EntityAnnotator entityAnnotator) {
        super(entityAnnotator);
    }
}
