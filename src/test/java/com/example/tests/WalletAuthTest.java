package com.example.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.Set;

public class WalletAuthTest {
    WebDriver driver;
    WebDriverWait wait;

    @BeforeClass
    public void setup() {
        // 1️⃣ Set the path for ChromeDriver
        System.setProperty("webdriver.chrome.driver", "C:\\webdriver\\chromium\\chrome.exe");  // Update for your OS

        // 2️⃣ Use Chromium instead of Chrome
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\webdriver\\chromium\\chrome.exe");  // Update path to Chromium

        // Optional Arguments for Stability
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--start-maximized");

        // 3️⃣ Launch Chromium with Selenium
        driver = new ChromeDriver(options);
        driver.get("https://my-staging.bubsy.ai/");
 
    }

    @Test(priority = 1)
    public void testOpenWebsite() {
        String title = driver.getTitle();
        System.out.println("Page title is: " + title);
    }

    @Test(priority = 2)
    public void testConnectWallet() {
        WebElement connectButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Connect Wallet')]")
        ));
        connectButton.click();
    }

    @Test(priority = 3)
    public void testSelectMetaMask() {
        String mainWindow = driver.getWindowHandle();

        // Wait for MetaMask popup to open
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();

        for (String window : allWindows) {
            if (!window.equals(mainWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }

        // Click on MetaMask wallet option
        WebElement metamaskOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'MetaMask')]")
        ));
        metamaskOption.click();

        // Switch back to the main window
        driver.switchTo().window(mainWindow);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
