import { test, expect } from '@playwright/test';
import { waitForVaadin } from '../helpers/vaadin';
import { login } from '../helpers/auth';

test.describe('Meal Plan', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('display meal plan calendar', async ({ page }) => {
    await page.goto('/meal-plan');
    await waitForVaadin(page);

    await expect(page.locator('h2').filter({ hasText: 'Meal Planner' })).toBeVisible();
    await expect(page.locator('.meal-plan-grid')).toBeVisible();

    // Verify slot labels are visible (Breakfast, Lunch, Dinner, Snack)
    await expect(page.getByText('Breakfast')).toBeVisible();
    await expect(page.getByText('Lunch')).toBeVisible();
    await expect(page.getByText('Dinner')).toBeVisible();
    await expect(page.getByText('Snack')).toBeVisible();
  });

  test('assign recipes to meal slots', async ({ page }) => {
    await page.goto('/meal-plan');
    await waitForVaadin(page);

    // Click the first empty meal cell
    const emptyCell = page.locator('.meal-cell-empty').first();
    await emptyCell.click();
    await waitForVaadin(page);

    // Recipe picker dialog should appear
    const dialog = page.locator('vaadin-dialog-overlay');
    await expect(dialog).toBeVisible();
    await expect(dialog.locator('h2').filter({ hasText: 'Pick a Recipe' })).toBeVisible();
    await expect(dialog.locator('.dialog-header-tag')).toBeVisible();

    // Pick the first recipe card
    const recipeCard = dialog.locator('.recipe-picker-card').first();
    const recipeName = await recipeCard.locator('span').first().textContent();
    await recipeCard.click();
    await waitForVaadin(page);

    // Assert assignment notification
    await expect(page.locator('vaadin-notification-card')).toContainText('assigned');

    // A filled cell should now exist
    await expect(page.locator('.meal-cell-filled').first()).toBeVisible();
  });

  test('show weekly summary', async ({ page }) => {
    await page.goto('/meal-plan');
    await waitForVaadin(page);

    await expect(page.locator('.meal-plan-weekly-summary')).toBeVisible();
    await expect(page.getByText('Weekly Total')).toBeVisible();
    await expect(page.getByText('Daily Average')).toBeVisible();
  });

  test('navigate weeks', async ({ page }) => {
    await page.goto('/meal-plan');
    await waitForVaadin(page);

    // Get current week label
    const weekNav = page.locator('.meal-plan-week-nav');
    const initialLabel = await weekNav.locator('span').textContent();

    // Click next week button (second .week-nav-btn)
    const nextBtn = page.locator('.week-nav-btn').last();
    await nextBtn.click();
    await waitForVaadin(page);

    // Week label should change
    const newLabel = await weekNav.locator('span').textContent();
    expect(newLabel).not.toBe(initialLabel);

    // Click "Today" to go back
    await page.getByRole('button', { name: 'Today' }).click();
    await waitForVaadin(page);

    const resetLabel = await weekNav.locator('span').textContent();
    expect(resetLabel).toBe(initialLabel);
  });
});
