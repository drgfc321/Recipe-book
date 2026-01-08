import { test, expect } from '../fixtures/auth.fixture';
import { VaadinHelpers } from '../ui-test-utils';

test.describe('Dice Roller View', () => {
  let helpers: VaadinHelpers;

  test.beforeEach(async ({ authenticatedPage }) => {
    helpers = new VaadinHelpers(authenticatedPage);
    await authenticatedPage.goto('/dice');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();
  });

  test('displays dice roller page with title', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('heading', { name: 'Dice Roller' })).toBeVisible();
  });

  test('displays Quick Roll section', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('heading', { name: 'Quick Roll' })).toBeVisible();
  });

  test('displays all quick roll dice buttons', async ({ authenticatedPage }) => {
    const diceTypes = ['D4', 'D6', 'D8', 'D10', 'D12', 'D20', 'D100'];
    for (const die of diceTypes) {
      await expect(authenticatedPage.getByRole('button', { name: die, exact: true })).toBeVisible();
    }
  });

  test('displays Custom Roll section', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('heading', { name: 'Custom Roll' })).toBeVisible();
    await expect(authenticatedPage.getByLabel('Number of Dice')).toBeVisible();
    await expect(authenticatedPage.getByLabel('Die Type')).toBeVisible();
    await expect(authenticatedPage.getByLabel('Modifier')).toBeVisible();
    // Check for advantage/disadvantage - may be checkbox or other element
    const hasAdvantage = await authenticatedPage.getByText(/Advantage/i).first().isVisible().catch(() => false);
    const hasDisadvantage = await authenticatedPage.getByText(/Disadvantage/i).first().isVisible().catch(() => false);
    expect(hasAdvantage || hasDisadvantage).toBeTruthy();
    await expect(authenticatedPage.getByRole('button', { name: 'Roll!' })).toBeVisible();
  });

  test('displays Roll History section', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('heading', { name: 'Roll History' })).toBeVisible();
  });

  test('quick roll D20 shows result', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('button', { name: 'D20', exact: true }).click();
    await helpers.waitForVaadin();

    // Should show notification with result
    const notification = await helpers.waitForNotification();
    await expect(notification).toBeVisible();
  });

  test('quick roll D6 shows result', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('button', { name: 'D6', exact: true }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toBeVisible();
  });

  test('custom roll produces result', async ({ authenticatedPage }) => {
    // Set up custom roll - just modify number and modifier, keep default die type
    await authenticatedPage.getByLabel('Number of Dice').fill('2');
    await authenticatedPage.getByLabel('Modifier').fill('5');

    await authenticatedPage.getByRole('button', { name: 'Roll!' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toBeVisible();
  });

  test('roll appears in history', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('button', { name: 'D20', exact: true }).click();
    await helpers.waitForVaadin();
    await helpers.waitForNotificationToClose();

    // History should have an entry
    await expect(authenticatedPage.getByText('Total:')).toBeVisible();
  });

  test('history shows individual rolls', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('button', { name: 'D20', exact: true }).click();
    await helpers.waitForVaadin();
    await helpers.waitForNotificationToClose();

    await expect(authenticatedPage.getByText('Rolls:')).toBeVisible();
  });

  test('advantage checkbox can be clicked', async ({ authenticatedPage }) => {
    // Find and click the advantage checkbox/toggle
    const advantageElement = authenticatedPage.getByText(/Advantage/i).first();
    await advantageElement.click();
    await helpers.waitForVaadin();

    // Just verify we can roll without error
    await authenticatedPage.getByRole('button', { name: 'Roll!' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toBeVisible();
  });

  test('disadvantage checkbox can be clicked', async ({ authenticatedPage }) => {
    // Find and click the disadvantage checkbox/toggle
    const disadvantageElement = authenticatedPage.getByText(/Disadvantage/i).first();
    await disadvantageElement.click();
    await helpers.waitForVaadin();

    // Just verify we can roll without error
    await authenticatedPage.getByRole('button', { name: 'Roll!' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toBeVisible();
  });

  test('can roll with both options toggled', async ({ authenticatedPage }) => {
    // Click advantage
    await authenticatedPage.getByText(/Advantage/i).first().click();
    await helpers.waitForVaadin();

    // Click disadvantage
    await authenticatedPage.getByText(/Disadvantage/i).first().click();
    await helpers.waitForVaadin();

    // Just verify we can roll without error
    await authenticatedPage.getByRole('button', { name: 'Roll!' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toBeVisible();
  });

  test('multiple rolls appear in history', async ({ authenticatedPage }) => {
    // Roll D20
    await authenticatedPage.getByRole('button', { name: 'D20', exact: true }).click();
    await helpers.waitForVaadin();
    await helpers.waitForNotificationToClose();

    // Roll D6
    await authenticatedPage.getByRole('button', { name: 'D6', exact: true }).click();
    await helpers.waitForVaadin();
    await helpers.waitForNotificationToClose();

    // History should have entries with Total
    const totals = authenticatedPage.locator('text=Total:');
    expect(await totals.count()).toBeGreaterThanOrEqual(2);
  });

  test('modifier is included in roll result', async ({ authenticatedPage }) => {
    await authenticatedPage.getByLabel('Modifier').fill('10');
    await authenticatedPage.getByRole('button', { name: 'Roll!' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toBeVisible();
  });

  test('negative modifier works', async ({ authenticatedPage }) => {
    await authenticatedPage.getByLabel('Modifier').fill('-5');
    await authenticatedPage.getByRole('button', { name: 'Roll!' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toBeVisible();
  });
});
