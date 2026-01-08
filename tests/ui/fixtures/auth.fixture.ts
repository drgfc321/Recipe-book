import { test as base, Page } from '@playwright/test';
import { VaadinHelpers, generateTestUser } from '../ui-test-utils';

export type AuthFixtures = {
  authenticatedPage: Page;
  testUser: { email: string; username: string; password: string };
};

// Cached test user for the test run
let cachedUser: { email: string; username: string; password: string } | null = null;

/**
 * Extended test with authentication fixtures
 */
export const test = base.extend<AuthFixtures>({
  testUser: async ({}, use) => {
    if (!cachedUser) {
      cachedUser = generateTestUser();
    }
    await use(cachedUser);
  },

  authenticatedPage: async ({ page, testUser }, use) => {
    const helpers = new VaadinHelpers(page);

    // Try to register first
    await page.goto('/register');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();

    await page.getByRole('textbox', { name: 'Email' }).fill(testUser.email);
    await page.getByRole('textbox', { name: 'Username' }).fill(testUser.username);
    await page.getByRole('textbox', { name: 'Password' }).first().fill(testUser.password);
    await page.getByRole('textbox', { name: 'Confirm Password' }).fill(testUser.password);
    await page.getByRole('button', { name: 'Register' }).click();
    await helpers.waitForVaadin();

    // Wait a moment for redirect
    await page.waitForTimeout(1000);
    const url = page.url();

    // If still on register, try login instead (user might already exist)
    if (url.includes('/register')) {
      await page.goto('/login');
      await helpers.waitForVaadin();
      await helpers.closeDevelopmentModeDialog();

      await page.getByRole('textbox', { name: 'Email' }).fill(testUser.email);
      await page.getByRole('textbox', { name: 'Password' }).fill(testUser.password);
      await page.getByRole('button', { name: 'Login' }).click();
      await helpers.waitForVaadin();
    }

    // Should be on dashboard now
    await page.waitForURL('**/');
    await helpers.closeDevelopmentModeDialog();

    await use(page);
  },
});

export { expect } from '@playwright/test';
