package r2d2;


import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;





//REFACTOR CODE TO SEPERATE OUT verify/waitFor/and other methods in parent class
@SuppressWarnings("all")
public class Utilities {
	public static int executionDelay = 0,  timeOutSeconds = 0; // various types of delay
	private static int passCmdCnt = 0, failedCmdCnt = 0, skippedCmdCnt = 0; // counter for pass,failed and skipped command
	private static int pollInterval = 1000; //wait and poll time
	private int testCaseCounter = 1;
	
	//Environment details
	public String baseURL = "", browser = "", locatorFilePath="", gridHubURL=""; 
	public String ieDriverPath = "", chromeDriverPath = "",safariDriverPath="", ieProfile="", chromeProfile="", ffProfile = "", safariProfile=""; 
	public static boolean highlightElement = false; // indicate weather highlight element or not during execution
	public static boolean verbose = false;  // used for debugging to show all the message
	private boolean browserLoaded = false;
	public  Logger logger = Logger.getLogger("Utilities");

	
	private Stack<String> myStack = new Stack(); // stack used to executed if-endif, conditions b
 
	public Map assignVariable = new HashMap(); // hash map is used to store variables from test case i.e. ${name}
	public StrSubstitutor substitueVariable = new StrSubstitutor(assignVariable);

	public String currTestCaseFilename = "X"; // current test case name used to take screenshot
	public String failedCommandLogFile = "log/failed_commands.log";
	public String configFilePath = "config/config.properties";
	public String log4jFilePath = "config/log4j.properties";
	
	public WebDriver driver;
	public WebDriverWait wait;
	public WebDriverWait zeroWait;
	public WebElement w = null;
	private Actions actionBuilder = null;
	private Action action = null;
	
	private boolean asserted = false;
	
	public String OS =null;
	private DataUtility dataUtil = new DataUtility();
	
	//selenium grid variable
	protected static boolean runOnGrid = false;
	public String platForm="UNKNOWN";
	//public static Utilities u = new Utilities();

	public static void main(String[] args) {
		//Use this command in xls file to contact selence command
		//=CONCATENATE("u.",A1,"(""",B1,IF(C1="",""");",CONCATENATE(""",""",C1,""");")))
		Utilities u = new Utilities();
	}

	
	/*
	 * Default constructor wipes out existing log files
	 */
	public Utilities() {
		//System.out.println("Inside Utility Constructor");
		//FileWriter stat;
		
		try {
			Properties properties = new Properties();
			properties.load(new FileReader(configFilePath));
			PropertyConfigurator.configure(log4jFilePath);
			try {
				this.baseURL = properties.getProperty("BASEURL");
				this.browser = properties.getProperty("BROWSER");
				this.timeOutSeconds = Integer.parseInt(properties.getProperty("TIMEOUT"));
				this.locatorFilePath = properties.getProperty("OBJECTREPOSITORY");
				this.executionDelay =  Integer.parseInt(properties.getProperty("EXECUTIONSPEED"));
			} catch (Exception e) {
				logger.error("Error Initilazing variable (BASEURL, BROWSER, TIMEOUT, OBJECTREPOSITORY, EXECUTIONSPEED) in "+this.configFilePath);
				logger.error("Error: "+e);
			}
			
			try {
				this.verbose = properties.getProperty("VERBOSE").matches("true") ? true : false;
				this.highlightElement = properties.getProperty("HIGHLIGHTELEMENT").matches("true") ? true : false;
			} catch (Exception e) {
				logger.error("Error Initilazing variable (VERBOSE,HIGHLIGHTELEMENT ) in "+this.configFilePath);
				logger.error("Error: "+e);
			}

			try {
				this.runOnGrid = properties.getProperty("RUN_TEST_ON_SELENIUM_GRID").matches("true") ? true : false;
				this.gridHubURL = properties.getProperty("GRIDHUBURL");
				
			} catch (Exception e) {
				logger.error("Error Initilazing Selenium grid variables (GRIDHUBURL & RUN_TEST_ON_SELENIUM_GRID ) from "+this.configFilePath);
				logger.error("Error: "+e);
			}
			
			try {
				this.ffProfile = properties.getProperty("FFPROFILE");
				this.chromeDriverPath = properties.getProperty("CHROMEDRIVERPATH");
				this.ieDriverPath = properties.getProperty("IEDRIVERPATH");
				this.safariDriverPath = properties.getProperty("SAFARIDRIVERPATH");
			} catch (Exception e) {
				logger.error("Error Initilazing variable (FFPROFILE, CHROMEDRIVERPATH, IEDRIVERPATH, SAFARIDRIVERPATH ) in "+this.configFilePath);
				logger.error("Error: "+e);
			}
			OS = System.getProperty("os.name");

			/*
			if (this.baseURL.trim().length() == 0){  
				this.baseURL = properties.getProperty("BASEURL");
			}

			if (this.browser.trim().length() == 0){  // if user sets the browser manually
				this.browser = properties.getProperty("BROWSER");
			}
			
			if (this.highlightElement == false){  // if user sets the values manually
				this.browser = properties.getProperty("BROWSER");
			}

			if (this.verbose  == false){  // if user sets the values manually
				this.verbose = properties.getProperty("VERBOSE").matches("true") ? true : false;
			}

			if (this.runOnGrid == false){  // if user sets the values manually
				this.runOnGrid = properties.getProperty("RUN_TEST_ON_SELENIUM_GRID").matches("true") ? true : false;
			}

			if (this.timeOutSeconds == 0){  // if user sets the timeOutSeconds manually
				this.timeOutSeconds = Integer.parseInt(properties.getProperty("TIMEOUT"));
			}

			if (this.locatorFilePath.trim().length() == 0){  // if user sets the object repository manually
				this.locatorFilePath = properties.getProperty("OBJECTREPOSITORY");
			}

			if (this.chromeDriverPath.trim().length() == 0){  // if user sets the object repository manually
				this.chromeDriverPath = properties.getProperty("CHROMEDRIVERPATH");
			}


			if (this.executionDelay == 0){  // if user sets the delay interval manually
				this.executionDelay =  Integer.parseInt(properties.getProperty("EXECUTIONSPEED"));
			}

			if (this.gridHubURL.trim().length() == 0){  
				this.gridHubURL = properties.getProperty("GRIDHUBURL");
			}
			*/

			
			//stat = new FileWriter(failedCommandLogFile);
			//stat.close();
		} catch (IOException e) {
			logger.error("Exception occured during initialtizing variable from config.properties file");
			logger.error("Error: "+e);
		}

	}

