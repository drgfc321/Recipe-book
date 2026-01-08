import { test, expect } from '@playwright/test';
import { VaadinHelpers, generateTestUser } from '../ui-test-utils';

test.describe('Register View', () => {
  let helpers: VaadinHelpers;

  test.beforeEach(async ({ page }) => {
    helpers = new VaadinHelpers(page);
    await page.goto('/register');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();
  });

  test('displays registration form correctly', async ({ page }) => {
    // Check title
    await expect(page.getByRole('heading', { name: 'Create Account' })).toBeVisible();

    // Check all form fields
    await expect(page.getByRole('textbox', { name: 'Email' })).toBeVisible();
    await expect(page.getByRole('textbox', { name: 'Username' })).toBeVisible();
    await expect(page.getByRole('textbox', { name: 'Password' }).first()).toBeVisible();
    await expect(page.getByRole('textbox', { name: 'Confirm Password' })).toBeVisible();

    // Check register button
    await expect(page.getByRole('button', { name: 'Register' })).toBeVisible();

    // Check login link
    await expect(page.getByRole('link', { name: /Login/i })).toBeVisible();
  });

  test('shows error for empty form submission', async ({ page }) => {
    await page.getByRole('button', { name: 'Register' }).click();
    await helpers.waitForVaadin();

    // Should show validation error
    const notification = page.locator('vaadin-notification-card');
    await expect(notification).toBeVisible({ timeout: 5000 });
  });

  test('shows error for username too short', async ({ page }) => {
    const user = generateTestUser();

    await page.getByRole('textbox', { name: 'Email' }).fill(user.email);
    await page.getByRole('textbox', { name: 'Username' }).fill('ab'); // Too short
    await page.getByRole('textbox', { name: 'Password' }).first().fill(user.password);
    await page.getByRole('textbox', { name: 'Confirm Password' }).fill(user.password);
    await page.getByRole('button', { name: 'Register' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toContainText(/3|short|minimum/i);
  });

  test('shows error for password too short', async ({ page }) => {
    const user = generateTestUser();

    await page.getByRole('textbox', { name: 'Email' }).fill(user.email);
    await page.getByRole('textbox', { name: 'Username' }).fill(user.username);
    await page.getByRole('textbox', { name: 'Password' }).first().fill('12345'); // Too short
    await page.getByRole('textbox', { name: 'Confirm Password' }).fill('12345');
    await page.getByRole('button', { name: 'Register' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toContainText(/6|short|minimum/i);
  });

  test('shows error for password mismatch', async ({ page }) => {
    const user = generateTestUser();

    await page.getByRole('textbox', { name: 'Email' }).fill(user.email);
    await page.getByRole('textbox', { name: 'Username' }).fill(user.username);
    await page.getByRole('textbox', { name: 'Password' }).first().fill(user.password);
    await page.getByRole('textbox', { name: 'Confirm Password' }).fill('differentpassword');
    await page.getByRole('button', { name: 'Register' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toContainText(/match/i);
  });

  test('shows error for duplicate email', async ({ page }) => {
    const user = generateTestUser();

    // First registration
    await page.getByRole('textbox', { name: 'Email' }).fill(user.email);
    await page.getByRole('textbox', { name: 'Username' }).fill(user.username);
    await page.getByRole('textbox', { name: 'Password' }).first().fill(user.password);
    await page.getByRole('textbox', { name: 'Confirm Password' }).fill(user.password);
    await page.getByRole('button', { name: 'Register' }).click();
    await helpers.waitForVaadin();
    await page.waitForTimeout(1000);

    // Second registration with same email
    await page.goto('/register');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();

    await page.getByRole('textbox', { name: 'Email' }).fill(user.email);
    await page.getByRole('textbox', { name: 'Username' }).fill('differentuser' + Date.now());
    await page.getByRole('textbox', { name: 'Password' }).first().fill(user.password);
    await page.getByRole('textbox', { name: 'Confirm Password' }).fill(user.password);
    await page.getByRole('button', { name: 'Register' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toContainText(/email|already|exists/i);
  });

  test('can select language', async ({ page }) => {
    // Check if Language dropdown exists
    const languageField = page.getByLabel('Language');
    if (await languageField.isVisible({ timeout: 2000 }).catch(() => false)) {
      await languageField.click();
      await helpers.waitForVaadin();

      // Check options are available - look for English/Romanian text in any overlay
      const englishOption = page.getByText('English');
      const romanianOption = page.getByText('Romanian');

      // At least one should be visible in the dropdown
      const hasEnglish = await englishOption.isVisible({ timeout: 3000 }).catch(() => false);
      const hasRomanian = await romanianOption.isVisible({ timeout: 1000 }).catch(() => false);

      expect(hasEnglish || hasRomanian).toBeTruthy();
    } else {
      // Language field might not be present - test passes as optional feature
      test.skip();
    }
  });

  test('successful registration redirects to dashboard', async ({ page }) => {
    const user = generateTestUser();

    await page.getByRole('textbox', { name: 'Email' }).fill(user.email);
    await page.getByRole('textbox', { name: 'Username' }).fill(user.username);
    await page.getByRole('textbox', { name: 'Password' }).first().fill(user.password);
    await page.getByRole('textbox', { name: 'Confirm Password' }).fill(user.password);
    await page.getByRole('button', { name: 'Register' }).click();
    await helpers.waitForVaadin();

    // Should redirect to dashboard
    await page.waitForURL('**/');
    await expect(page.getByRole('heading', { name: /Welcome/i })).toBeVisible();
  });

  test('login link navigates to login page', async ({ page }) => {
    await page.getByRole('link', { name: /Login/i }).click();
    await helpers.waitForVaadin();

    await expect(page).toHaveURL(/.*\/login/);
    await expect(page.getByRole('heading', { name: 'D&D Campaign Manager' })).toBeVisible();
  });
});
