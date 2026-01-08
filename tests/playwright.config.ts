import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',

  projects: [
    // API Tests Project (existing)
    {
      name: 'api',
      testDir: './api',
      use: {
        baseURL: 'http://localhost:8080',
        extraHTTPHeaders: {
          'Content-Type': 'application/json',
        },
      },
    },

    // UI Tests Project (new)
    {
      name: 'ui-chromium',
      testDir: './ui',
      use: {
        ...devices['Desktop Chrome'],
        baseURL: 'http://localhost:8081',
        actionTimeout: 10000,
        navigationTimeout: 30000,
        screenshot: 'only-on-failure',
        trace: 'on-first-retry',
      },
    },
  ],
});