	/*
	 * Method to initialize browser
	 */
	public void loadBrowser() {
		try {
			// Load browser depending on browser name
			if (runOnGrid){ //check if execution should happen over selenium grid
				 URL hubUrl = new URL(gridHubURL);
			     DesiredCapabilities capabilities = new DesiredCapabilities();
			     capabilities.setBrowserName(this.browser);
			     
			     if(this.platForm.equalsIgnoreCase("windows")){
				     capabilities.setPlatform(Platform.WINDOWS);
			     }else if(this.platForm.equalsIgnoreCase("linux")){
				     capabilities.setPlatform(Platform.LINUX);
				 }else if(this.platForm.equalsIgnoreCase("max")){
				     capabilities.setPlatform(Platform.MAC);
				 }
			     driver = new RemoteWebDriver(hubUrl, capabilities);
			}else if (this.browser.equalsIgnoreCase("FireFox")) {
				//this.ffProfile = properties.getProperty("FFPROFILE");
				ProfilesIni profileIni = new ProfilesIni();
				FirefoxProfile profile = null;
				
				if(this.ffProfile.equalsIgnoreCase("fresh")){
					// start a fresh instance without any plug-in,cache,cookie
					this.driver = new FirefoxDriver();
				}else if(this.ffProfile.matches("default")){
					profile = profileIni.getProfile(this.ffProfile);
					this.driver = new FirefoxDriver(profile);
				}else{
					profile = new FirefoxProfile(new File(this.ffProfile));
					this.driver = new FirefoxDriver(profile);		
				}

			} else if (this.browser.equalsIgnoreCase("Chrome")) {
				//chromeDriverPath = properties.getProperty("CHROMEDRIVERPATH");
				System.setProperty("webdriver.chrome.driver", chromeDriverPath);
				this.driver = new ChromeDriver();
			} else if (this.browser.equalsIgnoreCase("IE")) {
				if(OS.matches("Linux")){
					logger.error("Internet Exploer is not available for "+OS);
					return;
				}
				System.setProperty("webdriver.ie.driver", ieDriverPath);
				this.driver = new InternetExplorerDriver();
			} else if (this.browser.equalsIgnoreCase("Safari")) {
				if(OS.matches("Linux")){
					logger.error("Safari is not available for "+OS);
					return;
				}
				System.setProperty("webdriver.safari.driver", safariDriverPath);
				this.driver = new SafariDriver();
			} else if (this.browser.equalsIgnoreCase("Opera")) {
				this.driver = new OperaDriver();
			} else if (this.browser.equalsIgnoreCase("HtmlUnit")){
				this.driver = new HtmlUnitDriver();
			}else{
				logger.error("Typo in the browsername or Browser not supported by framework.\nPlease try with any of these: Firefox | Chrome | IE | Safari| Opera");
			}
			wait = new WebDriverWait(driver, timeOutSeconds);
			zeroWait = new WebDriverWait(driver, 0);
			actionBuilder = new Actions(driver);
			browserLoaded = true;
			this.driver.manage().window().maximize();
		}catch(UnreachableBrowserException e ){
			logger.error("Exception: Please check if Selenium Grid Hub is reachable");
			driver=null;
			return;
		}catch (Exception e) {
			logger.error("EXCEPTION: In loadBrowser. Please check the dependancies are available: ",e);
			driver = null;
			return;
		}
	}

