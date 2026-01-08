import { test, expect } from '../fixtures/auth.fixture';
import { VaadinHelpers, generateCampaignData, generateSessionData } from '../ui-test-utils';

test.describe('Campaign Detail View', () => {
  let helpers: VaadinHelpers;
  let campaignData: ReturnType<typeof generateCampaignData>;

  test.beforeEach(async ({ authenticatedPage }) => {
    helpers = new VaadinHelpers(authenticatedPage);

    // Create a campaign first
    await authenticatedPage.goto('/campaigns');
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();

    campaignData = generateCampaignData();
    await authenticatedPage.getByRole('button', { name: 'New Campaign' }).click();

    // Wait for dialog overlay to open
    const overlay = authenticatedPage.locator('vaadin-dialog-overlay[opened]');
    await overlay.waitFor({ state: 'visible', timeout: 10000 });
    await helpers.waitForVaadin();

    // Wait for dialog form field to be visible
    const nameField = authenticatedPage.getByRole('textbox', { name: 'Name' });
    await nameField.waitFor({ state: 'visible', timeout: 10000 });

    // Fill dialog fields
    await nameField.fill(campaignData.name);
    await authenticatedPage.getByRole('textbox', { name: 'Description' }).fill(campaignData.description);
    await authenticatedPage.getByRole('textbox', { name: 'Setting' }).fill(campaignData.setting);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Wait for success notification to confirm creation
    await helpers.waitForNotification('Campaign created!');
    await helpers.waitForNotificationToClose();

    // Navigate to campaign detail (view button = index 0) - clickGridRowAction will scroll to find the row
    await helpers.clickGridRowAction(campaignData.name, 0);
    await helpers.waitForVaadin();
    await helpers.closeDevelopmentModeDialog();
  });

  test('displays campaign header with name', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('heading', { name: campaignData.name })).toBeVisible();
  });

  test('displays back button', async ({ authenticatedPage }) => {
    const backButton = authenticatedPage.getByRole('button', { name: /Back/i });
    await expect(backButton).toBeVisible();
  });

  test('back button returns to campaigns list', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('button', { name: /Back/i }).click();
    await helpers.waitForVaadin();

    await expect(authenticatedPage).toHaveURL(/.*\/campaigns$/);
  });

  test('displays campaign details', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByText(new RegExp(campaignData.setting))).toBeVisible();
    await expect(authenticatedPage.getByText(campaignData.description)).toBeVisible();
  });

  test('displays Sessions section', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByRole('heading', { name: 'Sessions' })).toBeVisible();
    await expect(authenticatedPage.getByRole('button', { name: 'Add Session' })).toBeVisible();
  });

  test('displays sessions grid', async ({ authenticatedPage }) => {
    const grid = authenticatedPage.locator('vaadin-grid');
    await expect(grid).toBeVisible();
  });

  test('Add Session button opens dialog', async ({ authenticatedPage }) => {
    await authenticatedPage.getByRole('button', { name: 'Add Session' }).click();
    await helpers.waitForVaadin();

    await expect(authenticatedPage.getByRole('dialog', { name: /Session/i })).toBeVisible();
    await expect(authenticatedPage.getByRole('spinbutton', { name: 'Session Number' })).toBeVisible();
    await expect(authenticatedPage.getByRole('spinbutton', { name: 'XP Awarded' })).toBeVisible();
    await expect(authenticatedPage.getByRole('textbox', { name: 'Summary' })).toBeVisible();
  });

  test('can create a session', async ({ authenticatedPage }) => {
    const session = generateSessionData();

    await authenticatedPage.getByRole('button', { name: 'Add Session' }).click();
    await helpers.waitForVaadin();

    await authenticatedPage.getByRole('spinbutton', { name: 'Session Number' }).fill(session.sessionNumber.toString());
    await authenticatedPage.getByRole('spinbutton', { name: 'XP Awarded' }).fill(session.xpAwarded.toString());
    await authenticatedPage.getByRole('textbox', { name: 'Summary' }).fill(session.summary);

    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Session should appear in grid
    await expect(authenticatedPage.getByText(session.summary)).toBeVisible();
  });

  test('can edit a session', async ({ authenticatedPage }) => {
    // Create a session first
    const session = generateSessionData();
    await authenticatedPage.getByRole('button', { name: 'Add Session' }).click();
    await helpers.waitForVaadin();

    await authenticatedPage.getByRole('spinbutton', { name: 'Session Number' }).fill(session.sessionNumber.toString());
    await authenticatedPage.getByRole('textbox', { name: 'Summary' }).fill(session.summary);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Click edit button (index 1 in grid row)
    await helpers.clickGridRowAction(session.summary, 1);
    await helpers.waitForVaadin();

    // Update session
    const updatedSummary = 'Updated session summary';
    await authenticatedPage.getByRole('textbox', { name: 'Summary' }).fill(updatedSummary);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Grid should show updated summary
    await expect(authenticatedPage.getByText(updatedSummary)).toBeVisible();
  });

  test('can delete a session', async ({ authenticatedPage }) => {
    // Create a session first
    const session = generateSessionData();
    await authenticatedPage.getByRole('button', { name: 'Add Session' }).click();
    await helpers.waitForVaadin();

    await authenticatedPage.getByRole('spinbutton', { name: 'Session Number' }).fill(session.sessionNumber.toString());
    await authenticatedPage.getByRole('textbox', { name: 'Summary' }).fill(session.summary);
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();
    await helpers.waitForVaadin();

    // Click delete button (index 2 in grid row)
    await helpers.clickGridRowAction(session.summary, 2);

    // Confirm deletion
    await helpers.confirmDialogAction();

    // Session should be removed
    await expect(authenticatedPage.getByText(session.summary)).not.toBeVisible();
  });
});
