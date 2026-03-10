package com.recipebook.graphql;

import com.recipebook.dto.PantryItemResponse;
import com.recipebook.service.PantryService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.List;

@GraphQLApi
public class PantryGraphQL {

    private static final Logger LOG = Logger.getLogger(PantryGraphQL.class);

    @Inject
    JsonWebToken jwt;

    @Inject
    PantryService pantryService;

    @Query("pantryItems")
    @Description("List all pantry items for the current user")
    @Authenticated
    public List<PantryItemResponse> getPantryItems() {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.debugf("getPantryItems userId=%d", userId);
        return pantryService.getPantryItems(userId);
    }

    @Query("expiringPantryItems")
    @Description("List pantry items expiring within N days")
    @Authenticated
    public List<PantryItemResponse> getExpiringPantryItems(@Name("withinDays") @DefaultValue("3") int withinDays) {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.debugf("getExpiringPantryItems userId=%s, withinDays=%d", (Object) userId, withinDays);
        return pantryService.getExpiringItems(userId, withinDays);
    }

    @Mutation("addPantryItem")
    @Description("Add an item to the pantry (auto-merges if ingredient exists)")
    @Authenticated
    @Transactional
    public PantryItemResponse addPantryItem(@Name("input") PantryItemInput input) {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.infof("addPantryItem userId=%d, ingredientId=%d", userId, input.ingredientId);
        return pantryService.addPantryItem(userId, input);
    }

    @Mutation("updatePantryItem")
    @Description("Update a pantry item")
    @Authenticated
    @Transactional
    public PantryItemResponse updatePantryItem(@Name("id") Long id, @Name("input") PantryItemUpdateInput input) {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.infof("updatePantryItem userId=%d, itemId=%d", userId, id);
        return pantryService.updatePantryItem(userId, id, input);
    }

    @Mutation("removePantryItem")
    @Description("Remove a pantry item")
    @Authenticated
    @Transactional
    public boolean removePantryItem(@Name("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.infof("removePantryItem userId=%d, itemId=%d", userId, id);
        return pantryService.removePantryItem(userId, id);
    }
}
