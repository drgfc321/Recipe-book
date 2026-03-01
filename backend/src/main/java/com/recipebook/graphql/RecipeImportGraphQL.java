package com.recipebook.graphql;

import com.recipebook.entity.User;
import com.recipebook.service.RecipeImportService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@GraphQLApi
public class RecipeImportGraphQL {

    @Inject
    JsonWebToken jwt;

    @Inject
    RecipeImportService importService;

    @Mutation("importRecipesFromFile")
    @Description("Import recipes from a JSON file (authenticated)")
    @Authenticated
    public ImportResultDTO importRecipes(@Name("filePath") @DefaultValue("recipes_import.json") String filePath) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());
        User owner = User.findById(userId);
        if (owner == null) {
            throw new GraphQLException("User not found");
        }

        Path path = Paths.get(filePath);
        if (!path.isAbsolute()) {
            // Resolve relative to working directory
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }

        if (!path.toFile().exists()) {
            throw new GraphQLException("File not found: " + path);
        }

        RecipeImportService.ImportResult result = importService.importFromJson(path, owner);
        return new ImportResultDTO(result.imported(), result.failed(), result.errors());
    }

    public static class ImportResultDTO {
        public int imported;
        public int failed;
        public List<String> errors;

        public ImportResultDTO(int imported, int failed, List<String> errors) {
            this.imported = imported;
            this.failed = failed;
            this.errors = errors;
        }
    }
}
