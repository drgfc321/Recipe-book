import { Page, expect, Locator } from '@playwright/test';

/**
 * Helper class for interacting with Vaadin components
 */
export class VaadinHelpers {
  constructor(private page: Page) {}

  /**
   * Wait for Vaadin to finish loading/updating
   */
  async waitForVaadin(): Promise<void> {
    await this.page.waitForFunction(() => {
      const vaadin = (window as any).Vaadin;
      if (!vaadin || !vaadin.Flow) return true;
      const clients = vaadin.Flow.clients;
      if (!clients) return true;
      return Object.keys(clients).every(key => {
        const client = clients[key];
        return !client.isActive || !client.isActive();
      });
    }, { timeout: 30000 });
  }

  /**
   * Close the Vaadin development mode dialog if present
   */
  async closeDevelopmentModeDialog(): Promise<void> {
    try {
      const closeButton = this.page.locator('button:has-text("Close")').first();
      if (await closeButton.isVisible({ timeout: 2000 })) {
        await closeButton.click();
        await this.waitForVaadin();
      }
    } catch {
      // Dialog not present, continue
    }
  }

  /**
   * Wait for notification to appear and optionally verify text
   */
  async waitForNotification(expectedText?: string): Promise<Locator> {
    if (expectedText) {
      // When specific text is expected, use getByText to find the specific notification
      const notification = this.page.getByText(expectedText);
      await notification.waitFor({ state: 'visible', timeout: 10000 });
      return notification;
    } else {
      // Wait for any notification
      const notification = this.page.locator('vaadin-notification-card').first();
      await notification.waitFor({ state: 'visible', timeout: 10000 });
      return notification;
    }
  }

  /**
   * Check notification has specific theme variant
   */
  async expectNotificationVariant(variant: 'success' | 'error' | 'primary'): Promise<void> {
    const notification = this.page.locator('vaadin-notification-card');
    await expect(notification).toHaveAttribute('theme', new RegExp(variant));
  }

  /**
   * Wait for notification to disappear
   */
  async waitForNotificationToClose(): Promise<void> {
    const notification = this.page.locator('vaadin-notification-card');
    if (await notification.isVisible()) {
      await notification.waitFor({ state: 'hidden', timeout: 5000 });
    }
  }

  /**
   * Wait for dialog to open (excludes Vaadin dev dialogs)
   */
  async waitForDialog(): Promise<Locator> {
    await this.waitForVaadin();

    // Wait for any vaadin-dialog-overlay that's opened
    const dialog = this.page.locator('vaadin-dialog-overlay[opened]');
    await dialog.first().waitFor({ state: 'visible', timeout: 10000 });

    // If multiple dialogs, try to find one that's not the dev dialog
    const count = await dialog.count();
    if (count > 1) {
      // Look for the one with form fields (not the dev mode dialog)
      for (let i = 0; i < count; i++) {
        const d = dialog.nth(i);
        const hasNameField = await d.getByLabel('Name').count() > 0 ||
                            await d.getByLabel('Session Number').count() > 0;
        if (hasNameField) {
          return d;
        }
      }
    }

    return dialog.first();
  }

  /**
   * Close dialog via Cancel button
   */
  async closeDialog(): Promise<void> {
    await this.page.getByRole('button', { name: 'Cancel' }).click();
    await this.waitForVaadin();
  }

  /**
   * Confirm action in ConfirmDialog
   * Uses page-level locator because Vaadin ConfirmDialog uses Shadow DOM
   */
  async confirmDialogAction(): Promise<void> {
    const confirmDialog = this.page.locator('vaadin-confirm-dialog-overlay');
    await confirmDialog.waitFor({ state: 'visible' });
    // Use page-level getByRole - the confirm button text is "Delete" (set in CampaignsView)
    await this.page.getByRole('button', { name: 'Delete', exact: true }).click();
    await this.waitForVaadin();
  }

