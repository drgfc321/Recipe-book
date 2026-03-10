import { Page } from '@playwright/test';

/**
 * Waits for all Vaadin Flow server roundtrips to complete.
 * Vaadin Flow does server-side rendering over XHR — every UI interaction
 * triggers a roundtrip. This helper polls Vaadin.Flow.clients until all
 * clients report inactive, then waits for network to settle.
 */
export async function waitForVaadin(page: Page): Promise<void> {
  await page.waitForFunction(() => {
    const vaadin = (window as any).Vaadin;
    if (!vaadin || !vaadin.Flow || !vaadin.Flow.clients) return false;
    const clients = vaadin.Flow.clients;
    for (const key of Object.keys(clients)) {
      if (clients[key].isActive && clients[key].isActive()) return false;
    }
    return true;
  }, { timeout: 15_000 });
  await page.waitForLoadState('networkidle');
}
