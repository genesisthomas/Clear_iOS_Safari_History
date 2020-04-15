package com.perfecto.sampleproject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSTouchAction;
import io.appium.java_client.touch.LongPressOptions;
import io.appium.java_client.touch.offset.ElementOption;

public class Utils {
	public static String fetchCloudName(String cloudName) throws Exception {
		//Verifies if cloudName is hardcoded, else loads from Maven properties 
		String finalCloudName = cloudName.equalsIgnoreCase("<<cloud name>>") ? System.getProperty("cloudName") : cloudName;
		//throw exceptions if cloudName isnt passed:
		if(finalCloudName == null || finalCloudName.equalsIgnoreCase("<<cloud name>>"))
			throw new Exception("Please replace <<cloud name>> with your perfecto cloud name (e.g. demo) or pass it as maven properties: -DcloudName=<<cloud name>>");
		else
			return finalCloudName;
	}

	public static String fetchSecurityToken(String securityToken) throws Exception {
		//Verifies if securityToken is hardcoded, else loads from Maven properties
		String finalSecurityToken = securityToken.equalsIgnoreCase("<<security token>>") ? System.getProperty("securityToken") : securityToken;
		//throw exceptions if securityToken isnt passed:
		if(finalSecurityToken == null || finalSecurityToken.equalsIgnoreCase("<<security token>>"))
			throw new Exception("Please replace <<security token>> with your perfecto security token or pass it as maven properties: -DsecurityToken=<<SECURITY TOKEN>>");
		else
			return finalSecurityToken;
	}

	public static String deviceInfo(RemoteWebDriver driver, String deviceProperty){
		Map<String, Object> params = new HashMap<>();
		params.put("property", deviceProperty);
		return (String) driver.executeScript("mobile:device:info", params);
	}

	public static void clearHistoryiOS(RemoteWebDriver driver) throws InterruptedException {
		//Use this for iOS version 11 and above
		WebDriverWait wait = new WebDriverWait(driver, 30);
		try{
			Map<String, Object> params = new HashMap<>();
			params.put("identifier", "com.apple.Preferences");
			driver.executeScript("mobile:application:close", params);
		}catch(Exception e){}
		Map<String, Object> params = new HashMap<>();
		params.put("identifier", "com.apple.Preferences");
		driver.executeScript("mobile:application:open", params);
		Map<String, Object> pars = new HashMap<>();
		pars.put("children", 4);
		driver.executeScript("mobile:objects.optimization:start", pars);
		params.clear();
		params.put("start", "20%,30%");
		params.put("end", "15%,90%");
		params.put("duration", "1");
		driver.executeScript("mobile:touch:swipe", params);
		switchToContext(driver, "NATIVE_APP");
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		WebElement search = driver.findElementByXPath("//XCUIElementTypeSearchField[@label='Search' or @value='Settings' and @enabled='true']");
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.visibilityOf(search)).click();
		wait.until(ExpectedConditions.elementToBeClickable(search)).sendKeys("Safari");
		wait.until(ExpectedConditions.visibilityOf(driver.findElementByXPath("(//XCUIElementTypeStaticText[@label='Safari'])[last()]"))).click();

		if(deviceInfo(driver, "model").contains("iPad")){
			driver.findElementByXPath("//*[@label='Cancel']").click();
		}
		params.clear();
		params.put("content", "Clear History");
		params.put("threshold", "90");
		params.put("scrolling", "scroll");
		params.put("next", "SWIPE_UP");
		driver.executeScript("mobile:text:select", params);
		if(deviceInfo(driver, "model").contains("iPad")){
			wait.until(ExpectedConditions.visibilityOf(((IOSDriver) driver).findElementByIosClassChain("**/XCUIElementTypeButton[`label =='Clear'`]"))).click();
		}else {
			wait.until(ExpectedConditions.visibilityOf(((IOSDriver) driver).findElementByIosClassChain("**/XCUIElementTypeButton[`label =='Clear History and Data'`]"))).click();
		}
		Thread.sleep(4000);
		try{
			params = new HashMap<>();
			params.put("identifier", "com.apple.Preferences");
			driver.executeScript("mobile:application:close", params);
		}catch(Exception e){}

		Map<String, Object> pars2 = new HashMap<>();
		driver.executeScript("mobile:objects.optimization:stop", pars2);
	}

	private static void switchToContext(RemoteWebDriver driver, String context) {
		RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", context); //
		executeMethod.execute(DriverCommand.SWITCH_TO_CONTEXT, params);
	}

	public static void closeTabs(IOSDriver driver) {
		try{
			Map<String, Object> params = new HashMap<>();
			params.put("identifier", "com.apple.mobilesafari");
			driver.executeScript("mobile:application:close", params);
		}catch(Exception e){}
		Map<String, Object> params = new HashMap<>();
		params.put("identifier", "com.apple.mobilesafari");
		driver.executeScript("mobile:application:open", params);

		int i = 0;
		while(i <1) {
			switchToContext(driver, "NATIVE_APP");
			WebElement browserTab = driver.findElementByXPath("//*[@label=\"Tabs\"]");
			IOSTouchAction touch = new IOSTouchAction (driver);
			touch.longPress(LongPressOptions.longPressOptions()
			                .withElement (ElementOption.element (browserTab)))
			              .perform ();
			
//			TouchAction action = new TouchAction(driver);
//			action.longPress((ElementOption)browserTab).press((ElementOption)browserTab);
//			action.perform();
//			action.longPress((ElementOption)browserTab).release();
//			action.perform();
			try {
				driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
				WebElement closeAll = driver.findElementByXPath("//*[contains(@label,'Close All') and @visible='true']");
				closeAll.click();
				if (!deviceInfo(driver, "model").contains("iPad")) {
					params.clear();
					params.put("label", "Close All");
					params.put("threshold", 90);
					params.put("index", "2");
					params.put("timeout", "5");
					params.put("ignorecase", "nocase");
					driver.executeScript("mobile:button-text:click", params);
				}
			} catch (Exception e) {
				break;
			}
			i++;
		}
		try{
			params = new HashMap<>();
			params.put("identifier", "com.apple.mobilesafari");
			driver.executeScript("mobile:application:close", params);
		}catch(Exception e){}
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
	}

}

