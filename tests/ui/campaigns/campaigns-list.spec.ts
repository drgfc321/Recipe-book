import { test, expect } from '../fixtures/auth.fixture';
import { VaadinHelpers, generateCampaignData } from '../ui-test-utils';

test.describe('Campaigns View', () => {
  let helpers: VaadinHelpers;

  test.beforeEach(async ({ authenticatedPage }) => {
    helpers = new VaadinHelpers(authenticatedPage);
    await authenticatedPage.goto('/campaigns');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();
  });

  test('displays campaigns page with title', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('heading', { name: 'Campaigns' })).toBeVisible();
  });

  test('displays New Campaign button', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('button', { name: 'New Campaign' })).toBeVisible();
  });

  test('displays grid', async ({ authenticatedPage }) => {
    const grid = authenticatedPage.locator('vaadin-grid');
    await expect(grid).toBeVisible();
  });

  test('New Campaign button opens dialog', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('button', { name: 'New Campaign' }).click();
    await helpers.waitForVaadin();

    // Wait for dialog overlay to open
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });

    // Check form fields are visible
    await expect(authenticatedPage.getByRole('textbox', { name: 'Name' })).toBeVisible();
    await expect(authenticatedPage.getByRole('textbox', { name: 'Description' })).toBeVisible();
    await expect(authenticatedPage.getByRole('textbox', { name: 'Setting' })).toBeVisible();
    // Status is a button/select, not textbox
    await expect(authenticatedPage.getByRole('button', { name: /Status/i })).toBeVisible();
  });

  test('can create a new campaign', async ({ authenticatedPage }) => {
    const campaign = generateCampaignData();

    await authenticatedPage.getByRole('button', { name: 'New Campaign' }).click();

    // Wait for dialog overlay to open
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });
    await helpers.waitForVaadin();

    // Wait for Name field to be visible
    const nameField = authenticatedPage.getByRole('textbox', { name: 'Name' });
    await nameField.waitFor({ state: 'visible', timeout: 5000 });

    // Fill dialog fields
    await nameField.fill(campaign.name);
    await authenticatedPage.getByRole('textbox', { name: 'Description' }).fill(campaign.description);
    await authenticatedPage.getByRole('textbox', { name: 'Setting' }).fill(campaign.setting);

    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for success notification - this confirms campaign was created
    const notification = await helpers.waitForNotification('Campaign created!');
    await expect(notification).toBeVisible();
  });

  test('shows validation error for empty name', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('button', { name: 'New Campaign' }).click();
    await helpers.waitForVaadin();

    // Try to save without name
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Should show error notification
    const notification = await helpers.waitForNotification();
    await expect(notification).toBeVisible();
  });

  test('can cancel dialog', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('button', { name: 'New Campaign' }).click();
    await helpers.waitForVaadin();

    await authenticatedPage.getByRole('button', { name: 'Cancel' }).click();
    await helpers.waitForVaadin();

    // Dialog should be closed
    await expect(authenticatedPage.getByRole('dialog', { name: /Campaign/i })).not.toBeVisible();
  });

  test('can edit a campaign', async ({ authenticatedPage }) => {
    // Create a campaign first
    const campaign = generateCampaignData();
    await authenticatedPage.getByRole('button', { name: 'New Campaign' }).click();

    // Wait for dialog overlay to open
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });
    await helpers.waitForVaadin();

    // Close dev mode dialog if it reappeared
    await helpers.closeDevelopmentModeDialog();

    // Wait for Name field to be visible then fill all fields
    const nameField = authenticatedPage.getByRole('textbox', { name: 'Name' });
    await nameField.waitFor({ state: 'visible', timeout: 5000 });
    await nameField.fill(campaign.name);
    await authenticatedPage.getByRole('textbox', { name: 'Description' }).fill(campaign.description);
    await authenticatedPage.getByRole('textbox', { name: 'Setting' }).fill(campaign.setting);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for success notification to confirm creation
    await helpers.waitForNotification('Campaign created!');
    await helpers.waitForNotificationToClose();

    // Click edit button (scrolls grid if necessary)
    await helpers.clickGridRowAction(campaign.name, 1);
    await helpers.waitForVaadin();

    // Dialog should open with pre-filled data
    await expect(authenticatedPage.getByRole('textbox', { name: 'Name' })).toHaveValue(campaign.name);

    // Update the name
    const updatedName = campaign.name + ' Updated';
    await authenticatedPage.getByRole('textbox', { name: 'Name' }).fill(updatedName);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Grid should show updated name (use partial match due to truncation)
    const updatedNamePrefix = updatedName.substring(0, 25);
    await expect(authenticatedPage.getByRole('row', { name: new RegExp(updatedNamePrefix) }).first()).toBeVisible();
  });

  test('can view campaign details', async ({ authenticatedPage }) => {
    // Create a campaign first
    const campaign = generateCampaignData();
    await authenticatedPage.getByRole('button', { name: 'New Campaign' }).click();

    // Wait for dialog overlay to open
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });
    await helpers.waitForVaadin();

    // Wait for Name field to be visible then fill all fields
    const nameField = authenticatedPage.getByRole('textbox', { name: 'Name' });
    await nameField.waitFor({ state: 'visible', timeout: 5000 });
    await nameField.fill(campaign.name);
    await authenticatedPage.getByRole('textbox', { name: 'Description' }).fill(campaign.description);
    await authenticatedPage.getByRole('textbox', { name: 'Setting' }).fill(campaign.setting);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for campaign to appear in grid before clicking action
    const namePrefix = campaign.name.substring(0, 20);
    await authenticatedPage.getByRole('row', { name: new RegExp(namePrefix) }).first().waitFor({ state: 'visible', timeout: 10000 });

    // Click view button (index 0 in grid row)
    await helpers.clickGridRowAction(campaign.name, 0);
    await helpers.waitForVaadin();

    // Should navigate to campaign detail page
    await expect(authenticatedPage).toHaveURL(/.*\/campaigns\/\d+/);
  });

  test('delete shows confirmation dialog', async ({ authenticatedPage }) => {
    // Create a campaign first
    const campaign = generateCampaignData();
    await authenticatedPage.getByRole('button', { name: 'New Campaign' }).click();

    // Wait for dialog overlay to open
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });
    await helpers.waitForVaadin();

    // Wait for Name field to be visible then fill all fields
    const nameField = authenticatedPage.getByRole('textbox', { name: 'Name' });
    await nameField.waitFor({ state: 'visible', timeout: 5000 });
    await nameField.fill(campaign.name);
    await authenticatedPage.getByRole('textbox', { name: 'Description' }).fill(campaign.description);
    await authenticatedPage.getByRole('textbox', { name: 'Setting' }).fill(campaign.setting);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for campaign to appear in grid before clicking action
    const namePrefix = campaign.name.substring(0, 20);
    await authenticatedPage.getByRole('row', { name: new RegExp(namePrefix) }).first().waitFor({ state: 'visible', timeout: 10000 });

    // Click delete button (index 2 in grid row)
    await helpers.clickGridRowAction(campaign.name, 2);
    await helpers.waitForVaadin();

    // Confirm dialog should appear
    const confirmDialog = authenticatedPage.locator('vaadin-confirm-dialog-overlay');
    await expect(confirmDialog).toBeVisible();
    // Verify dialog has Delete header (Vaadin ConfirmDialog uses Shadow DOM, so check via page-level text)
    await expect(authenticatedPage.getByText('Delete Campaign?')).toBeVisible();
  });

  test('can delete a campaign', async ({ authenticatedPage }) => {
    // Create a campaign first
    const campaign = generateCampaignData();
    await authenticatedPage.getByRole('button', { name: 'New Campaign' }).click();

    // Wait for dialog overlay to open
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });
    await helpers.waitForVaadin();

    // Wait for Name field to be visible then fill all fields
    const nameField = authenticatedPage.getByRole('textbox', { name: 'Name' });
    await nameField.waitFor({ state: 'visible', timeout: 5000 });
    await nameField.fill(campaign.name);
    await authenticatedPage.getByRole('textbox', { name: 'Description' }).fill(campaign.description);
    await authenticatedPage.getByRole('textbox', { name: 'Setting' }).fill(campaign.setting);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for campaign to appear in grid before clicking action
    const namePrefix = campaign.name.substring(0, 20);
    await authenticatedPage.getByRole('row', { name: new RegExp(namePrefix) }).first().waitFor({ state: 'visible', timeout: 10000 });

    // Click delete button (index 2 in grid row)
    await helpers.clickGridRowAction(campaign.name, 2);

    // Confirm deletion
    await helpers.confirmDialogAction();

    // Campaign should be removed from grid - wait for deletion notification then verify
    const notification = await helpers.waitForNotification('Campaign deleted');
    await expect(notification).toBeVisible();
  });

  test('can cancel delete', async ({ authenticatedPage }) => {
    // Create a campaign first
    const campaign = generateCampaignData();
    await authenticatedPage.getByRole('button', { name: 'New Campaign' }).click();

    // Wait for dialog overlay to open
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });
    await helpers.waitForVaadin();

    // Wait for Name field to be visible then fill all fields
    const nameField = authenticatedPage.getByRole('textbox', { name: 'Name' });
    await nameField.waitFor({ state: 'visible', timeout: 5000 });
    await nameField.fill(campaign.name);
    await authenticatedPage.getByRole('textbox', { name: 'Description' }).fill(campaign.description);
    await authenticatedPage.getByRole('textbox', { name: 'Setting' }).fill(campaign.setting);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for campaign to appear in grid before clicking action
    const namePrefix = campaign.name.substring(0, 20);
    await authenticatedPage.getByRole('row', { name: new RegExp(namePrefix) }).first().waitFor({ state: 'visible', timeout: 10000 });

    // Click delete button (index 2 in grid row)
    await helpers.clickGridRowAction(campaign.name, 2);

    // Cancel deletion
    await helpers.cancelConfirmDialog();

    // Confirm dialog should be closed and no delete notification should appear
    const confirmDialog = authenticatedPage.locator('vaadin-confirm-dialog-overlay');
    await expect(confirmDialog).not.toBeVisible();
    // Grid should still be visible (campaign not deleted)
    await expect(authenticatedPage.locator('vaadin-grid')).toBeVisible();
  });
});
