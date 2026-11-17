const { defineConfig } = require("@playwright/test");

const baseURL = "http://127.0.0.1:8080";
const command = process.env.WEB_SERVER_COMMAND;

if (!command) {
  throw new Error("WEB_SERVER_COMMAND is required");
}

module.exports = defineConfig({
  testMatch: "check-web-page.spec.js",
  use: {
    baseURL,
    launchOptions: {
      args: [
        "--enable-webgl",
        "--ignore-gpu-blocklist",
        "--use-angle=swiftshader",
        "--enable-unsafe-swiftshader",
      ],
    },
  },
  webServer: {
    command,
    url: baseURL,
    timeout: 180_000,
  },
});
