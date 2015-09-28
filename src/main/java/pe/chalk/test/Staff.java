package pe.chalk.test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import pe.chalk.takoyaki.Takoyaki;
import pe.chalk.takoyaki.Target;
import pe.chalk.takoyaki.utils.TextFormat;

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
    private Target target;

    public Staff() throws IOException {
        this(null);
    }

    public Staff(Target target) throws IOException {
        this(target, Staff.getDefaultAccountProperties());
    }

    public Staff(Target target, Properties accountProperties) throws IOException {
        super(BrowserVersion.CHROME);
        this.target = target;

        if(!this.login(accountProperties)){
            throw new IllegalStateException("로그인 실패");
        }
    }

    private static Properties getDefaultAccountProperties() throws IOException {
        Properties accountProperties = new Properties();
        accountProperties.load(new FileInputStream("account.properties"));

        return accountProperties;
    }

    private boolean login(Properties accountProperties) throws IOException {
        final HtmlPage loginPage = this.getPage("https://nid.naver.com/nidlogin.login");
        final HtmlForm loginForm = loginPage.getFormByName("frmNIDLogin");

        final HtmlTextInput idInput = loginForm.getInputByName("id");
        final HtmlPasswordInput pwInput = loginForm.getInputByName("pw");
        final HtmlSubmitInput loginButton = (HtmlSubmitInput) loginForm.getByXPath("//fieldset/span/input").get(0);

        idInput.setValueAttribute(accountProperties.getProperty("user.id"));
        pwInput.setValueAttribute(accountProperties.getProperty("user.pw"));
        return !((HtmlPage) loginButton.click()).asText().contains("The username or password you entered is incorrect.");
    }

    public Target getTarget(){
        return this.target;
    }

    public void setTarget(Target target){
        this.target = target;
    }

    private List<String> getArticles(String url) throws IOException {
        return ((HtmlPage) this.getPage(url))
                .getByXPath("//ul[@class='lst4']/li/a/p/strong").stream()
                .map(elem -> ((HtmlElement) elem).asText())
                .collect(Collectors.toList());
    }

    public List<String> getNewArticles() throws IOException {
        return getArticles("http://m.cafe.naver.com/" + this.getTarget().getAddress());
    }

    public List<String> getStaffArticles() throws IOException {
        return getArticles("http://m.cafe.naver.com/StaffArticleList.nhn?search.clubid=" + this.getTarget().getClubId() + "&search.menuid=23&search.boardtype=L");
    }

    public static void main(String[] args) throws IOException {
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException{
                //I HATE RED LOGS!
            }
        }));

        try(final Staff staff = new Staff()){
            Takoyaki takoyaki = new Takoyaki();
            takoyaki.start();

            staff.setTarget(takoyaki.getTargets().get(0));

            System.out.println(TextFormat.AQUA.getAnsiCode() + "*** 스탭 게시판 최근 글 목록 ***" + TextFormat.RESET.getAnsiCode());
            staff.getStaffArticles().forEach(System.out::println);
        }catch(final IOException e){
            e.printStackTrace();
        }
    }
}