  /**
   * Cancel action in ConfirmDialog
   * Uses page-level locator because Vaadin ConfirmDialog uses Shadow DOM
   */
  async cancelConfirmDialog(): Promise<void> {
    const confirmDialog = this.page.locator('vaadin-confirm-dialog-overlay');
    await confirmDialog.waitFor({ state: 'visible' });
    // Use page-level getByRole - the cancel button text is "Cancel"
    await this.page.getByRole('button', { name: 'Cancel', exact: true }).click();
    await this.waitForVaadin();
  }

  /**
   * Click a button by text
   */
  async clickButton(text: string): Promise<void> {
    await this.page.getByRole('button', { name: text }).click();
    await this.waitForVaadin();
  }

  /**
   * Click action button in a grid row containing specific text
   * Scrolls the grid to find the row if necessary
   * @param rowText Text to find the row (uses first 20 chars for matching due to column truncation)
   * @param buttonIndex 0 for view, 1 for edit, 2 for delete
   */
  async clickGridRowAction(rowText: string, buttonIndex: 0 | 1 | 2): Promise<void> {
    // Use longer prefix for more precise matching
    const searchText = rowText.substring(0, 25);
    const escapedText = searchText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const row = this.page.getByRole('row', { name: new RegExp(escapedText) }).first();

    // Try to find the row, scrolling if necessary
    const grid = this.page.locator('vaadin-grid');
    let attempts = 0;
    const maxAttempts = 30;  // More attempts for large datasets

    while (attempts < maxAttempts) {
      const isVisible = await row.isVisible();
      if (isVisible) {
        break;
      }
      // Scroll down in the grid - using keyboard for better Vaadin compatibility
      await grid.click();
      await this.page.keyboard.press('PageDown');
      await this.page.waitForTimeout(100);
      attempts++;
    }

    await row.waitFor({ state: 'visible', timeout: 10000 });

    // Close dev mode dialog if it's blocking
    await this.closeDevelopmentModeDialog();

    // Scroll the row into view before clicking
    await row.scrollIntoViewIfNeeded();
    await this.page.waitForTimeout(200);

    // Click using the page-level button locator that already found visible buttons
    // Since row.getByRole('button') doesn't work through Vaadin's shadow DOM,
    // we need to use a different strategy: find by aria/role attribute at page level
    // and match by proximity to the row text

    // Get all buttons at page level in the grid area
    const allButtons = this.page.locator('vaadin-grid vaadin-button');
    const buttonCount = await allButtons.count();

    // Find the row's buttons by clicking through evaluating position
    // The buttons in each row are adjacent to the text content
    const clicked = await this.page.evaluate(({ text, index }) => {
      const grid = document.querySelector('vaadin-grid');
      if (!grid) return false;

      // Get all buttons in the grid
      const allButtons = Array.from(grid.querySelectorAll('vaadin-button'));

      // Group buttons by their row (using y-position)
      const buttonsByRow = new Map<number, HTMLElement[]>();
      allButtons.forEach(btn => {
        const rect = btn.getBoundingClientRect();
        const rowY = Math.round(rect.top / 50) * 50; // Approximate row grouping
        if (!buttonsByRow.has(rowY)) {
          buttonsByRow.set(rowY, []);
        }
        buttonsByRow.get(rowY)!.push(btn as HTMLElement);
      });

      // Find which row contains our text
      const cells = Array.from(grid.querySelectorAll('vaadin-grid-cell-content'));
      const matchingCell = cells.find(c => c.textContent?.includes(text));
      if (!matchingCell) return false;

      const cellRect = matchingCell.getBoundingClientRect();
      const cellY = Math.round(cellRect.top / 50) * 50;

      // Get buttons from that row
      const rowButtons = buttonsByRow.get(cellY);
      if (!rowButtons || rowButtons.length <= index) return false;

      rowButtons[index].click();
      return true;
    }, { text: searchText, index: buttonIndex });

    if (!clicked) {
      throw new Error(`Failed to click button ${buttonIndex} for row containing "${searchText}"`);
    }

    await this.waitForVaadin();
  }

