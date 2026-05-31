package io.crest.substitute.permissions.auth;

import java.util.Set;

public class PlatformPermissionPolicy {

    private final boolean systemAdmin;
    private final Long currentUserId;
    private final Set<String> userGrants;
    private final Set<String> roleGrants;
    private final Long resourceOrgId;

    public PlatformPermissionPolicy(boolean systemAdmin, Long currentUserId, Set<String> userGrants,
                                    Set<String> roleGrants, Long resourceOrgId) {
        this.systemAdmin = systemAdmin;
        this.currentUserId = currentUserId;
        this.userGrants = userGrants == null ? Set.of() : userGrants;
        this.roleGrants = roleGrants == null ? Set.of() : roleGrants;
        this.resourceOrgId = resourceOrgId;
    }

    public boolean canAccess(Long creatorId, Set<Long> roleIds, String action) {
        if (systemAdmin) {
            return true;
        }
        if (currentUserId != null && creatorId != null && currentUserId.equals(creatorId)) {
            return true;
        }
        String normalizedAction = normalize(action);
        return hasGrant(userGrants, normalizedAction) || hasGrant(roleGrants, normalizedAction);
    }

    public Long resourceOrgId() {
        return resourceOrgId;
    }

    private boolean hasGrant(Set<String> grants, String action) {
        return grants.contains("manage") || grants.contains(action);
    }

    private String normalize(String action) {
        if (action == null || action.isBlank()) {
            return "read";
        }
        return action.trim().toLowerCase();
    }
}
