import { test, expect } from '@playwright/test';
import { getAuthToken, authHeaders } from './test-utils';

test.describe('Sessions API', () => {
  let token: string;

  test.beforeAll(async ({ request }) => {
    token = await getAuthToken(request);
  });

  test('GET /api/sessions - should list all sessions', async ({ request }) => {
    const response = await request.get('/api/sessions', {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();
    expect(response.status()).toBe(200);

    const sessions = await response.json();
    expect(Array.isArray(sessions)).toBeTruthy();
  });

  test('GET /api/sessions/{id} - should get a single session', async ({ request }) => {
    // Create a campaign and session first
    const campaignResponse = await request.post('/api/campaigns', {
      headers: authHeaders(token),
      data: { name: 'Session Get Test', description: 'Test', setting: 'Test', status: 'ACTIVE' }
    });
    const campaign = await campaignResponse.json();

    const createResponse = await request.post(`/api/campaigns/${campaign.id}/sessions`, {
      headers: authHeaders(token),
      data: { sessionNumber: 1, summary: 'Test', xpAwarded: 100 }
    });
    const created = await createResponse.json();

    const response = await request.get(`/api/sessions/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();

    const session = await response.json();
    expect(session.id).toBe(created.id);
  });

  test('GET /api/sessions/{id} - should return 404 for non-existent session', async ({ request }) => {
    const response = await request.get('/api/sessions/99999', {
      headers: authHeaders(token)
    });
    expect(response.status()).toBe(404);
  });

  test('PUT /api/sessions/{id} - should update a session', async ({ request }) => {
    // Create a campaign and session first
    const campaignResponse = await request.post('/api/campaigns', {
      headers: authHeaders(token),
      data: { name: 'Session Update Test', description: 'Test', setting: 'Test', status: 'ACTIVE' }
    });
    const campaign = await campaignResponse.json();

    const createResponse = await request.post(`/api/campaigns/${campaign.id}/sessions`, {
      headers: authHeaders(token),
      data: { sessionNumber: 1, summary: 'Original', xpAwarded: 100 }
    });
    const created = await createResponse.json();

    const response = await request.put(`/api/sessions/${created.id}`, {
      headers: authHeaders(token),
      data: {
        sessionNumber: 1,
        sessionDate: '2024-12-15',
        summary: 'Updated summary',
        xpAwarded: 250,
        notes: 'Updated notes'
      }
    });

    expect(response.ok()).toBeTruthy();

    const session = await response.json();
    expect(session.summary).toBe('Updated summary');
    expect(session.xpAwarded).toBe(250);
  });

  test('DELETE /api/sessions/{id} - should delete a session', async ({ request }) => {
    // Create a campaign and session first
    const campaignResponse = await request.post('/api/campaigns', {
      headers: authHeaders(token),
      data: { name: 'Session Delete Test', description: 'Test', setting: 'Test', status: 'ACTIVE' }
    });
    const campaign = await campaignResponse.json();

    const createResponse = await request.post(`/api/campaigns/${campaign.id}/sessions`, {
      headers: authHeaders(token),
      data: { sessionNumber: 999, summary: 'Delete me', xpAwarded: 0 }
    });
    const created = await createResponse.json();

    const response = await request.delete(`/api/sessions/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(response.status()).toBe(204);

    const getResponse = await request.get(`/api/sessions/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(getResponse.status()).toBe(404);
  });
});
