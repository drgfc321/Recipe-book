package com.dnd.resource;

import com.dnd.entity.Session;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionResource {

    // GET /api/sessions - List all sessions
    @GET
    public List<Session> listAll() {
        return Session.listAll();
    }

    // GET /api/sessions/{id} - Get session by ID
    @GET
    @Path("/{id}")
    public Session get(@PathParam("id") Long id) {
        Session session = Session.findById(id);
        if (session == null) {
            throw new NotFoundException("Session not found");
        }
        return session;
    }

    // PUT /api/sessions/{id} - Update session
    @PUT
    @Path("/{id}")
    @Transactional
    public Session update(@PathParam("id") Long id, Session updated) {
        Session session = Session.findById(id);
        if (session == null) {
            throw new NotFoundException("Session not found");
        }
        session.sessionNumber = updated.sessionNumber;
        session.sessionDate = updated.sessionDate;
        session.summary = updated.summary;
        session.xpAwarded = updated.xpAwarded;
        session.notes = updated.notes;
        return session;
    }

    // DELETE /api/sessions/{id} - Delete session
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Session session = Session.findById(id);
        if (session == null) {
            throw new NotFoundException("Session not found");
        }
        session.delete();
        return Response.noContent().build();
    }
}