  /**
   * Get action button locator in a row by index
   * @param rowText Text to find the row (uses first 15 chars for matching due to column truncation)
   * @param buttonIndex 0 for view, 1 for edit, 2 for delete
   */
  getRowActionButton(rowText: string, buttonIndex: 0 | 1 | 2): Locator {
    const searchText = rowText.substring(0, 15);
    const row = this.page.getByRole('row', { name: new RegExp(searchText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')) });
    return row.getByRole('button').nth(buttonIndex);
  }

  /**
   * Get icon button locator in a locator by index (0=view, 1=edit, 2=delete)
   * @deprecated Use getRowActionButton instead
   */
  getIconButton(row: Locator, iconName: 'eye' | 'edit' | 'trash'): Locator {
    const index = iconName === 'eye' ? 0 : iconName === 'edit' ? 1 : 2;
    return row.getByRole('button').nth(index);
  }

  /**
   * Navigate to a route via side nav
   */
  async navigateTo(navItemText: string): Promise<void> {
    await this.page.getByRole('link', { name: navItemText }).click();
    await this.waitForVaadin();
  }

  /**
   * Get grid row count
   */
  async getGridRowCount(): Promise<number> {
    const grid = this.page.locator('vaadin-grid');
    const rows = grid.locator('tbody tr');
    return await rows.count();
  }
}

/**
 * Generate unique test user data
 */
export function generateTestUser() {
  const timestamp = Date.now();
  return {
    email: `uitest${timestamp}@example.com`,
    username: `uitester${timestamp}`,
    password: 'testpassword123',
  };
}

/**
 * Generate unique campaign data
 */
export function generateCampaignData() {
  const timestamp = Date.now();
  return {
    name: `Test Campaign ${timestamp}`,
    description: 'A test campaign created by UI tests',
    setting: 'Forgotten Realms',
    status: 'ACTIVE',
  };
}

/**
 * Generate unique character data
 */
export function generateCharacterData() {
  const timestamp = Date.now();
  return {
    name: `Test Hero ${timestamp}`,
    race: 'Human',
    characterClass: 'Fighter',
    level: 5,
    strength: 16,
    dexterity: 14,
    constitution: 15,
    intelligence: 10,
    wisdom: 12,
    charisma: 8,
    hitPoints: 45,
    armorClass: 16,
    speed: 30,
  };
}

/**
 * Generate session data
 */
export function generateSessionData() {
  return {
    sessionNumber: Math.floor(Math.random() * 100) + 1,
    summary: 'Test session created by UI tests',
    xpAwarded: 500,
    notes: 'Testing notes',
  };
}

/**
 * Perform login with credentials
 */
export async function login(page: Page, email: string, password: string): Promise<void> {
  const helpers = new VaadinHelpers(page);

  await page.goto('/login');
  await helpers.waitForVaadin();
  await helpers.closeDevelopmentModeDialog();

  await page.getByRole('textbox', { name: 'Email' }).fill(email);
  await page.getByRole('textbox', { name: 'Password' }).fill(password);
  await page.getByRole('button', { name: 'Login' }).click();
  await helpers.waitForVaadin();
}

/**
 * Perform registration
 */
export async function register(page: Page, email: string, username: string, password: string): Promise<void> {
  const helpers = new VaadinHelpers(page);

  await page.goto('/register');
  await helpers.waitForVaadin();
  await helpers.closeDevelopmentModeDialog();

  await page.getByRole('textbox', { name: 'Email' }).fill(email);
  await page.getByRole('textbox', { name: 'Username' }).fill(username);
  await page.getByRole('textbox', { name: 'Password' }).first().fill(password);
  await page.getByRole('textbox', { name: 'Confirm Password' }).fill(password);
  await page.getByRole('button', { name: 'Register' }).click();
  await helpers.waitForVaadin();
}

/**
 * Login or register a test user
 */
export async function loginOrRegister(page: Page): Promise<{ email: string; username: string; password: string }> {
  const user = generateTestUser();
  const helpers = new VaadinHelpers(page);

  // Try to register first
  await register(page, user.email, user.username, user.password);

  // Check if we're redirected to dashboard or still on register (error)
  await page.waitForTimeout(1000);
  const url = page.url();

  if (url.includes('/register')) {
    // Registration might have failed (user exists), try login
    await login(page, user.email, user.password);
  }

  return user;
}
