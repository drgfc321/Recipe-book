import { test, expect } from '@playwright/test';
import { waitForVaadin } from '../helpers/vaadin';
import { login } from '../helpers/auth';

test.describe('Login & Register', () => {
  test('show login page', async ({ page }) => {
    await page.goto('/login');
    await waitForVaadin(page);

    await expect(page.getByLabel('Email')).toBeVisible();
    await expect(page.getByLabel('Password')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();
  });

  test('reject invalid credentials', async ({ page }) => {
    await page.goto('/login');
    await waitForVaadin(page);

    await page.getByLabel('Email').fill('wrong@example.com');
    await page.getByLabel('Password').fill('wrongpassword');
    await page.getByRole('button', { name: 'Login' }).click();
    await waitForVaadin(page);

    await expect(page.locator('vaadin-notification-card')).toContainText('Invalid email or password');
  });

  test('login with seeded admin', async ({ page }) => {
    await login(page);

    await expect(page.locator('h2').filter({ hasText: 'Welcome back, chef_admin' })).toBeVisible();
    await expect(page.locator('.dashboard-stat-card').first()).toBeVisible();
  });

  test('logout', async ({ page }) => {
    await login(page);

    await page.getByRole('button', { name: 'Logout' }).click();
    await waitForVaadin(page);

    await expect(page).toHaveURL(/\/login/);
  });

  test('register new user', async ({ page }) => {
    const ts = Date.now();
    await page.goto('/register');
    await waitForVaadin(page);

    await page.getByLabel('Email').fill(`test${ts}@example.com`);
    await page.getByLabel('Username').fill(`user${ts}`);
    await page.getByLabel('Password', { exact: true }).fill('testpass123');
    await page.getByLabel('Confirm Password').fill('testpass123');
    await page.getByRole('button', { name: 'Register' }).click();
    await waitForVaadin(page);

    await expect(page.locator('vaadin-notification-card')).toContainText('Registration successful!');
    // After successful registration, user is redirected to dashboard
    await expect(page.locator('h2').filter({ hasText: 'Welcome back' })).toBeVisible({ timeout: 15_000 });
  });

  test('reject duplicate registration', async ({ page }) => {
    await page.goto('/register');
    await waitForVaadin(page);

    await page.getByLabel('Email').fill('admin@recipebook.com');
    await page.getByLabel('Username').fill('chef_admin');
    await page.getByLabel('Password', { exact: true }).fill('testpass123');
    await page.getByLabel('Confirm Password').fill('testpass123');
    await page.getByRole('button', { name: 'Register' }).click();
    await waitForVaadin(page);

    await expect(page.locator('vaadin-notification-card')).toContainText('already exists');
  });
});
