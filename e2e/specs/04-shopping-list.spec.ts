import { test, expect } from '@playwright/test';
import { waitForVaadin } from '../helpers/vaadin';
import { login } from '../helpers/auth';

test.describe('Shopping List', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('display shopping list page', async ({ page }) => {
    await page.goto('/shopping-list');
    await waitForVaadin(page);

    await expect(page.locator('h2').filter({ hasText: 'Shopping List' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Generate from Meal Plan' })).toBeVisible();
  });

  test('generate from meal plan', async ({ page }) => {
    await page.goto('/shopping-list');
    await waitForVaadin(page);

    await page.getByRole('button', { name: 'Generate from Meal Plan' }).click();
    await waitForVaadin(page);

    await expect(page.locator('vaadin-notification-card')).toContainText('Shopping list generated!');

    const items = page.locator('.shopping-item-row');
    await expect(items.first()).toBeVisible({ timeout: 15_000 });
    expect(await items.count()).toBeGreaterThan(0);

    // Progress label should show "0 / N items (0%)"
    await expect(page.getByText(/\d+ \/ \d+ items/)).toBeVisible();
  });

  test('verify ingredients present', async ({ page }) => {
    await page.goto('/shopping-list');
    await waitForVaadin(page);

    // At least some ingredient names should be visible from the assigned meals
    const items = page.locator('.shopping-item-row');
    await expect(items.first()).toBeVisible({ timeout: 15_000 });
    expect(await items.count()).toBeGreaterThan(0);
  });

  test('toggle item purchased', async ({ page }) => {
    await page.goto('/shopping-list');
    await waitForVaadin(page);

    const items = page.locator('.shopping-item-row');
    await expect(items.first()).toBeVisible({ timeout: 15_000 });

    // Get the initial progress text
    const progressText = await page.getByText(/\d+ \/ \d+ items/).textContent();

    // Click the checkbox in the first item row
    await items.first().locator('vaadin-checkbox').click();
    await waitForVaadin(page);

    // Progress should update
    const updatedText = await page.getByText(/\d+ \/ \d+ items/).textContent();
    expect(updatedText).not.toBe(progressText);
  });

  test('clear shopping list', async ({ page }) => {
    await page.goto('/shopping-list');
    await waitForVaadin(page);

    // Ensure items exist before clearing
    await expect(page.locator('.shopping-item-row').first()).toBeVisible({ timeout: 15_000 });

    await page.getByRole('button', { name: 'Clear List' }).click();
    await waitForVaadin(page);

    // Confirm dialog
    const confirmDialog = page.locator('vaadin-confirm-dialog-overlay');
    await expect(confirmDialog).toBeVisible();
    await expect(confirmDialog.getByText('Are you sure')).toBeVisible();
    await confirmDialog.getByRole('button', { name: 'Clear' }).click();
    await waitForVaadin(page);

    await expect(page.locator('vaadin-notification-card')).toContainText('Shopping list cleared');
    await expect(page.locator('.shopping-item-row')).toHaveCount(0);
  });
});
