package com.recipebook.graphql;

import com.recipebook.entity.User;
import com.recipebook.service.PdfImageExtractorService;
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

    @Inject
    PdfImageExtractorService pdfImageExtractorService;

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

    @Mutation("extractPdfImages")
    @Description("Extract images from a PDF cookbook and match them to recipes")
    @Authenticated
    public String extractPdfImages(@Name("filePath") @DefaultValue("carte.pdf") String filePath) throws GraphQLException {
        Path path = Paths.get(filePath);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }

        if (!path.toFile().exists()) {
            throw new GraphQLException("File not found: " + path);
        }

        try {
            PdfImageExtractorService.ExtractionResult result = pdfImageExtractorService.extractAndMatch(path);

            StringBuilder report = new StringBuilder();
            report.append("Images extracted: ").append(result.imagesExtracted()).append("\n");
            report.append("Recipes matched: ").append(result.recipesMatched()).append("\n");
            report.append("\n--- Matches ---\n");
            for (String detail : result.details()) {
                report.append(detail).append("\n");
            }
            if (!result.unmatchedRecipes().isEmpty()) {
                report.append("\n--- Unmatched recipes ---\n");
                for (String name : result.unmatchedRecipes()) {
                    report.append("  - ").append(name).append("\n");
                }
            }
            return report.toString();
        } catch (Exception e) {
            throw new GraphQLException("Extraction failed: " + e.getMessage());
        }
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
