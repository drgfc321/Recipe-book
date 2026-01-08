import { test, expect } from '@playwright/test';
import { getAuthToken, authHeaders } from './test-utils';

test.describe('Dice Roller API', () => {
  let token: string;

  test.beforeAll(async ({ request }) => {
    token = await getAuthToken(request);
  });

  test('POST /api/dice/roll - should roll a single d20', async ({ request }) => {
    const response = await request.post('/api/dice/roll', {
      headers: authHeaders(token),
      data: { dice: '1d20' }
    });

    expect(response.ok()).toBeTruthy();

    const result = await response.json();
    expect(result.dice).toBe('1d20');
    expect(result.rolls).toHaveLength(1);
    expect(result.rolls[0]).toBeGreaterThanOrEqual(1);
    expect(result.rolls[0]).toBeLessThanOrEqual(20);
  });

  test('POST /api/dice/roll - should roll multiple dice', async ({ request }) => {
    const response = await request.post('/api/dice/roll', {
      headers: authHeaders(token),
      data: { dice: '4d6' }
    });

    expect(response.ok()).toBeTruthy();

    const result = await response.json();
    expect(result.rolls).toHaveLength(4);

    for (const roll of result.rolls) {
      expect(roll).toBeGreaterThanOrEqual(1);
      expect(roll).toBeLessThanOrEqual(6);
    }
  });

  test('POST /api/dice/roll - should apply modifier', async ({ request }) => {
    const response = await request.post('/api/dice/roll', {
      headers: authHeaders(token),
      data: { dice: '1d20', modifier: 5 }
    });

    expect(response.ok()).toBeTruthy();

    const result = await response.json();
    expect(result.modifier).toBe(5);
    expect(result.total).toBe(result.rolls[0] + 5);
  });

  test('POST /api/dice/roll - should apply negative modifier', async ({ request }) => {
    const response = await request.post('/api/dice/roll', {
      headers: authHeaders(token),
      data: { dice: '1d20', modifier: -2 }
    });

    expect(response.ok()).toBeTruthy();

    const result = await response.json();
    expect(result.modifier).toBe(-2);
    expect(result.total).toBe(result.rolls[0] - 2);
  });

  test('POST /api/dice/roll - should handle advantage', async ({ request }) => {
    const response = await request.post('/api/dice/roll', {
      headers: authHeaders(token),
      data: { dice: '1d20', advantage: true }
    });

    expect(response.ok()).toBeTruthy();

    const result = await response.json();
    expect(result.rolls).toHaveLength(2);
    expect(result.description).toContain('Advantage');
  });

  test('POST /api/dice/roll - should handle disadvantage', async ({ request }) => {
    const response = await request.post('/api/dice/roll', {
      headers: authHeaders(token),
      data: { dice: '1d20', disadvantage: true }
    });

    expect(response.ok()).toBeTruthy();

    const result = await response.json();
    expect(result.rolls).toHaveLength(2);
    expect(result.description).toContain('Disadvantage');
  });

  test('POST /api/dice/roll - should handle advantage with modifier', async ({ request }) => {
    const response = await request.post('/api/dice/roll', {
      headers: authHeaders(token),
      data: { dice: '1d20', advantage: true, modifier: 3 }
    });

    expect(response.ok()).toBeTruthy();

    const result = await response.json();
    expect(result.rolls).toHaveLength(2);
    const maxRoll = Math.max(result.rolls[0], result.rolls[1]);
    expect(result.total).toBe(maxRoll + 3);
  });

  test('POST /api/dice/roll - should roll various dice types', async ({ request }) => {
    const diceTypes = ['d4', 'd6', 'd8', 'd10', 'd12', 'd20', 'd100'];

    for (const die of diceTypes) {
      const response = await request.post('/api/dice/roll', {
        headers: authHeaders(token),
        data: { dice: `1${die}` }
      });

      expect(response.ok()).toBeTruthy();

      const result = await response.json();
      const maxValue = parseInt(die.substring(1));
      expect(result.rolls[0]).toBeGreaterThanOrEqual(1);
      expect(result.rolls[0]).toBeLessThanOrEqual(maxValue);
    }
  });

  test('POST /api/dice/roll - should reject invalid dice notation', async ({ request }) => {
    const response = await request.post('/api/dice/roll', {
      headers: authHeaders(token),
      data: { dice: 'invalid' }
    });

    expect(response.status()).toBe(400);
  });

  test('POST /api/dice/roll - should reject missing dice parameter', async ({ request }) => {
    const response = await request.post('/api/dice/roll', {
      headers: authHeaders(token),
      data: {}
    });

    expect(response.status()).toBe(400);
  });
});
