package com.example.tests;

import java.util.List;
import java.util.Arrays;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;
import java.util.Set;

public class WalletAuthTest {
    WebDriver driver;
    WebDriverWait wait;
    private static final String BUBSY_URL = "https://my-staging.bubsy.ai/";

    @BeforeClass
    public void setup() {
        // Set up ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Configure ChromeOptions
        ChromeOptions options = new ChromeOptions();

        // IMPORTANT: Close all Chrome instances before running test

        // Set path to user data directory - keep this as a variable for easier modification
        String userDataDir = "C:\\Users\\DELL\\AppData\\Local\\Google\\Chrome\\User Data";
        options.addArguments("user-data-dir=" + userDataDir);

        // Try "Default" profile - this is usually where extensions are installed
        options.addArguments("profile-directory=Default");

        // Don't try to explicitly load extensions - let Chrome load them from the profile
        // Instead, ensure extensions are enabled
        options.addArguments("--enable-extensions");
        options.setExperimentalOption("excludeSwitches", new String[]{"disable-extensions"});

        // Other required options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");

        // Debug output
        System.out.println("Using Chrome profile at: " + userDataDir + "\\Default");

        try {
            // Initialize ChromeDriver with options
            driver = new ChromeDriver(options);

            // Initialize WebDriverWait with a reasonable timeout
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // Set page load timeout
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        } catch (Exception e) {
            System.out.println("Error initializing ChromeDriver: " + e.getMessage());
            e.printStackTrace();

            // Try without profile if it fails
            if (driver == null) {
                System.out.println("Attempting to start Chrome without profile...");
                options = new ChromeOptions();
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--start-maximized");
                options.addArguments("--remote-allow-origins=*");
                driver = new ChromeDriver(options);
                wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            }
        }
    }

