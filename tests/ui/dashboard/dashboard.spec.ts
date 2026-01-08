import { test, expect } from '../fixtures/auth.fixture';
import { VaadinHelpers } from '../ui-test-utils';

test.describe('Dashboard View', () => {
  let helpers: VaadinHelpers;

  test.beforeEach(async ({ authenticatedPage }) => {
    helpers = new VaadinHelpers(authenticatedPage);
  });

  test('displays welcome message with username', async ({ authenticatedPage, testUser }) => {
    const welcome = authenticatedPage.getByRole('heading', { name: /Welcome/i });
    await expect(welcome).toBeVisible();
    await expect(welcome).toContainText(testUser.username);
  });

  test('displays stats cards', async ({ authenticatedPage }) => {
    // Stats cards section should exist with campaign and character info
    // Look for any text containing campaign stats
    const hasTotalCampaigns = await authenticatedPage.getByText('Total Campaigns').isVisible().catch(() => false);
    const hasActiveCampaigns = await authenticatedPage.getByText('Active Campaigns').isVisible().catch(() => false);
    const hasCampaigns = await authenticatedPage.getByText(/Campaigns:/).isVisible().catch(() => false);

    expect(hasTotalCampaigns || hasActiveCampaigns || hasCampaigns).toBeTruthy();
  });

  test('stats cards show numeric values', async ({ authenticatedPage }) => {
    // Each stat card should show a number (could be 0)
    const statValues = authenticatedPage.getByRole('heading', { level: 3 });
    const count = await statValues.count();
    expect(count).toBeGreaterThanOrEqual(3);
  });

  test('displays quick action cards', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByText('New Campaign')).toBeVisible();
    await expect(authenticatedPage.getByText('New Character')).toBeVisible();
    await expect(authenticatedPage.getByText('Roll Dice')).toBeVisible();
  });

  test('New Campaign action navigates to campaigns', async ({ authenticatedPage }) => {
    await authenticatedPage.getByText('New Campaign').click();
    await helpers.waitForVaadin();

    await expect(authenticatedPage).toHaveURL(/.*\/campaigns/);
  });

  test('New Character action navigates to characters', async ({ authenticatedPage }) => {
    await authenticatedPage.getByText('New Character').click();
    await helpers.waitForVaadin();

    await expect(authenticatedPage).toHaveURL(/.*\/characters/);
  });

  test('Roll Dice action navigates to dice roller', async ({ authenticatedPage }) => {
    await authenticatedPage.getByText('Roll Dice').click();
    await helpers.waitForVaadin();

    await expect(authenticatedPage).toHaveURL(/.*\/dice/);
  });
});
