package com.dnd.resource;

import com.dnd.entity.Campaign;
import com.dnd.entity.Session;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;

@Path("/api/campaigns")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CampaignResource {

    // GET /api/campaigns - List all campaigns
    @GET
    public List<Campaign> list() {
        return Campaign.listAll();
    }

    // GET /api/campaigns/{id} - Get one campaign
    @GET
    @Path("/{id}")
    public Campaign get(@PathParam("id") Long id) {
        Campaign campaign = Campaign.findById(id);
        if (campaign == null) {
            throw new NotFoundException("Campaign not found");
        }
        return campaign;
    }

    // POST /api/campaigns - Create new campaign
    @POST
    @Transactional
    public Response create(Campaign campaign) {
        campaign.createdAt = LocalDateTime.now();
        campaign.updatedAt = LocalDateTime.now();
        campaign.persist();
        return Response.status(Response.Status.CREATED).entity(campaign).build();
    }

    // PUT /api/campaigns/{id} - Update campaign
    @PUT
    @Path("/{id}")
    @Transactional
    public Campaign update(@PathParam("id") Long id, Campaign updated) {
        Campaign campaign = Campaign.findById(id);
        if (campaign == null) {
            throw new NotFoundException("Campaign not found");
        }
        campaign.name = updated.name;
        campaign.description = updated.description;
        campaign.setting = updated.setting;
        campaign.status = updated.status;
        campaign.updatedAt = LocalDateTime.now();
        return campaign;
    }

    // DELETE /api/campaigns/{id} - Delete campaign
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Campaign campaign = Campaign.findById(id);
        if (campaign == null) {
            throw new NotFoundException("Campaign not found");
        }
        campaign.delete();
        return Response.noContent().build();
    }

    // GET /api/campaigns/{id}/sessions - List sessions for a campaign
    @GET
    @Path("/{id}/sessions")
    public List<Session> getSessions(@PathParam("id") Long id) {
        Campaign campaign = Campaign.findById(id);
        if (campaign == null) {
            throw new NotFoundException("Campaign not found");
        }
        return Session.list("campaign.id", id);
    }

    // POST /api/campaigns/{id}/sessions - Create session for a campaign
    @POST
    @Path("/{id}/sessions")
    @Transactional
    public Response createSession(@PathParam("id") Long id, Session session) {
        Campaign campaign = Campaign.findById(id);
        if (campaign == null) {
            throw new NotFoundException("Campaign not found");
        }
        session.campaign = campaign;
        session.createdAt = LocalDateTime.now();
        session.persist();
        return Response.status(Response.Status.CREATED).entity(session).build();
    }
}