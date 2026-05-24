package io.crest.api.permissions.relation.api;

import io.crest.exception.DEException;

/**
 * @Author Junjun
 */
public interface RelationApi {
    Long getDsResource(Long id);

    Long getDatasetResource(Long id);

    void checkAuth() throws DEException;
}
