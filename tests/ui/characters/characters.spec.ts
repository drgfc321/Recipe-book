import { test, expect } from '../fixtures/auth.fixture';
import { VaadinHelpers, generateCharacterData } from '../ui-test-utils';

test.describe('Characters View', () => {
  let helpers: VaadinHelpers;

  test.beforeEach(async ({ authenticatedPage }) => {
    helpers = new VaadinHelpers(authenticatedPage);
    await authenticatedPage.goto('/characters');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();
  });

  test('displays characters page with title', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('heading', { name: 'Characters' })).toBeVisible();
  });

  test('displays New Character button', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('button', { name: 'New Character' })).toBeVisible();
  });

  test('displays grid', async ({ authenticatedPage }) => {
    const grid = authenticatedPage.locator('vaadin-grid');
    await expect(grid).toBeVisible();
  });

  test('New Character button opens dialog', async ({ authenticatedPage }) => {
    // Close dev mode dialog first if present
    await helpers.closeDevelopmentModeDialog();

    await authenticatedPage.getByRole('button', { name: 'New Character' }).click();
    await helpers.waitForVaadin();

    // Wait for dialog overlay to open (Vaadin dialogs use this structure)
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    await expect(authenticatedPage.getByRole('textbox', { name: 'Name' })).toBeVisible();
    // Race and Class are select buttons
    await expect(authenticatedPage.getByRole('button', { name: /Race/i })).toBeVisible();
    await expect(authenticatedPage.getByRole('button', { name: /Class/i })).toBeVisible();
    await expect(authenticatedPage.getByRole('spinbutton', { name: 'Level' })).toBeVisible();
  });

  test('dialog shows ability score fields', async ({ authenticatedPage }) => {
    await helpers.closeDevelopmentModeDialog();
    await authenticatedPage.getByRole('button', { name: 'New Character' }).click();
    await helpers.waitForVaadin();
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    await expect(authenticatedPage.getByRole('spinbutton', { name: 'STR' })).toBeVisible();
    await expect(authenticatedPage.getByRole('spinbutton', { name: 'DEX' })).toBeVisible();
    await expect(authenticatedPage.getByRole('spinbutton', { name: 'CON' })).toBeVisible();
    await expect(authenticatedPage.getByRole('spinbutton', { name: 'INT' })).toBeVisible();
    await expect(authenticatedPage.getByRole('spinbutton', { name: 'WIS' })).toBeVisible();
    await expect(authenticatedPage.getByRole('spinbutton', { name: 'CHA' })).toBeVisible();
  });

  test('dialog shows combat stat fields', async ({ authenticatedPage }) => {
    await helpers.closeDevelopmentModeDialog();
    await authenticatedPage.getByRole('button', { name: 'New Character' }).click();
    await helpers.waitForVaadin();
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    await expect(authenticatedPage.getByRole('spinbutton', { name: 'Hit Points' })).toBeVisible();
    await expect(authenticatedPage.getByRole('spinbutton', { name: 'Armor Class' })).toBeVisible();
    await expect(authenticatedPage.getByRole('spinbutton', { name: 'Speed' })).toBeVisible();
  });

  test('race dropdown has options', async ({ authenticatedPage }) => {
    await helpers.closeDevelopmentModeDialog();
    await authenticatedPage.getByRole('button', { name: 'New Character' }).click();
    await helpers.waitForVaadin();
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    await authenticatedPage.getByRole('button', { name: /Race/i }).click();
    await helpers.waitForVaadin();

    // Check for D&D races
    await expect(authenticatedPage.getByText('Human')).toBeVisible();
    await expect(authenticatedPage.getByText('Elf')).toBeVisible();
    await expect(authenticatedPage.getByText('Dwarf')).toBeVisible();
  });

  test('class dropdown has options', async ({ authenticatedPage }) => {
    await helpers.closeDevelopmentModeDialog();
    await authenticatedPage.getByRole('button', { name: 'New Character' }).click();
    await helpers.waitForVaadin();
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    await authenticatedPage.getByRole('button', { name: /Class(?!.*Armor)/i }).click();
    await helpers.waitForVaadin();

    // Check for D&D classes
    await expect(authenticatedPage.getByText('Fighter')).toBeVisible();
    await expect(authenticatedPage.getByText('Wizard')).toBeVisible();
    await expect(authenticatedPage.getByText('Rogue')).toBeVisible();
  });

  test('can create a character', async ({ authenticatedPage }) => {
    const character = generateCharacterData();

    await helpers.closeDevelopmentModeDialog();
    await authenticatedPage.getByRole('button', { name: 'New Character' }).click();
    await helpers.waitForVaadin();
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    // Fill basic info
    await authenticatedPage.getByRole('textbox', { name: 'Name' }).fill(character.name);

    // Select race
    await authenticatedPage.getByRole('button', { name: /Race/i }).click();
    await authenticatedPage.getByText(character.race).click();
    await helpers.waitForVaadin();

    // Select class
    await authenticatedPage.getByRole('button', { name: /Class(?!.*Armor)/i }).click();
    await authenticatedPage.getByText(character.characterClass).click();
    await helpers.waitForVaadin();

    // Set level
    await authenticatedPage.getByRole('spinbutton', { name: 'Level' }).fill(character.level.toString());

    // Save
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for success notification
    const notification = await helpers.waitForNotification('Character created');
    await expect(notification).toBeVisible();
  });

  test('shows validation error for empty name', async ({ authenticatedPage }) => {
    await helpers.closeDevelopmentModeDialog();
    await authenticatedPage.getByRole('button', { name: 'New Character' }).click();
    await helpers.waitForVaadin();
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    const notification = await helpers.waitForNotification();
    await expect(notification).toBeVisible();
  });

  test('can view character details', async ({ authenticatedPage }) => {
    // Create a character first
    const character = generateCharacterData();
    await helpers.closeDevelopmentModeDialog();
    await authenticatedPage.getByRole('button', { name: 'New Character' }).click();
    await helpers.waitForVaadin();
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    await authenticatedPage.getByRole('textbox', { name: 'Name' }).fill(character.name);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for notification and close
    await helpers.waitForNotification('Character created');
    await helpers.waitForNotificationToClose();

    // Click view button (index 0 in grid row)
    await helpers.clickGridRowAction(character.name, 0);
    await helpers.waitForVaadin();

    // Details dialog should open - check for character name in dialog
    const detailOverlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await detailOverlay.waitFor({ state: 'visible', timeout: 10000 });
    await expect(authenticatedPage.getByText(character.name)).toBeVisible();
  });

  test('can edit a character', async ({ authenticatedPage }) => {
    // Create a character first
    const character = generateCharacterData();
    await helpers.closeDevelopmentModeDialog();
    await authenticatedPage.getByRole('button', { name: 'New Character' }).click();
    await helpers.waitForVaadin();
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    await authenticatedPage.getByRole('textbox', { name: 'Name' }).fill(character.name);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for notification and close
    await helpers.waitForNotification('Character created');
    await helpers.waitForNotificationToClose();

    // Click edit button (index 1 in grid row)
    await helpers.clickGridRowAction(character.name, 1);
    await helpers.waitForVaadin();

    // Wait for edit dialog
    const editOverlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await editOverlay.waitFor({ state: 'visible', timeout: 10000 });

    // Edit dialog should open with pre-filled data
    await expect(authenticatedPage.getByRole('textbox', { name: 'Name' })).toHaveValue(character.name);

    // Update the name
    const updatedName = character.name + ' Updated';
    await authenticatedPage.getByRole('textbox', { name: 'Name' }).fill(updatedName);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for update notification
    const updateNotification = await helpers.waitForNotification('Character updated');
    await expect(updateNotification).toBeVisible();
  });

  test('can delete a character', async ({ authenticatedPage }) => {
    // Create a character first
    const character = generateCharacterData();
    await helpers.closeDevelopmentModeDialog();
    await authenticatedPage.getByRole('button', { name: 'New Character' }).click();
    await helpers.waitForVaadin();
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    await authenticatedPage.getByRole('textbox', { name: 'Name' }).fill(character.name);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for notification and close
    await helpers.waitForNotification('Character created');
    await helpers.waitForNotificationToClose();

    // Click delete button (index 2 in grid row)
    await helpers.clickGridRowAction(character.name, 2);

    // Confirm deletion
    await helpers.confirmDialogAction();

    // Wait for delete notification
    const deleteNotification = await helpers.waitForNotification('Character deleted');
    await expect(deleteNotification).toBeVisible();
  });
});
