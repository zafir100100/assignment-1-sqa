package testrunner;

import io.qameta.allure.Allure;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.DashboardPage;
import pages.EmployeePage;
import pages.LoginPage;
import setup.Setup;
import utils.Utils;

import java.io.IOException;
import java.util.List;

public class LoginTestRunner extends Setup {
    LoginPage loginPage;
    DashboardPage dashboardPage;
    EmployeePage employeePage;

    @Test(description = "User can not login with wrong credential")
    public void doLoginWithWrongCreds() {
        driver.get("https://opensource-demo.orangehrmlive.com");
        loginPage = new LoginPage(driver);
        loginPage.doLogin("wrongUser", "password");
        String validationErrorActual = driver.findElement(By.className("oxd-alert-content-text")).getText();
        String validationErrorExpected = "Invalid credentials";
        Assert.assertTrue(validationErrorActual.contains(validationErrorExpected));
        Allure.description("Admin failed to log in");
    }

    @Test(priority = 1, description = "Admin log in successfully")
    public void doLogin() {
        driver.get("https://opensource-demo.orangehrmlive.com");
        loginPage = new LoginPage(driver);
        loginPage.doLogin("admin", "admin123");
        String urlActual = driver.getCurrentUrl();
        String urlExpected = "dashboard";
        Assert.assertTrue(urlActual.contains(urlExpected));
    dashboardPage = new DashboardPage(driver);
        Allure.description("Admin logged in successfully");
    }

    @Test(priority = 2, description = "Create two new Employees")
    public void createEmployee() throws InterruptedException, IOException, ParseException {
        dashboardPage.sideNavs.get(1).click();
        employeePage = new EmployeePage(driver);
        // click on top nav
        employeePage.topNavs.get(2).click();
        Thread.sleep(4000);
        for (int i = 0; i < 2; i++) {
            Utils utils = new Utils();
            utils.generateRandomData();
            int randomId = Utils.generateRandomNumber(1000, 9999);
            String firstName = utils.getFirstName();
            String lastName = utils.getLastName() + randomId;
            String userName = firstName + randomId;
            String password = "P@ssword123";
            Thread.sleep(2000);
            employeePage.toggleButton.click();
            Thread.sleep(4000);
            int j = 0;
            for (WebElement txtUserCred : employeePage.txtUserCreds) {
                txtUserCred.clear();
                if (j == 0) {
                    txtUserCred.sendKeys(firstName);
                } else if (j == 2) {
                    txtUserCred.sendKeys(lastName);
                } else if (j == 3) {
                    txtUserCred.sendKeys("" + randomId);
                }
                j++;
            }
            employeePage.createEmployee(firstName, lastName, userName, password, password);
            Thread.sleep(2000);
            List<WebElement> headerTitle = driver.findElements(By.className("orangehrm-main-title"));
            Utils.waitForElement(driver, headerTitle.get(0), 50);
            if (headerTitle.get(0).isDisplayed()) {
                utils.saveJsonList(userName, password);
            }
            employeePage.topNavs.get(2).click();
            Thread.sleep(2000);
        }
        Assert.assertTrue(driver.findElements(By.className("orangehrm-main-title")).get(0).isDisplayed());
    }

    @Test(priority = 5, description = "Search for first created user")
    public void searchUser() throws IOException, ParseException, InterruptedException {
        String fileName = "./src/test/resources/Users.json";
        List data = Utils.readJSONArray(fileName);
        JSONObject userObj = (JSONObject) data.get(data.size() - 2);
        String userName = (String) userObj.get("userName");
        String firstName = userName.replaceAll("\\d", "");
        dashboardPage.sideNavs.get(1).click();
        Thread.sleep(2000);
        driver.findElements(By.tagName("input")).get(1).clear();
        driver.findElements(By.tagName("input")).get(1).sendKeys(firstName);
        driver.findElements(By.cssSelector("button[type='submit']")).get(0).click();
        WebElement table = driver.findElement(By.className("oxd-table-body"));
        List<WebElement> allRows = table.findElements(By.className("oxd-table-row"));
        int i = 0;
        for (WebElement row : allRows) {
            List<WebElement> cells = row.findElements(By.className("oxd-table-cell"));
            for (WebElement cell : cells) {
                if(cell.getText().contains(firstName)){
                    Assert.assertTrue(true);
                    break;
                }
                i++;
            }
        }
    }

    @Test(priority = 6, description = "Login with second created user")
    public void loginWith2ndUser() throws IOException, ParseException, InterruptedException {

        driver.findElements(By.className("oxd-userdropdown-tab")).get(0).click();
        driver.findElements(By.className("oxd-userdropdown-link")).get(3).click();
        String fileName = "./src/test/resources/Users.json";
        List data = Utils.readJSONArray(fileName);
        JSONObject userObj = (JSONObject) data.get(data.size() - 1);
        String userName = (String) userObj.get("userName");
        String password = (String) userObj.get("password");
        loginPage = new LoginPage(driver);
        loginPage.doLogin(userName, password);
        String urlActual = driver.getCurrentUrl();
        String urlExpected = "dashboard";
        Assert.assertTrue(urlActual.contains(urlExpected));
        Allure.description("Second User logged in successfully");
    }

    @Test(priority = 7, description = "Click On my info menu")
    public void clickOnMyInfo() throws IOException, ParseException, InterruptedException {
        String actual = driver.findElements(By.className("oxd-test")).get(0).getText();
        String expected = "Personal Details";
        Assert.assertTrue(actual.contains(expected));
        Allure.description("User clicked on my menu successfully");
    }

}
