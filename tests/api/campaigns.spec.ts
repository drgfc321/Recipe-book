import { test, expect } from '@playwright/test';
import { getAuthToken, authHeaders } from './test-utils';

test.describe('Campaigns API', () => {
  let token: string;

  test.beforeAll(async ({ request }) => {
    token = await getAuthToken(request);
  });

  test('GET /api/campaigns - should list all campaigns', async ({ request }) => {
    const response = await request.get('/api/campaigns', {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();
    expect(response.status()).toBe(200);

    const campaigns = await response.json();
    expect(Array.isArray(campaigns)).toBeTruthy();
  });

  test('POST /api/campaigns - should create a new campaign', async ({ request }) => {
    const newCampaign = {
      name: 'Test Campaign',
      description: 'A test campaign created by Playwright',
      setting: 'Test World',
      status: 'ACTIVE'
    };

    const response = await request.post('/api/campaigns', {
      headers: authHeaders(token),
      data: newCampaign
    });

    expect(response.status()).toBe(201);

    const campaign = await response.json();
    expect(campaign.name).toBe(newCampaign.name);
    expect(campaign.id).toBeDefined();
  });

  test('GET /api/campaigns/{id} - should get a single campaign', async ({ request }) => {
    // First create a campaign
    const createResponse = await request.post('/api/campaigns', {
      headers: authHeaders(token),
      data: { name: 'Get Test', description: 'Test', setting: 'Test', status: 'ACTIVE' }
    });
    const created = await createResponse.json();

    const response = await request.get(`/api/campaigns/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();

    const campaign = await response.json();
    expect(campaign.id).toBe(created.id);
  });

  test('GET /api/campaigns/{id} - should return 404 for non-existent campaign', async ({ request }) => {
    const response = await request.get('/api/campaigns/99999', {
      headers: authHeaders(token)
    });
    expect(response.status()).toBe(404);
  });

  test('PUT /api/campaigns/{id} - should update a campaign', async ({ request }) => {
    // First create a campaign
    const createResponse = await request.post('/api/campaigns', {
      headers: authHeaders(token),
      data: { name: 'Update Test', description: 'Original', setting: 'Original', status: 'ACTIVE' }
    });
    const created = await createResponse.json();

    const updatedData = {
      name: 'Updated Campaign Name',
      description: 'Updated description',
      setting: 'Updated Setting',
      status: 'PAUSED'
    };

    const response = await request.put(`/api/campaigns/${created.id}`, {
      headers: authHeaders(token),
      data: updatedData
    });

    expect(response.ok()).toBeTruthy();

    const campaign = await response.json();
    expect(campaign.name).toBe(updatedData.name);
    expect(campaign.status).toBe('PAUSED');
  });

  test('DELETE /api/campaigns/{id} - should delete a campaign', async ({ request }) => {
    // First create a campaign
    const createResponse = await request.post('/api/campaigns', {
      headers: authHeaders(token),
      data: { name: 'Delete Test', description: 'Delete me', setting: 'Test', status: 'ACTIVE' }
    });
    const created = await createResponse.json();

    const response = await request.delete(`/api/campaigns/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(response.status()).toBe(204);

    // Verify it's deleted
    const getResponse = await request.get(`/api/campaigns/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(getResponse.status()).toBe(404);
  });

  test('GET /api/campaigns/{id}/sessions - should list sessions for a campaign', async ({ request }) => {
    // Create a campaign first
    const createResponse = await request.post('/api/campaigns', {
      headers: authHeaders(token),
      data: { name: 'Session Test', description: 'Test', setting: 'Test', status: 'ACTIVE' }
    });
    const campaign = await createResponse.json();

    const response = await request.get(`/api/campaigns/${campaign.id}/sessions`, {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();

    const sessions = await response.json();
    expect(Array.isArray(sessions)).toBeTruthy();
  });

  test('POST /api/campaigns/{id}/sessions - should create a session for a campaign', async ({ request }) => {
    // Create a campaign first
    const createResponse = await request.post('/api/campaigns', {
      headers: authHeaders(token),
      data: { name: 'New Session Test', description: 'Test', setting: 'Test', status: 'ACTIVE' }
    });
    const campaign = await createResponse.json();

    const newSession = {
      sessionNumber: 1,
      sessionDate: '2024-12-01',
      summary: 'Test session',
      xpAwarded: 500,
      notes: 'Test notes'
    };

    const response = await request.post(`/api/campaigns/${campaign.id}/sessions`, {
      headers: authHeaders(token),
      data: newSession
    });

    expect(response.status()).toBe(201);

    const session = await response.json();
    expect(session.sessionNumber).toBe(1);
  });
});
