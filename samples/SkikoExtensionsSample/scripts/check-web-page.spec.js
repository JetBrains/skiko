const { expect, test } = require("@playwright/test");

function collectRuntimeFailures(page) {
  const failures = [];

  page.on("pageerror", error => {
    failures.push(`Uncaught exception:\n${error.stack || error.message}`);
  });

  page.on("console", message => {
    const type = message.type();
    const text = message.text();
    if (type === "error" || type === "assert") {
      failures.push(`console.${type}: ${text}`);
    } else {
      console.log(`browser ${type}: ${text}`);
    }
  });

  page.on("requestfailed", request => {
    const reason = request.failure()?.errorText || "unknown network error";
    failures.push(`Failed request: ${request.method()} ${request.url()} - ${reason}`);
  });

  page.on("response", response => {
    const url = new URL(response.url());
    if (response.status() >= 400 && url.pathname !== "/favicon.ico") {
      failures.push(`HTTP ${response.status()}: ${response.request().method()} ${response.url()}`);
    }
  });

  page.on("crash", () => {
    failures.push("The browser page crashed");
  });

  return failures;
}

test("Skiko extensions sample starts in browser", async ({ page }) => {
  const failures = collectRuntimeFailures(page);
  const response = await page.goto("/", { waitUntil: "domcontentloaded" });

  expect(response, "No navigation response").not.toBeNull();
  expect(response.ok(), `Page returned HTTP ${response.status()}`).toBeTruthy();

  await page.waitForSelector("#SkikoTarget[data-skiko-ready='true']");
  await page.waitForTimeout(500);

  expect(failures, `Runtime failures detected:\n${failures.join("\n\n")}`).toEqual([]);
});
