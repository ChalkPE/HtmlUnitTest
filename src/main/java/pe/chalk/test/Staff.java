package pe.chalk.test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author ChalkPE <amato0617@gmail.com>
 * @since 2015-09-27
 */
public class Staff extends WebClient {
    public Staff() throws IOException {
        super(BrowserVersion.CHROME);
        this.waitForBackgroundJavaScript(5000);

        final HtmlPage loginPage = this.getPage("https://nid.naver.com/nidlogin.login");
        final HtmlForm loginForm = loginPage.getFormByName("frmNIDLogin");

        final HtmlTextInput idInput = loginForm.getInputByName("id");
        final HtmlPasswordInput pwInput = loginForm.getInputByName("pw");
        final HtmlSubmitInput loginButton = (HtmlSubmitInput) loginForm.getByXPath("//fieldset/span/input").get(0);

        Properties accountProperties = new Properties();
        accountProperties.load(new FileInputStream("account.properties"));

        idInput.setValueAttribute(accountProperties.getProperty("user.id"));
        pwInput.setValueAttribute(accountProperties.getProperty("user.pw"));
        loginButton.click();
    }

    public List<String> getStaffArticles() throws IOException {
        return ((HtmlPage) this.getPage("http://m.cafe.naver.com/StaffArticleList.nhn?search.clubid=23683173&search.menuid=23&search.boardtype=L"))
                .getByXPath("//ul[@class='lst4']/li/a/p/strong").stream()
                .map(elem -> ((HtmlElement) elem).asText())
                .collect(Collectors.toList());
    }

    public static void main(String[] args){
        System.setErr(new PrintStream(new OutputStream(){
            @Override
            public void write(int b) throws IOException{
                //I HATE RED LOGS!
            }
        }));

        try(final Staff staff = new Staff()){
            staff.getStaffArticles().forEach(System.out::println);
        }catch(final IOException e){
            e.printStackTrace();
        }
    }
}
