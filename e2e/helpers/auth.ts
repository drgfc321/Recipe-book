import { Page, expect } from '@playwright/test';
import { waitForVaadin } from './vaadin';

/**
 * Logs in via the /login page using the given credentials.
 * Defaults to the seeded admin account.
 */
export async function login(
  page: Page,
  email = 'admin@recipebook.com',
  password = 'admin123',
): Promise<void> {
  await page.goto('/login');
  await waitForVaadin(page);

  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Login' }).click();
  await waitForVaadin(page);

  // Wait for dashboard to load
  await expect(page.locator('h2').filter({ hasText: 'Welcome back' })).toBeVisible({ timeout: 15_000 });
}
