import { APIRequestContext } from '@playwright/test';

let cachedToken: string | null = null;
const timestamp = Date.now();

export async function getAuthToken(request: APIRequestContext): Promise<string> {
  if (cachedToken) {
    return cachedToken;
  }

  // Register a test user
  const response = await request.post('/api/auth/register', {
    data: {
      email: `testrunner${timestamp}@example.com`,
      username: `testrunner${timestamp}`,
      password: 'testpassword123'
    }
  });

  if (response.status() === 201) {
    const body = await response.json();
    cachedToken = body.token;
    return cachedToken;
  }

  // If registration failed (user exists), try login
  const loginResponse = await request.post('/api/auth/login', {
    data: {
      email: `testrunner${timestamp}@example.com`,
      password: 'testpassword123'
    }
  });

  const body = await loginResponse.json();
  cachedToken = body.token;
  return cachedToken;
}

export function authHeaders(token: string) {
  return {
    'Authorization': `Bearer ${token}`
  };
}
