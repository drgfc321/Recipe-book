package com.dnd.resource;

import com.dnd.entity.Character;
import com.dnd.entity.Campaign;
import com.dnd.entity.User;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;

@Path("/api/characters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CharacterResource {

    @GET
    public List<Character> list() {
        return Character.listAll();
    }

    @GET
    @Path("/{id}")
    public Character get(@PathParam("id") Long id) {
        Character character = Character.findById(id);
        if (character == null) {
            throw new NotFoundException("Character not found");
        }
        return character;
    }

    @POST
    @Transactional
    public Response create(Character character) {
        character.createdAt = LocalDateTime.now();
        character.updatedAt = LocalDateTime.now();
        character.persist();
        return Response.status(Response.Status.CREATED).entity(character).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Character update(@PathParam("id") Long id, Character updated) {
        Character character = Character.findById(id);
        if (character == null) {
            throw new NotFoundException("Character not found");
        }
        character.name = updated.name;
        character.race = updated.race;
        character.characterClass = updated.characterClass;
        character.level = updated.level;
        character.strength = updated.strength;
        character.dexterity = updated.dexterity;
        character.constitution = updated.constitution;
        character.intelligence = updated.intelligence;
        character.wisdom = updated.wisdom;
        character.charisma = updated.charisma;
        character.hitPoints = updated.hitPoints;
        character.armorClass = updated.armorClass;
        character.speed = updated.speed;
        character.backstory = updated.backstory;
        character.updatedAt = LocalDateTime.now();
        return character;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Character character = Character.findById(id);
        if (character == null) {
            throw new NotFoundException("Character not found");
        }
        character.delete();
        return Response.noContent().build();
    }

    // Get characters by campaign
    @GET
    @Path("/campaign/{campaignId}")
    public List<Character> getByCampaign(@PathParam("campaignId") Long campaignId) {
        return Character.list("campaign.id", campaignId);
    }

    // Get characters by user
    @GET
    @Path("/user/{userId}")
    public List<Character> getByUser(@PathParam("userId") Long userId) {
        return Character.list("user.id", userId);
    }
}
