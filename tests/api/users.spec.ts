import { test, expect } from '@playwright/test';
import { getAuthToken, authHeaders } from './test-utils';

test.describe('Users API', () => {
  let token: string;

  test.beforeAll(async ({ request }) => {
    token = await getAuthToken(request);
  });

  test('GET /api/users - should list all users', async ({ request }) => {
    const response = await request.get('/api/users', {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();
    expect(response.status()).toBe(200);

    const users = await response.json();
    expect(Array.isArray(users)).toBeTruthy();
  });

  test('POST /api/users - should create a new user', async ({ request }) => {
    const timestamp = Date.now();
    const newUser = {
      email: `test${timestamp}@example.com`,
      username: `testuser${timestamp}`,
      passwordHash: 'hashed_password_test',
      role: 'PLAYER',
      language: 'en'
    };

    const response = await request.post('/api/users', {
      headers: authHeaders(token),
      data: newUser
    });

    expect(response.status()).toBe(201);

    const user = await response.json();
    expect(user.email).toBe(newUser.email);
    expect(user.id).toBeDefined();
  });

  test('GET /api/users/{id} - should get a single user', async ({ request }) => {
    // Create a user first
    const timestamp = Date.now();
    const createResponse = await request.post('/api/users', {
      headers: authHeaders(token),
      data: {
        email: `getuser${timestamp}@example.com`,
        username: `getuser${timestamp}`,
        passwordHash: 'hash',
        role: 'PLAYER',
        language: 'en'
      }
    });
    const created = await createResponse.json();

    const response = await request.get(`/api/users/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(response.ok()).toBeTruthy();

    const user = await response.json();
    expect(user.id).toBe(created.id);
  });

  test('GET /api/users/{id} - should return 404 for non-existent user', async ({ request }) => {
    const response = await request.get('/api/users/99999', {
      headers: authHeaders(token)
    });
    expect(response.status()).toBe(404);
  });

  test('PUT /api/users/{id} - should update a user', async ({ request }) => {
    const timestamp = Date.now();
    const createResponse = await request.post('/api/users', {
      headers: authHeaders(token),
      data: {
        email: `update${timestamp}@example.com`,
        username: `updateuser${timestamp}`,
        passwordHash: 'hash',
        role: 'PLAYER',
        language: 'en'
      }
    });
    const created = await createResponse.json();

    const response = await request.put(`/api/users/${created.id}`, {
      headers: authHeaders(token),
      data: {
        email: `updated${timestamp}@example.com`,
        username: `updateduser${timestamp}`,
        role: 'DUNGEON_MASTER',
        language: 'ro'
      }
    });

    expect(response.ok()).toBeTruthy();

    const user = await response.json();
    expect(user.role).toBe('DUNGEON_MASTER');
  });

  test('DELETE /api/users/{id} - should delete a user', async ({ request }) => {
    const timestamp = Date.now();
    const createResponse = await request.post('/api/users', {
      headers: authHeaders(token),
      data: {
        email: `delete${timestamp}@example.com`,
        username: `deleteuser${timestamp}`,
        passwordHash: 'hash',
        role: 'PLAYER',
        language: 'en'
      }
    });
    const created = await createResponse.json();

    const response = await request.delete(`/api/users/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(response.status()).toBe(204);

    const getResponse = await request.get(`/api/users/${created.id}`, {
      headers: authHeaders(token)
    });
    expect(getResponse.status()).toBe(404);
  });
});
