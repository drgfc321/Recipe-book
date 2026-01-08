import { test, expect } from '@playwright/test';
import { VaadinHelpers, generateTestUser, register } from '../ui-test-utils';

test.describe('Login View', () => {
  let helpers: VaadinHelpers;

  test.beforeEach(async ({ page }) => {
    helpers = new VaadinHelpers(page);
    await page.goto('/login');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();
  });

  test('displays login form correctly', async ({ page }) => {
    // Check title
    await expect(page.getByRole('heading', { name: 'D&D Campaign Manager' })).toBeVisible();

    // Check form fields
    await expect(page.getByRole('textbox', { name: 'Email' })).toBeVisible();
    await expect(page.getByRole('textbox', { name: 'Password' })).toBeVisible();

    // Check login button
    await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();

    // Check register link
    await expect(page.getByRole('link', { name: /Register/i })).toBeVisible();
  });

  test('shows error for empty form submission', async ({ page }) => {
    await page.getByRole('button', { name: 'Login' }).click();
    await helpers.waitForVaadin();

    // Should show validation or error notification
    const notification = page.locator('vaadin-notification-card');
    await expect(notification).toBeVisible({ timeout: 5000 });
  });

  test('shows error for invalid credentials', async ({ page }) => {
    await page.getByRole('textbox', { name: 'Email' }).fill('nonexistent@example.com');
    await page.getByRole('textbox', { name: 'Password' }).fill('wrongpassword');
    await page.getByRole('button', { name: 'Login' }).click();
    await helpers.waitForVaadin();

    // Should show error notification
    const notification = await helpers.waitForNotification();
    await expect(notification).toContainText(/Invalid|error|failed/i);
  });

  test('successful login redirects to dashboard', async ({ page }) => {
    // First register a user
    const user = generateTestUser();
    await register(page, user.email, user.username, user.password);

    // Wait for redirect
    await page.waitForTimeout(1000);

    // Now login
    await page.goto('/login');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();

    await page.getByRole('textbox', { name: 'Email' }).fill(user.email);
    await page.getByRole('textbox', { name: 'Password' }).fill(user.password);
    await page.getByRole('button', { name: 'Login' }).click();
    await helpers.waitForVaadin();

    // Should be on dashboard
    await expect(page).toHaveURL(/.*\//);
    await expect(page.getByRole('heading', { name: /Welcome/i })).toBeVisible();
  });

  test('register link navigates to registration page', async ({ page }) => {
    await page.getByRole('link', { name: /Register/i }).click();
    await helpers.waitForVaadin();

    await expect(page).toHaveURL(/.*\/register/);
    await expect(page.getByRole('heading', { name: 'Create Account' })).toBeVisible();
  });

  test('enter key submits form', async ({ page }) => {
    await page.getByRole('textbox', { name: 'Email' }).fill('test@example.com');
    await page.getByRole('textbox', { name: 'Password' }).fill('password');
    await page.getByRole('textbox', { name: 'Password' }).press('Enter');
    await helpers.waitForVaadin();

    // Should attempt login (showing error notification since user doesn't exist)
    const notification = page.locator('vaadin-notification-card');
    await expect(notification).toBeVisible({ timeout: 5000 });
  });
});
