import { test, expect } from '@playwright/test';

test.describe('Authentication API', () => {
  const timestamp = Date.now();
  const testUser = {
    email: `authtest${timestamp}@example.com`,
    username: `authuser${timestamp}`,
    password: 'testpassword123'
  };
  let authToken: string;

  test('POST /api/auth/register - should register a new user', async ({ request }) => {
    const response = await request.post('/api/auth/register', {
      data: testUser
    });

    expect(response.status()).toBe(201);

    const body = await response.json();
    expect(body.token).toBeDefined();
    expect(body.userId).toBeDefined();
    expect(body.username).toBe(testUser.username);
    expect(body.email).toBe(testUser.email);
    expect(body.role).toBe('PLAYER');
    expect(body.type).toBe('Bearer');

    authToken = body.token;
  });

  test('POST /api/auth/register - should reject duplicate email', async ({ request }) => {
    // First register
    await request.post('/api/auth/register', {
      data: {
        email: `dup${timestamp}@example.com`,
        username: `dupuser${timestamp}`,
        password: 'password123'
      }
    });

    // Try to register again with same email
    const response = await request.post('/api/auth/register', {
      data: {
        email: `dup${timestamp}@example.com`,
        username: `different${timestamp}`,
        password: 'password123'
      }
    });

    expect(response.status()).toBe(409);
    const body = await response.json();
    expect(body.message).toContain('Email');
  });

  test('POST /api/auth/register - should reject duplicate username', async ({ request }) => {
    // First register
    await request.post('/api/auth/register', {
      data: {
        email: `unique${timestamp}@example.com`,
        username: `dupname${timestamp}`,
        password: 'password123'
      }
    });

    // Try to register again with same username
    const response = await request.post('/api/auth/register', {
      data: {
        email: `different${timestamp}@example.com`,
        username: `dupname${timestamp}`,
        password: 'password123'
      }
    });

    expect(response.status()).toBe(409);
    const body = await response.json();
    expect(body.message).toContain('Username');
  });

  test('POST /api/auth/login - should login with valid credentials', async ({ request }) => {
    // First register a user
    const regResponse = await request.post('/api/auth/register', {
      data: {
        email: `login${timestamp}@example.com`,
        username: `loginuser${timestamp}`,
        password: 'mypassword123'
      }
    });
    expect(regResponse.status()).toBe(201);

    // Now login
    const response = await request.post('/api/auth/login', {
      data: {
        email: `login${timestamp}@example.com`,
        password: 'mypassword123'
      }
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.token).toBeDefined();
    expect(body.username).toBe(`loginuser${timestamp}`);
    expect(body.role).toBe('PLAYER');
  });

  test('POST /api/auth/login - should reject invalid password', async ({ request }) => {
    // First register
    await request.post('/api/auth/register', {
      data: {
        email: `badpass${timestamp}@example.com`,
        username: `badpassuser${timestamp}`,
        password: 'correctpassword'
      }
    });

    // Try wrong password
    const response = await request.post('/api/auth/login', {
      data: {
        email: `badpass${timestamp}@example.com`,
        password: 'wrongpassword'
      }
    });

    expect(response.status()).toBe(401);
  });

  test('POST /api/auth/login - should reject non-existent user', async ({ request }) => {
    const response = await request.post('/api/auth/login', {
      data: {
        email: 'nonexistent@example.com',
        password: 'anypassword'
      }
    });

    expect(response.status()).toBe(401);
  });

  test('GET /api/auth/me - should return user info with valid token', async ({ request }) => {
    // Register and get token
    const regResponse = await request.post('/api/auth/register', {
      data: {
        email: `metest${timestamp}@example.com`,
        username: `meuser${timestamp}`,
        password: 'password123'
      }
    });
    const { token } = await regResponse.json();

    // Call /me with token
    const response = await request.get('/api/auth/me', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    expect(response.status()).toBe(200);

    const user = await response.json();
    expect(user.email).toBe(`metest${timestamp}@example.com`);
    expect(user.username).toBe(`meuser${timestamp}`);
  });

  test('GET /api/auth/me - should reject without token', async ({ request }) => {
    const response = await request.get('/api/auth/me');
    expect(response.status()).toBe(401);
  });

  test('Protected routes should require authentication', async ({ request }) => {
    // Test various protected endpoints without token
    const endpoints = [
      { method: 'GET', path: '/api/campaigns' },
      { method: 'GET', path: '/api/users' },
      { method: 'GET', path: '/api/characters' },
      { method: 'GET', path: '/api/sessions' },
    ];

    for (const endpoint of endpoints) {
      const response = await request.get(endpoint.path);
      expect(response.status()).toBe(401);
    }
  });

  test('Protected routes should work with valid token', async ({ request }) => {
    // Register and get token
    const regResponse = await request.post('/api/auth/register', {
      data: {
        email: `protected${timestamp}@example.com`,
        username: `protecteduser${timestamp}`,
        password: 'password123'
      }
    });
    const { token } = await regResponse.json();

    // Test endpoints with token
    const response = await request.get('/api/campaigns', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    expect(response.status()).toBe(200);
  });
});
