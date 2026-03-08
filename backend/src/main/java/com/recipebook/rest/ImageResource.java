package com.recipebook.rest;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.RestForm;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Path("/api/images")
public class ImageResource {

    private static final Logger LOG = Logger.getLogger(ImageResource.class);

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    @Inject
    JsonWebToken jwt;

    @ConfigProperty(name = "app.upload.dir", defaultValue = "./uploads/images")
    String uploadDir;

    @POST
    @Authenticated
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(@RestForm("file") FileUpload file) throws IOException {
        LOG.debugf("Image upload request received");
        if (file == null || file.filePath() == null) {
            LOG.warn("Upload rejected: no file provided");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "No file provided"))
                    .build();
        }

        String contentType = file.contentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            LOG.warnf("Upload rejected: invalid content type '%s'", contentType);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Only JPEG, PNG, and WebP images are allowed"))
                    .build();
        }

        if (file.size() > MAX_SIZE) {
            LOG.warnf("Upload rejected: file too large (%d bytes)", file.size());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "File size must be 5MB or less"))
                    .build();
        }

        String ext = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };

        String filename = UUID.randomUUID() + ext;
        java.nio.file.Path dir = java.nio.file.Path.of(uploadDir);
        Files.createDirectories(dir);
        java.nio.file.Path target = dir.resolve(filename);
        Files.copy(file.filePath(), target);

        String imageUrl = "/api/images/" + filename;
        LOG.infof("Image uploaded successfully: %s (%d bytes)", filename, file.size());
        return Response.ok(Map.of("imageUrl", imageUrl)).build();
    }

    @GET
    @Path("/{filename}")
    public Response serve(@PathParam("filename") String filename) throws IOException {
        LOG.debugf("Serving image: %s", filename);
        // Sanitize filename to prevent path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            LOG.warnf("Image serve rejected: path traversal attempt with filename '%s'", filename);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        java.nio.file.Path file = java.nio.file.Path.of(uploadDir).resolve(filename);
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return Response.ok(Files.readAllBytes(file))
                .type(contentType)
                .header("Cache-Control", "public, max-age=86400")
                .build();
    }
}
