import { test, expect } from '@playwright/test';
import { waitForVaadin } from '../helpers/vaadin';
import { login } from '../helpers/auth';

test.describe('Recipes', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('display seeded recipes', async ({ page }) => {
    await page.goto('/recipes');
    await waitForVaadin(page);

    const cards = page.locator('.recipe-list-card');
    await expect(cards).toHaveCount(5, { timeout: 15_000 });
    await expect(page.getByText('Pasta Carbonara')).toBeVisible();
  });

  test('create recipe via dialog', async ({ page }) => {
    await page.goto('/recipes');
    await waitForVaadin(page);

    await page.getByRole('button', { name: 'New Recipe' }).click();
    await waitForVaadin(page);

    const dialog = page.locator('vaadin-dialog-overlay');
    await expect(dialog).toBeVisible();
    await expect(dialog.locator('.dialog-header-tag')).toContainText('NEW');

    // Basic info
    await dialog.getByLabel('Name').fill('E2E Test Pancakes');
    await dialog.getByLabel('Description').fill('Fluffy pancakes for E2E testing');

    // Category combo box
    await dialog.getByLabel('Category').click();
    await page.locator('vaadin-combo-box-item').filter({ hasText: 'BREAKFAST' }).click();
    await waitForVaadin(page);

    // Difficulty combo box
    await dialog.getByLabel('Difficulty').click();
    await page.locator('vaadin-combo-box-item').filter({ hasText: 'EASY' }).click();
    await waitForVaadin(page);

    // Timing & Servings
    await dialog.getByLabel('Prep Time').clear();
    await dialog.getByLabel('Prep Time').fill('10');
    await dialog.getByLabel('Cook Time').clear();
    await dialog.getByLabel('Cook Time').fill('15');
    await dialog.getByLabel('Servings').clear();
    await dialog.getByLabel('Servings').fill('4');

    // Instructions
    await dialog.getByLabel('Instructions').fill('Mix ingredients and cook on a pan.');

    // Add ingredient
    await page.getByRole('button', { name: 'Add Ingredient' }).click();
    await waitForVaadin(page);

    await dialog.getByLabel('Ingredient').click();
    await page.locator('vaadin-combo-box-item').filter({ hasText: 'Eggs' }).click();
    await waitForVaadin(page);

    // Save
    await page.getByRole('button', { name: 'Save' }).click();
    await waitForVaadin(page);

    await expect(page.locator('vaadin-notification-card')).toContainText('Recipe saved');
    await expect(page.getByText('E2E Test Pancakes')).toBeVisible({ timeout: 15_000 });
  });

  test('view recipe detail', async ({ page }) => {
    await page.goto('/recipes');
    await waitForVaadin(page);

    await page.locator('.recipe-list-card').filter({ hasText: 'E2E Test Pancakes' }).click();
    await waitForVaadin(page);

    await expect(page).toHaveURL(/\/recipes\/\d+/);
    await expect(page.locator('h2').filter({ hasText: 'E2E Test Pancakes' })).toBeVisible();
    await expect(page.getByText('BREAKFAST')).toBeVisible();
    await expect(page.getByText('EASY')).toBeVisible();
  });

  test('navigate back', async ({ page }) => {
    await page.goto('/recipes');
    await waitForVaadin(page);

    await page.locator('.recipe-list-card').filter({ hasText: 'E2E Test Pancakes' }).click();
    await waitForVaadin(page);

    await page.getByRole('button', { name: 'Back to Recipes' }).click();
    await waitForVaadin(page);

    await expect(page).toHaveURL(/\/recipes$/);
  });
});
