import { test, expect } from '../fixtures/auth.fixture';
import { VaadinHelpers } from '../ui-test-utils';

test.describe('Main Layout Navigation', () => {
  let helpers: VaadinHelpers;

  test.beforeEach(async ({ authenticatedPage }) => {
    helpers = new VaadinHelpers(authenticatedPage);
  });

  test('displays all navigation items', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('link', { name: /Dashboard/i })).toBeVisible();
    await expect(authenticatedPage.getByRole('link', { name: /Campaigns/i })).toBeVisible();
    await expect(authenticatedPage.getByRole('link', { name: /Characters/i })).toBeVisible();
    await expect(authenticatedPage.getByRole('link', { name: /Dice Roller/i })).toBeVisible();
  });

  test('navigates to Dashboard', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('link', { name: /Dashboard/i }).click();
    await helpers.waitForVaadin();

    await expect(authenticatedPage).toHaveURL(/.*\//);
    await expect(authenticatedPage.getByRole('heading', { name: /Welcome/i })).toBeVisible();
  });

  test('navigates to Campaigns', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('link', { name: /Campaigns/i }).click();
    await helpers.waitForVaadin();

    await expect(authenticatedPage).toHaveURL(/.*\/campaigns/);
    await expect(authenticatedPage.getByRole('heading', { name: 'Campaigns' })).toBeVisible();
  });

  test('navigates to Characters', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('link', { name: /Characters/i }).click();
    await helpers.waitForVaadin();

    await expect(authenticatedPage).toHaveURL(/.*\/characters/);
    await expect(authenticatedPage.getByRole('heading', { name: 'Characters' })).toBeVisible();
  });

  test('navigates to Dice Roller', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('link', { name: /Dice Roller/i }).click();
    await helpers.waitForVaadin();

    await expect(authenticatedPage).toHaveURL(/.*\/dice/);
    await expect(authenticatedPage.getByRole('heading', { name: 'Dice Roller' })).toBeVisible();
  });

  test('displays user info', async ({ authenticatedPage, testUser }) => {
    // Should display the username somewhere OR a logout button (indicating user is logged in)
    const hasUsername = await authenticatedPage.getByText(testUser.username).isVisible().catch(() => false);
    const hasUserEmail = await authenticatedPage.getByText(testUser.email).isVisible().catch(() => false);
    const hasLogout = await authenticatedPage.getByRole('button', { name: /Logout/i }).isVisible().catch(() => false);

    expect(hasUsername || hasUserEmail || hasLogout).toBeTruthy();
  });

  test('logout redirects to login page', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('button', { name: /Logout/i }).click();
    await helpers.waitForVaadin();

    await expect(authenticatedPage).toHaveURL(/.*\/login/);
  });
});

test.describe('Route Protection', () => {
  test('redirects to login when accessing protected route without auth', async ({ page }) => {
    const helpers = new VaadinHelpers(page);

    await page.goto('/campaigns');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();
    await page.waitForTimeout(2000);

    // Should either redirect to login OR show login content
    const url = page.url();
    const hasLoginUrl = url.includes('/login');
    const hasLoginForm = await page.getByRole('button', { name: 'Login' }).isVisible().catch(() => false);

    expect(hasLoginUrl || hasLoginForm).toBeTruthy();
  });

  test('redirects to login when accessing dashboard without auth', async ({ page }) => {
    const helpers = new VaadinHelpers(page);

    await page.goto('/');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();
    await page.waitForTimeout(2000);

    // Should either redirect to login OR show login content
    const url = page.url();
    const hasLoginUrl = url.includes('/login');
    const hasLoginForm = await page.getByRole('button', { name: 'Login' }).isVisible().catch(() => false);

    expect(hasLoginUrl || hasLoginForm).toBeTruthy();
  });
});
