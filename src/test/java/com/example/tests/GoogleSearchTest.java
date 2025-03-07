//package com.example.tests;
//
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
//import io.github.bonigarcia.wdm.WebDriverManager;
//import org.testng.annotations.AfterTest;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//
//public class GoogleSearchTest {
//    WebDriver driver;
//
//    @BeforeTest
//    public void setup() {
//        // Automatically download and setup ChromeDriver
//        WebDriverManager.chromedriver().setup();
//        driver = new ChromeDriver();
//        driver.manage().window().maximize();
//    }
//
//    @Test
//    public void openGoogleHomePage() {
//        driver.get("https://www.google.com");
//        System.out.println("Google Homepage Opened: " + driver.getTitle());
//    }
//
//    @AfterTest
//    public void tearDown() {
//    
//    	driver.quit();
//       
//    }
//}