    @Test(priority = 1)
    public void testOpenWebsite() {
        // Navigate to the website
        System.out.println("Navigating to " + BUBSY_URL);
        driver.get(BUBSY_URL);

        // Give the page a moment to load
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            // Handle cookie consent popup if it exists
            WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Accept') or contains(text(),'accept') or contains(@id,'accept')]")
            ));
            System.out.println("Clicking on cookie accept button");
            acceptCookies.click();
        } catch (Exception e) {
            System.out.println("No cookie popup found or unable to interact with it.");
        }

        // Verify successful navigation
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Current URL is: " + currentUrl);
        Assert.assertTrue(currentUrl.contains("bubsy"), "Failed to navigate to Bubsy website");

        // Print the page title
        String title = driver.getTitle();
        System.out.println("Page title is: " + title);

        // Debug: Print user agent to confirm profile loading
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        Object userAgent = jsExecutor.executeScript("return window.navigator.userAgent");
        System.out.println("User Agent: " + userAgent);

        // Debug: Check if MetaMask is available
        try {
            Object hasMetaMask = jsExecutor.executeScript("return typeof window.ethereum !== 'undefined'");
            System.out.println("MetaMask detected: " + hasMetaMask);
        } catch (Exception e) {
            System.out.println("Could not detect MetaMask: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    public void testConnectWallet() {
        try {
            System.out.println("Looking for Connect Wallet button");

            // Try different selector strategies in case the first one fails
            WebElement connectButton = null;

            try {
                // First try with button text
                connectButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Connect Wallet')]")
                ));
            } catch (Exception e) {
                try {
                    // Then try with data attributes or class names
                    connectButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[data-testid='connect-wallet'], .connect-wallet-btn, [aria-label='Connect Wallet']")
                    ));
                } catch (Exception e2) {
                    // Finally try with any element containing the text
                    connectButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[contains(text(),'Connect Wallet') or contains(text(),'connect wallet')]")
                    ));
                }
            }

            System.out.println("Connect Wallet button found, clicking it");
            connectButton.click();

            // Wait briefly to ensure the click action is processed
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Failed to find or click Connect Wallet button: " + e.getMessage());
            Assert.fail("Connect Wallet button not found or not clickable");
        }
    }
    
    @Test(priority = 3)
    public void testSelectMetaMask() {
        try {
            String mainWindow = driver.getWindowHandle();
            System.out.println("Waiting for Web3 wallet selection modal...");

            // Wait for the modal to appear - using a more general approach
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("body")
            ));
            System.out.println("Page loaded, looking for wallet modal");
            
            // Sleep briefly to allow the modal to fully render
            Thread.sleep(2000);
            
            System.out.println("Web3 wallet modal is visible");
            
            // First try: Find MetaMask using JavaScript query for better shadow DOM penetration
            try {
                System.out.println("Attempting to find MetaMask button using JavaScript...");
                WebElement metamaskOption = (WebElement) ((JavascriptExecutor) driver).executeScript(
                    "return document.querySelector('button:has-text(\"MetaMask\"), " +
                    "button:contains(\"MetaMask\"), " +
                    "button[title*=\"MetaMask\"], " +
                    "button[aria-label*=\"MetaMask\"]');");
                    
                if (metamaskOption != null) {
                    System.out.println("Found MetaMask button via JavaScript, clicking it");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", metamaskOption);
                } else {
                    throw new Exception("Button not found via JavaScript");
                }
            } catch (Exception jsException) {
                System.out.println("JavaScript approach failed: " + jsException.getMessage());
                
                // Second try: Try with presence instead of clickable
                try {
                    System.out.println("Attempting with presence instead of clickable...");
                    // Try multiple possible selectors
                    List<By> selectors = Arrays.asList(
                        By.xpath("//button[contains(., 'MetaMask')]"),
                        By.xpath("//button[.//wui-text[contains(text(), 'MetaMask')]]"),
                        By.xpath("//button[.//wui-wallet-image[@name='MetaMask']]"),
                        By.cssSelector("button:has-text('MetaMask')"),
                        By.xpath("//*[text()='MetaMask']/ancestor::button"),
                        By.xpath("//button[.//img[contains(@src, 'metamask') or contains(@alt, 'metamask')]]")
                    );
                    
                    WebElement metamaskOption = null;
                    for (By selector : selectors) {
                        try {
                            System.out.println("Trying selector: " + selector);
                            metamaskOption = wait.until(ExpectedConditions.presenceOfElementLocated(selector));
                            if (metamaskOption != null && metamaskOption.isDisplayed()) {
                                System.out.println("Found MetaMask button with selector: " + selector);
                                break;
                            }
                        } catch (Exception e) {
                            System.out.println("Selector failed: " + selector);
                        }
                    }
                    
                    if (metamaskOption != null) {
                        // Force click using JavaScript
                        System.out.println("Attempting to click MetaMask button using JavaScript...");
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", metamaskOption);
                    } else {
                        throw new Exception("MetaMask button not found with any selector");
                    }
                } catch (Exception e) {
                    // Third try: Take a visual approach
                    System.out.println("Standard approaches failed, trying visual cues...");
                    
                    // Look for anything visually related to MetaMask
                    List<WebElement> buttons = driver.findElements(By.tagName("button"));
                    for (WebElement button : buttons) {
                        try {
                            String buttonText = button.getText().toLowerCase();
                            System.out.println("Found button with text: " + buttonText);
                            if (buttonText.contains("metamask") || 
                                buttonText.contains("meta mask") ||
                                button.getAttribute("innerHTML").toLowerCase().contains("metamask")) {
                                System.out.println("Found MetaMask button visually");
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                                break;
                            }
                        } catch (Exception ex) {
                            // Continue to next button
                        }
                    }
                }
            }

            // Wait for new MetaMask pop-up window to appear
            System.out.println("Waiting for MetaMask popup window...");
            wait.until(d -> driver.getWindowHandles().size() > 1);
            Set<String> allWindows = driver.getWindowHandles();
            System.out.println("MetaMask popup detected");

            for (String window : allWindows) {
                if (!window.equals(mainWindow)) {
                    driver.switchTo().window(window);
                    System.out.println("Switched to MetaMask pop-up window");
                    break;
                }
            }

            // Wait for MetaMask pop-up UI elements with more robust handling
            try {
                System.out.println("Looking for 'Next' button...");
                WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Next') or @data-testid='page-container-footer-next']")
                ));
                nextButton.click();
                System.out.println("Clicked 'Next' on MetaMask pop-up");
            } catch (Exception e) {
                System.out.println("Could not find 'Next' button: " + e.getMessage());
                System.out.println("Trying to find 'Connect' button directly...");
            }

            // Wait for "Connect" button and click it
            WebElement connectButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Connect') or @data-testid='page-container-footer-next']")
            ));
            connectButton.click();
            System.out.println("Clicked 'Connect' on MetaMask pop-up");

            // Switch back to the main window
            driver.switchTo().window(mainWindow);
            System.out.println("Switched back to Bubsy site");

            // Success - Modal should be closed now
            System.out.println("Web3 wallet modal should be closed now");

        } catch (Exception e) {
            System.err.println("Error during MetaMask selection: " + e.getMessage());
            e.printStackTrace();
            Assert.fail("Failed to select MetaMask wallet: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        // Close the browser after tests are done
        if (driver != null) {
            System.out.println("Closing browser");
            driver.quit();
        }
    }
}