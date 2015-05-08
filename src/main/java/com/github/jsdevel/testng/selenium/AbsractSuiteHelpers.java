package com.github.jsdevel.testng.selenium;

import com.github.jsdevel.testng.selenium.annotations.driverconfig.UserAgent;
import com.github.jsdevel.testng.selenium.annotations.drivers.Chrome;
import com.github.jsdevel.testng.selenium.annotations.drivers.Firefox;
import com.github.jsdevel.testng.selenium.annotations.drivers.InternetExplorer;
import com.github.jsdevel.testng.selenium.exceptions.MissingPageFactoryException;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import net.anthavio.phanbedder.Phanbedder;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Internal helpers for AbstractSuite.
 * 
 * @author Joe Spencer
 */
class AbsractSuiteHelpers {
  private static final File phantomBinary = Phanbedder.unpack();

  static <PF extends PageFactory> void addPageFactory(MethodContextImpl context) {
    Class<?> suite = context.method.getDeclaringClass();
    ParameterizedType abstractSuite;

    try {
       abstractSuite = (ParameterizedType)suite.getGenericSuperclass();
    } catch (ClassCastException e) {
      throw new MissingPageFactoryException(
          "AbstractSuite must receive Type parameters I.E. class MySuite " +
          "extends AbstractSuite<MyPageFactory>.  None were given in " +
          suite.getName());
    }

    Class<PF> pageFactoryClass = (Class<PF>) abstractSuite.getActualTypeArguments()[0];
    context.setPageFactory(PageFactoryProxy.newInstance(pageFactoryClass, context));
  } 

  static synchronized void addWebDriver(MethodContextImpl context) {
    Method method = context.method;
    if (method.isAnnotationPresent(Chrome.class)) {
      addChromeDriver(context);
    } else if (method.isAnnotationPresent(Firefox.class)) {
      addFirefoxDriver(context);
    } else if (method.isAnnotationPresent(InternetExplorer.class)) {
      addInternetExplorerDriver(context);
    } else {
      addPhantomJSDriver(context);
    }
  }

  static void addUserAgent(MethodContextImpl context) {
    Method method = context.method;
    if (method.isAnnotationPresent(UserAgent.class)) {
      context.setUserAgent(method.getAnnotation(UserAgent.class).value()); 
    }
  }

  private static void addChromeDriver(MethodContextImpl context) {
    ChromeDriver driver = new ChromeDriver();
    context.setWebDriver(driver);
  }

  private static void addFirefoxDriver(MethodContextImpl context) {
    /*FirefoxDriver driver = new FirefoxDriver();
    context.setWebDriver(driver);*/
  }

  private static void addInternetExplorerDriver(MethodContextImpl context) {
    /*InternetExplorerDriver driver = new InternetExplorerDriver();
    context.setWebDriver(driver);*/
  }

  private static void addPhantomJSDriver(MethodContextImpl context) {
    DesiredCapabilities dcaps = new DesiredCapabilities();
    dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
        phantomBinary.getAbsolutePath());
    /*
    dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,                      
        new String[] {                                                                  
          "--cookies-file=" + TestHelper.getPhantomCookieFilePath(context),             
          "--web-security=false",                                                       
          "--ignore-ssl-errors=true",                                                   
          "--ssl-protocol=any",                                                         
          "--local-storage-path=/some/path" + TestHelper.getLocalStoragePath(context)
        });*/                                                                          

    if (context.getUserAgent() != null) {
      dcaps.setCapability("phantomjs.page.settings.userAgent", context.getUserAgent()); 
    }

    PhantomJSDriver driver = new PhantomJSDriver(dcaps);
    //driver.manage().window().setSize(context.getDimension());
    context.setWebDriver(driver);
  }
}
