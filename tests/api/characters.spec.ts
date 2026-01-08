import { test, expect } from '@playwright/test';
import { getAuthToken, authHeaders } from './test-utils';

test.describe('Characters API', () => {
  let token: string;

  test.beforeAll(async ({ request }) => {
    token = await getAuthToken(request);
  });

  test('GET /api/characters - should list all characters', async ({ request }) => {
    const response = await request.get('/api/characters', {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();
    expect(response.status()).toBe(200);

    const characters = await response.json();
    expect(Array.isArray(characters)).toBeTruthy();
  });

  test('POST /api/characters - should create a new character', async ({ request }) => {
    const newCharacter = {
      name: 'Test Hero',
      race: 'Human',
      characterClass: 'Barbarian',
      level: 3,
      strength: 16,
      dexterity: 14,
      constitution: 15,
      intelligence: 10,
      wisdom: 12,
      charisma: 8,
      hitPoints: 35,
      armorClass: 14,
      speed: 30,
      backstory: 'A test character'
    };

    const response = await request.post('/api/characters', {
      headers: authHeaders(token),
      data: newCharacter
    });

    expect(response.status()).toBe(201);

    const character = await response.json();
    expect(character.name).toBe(newCharacter.name);
    expect(character.id).toBeDefined();
  });

  test('GET /api/characters/{id} - should get a single character', async ({ request }) => {
    const createResponse = await request.post('/api/characters', {
      headers: authHeaders(token),
      data: { name: 'Get Test', race: 'Elf', characterClass: 'Wizard', level: 1 }
    });
    const created = await createResponse.json();

    const response = await request.get(`/api/characters/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();

    const character = await response.json();
    expect(character.id).toBe(created.id);
  });

  test('GET /api/characters/{id} - should return 404 for non-existent character', async ({ request }) => {
    const response = await request.get('/api/characters/99999', {
      headers: authHeaders(token)
    });
    expect(response.status()).toBe(404);
  });

  test('PUT /api/characters/{id} - should update a character', async ({ request }) => {
    const createResponse = await request.post('/api/characters', {
      headers: authHeaders(token),
      data: { name: 'Update Test', race: 'Dwarf', characterClass: 'Fighter', level: 1 }
    });
    const created = await createResponse.json();

    const response = await request.put(`/api/characters/${created.id}`, {
      headers: authHeaders(token),
      data: {
        name: 'Updated Name',
        race: 'Half-Dwarf',
        characterClass: 'Fighter',
        level: 5,
        strength: 18,
        dexterity: 12,
        constitution: 16,
        intelligence: 10,
        wisdom: 12,
        charisma: 8,
        hitPoints: 50,
        armorClass: 18,
        speed: 25,
        backstory: 'Updated backstory'
      }
    });

    expect(response.ok()).toBeTruthy();

    const character = await response.json();
    expect(character.name).toBe('Updated Name');
    expect(character.level).toBe(5);
  });

  test('DELETE /api/characters/{id} - should delete a character', async ({ request }) => {
    const createResponse = await request.post('/api/characters', {
      headers: authHeaders(token),
      data: { name: 'Delete Test', race: 'Halfling', characterClass: 'Rogue', level: 1 }
    });
    const created = await createResponse.json();

    const response = await request.delete(`/api/characters/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(response.status()).toBe(204);

    const getResponse = await request.get(`/api/characters/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(getResponse.status()).toBe(404);
  });

  test('GET /api/characters/campaign/{campaignId} - should list characters by campaign', async ({ request }) => {
    // Create a campaign first
    const campaignResponse = await request.post('/api/campaigns', {
      headers: authHeaders(token),
      data: { name: 'Char Campaign', description: 'Test', setting: 'Test', status: 'ACTIVE' }
    });
    const campaign = await campaignResponse.json();

    const response = await request.get(`/api/characters/campaign/${campaign.id}`, {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();

    const characters = await response.json();
    expect(Array.isArray(characters)).toBeTruthy();
  });

  test('GET /api/characters/user/{userId} - should list characters by user', async ({ request }) => {
    const response = await request.get('/api/characters/user/1', {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();

    const characters = await response.json();
    expect(Array.isArray(characters)).toBeTruthy();
  });
});
