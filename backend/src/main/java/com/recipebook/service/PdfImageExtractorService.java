package com.recipebook.service;

import com.recipebook.entity.Recipe;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PdfImageExtractorService {

    private static final Logger LOG = Logger.getLogger(PdfImageExtractorService.class);
    private static final int MIN_IMAGE_SIZE = 100;

    @ConfigProperty(name = "app.upload.dir", defaultValue = "./uploads/images")
    String uploadDir;

    public record ExtractionResult(int pagesProcessed, int imagesExtracted, int recipesMatched,
                                   List<String> unmatchedRecipes, List<String> details) {}

    @Transactional
    public ExtractionResult extractAndMatch(Path pdfPath) throws IOException {
        Files.createDirectories(Path.of(uploadDir));

        List<String> details = new ArrayList<>();
        int imagesExtracted = 0;
        int recipesMatched = 0;

        List<Recipe> allRecipes = Recipe.listAll();
        LOG.infof("Found %d recipes in database", allRecipes.size());

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            int totalPages = document.getNumberOfPages();
            LOG.infof("PDF has %d pages", totalPages);

            PDFTextStripper textStripper = new PDFTextStripper();

            for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                PDPage page = document.getPage(pageNum);
                int displayPage = pageNum + 1;

                // Extract largest image from page
                BufferedImage largestImage = extractLargestImage(page);
                if (largestImage == null) {
                    continue;
                }

                // Save image
                String filename = "pdf_page_" + displayPage + ".jpg";
                File outputFile = Path.of(uploadDir).resolve(filename).toFile();
                ImageIO.write(largestImage, "jpg", outputFile);
                imagesExtracted++;

                // Extract text from this page
                textStripper.setStartPage(displayPage);
                textStripper.setEndPage(displayPage);
                String pageText = textStripper.getText(document);
                String normalizedPageText = normalize(pageText);

                // Try to match against recipes
                Recipe matched = findMatchingRecipe(allRecipes, normalizedPageText);
                if (matched != null) {
                    matched.imageUrl = "/api/images/" + filename;
                    recipesMatched++;
                    details.add("Page " + displayPage + " → " + matched.name);
                    LOG.infof("Matched page %d to recipe: %s", displayPage, matched.name);
                } else {
                    String preview = pageText.replaceAll("\\s+", " ").trim();
                    if (preview.length() > 80) preview = preview.substring(0, 80) + "...";
                    details.add("Page " + displayPage + " — no match (text: " + preview + ")");
                }
            }
        }

        // Find unmatched recipes
        List<String> unmatchedRecipes = allRecipes.stream()
                .filter(r -> r.imageUrl == null || r.imageUrl.isBlank())
                .map(r -> r.name)
                .toList();

        LOG.infof("Extraction complete: %d images, %d matched, %d unmatched",
                imagesExtracted, recipesMatched, unmatchedRecipes.size());

        return new ExtractionResult(0, imagesExtracted, recipesMatched, unmatchedRecipes, details);
    }

    private BufferedImage extractLargestImage(PDPage page) {
        PDResources resources = page.getResources();
        if (resources == null) return null;

        BufferedImage largest = null;
        int largestArea = 0;

        for (COSName name : resources.getXObjectNames()) {
            try {
                PDXObject xobj = resources.getXObject(name);
                if (xobj instanceof PDImageXObject img) {
                    int w = img.getWidth();
                    int h = img.getHeight();
                    if (w >= MIN_IMAGE_SIZE && h >= MIN_IMAGE_SIZE) {
                        int area = w * h;
                        if (area > largestArea) {
                            largest = img.getImage();
                            largestArea = area;
                        }
                    }
                }
            } catch (IOException e) {
                LOG.warnf("Error reading XObject %s: %s", name.getName(), e.getMessage());
            }
        }

        return largest;
    }

    private Recipe findMatchingRecipe(List<Recipe> recipes, String normalizedPageText) {
        if (normalizedPageText.isBlank()) return null;

        Recipe bestMatch = null;
        int bestLength = 0;

        for (Recipe recipe : recipes) {
            // Skip already-matched recipes
            if (recipe.imageUrl != null && !recipe.imageUrl.isBlank()) continue;

            String normalizedName = normalize(recipe.name);
            if (normalizedName.length() < 3) continue;

            if (normalizedPageText.contains(normalizedName)) {
                // Prefer longer name matches (more specific)
                if (normalizedName.length() > bestLength) {
                    bestMatch = recipe;
                    bestLength = normalizedName.length();
                }
            }
        }

        return bestMatch;
    }

    private String normalize(String text) {
        if (text == null) return "";
        // Remove diacritics, lowercase, strip punctuation/symbols, collapse whitespace
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        String noDiacritics = nfd.replaceAll("\\p{M}", "");
        // Replace non-alphanumeric (except spaces) with space — fixes ®, commas, & without space
        String cleaned = noDiacritics.replaceAll("[^a-zA-Z0-9\\s]", " ");
        return cleaned.toLowerCase().replaceAll("\\s+", " ").trim();
    }
}