	/*
	 * store key, value in the has which can be used later as variable. i.e.
	 * ${var1}
	 */
	/*
	public boolean store(String value, String key) {
		try {
			if (value.matches("rand\\(.*\\)"))
				value = randomData(value);
			this.assignVariable.put(key, value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	*/

	
	/*
	 * Highlight element during execution for 100ms by adding a border
	 */
	public void highlightElement(WebElement w) {
		try {
			if (highlightElement) {
				JavascriptExecutor js = (JavascriptExecutor) driver;
				String prevStyle = w.getAttribute("style");
				js.executeScript("arguments[0].setAttribute('style', arguments[1]);", w,prevStyle+ "backgroundcolor: yellow; border: 3px solid yellow;");
				pause(100);
				js.executeScript("arguments[0].setAttribute('style', arguments[1]);", w,prevStyle);
			}
		} catch (Exception e) {
			logger.error("EXCEPTION: while highlighting element");
		}
	}

	
	/*
	 * waitForAlert if the alert present and accepts it
	 */
	public boolean waitForAlert(String alertString) {
		Alert alert;
		try {
			if (wait.until(ExpectedConditions.alertIsPresent()) == null) {
				return false;
			}else {
				alert = driver.switchTo().alert();
				if (alert.getText().equals(alertString)) {
					alert.accept();
					return true;
				} else {
					logger.error("Expected Alert Text: " + alertString);
					logger.error("  Actual Alert Text: " + alert.getText());
					alert.dismiss();
					return false;
				}
			}
		}catch(TimeoutException e){
			logger.error("[TIMEOUT] waitForalert for : "+ alertString+" timedout after waiting for "+this.timeOutSeconds + " seconds");
			return false;
		}catch (Exception e) {
			logger.error("EXCEPTION: In waitForAlert for message: "+alertString);
		}
		return false;
	}

	
	/*
	 * verifyAlert is actually waitforAlert
	 */
	public boolean verifyAlert(String str) {
		return (waitForAlert(str));
	}

	
	/*
	 * Type to type anything in select box
	 */
	public boolean type(String locator, String value) {
		try {
			w = findElement(locator);
			if (w != null) {
				w.sendKeys(value);
				return true;
			}
		} catch (Exception e) {
			logger.error("EXCEPTION: In type for : "+locator);
		}
		return false;
	}

	
	/*
	 * Type to type anything in select box
	 */
	public boolean clear(String locator) {
		try {
			w = null;
			w = findElement(locator);
			if (w != null) {
				w.clear();
				return true;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}

	
	/*
	 * Click
	 */
	public boolean click(String locator) {
		try {
			// WebElement w= findElement(locator);
			w = null;
			w = findElement(locator);
			if (w != null) {
				w.click();
				return true;
			}

		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}


	/*
	 * double Click
	 */
	public boolean doubleClick(String locator) {
		try {
			// WebElement w= findElement(locator);
			w = null;
			w = findElement(locator);
			if (w != null) {
				actionBuilder.doubleClick(w).perform();
			}
			return true;
		} catch (Exception e) {
			logger.error("EXCEPTION: In doubleclick : "+locator);
		}
		return false;
	}

	
	/*
	 * Mouse over to check things like tooltip is displayed or not
	 */
	
	public boolean mouseHover(String locator) {
		w = findElement(locator);
		if (w != null){
			actionBuilder.moveToElement(w).perform();
			return true;
		}
		return false;
	}

	
	/*
	 *  Deletes all the cookie from current domain
	 */
	public boolean deleteAllVisibleCookies(){
		driver.manage().deleteAllCookies();
		return true;
	}
	

	/*
	 *  Deletes all the cookie from current domain
	 */
	public boolean deleteCookieNamed(String cookieName){
		driver.manage().deleteCookieNamed(cookieName);
		return true;
	}
	

	
	/*
	 * Select : for dropdown
	 */
	public boolean select(String locator, String value) {
		try {
			// /WebElement w= findElement(locator);
			w = null;
			w = findElement(locator);
			if (w != null) {
				new Select(w).selectByValue(value);
				return true;
			}
		} catch (Exception e) {
			logger.error("EXCEPTION: In selecting from dropdown: "+locator);
		}
		return false;
	}

	/*
	 * this is a getText method named as storeText to resemble with selenium
	 * command it return the text of object
	 */
	public String storeText(String locator) {
		try {
			w = findElement(locator);
			if (w != null) {
				return w.getText();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	/*
	 * this is a getValue method named as storeValue to resemble with selenium
	 * command it return the "value" attribute of object
	 */
	public String storeValue(String locator) {
		try {
			w = findElement(locator);
			if (w != null) {
				return w.getAttribute("value");
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	/*
	 * Verify if the text matches with the text in locator
	 */
	public boolean verifyText(String locator, String value) {
		boolean match = false;
		String actual = null;
		try {
			w = findElement(locator);
			if (w != null) {
				actual = w.getText();
				//str1 = str1.replace("\n", "").trim();
				match = actual.matches(value);
				if (!match) {
					logger.error("Expected Text: " + value);
					logger.error("  Actual Text: " + actual);
				}
			}
		} catch (Exception e) {
			logger.error("EXCEPTION: In verifyText for element: "+locator);
		}
		return match;
	}


	
	/*
	 * Verify if the text matches with the text in locator
	 */
	public boolean assertText(String locator, String value) {
		boolean match = false;
		String actual = null;
		try{
			w = findElement(locator);
			if (w != null) {
				actual = w.getText();
				Assert.assertEquals(actual, value);
			}
		} catch (Exception e) {
			logger.error("EXCEPTION: In assertText for element: "+locator);
		}
		return match;
	}
	
	
	/*
	 * Verify Title
	 */
	public boolean verifyTitle(String value) {
		boolean result = this.driver.getTitle().equals(value);
		return result;
	}
	

	/*
	 * Assert Title
	 */
	public boolean assertTitle(String value) {
		Assert.assertEquals(this.driver.getTitle(), value );
		return true;
	}
	

	/*
	 * Assert Title
	 */
	public boolean waitForTitle(String title) {
		boolean titlePresent=false;
		try{
			titlePresent = wait.until(ExpectedConditions.titleIs(title));
		}catch(TimeoutException e){
			logger.error("[TIMEOUT] waitForTitle for : "+title +" timedout after waiting for "+this.timeOutSeconds + " seconds");
			return titlePresent;
		}catch (Exception e) {
			logger.error("[EXCEPTION] in waitForTitle for : "+title );
			return titlePresent;
		}
		return titlePresent;
	}

	
	/*
	 * Verify if the value matches with the text in locator
	 */
	public boolean verifyValue(String locator, String value) {
		boolean match = false;
		try {
			// WebElement w= findElement(locator);
			w = null;
			w = findElement(locator);
			if (w != null) {
				match = w.getAttribute("value").matches(value);
				if (!match) {
					logger.error("Expected Value: " + value);
					logger.error("  Actual Value: " + w.getAttribute("value"));
				}
			}

		} catch (Exception e) {
			logger.error("EXCEPTION: In verifyValue for element: "+locator);
		}
		return match;
	}

	/*
	 * Verify element present and return if it is there
	 */
	public boolean verifyElementPresent(String locator) {
		try{
			w = findElement(locator);
			if (w != null) {
				return true;
			}
		} catch (Exception e) {
			logger.error("EXCEPTION: In verifyElementPresent for element: "+locator);
		}
		return false;
	}

	
	/*
	 * Asserr element present and return if it is there
	 */
	public boolean assertElementPresent(String locator) {
		try{
			w = findElement(locator);
			if (w == null){
				Assert.fail("Element Not Present: "+locator);
			}
		} catch (Exception e) {
			logger.error("Element Not Present: " + locator, e);
		}
		return false;
	}

	
	
	
	/*
	 * Wait for element to physically present (it may be invisible on browser by
	 * its property)
	 */
	public boolean waitForElementPresent(String locator) {
		
		boolean elementPresent = false;

		try{
			By newByObj  = findLocatorType(locator);
			elementPresent = wait.until(ExpectedConditions.presenceOfElementLocated(newByObj)) != null;
		}catch(TimeoutException e){
			logger.error("[TIMEOUT] waitForElementPresent for "+locator+" Timed Out after waiting for "+timeOutSeconds+" seconds.");
			return elementPresent;
		}catch (Exception e) {
			logger.error("EXCEPTION: In waitForElementPresent for element: "+locator);
		}
		return elementPresent;
	}

	
	/*
	 * Wait for element physically not present
	 */
	public boolean waitForElementNotPresent(String locator) {
		boolean currHighlightStatus = this.highlightElement;
		try{
			int i = timeOutSeconds;
			
			//disabled highlihght element feature
			this.highlightElement= false;
			
			while (i > 0 && w != null) {
				i--;
				w = findElement(locator);
				pause(pollInterval);			
			}
			if (w != null){
				logger.error("Element " + locator + " is still present");;
			}

		}catch(TimeoutException e){
			this.highlightElement = currHighlightStatus ;
			logger.error("[TIMEOUT] waitForElementNotPresent for "+locator+" Timed Out after waiting for "+timeOutSeconds+" seconds.");
			return false;
		} catch (Exception e) {
			logger.error("EXCEPTION: In waitForElementNotPresent for element: "+locator);
		}finally{
			this.highlightElement = currHighlightStatus ;
		}
		return false;
	}
	

	/*
	 * echo used to print messages
	 */
	public boolean echo(String str) {
		logger.info(str);
		return true;
	}
	

	/*
	 * wait for element element is visible and return if it is there
	 */
	public boolean waitForVisible(String locator) {
		boolean visible = false;
		try {
			w = findElement(locator);
			if (w != null) {
				visible = wait.until(ExpectedConditions.visibilityOf(w)) != null;
			}
		}catch(TimeoutException e){
			logger.error("[TIMEOUT] waitForVisible for "+locator+" Timed Out after waiting for "+timeOutSeconds+" seconds.");
		} catch (Exception e) {
			logger.error("EXCEPTION: In waitForVisible for element: "+locator);
		}
		return visible;
	}

	
	/*
	 * verify for element element is visible and stop flow if it is present
	 */
	public boolean verifyVisible(String locator) {
		boolean visible = false;
		try {
			w = findElement(locator);
			if (w != null){
				zeroWait.until(ExpectedConditions.visibilityOf(w));
				visible = true;
			}
		} catch (Exception e) {
			logger.error("EXCEPTION: In verifyVisible for element: "+locator);
		}
		return visible;
	}

	
	/*
	 * assert for element element is visible and stop flow if it is present
	 */
	public boolean assertVisible(String locator) {
		boolean visible = false;
		try {
			w = findElement(locator);
			if (w != null) {
				zeroWait.until(ExpectedConditions.visibilityOf(w));
				visible = true;
			}
		} catch (Exception e) {
			//logger.error(e);
			if (!visible) {
				Assert.fail("[ASSERT VISIBLE] failed for: "+locator);
			}
		}
		return visible;
	}
	
	
	/*
	 * verify for element element is not visible and returns false flow if it is present
	 */
	public boolean verifyNotVisible(String locator) {
		boolean invisible = false;
		By newByObj  = findLocatorType(locator);
    	invisible  = zeroWait.until(ExpectedConditions.invisibilityOfElementLocated(newByObj));

		return invisible ;
	}

	/*
	 * verify for element element is not visible and returns false flow if it is present
	 */
	public boolean assertNotVisible(String locator) {
		boolean invisible = false;
		By newByObj  = findLocatorType(locator);
    	invisible  = zeroWait.until(ExpectedConditions.invisibilityOfElementLocated(newByObj));
    	if(!invisible){
        	Assert.fail("[ASSERT NOT VISIBLE] failed for: "+locator);
    	}
		return invisible ;
	}

	

	/*
	 * wait for element disappears
	 */
	public boolean waitForNotVisible(String locator) {
		boolean invisible = false;
		By newByObj  = findLocatorType(locator);
		try {
	    	invisible  = wait.until(ExpectedConditions.invisibilityOfElementLocated(newByObj));
		}catch(TimeoutException e){
			logger.error("[TIMEOUT] waitForNotVisible for "+locator+" Timed Out after waiting for "+timeOutSeconds+" seconds.");
			return invisible;
		} catch (Exception e) {
			logger.error("EXCEPTION: In waitForNotVisible for element: "+locator);
		}
		return invisible ;

	}

	
	/*
	 * Wait for text
	 */
	public boolean waitForText(String locator, String pattern) {
		// WebElement w = findElement(locator);
		boolean textPresent = false;

		try {
			w = findElement(locator);
			if (w != null) {
				textPresent = wait.until(ExpectedConditions.textToBePresentInElement(w,pattern));
			}
		}catch(TimeoutException e){
			logger.error("[TIMEOUT] waitForText for "+locator+" Timed Out after waiting for "+timeOutSeconds+" seconds.");
			return textPresent;
		} catch (Exception e) {
			logger.error("EXCEPTION: In waitForText for element: "+locator);
		}
		return textPresent;
	}

	
	
	/*
	 *  verify Element clickable
	 */
	
	public boolean verifyElementClickable(String locator){
		boolean clickable = false;
		By newByObj  = findLocatorType(locator);
		clickable = zeroWait.until(ExpectedConditions.elementToBeClickable(newByObj)) != null;
		return clickable ;
	}


	/*
	 *  assert Element clickable
	 */
	
	public boolean assertElementClickable(String locator){
		boolean clickable = false;
		By newByObj  = findLocatorType(locator);
		clickable = zeroWait.until(ExpectedConditions.elementToBeClickable(newByObj)) != null;
		if(!clickable){
	    	Assert.fail("[ASSERT ELEMENT CLICKABLE] failed for: "+locator);
		}
		
		return clickable ;
	}

	
	/*
	 *  Wait For Element clickable
	 */
	
	public boolean waitForElementClickable(String locator){
		boolean clickable = false;
		By newByObj  = findLocatorType(locator);
		clickable = wait.until(ExpectedConditions.elementToBeClickable(newByObj)) != null;
		return clickable ;
	}

	
	
	
	
	/*
	 * this is getAttribute method. name starts with Store to match selenium IDE keywords
	 */
	public String storeAttribute(String locator, String value) {
		try {
			w = findElement(locator);
			if (w != null) {
				return w.getAttribute(value);
			}
		} catch (Exception e) {
			logger.error("EXCEPTION: In storeAttribute: "+locator);
		}
		return null;
	}

	
	
	/*
	 * Verify value of attribute. i.e. check if style=display:block or
	 * style=backgroundcolor:red etc
	 */
	public boolean verifyAttribute(String locator, String attribute) {
		try{
			w = findElement(locator);
			if (w != null) {
				Pattern pattern = Pattern.compile("(^[a-zA-Z ]*)(\\s*=\\s*)(.*)");
				Matcher matcher = pattern.matcher(attribute);
				if (matcher.find()) {
					String attributeType=matcher.group(1).trim();
					String expectedValue=matcher.group(3).trim();
					String actualAttribute = w.getAttribute(attributeType);
					boolean match = actualAttribute.matches(expectedValue);
					
					if (!match) {
						logger.error("Expected Attribute: " + expectedValue);
						logger.error("  Actual Attribute: " + actualAttribute);
					}
					return (match);
				} else {
					logger.error("Attribute string format is incorrect");
				}
			}
		} catch (Exception e) {
			logger.equals(e);
		}
		return false;

	}

	/*
	 * Wait for attribute
	 */
	public boolean waitForAttribute(String locator, String attribute) {
		String expectedValue="";
		String attributeType="";
		String actualAttribute="";
		
		boolean match=false;
		try{
			int localTimeOut = timeOutSeconds;
			w = findElement(locator);
			if (w != null) {
				Pattern pattern = Pattern.compile("(^[a-zA-Z ]*)(\\s*=\\s*)(.*)");
				Matcher matcher = pattern.matcher(attribute);
				
				if (matcher.find()) {
					attributeType=matcher.group(1).trim();
					expectedValue=matcher.group(3).trim();
					
					actualAttribute = w.getAttribute(attributeType).trim();
					match = actualAttribute.matches(expectedValue);
					while (!match && localTimeOut > 0) {
						actualAttribute = w.getAttribute(attributeType).trim();
						match = actualAttribute.matches(expectedValue);
						pause(pollInterval);
						localTimeOut --;
					}
				} else {
					logger.error("Attribute string format is incorrect");
				}
			}
		}catch(TimeoutException e){
			logger.error("[TIMEOUT] waitForAttribute for : "+ locator+" timedout after waiting for "+this.timeOutSeconds + " seconds");
			logger.error("[TIMEOUT] Expected: " + expectedValue + "\n  Actual: " + actualAttribute);
			return match;
		}catch (Exception e) {
			logger.error("EXCEPTION: In waitForAttribute for element: "+locator);
		}
		
		if (!match){
			logger.error("TIMEOUT: waitForAttribute: Expected was: " + expectedValue + "Actual value: " + actualAttribute);
		}
		return match;
	}

	/*
	 * Add a delay of N milliseconds
	 */
	public boolean pause(long value) {
		try{
			Thread.sleep(value);
			return true;
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}

	//pause method with string written to handle selence command which sends time in string 
	public boolean pause(String value) {
		return pause(Long.parseLong(value));
	}

	
	/*
	 * method to open the URL
	 */
	public boolean open(String url) {
		try {
			if(!browserLoaded){
				this.loadBrowser();
			}
			url = baseURL + url;
			driver.get(url);
			return true;
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}

	/*
	 * method to close the browser
	 */
	public boolean closeBrowser() {
		try {
			driver.close();
			browserLoaded = false;
			return true;
		} catch (Exception e) {
			logger.error(e);
		}
		return false;

	}

	/*
	 * Drag and Drop
	 */
	public boolean dragAndDrop(String sourceElement, String destinaionElement) {
		try {
			WebElement w1 = findElement(sourceElement);
			WebElement w2 = findElement(destinaionElement);

			actionBuilder.dragAndDrop(w1, w2);
			actionBuilder.build().perform();

			// (new Actions(driver)).dragAndDrop(w1, w2).perform();
			// Actions builder = new Actions(driver);
			// Action dragAndDrop =
			// builder.clickAndHold(w1).moveToElement(w2).release(w2).build();
			// dragAndDrop.perform();
			return true;
		} catch (Exception e) {
			logger.error("EXCEPTION: In dragAndDrop", e);
		}
		return false;
	}

	/*
	 * if condition return true or false
	 */
	public boolean ifCondition(String expr) {
		String expr1 = "", expr2 = "", operator = "";

		Pattern pattern = Pattern.compile("([^=!><]*)(\\s*[=><!]*\\s*)(.*)");
		Matcher matcher = pattern.matcher(expr);

		if (matcher.find()) {
			expr1 = matcher.group(1).trim();
			expr2 = matcher.group(3).trim();
			operator = matcher.group(2).trim();
			logger.info("INSIDE IF : 1:" + expr1 + "3:" + expr2 + "op:"+ operator);
		}

		boolean result = false;
		if (operator.matches("==") || operator.matches("matches")) {
			result = expr1.matches(expr2);
			// System.out.println("Result:"+result);
		} else if (operator.matches("!=")) {
			result = (!(expr1.matches(expr2)));
		} else if (operator.matches(">")) {
			result = (Double.parseDouble(expr1) > Double.parseDouble(expr2));
		} else if (operator.matches("<")) {
			result = (Double.parseDouble(expr1) < Double.parseDouble(expr2));
		} else if (operator.matches(">=")) {
			result = (Double.parseDouble(expr1) >= Double.parseDouble(expr2));
		} else if (operator.matches("<=")) {
			result = (Double.parseDouble(expr1) <= Double.parseDouble(expr2));
		} else {
			logger.error("INVALID OPERATOR: " + operator);
		}
		/*
		 * if(result){ this.variablesHash.put("once", "false"); }
		 */
		return result;
	}

	/*
	 * This method take file and input fill it in list and returns list
	 */
	public List<String> fileToList(String fileName) {
		List<String> cmdList = new ArrayList<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = "";
			while ((line = br.readLine()) != null) {
				cmdList.add(line);
			}
			br.close();

			Pattern pattern = Pattern.compile("(testcases/)(.*)");
			Matcher matcher = pattern.matcher(fileName);
			if (matcher.find()) {
				this.currTestCaseFilename = matcher.group(2);
				this.testCaseCounter = 0;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return cmdList;
	}


	/*
	 *  Select file from OS
	 */
	public boolean selectFileFromOS(String file){
		StringSelection filePath = new StringSelection(file);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(filePath, null);
		
		Robot r;
		try {
			r = new Robot();
			// paste string on box
			r.keyPress(KeyEvent.VK_CONTROL);
			r.keyPress(KeyEvent.VK_V);
			r.keyRelease(KeyEvent.VK_V);
			r.keyRelease(KeyEvent.VK_CONTROL);
			this.pause(500);
			
			//press enter
			r.keyPress(KeyEvent.VK_ENTER);
			r.keyRelease(KeyEvent.VK_ENTER);
			this.pause(500);
			return true;
		} catch (AWTException e) {
			logger.error("EXCEPTION: In selectFileFromOS for file: "+file);
		}	
		
		return false;
		
	}

	/*
	 * 
	 */

	public boolean showjsalert(String str) {
		Scanner in = new Scanner(System.in);
		System.out.println("Enter a string");
		in.nextLine();
		/*
		 * if (driver instanceof JavascriptExecutor) { ((JavascriptExecutor)driver).executeScript("alert('"+str+"');"); }
		 */
		return true;
	}


	/*
	 * Capture entirePareScreenshot
	 */

	public void captureEntirePageScreenshot(String fileName) {
		try {
			if (null == this.w) {
				File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(scrFile, new File(fileName));
			} else {
				try {
					JavascriptExecutor js = (JavascriptExecutor) driver;
					String prevStyle = w.getAttribute("style");
					js.executeScript("arguments[0].setAttribute('style', arguments[1]);",this.w,
							prevStyle + "backgroundcolor: yellow; border: 3px solid yellow;");
					File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
					FileUtils.copyFile(scrFile, new File(fileName));
					js.executeScript("arguments[0].setAttribute('style', arguments[1]);", this.w, prevStyle);
				} catch (StaleElementReferenceException e) {
					logger.error("EXCEPTION: Failed to highlight element as Element is stale");
					File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
					FileUtils.copyFile(scrFile, new File(fileName));
				} catch (Exception e) {
					logger.error("EXCEPTION: Failed to capture schreeshot", e);
				}
			}
		} catch (Exception e) {
			logger.error("EXCEPTION: while capturing screehost");
		}
	}

	/*
	 * This method uses Java Reflection class which take
	 * methodName,param1,paramN as argument and invokes the method
	 */
	public void invokeMethod(String command, String target, String value){
		try{
			testCaseCounter++; // increase test case counter by one
			// skip empty and commented lines
			if (command == null || command == "" || command.matches("^\\s*#.*")
					|| command.toLowerCase().matches("command")) {
				if (command.toLowerCase().matches("^\\s*#.*")) {
					skippedCmdCnt++;
					logger.warn("SKIPPED: " + command + "|" + target + "|"
							+ value);
				}
				return;
			}

			// when last condition if false skip command till endif
			if (command.equalsIgnoreCase("endif")
					|| command.equalsIgnoreCase("end if")) {
				if (!this.myStack.empty()) {
					if (this.myStack.peek().equals("false")) {
						this.myStack.pop();
					}
				}
				return;
			}

			// replace method name "if" with ifCondition
			if (command.equalsIgnoreCase("if")) {
				command = "ifCondition";
			}

			// if current command is not endif and last "if" was false then skip
			// command till next endif encounters
			if (!this.myStack.empty()) {
				if (this.myStack.peek().equals("false")) {
					return;
				}
			}

			// replace variables i.e. store|${VAR} with its appropriate value
			target = this.substitueVariable.replace(target);
			value = this.substitueVariable.replace(value);

			// unescape characters so that characters line \t dose not become
			// \\t
			value = StringEscapeUtils.unescapeJava(value);
			// target= StringEscapeUtils.unescapeJava(target);
			// line= StringEscapeUtils.unescapeJava(line);

			// no parameter
			Class<?>[] noParams = {};
			// one parameter
			Class<?>[] paramOneString = new Class[1];
			paramOneString[0] = String.class;
			// two parameter
			Class<?>[] paramTwoString = new Class[2];
			paramTwoString[0] = String.class;
			paramTwoString[1] = String.class;

			// Class<?> cls = Class.forName(u.getClass().getName());
			Class<?> cls = Class.forName(this.getClass().getPackage().getName() + "." + "Utilities");
			Object obj = this;

			boolean result = false;
			String resultStr = "", retType = "";

			Method method;
			if (target.equalsIgnoreCase("")) { // method with no parameters
				method = cls.getDeclaredMethod(command, noParams);
				// return values bases on its return type
				if (method.getReturnType().toString().matches("boolean")) {
					result = (Boolean) method.invoke(obj, null);
				} else if (method.getReturnType().toString()
						.matches("class java.lang.String")) {
					resultStr = (String) method.invoke(obj, null);
					result = resultStr.matches("__FALSE__") ? false : true;
				}
			} else if (value.equalsIgnoreCase("")) { // method with one parameters
				method = cls.getDeclaredMethod(command, paramOneString);

				if (method.getReturnType().toString().matches("boolean")) {
					result = (Boolean) method.invoke(obj, target.trim());
				} else if (method.getReturnType().toString()
						.matches("class java.lang.String")) {
					resultStr = (String) method.invoke(obj, target.trim());
					result = resultStr.matches("__FALSE__") ? false : true;
				}
			} else { // method with two parameters
				method = cls.getDeclaredMethod(command, paramTwoString);

				if (method.getReturnType().toString().matches("boolean")) {
					result = (Boolean) method.invoke(obj, target.trim(),
							value.trim());
				} else if (method.getReturnType().toString()
						.matches("class java.lang.String")) {
					resultStr = (String) method.invoke(obj, target.trim(),
							value.trim());
					result = resultStr.matches("__FALSE__") ? false : true;
				}
			}
			// replace method name "if" with ifCondition

			if (result) {
				this.passCmdCnt++;
				logger.info("[PASS] : " + command + "|" + target + "|" + value);
			} else {
				if (command.equalsIgnoreCase("ifCondition")) {
					this.myStack.push("false");
				} else {
					this.failedCmdCnt++;
					String logStr = "[FAILED] : " + currTestCaseFilename + "("
							+ this.testCaseCounter + ")-" + command + "|" + target + "|" + value;
					writeLogsTofile(logStr, failedCommandLogFile);
					// failedCommands.add(logStr);
					logger.error(logStr);

					if (null == this.w) {
						File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
						FileUtils.copyFile(scrFile, new File("output/"+ currTestCaseFilename + "_"
								+ this.testCaseCounter + "_" + command + "_" + dataUtil.getDate("dd_hhmmss", 0) + ".png"));
					}else{
						try{
							JavascriptExecutor js = (JavascriptExecutor) driver;
							String prevStyle = w.getAttribute("style");
							js.executeScript("arguments[0].setAttribute('style', arguments[1]);",
									this.w,prevStyle+ "backgroundcolor: yellow; border: 3px solid yellow;");
							File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
							FileUtils.copyFile(scrFile, new File("output/" + currTestCaseFilename + "_"
									+ this.testCaseCounter + "_" + command + "_"+ dataUtil.getDate("dd_hhmmss", 0) + ".png"));
							js.executeScript("arguments[0].setAttribute('style', arguments[1]);",this.w, prevStyle);
							System.out.println("PIC TAKEN:" + currTestCaseFilename + "_"
									+ this.testCaseCounter + "_" + command + "_"
									+ dataUtil.getDate("dd_hhmmss", 0) + ".png");
						}catch (StaleElementReferenceException e) {
							logger.error("EXCEPTION: Failed to highlight element as Element is stale");
							File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
							FileUtils.copyFile(scrFile, new File("output/"+ currTestCaseFilename + "_"
									+ this.testCaseCounter + "_" + command + "_" + dataUtil.getDate("dd_hhmmss", 0) + ".png"));
							System.out.println("PIC TAKEN:"+ currTestCaseFilename + "_"
									+ this.testCaseCounter + "_" + command + "_" + dataUtil.getDate("dd_hhmmss", 0) + ".png");
						}catch (Exception e) {
							logger.error("EXCEPTION: Failed to capture schreeshot",e);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("EXCEPTION: Command was not executed : "
					+ command + "|" + target + "|" + value, e);
		}
	}

	
	
	/*
	 * Take locator as input and return locator type: locator value
	 */
	private By findLocatorType(String locator){
		Properties properties = new Properties();
	    By byObj=null; //The by type can be name ,id ,class name - supported by the Selenium by class
	    By newByObj=null;

		String by = "";
		try{
			// Regex to check if locator type exists in locator
			Pattern pattern = Pattern.compile("(^[a-zA-Z ]*)(\\s*=\\s*)(.*)");
			Matcher matcher = null;

			// Check if locator exists in object repository
			properties.load(new FileReader(this.locatorFilePath));
			if (properties.getProperty(locator) != null) {
				locator = properties.getProperty(locator);
				matcher = pattern.matcher(locator);

				if (!matcher.find()){ // log error message for locator without locator type on object repository
					if (!(locator.matches("/.*") || locator.matches("\\.*") || locator.matches("\\(.*") )) { //in case locator is not present then default locator will be XPATH
						logger.warn("Locator without locatorType found in repository: "+locator);
					}
				}
			}else{ // if not found, log a warning message
				if(verbose){
					logger.warn("Did not find the "+locator+" in repository");
				}
			}
			// execute regex to check if locator type exists in locator
			matcher = pattern.matcher(locator);
			
			if (matcher.find()) { 				// locator found
				by = matcher.group(1);
				locator = matcher.group(3);
			} else {
				if (locator.matches("/.*") || locator.matches("\\.*") || locator.matches("\\(.*") ) { //in case locator is not present then default locator will be XPATH
					by = "xpath";
				}
			}

			if(by.equals("link")){
				by="linkText";
			}
			if(by.equals("css")){
				by="cssSelector";
			}
			
			String[] loctype = { "id", "name", "xpath", "partialLinkText","cssSelector", "className", "tagName", "linkText" };
			if(!Arrays.asList(loctype).contains(by)){
				if(by.trim().matches("")){
					logger.error("Locator type not found for" + locator);
				}else{
					logger.error("Invalid Locator type \""+by + "\" " + locator);
				}
				return null;
			}

			
		    //WebElement w=null; 
	        pause(executionDelay);
	        try {
	        	// get by By object
	        	Class<?> byClass = Class.forName(By.class.getName());
	        	Method getBy = byClass.getMethod(by, String.class);
	        	newByObj = (By) getBy.invoke(byObj, locator); // type cast to 'By' type
				
			} catch (Exception e) {
				logger.error("EXCEPTION: in findLocator \"By\" : "+locator);
			}

		}catch (Exception e){
			logger.error("EXCEPTION: Exception Occured in findLocatorType: "+e);
		}

        return newByObj;
	}

	
	
	//This is very important method as almost every methods calls this method before performing any action
    public WebElement findElement(String locator){
    	By newByObj;
        pause(executionDelay);
        try{
        	newByObj = findLocatorType(locator);
        	w = this.driver.findElement(newByObj);
	      }catch (ElementNotVisibleException env){
	    	  logger.error("EXCEPTION: Element found but not visible: " + locator);
	    	  logger.error(env.getCause());
	      }catch (Exception e){//General exception
					 //logger.error("EXCEPTION: Element not found:  " + locator );
	      }finally {
				if (w == null) {
					logger.error(" Couldn't find element: " + locator);
				}else {
					highlightElement(w);
				}
			}
        return w; //return the webelement 
      }
 

    
	//returns the list of web elements
    public List<WebElement> findElements(String locator){
		
        List<WebElement> wl = null;
        pause(executionDelay);
        try{
           	By newByObj = findLocatorType(locator);
        	wl = this.driver.findElements(newByObj);
	      }catch (ElementNotVisibleException env){
	    	  logger.error("EXCEPTION: Element found but not visible: " + locator);
	    	  logger.error(env.getCause());
	      }catch (Exception e){//General exception
	    	  logger.error("EXCEPTION: ELEMENT NOT FOUND " + e); 
	      }
        return wl; //return the webelement 
      }
 
    
    
	
	/*
	 * Execute test case directly from XLS file
	 */
	public void executeTestCase(String xlsFileName, String xlsTabName, boolean skipTitleRow) {
		try {
			ReadFromFile xls = new ReadFromFile();
			String[][] selence = xls.readFromXLS(xlsFileName, xlsTabName, skipTitleRow);
			String command = "", target = "", value = "";
			for (int i = 0; i < selence.length; i++) {
				if (this.asserted) {
					this.asserted = false;
					Assert.fail("COMMAND ASSERTED: " + command + "|" + target+ "|" + value);
				} else {
					command = selence[i][0];
					target = selence[i][1];
					value = selence[i][2];
					this.invokeMethod(command, target, value);
				}
			}
		} catch (Exception e) {
			this.logger.error(e);
		}

	}

	/*
	 * Write logs to the file
	 */
	public void writeLogsTofile(String str, String logFile) {
		try {
			FileWriter stat = new FileWriter(logFile, true);
			stat.write(str + "\n");
			stat.close();
		} catch (Exception e) {

		}
	}
 

	
	/*
	 * returns of Absolute XPath of current webElement
	 */
	public String getAbsoluteXPath(WebElement w){
		String jsString  = "function absoluteXPath(element) {"+
						"var comp, comps = [];"+
						"var parent = null;"+
						"var xpath = '';"+
						"var getPos = function(element) {"+
							"var position = 1, curNode;"+
							"if (element.nodeType == Node.ATTRIBUTE_NODE) {"+
								"return null;"+
							"}"+
							"for (curNode = element.previousSibling; curNode; curNode = curNode.previousSibling) {"+
								"if (curNode.nodeName == element.nodeName) {"+
									"++position;"+
								"}"+
							"}"+
							"return position;"+
						"};"+


						"if (element instanceof Document) {"+
							"return '/';"+
						"}"+
					
					    "for (; element && !(element instanceof Document); element = element.nodeType == Node.ATTRIBUTE_NODE ? element.ownerElement : element.parentNode) {"+
						    "comp = comps[comps.length] = {};"+
						    "switch (element.nodeType) {"+
							    "case Node.TEXT_NODE:"+
								    "comp.name = 'text()';"+
								    "break;"+
							    "case Node.ATTRIBUTE_NODE:"+
								    "comp.name = '@' + element.nodeName;"+
								    "break;"+
							    "case Node.PROCESSING_INSTRUCTION_NODE:"+
								    "comp.name = 'processing-instruction()';"+
								    "break;"+
							    "case Node.COMMENT_NODE:"+
								    "comp.name = 'comment()';"+
								    "break;"+
							    "case Node.ELEMENT_NODE:"+
								    "comp.name = element.nodeName;"+
								    "break;"+
						    "}"+
						    "comp.position = getPos(element);"+
					    "}"+
					
					    "for (var i = comps.length - 1; i >= 0; i--) {"+
						    "comp = comps[i];"+
						    "xpath += '/' + comp.name.toLowerCase();"+
						    "if (comp.position !== null) {"+
						    	"xpath += '[' + comp.position + ']';"+
						    "}"+
					    "}"+
					
					    "return xpath;"+

				"} return absoluteXPath(arguments[0]);";
		
		return (String) ((JavascriptExecutor) this.driver).executeScript(jsString, w);
	}


} // end of class
