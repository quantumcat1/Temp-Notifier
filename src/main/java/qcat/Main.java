package qcat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class Main {

	private static HtmlElement latestMessage = null;
	private static User user;
	private static HtmlPage messagePage = null;
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("Reading user.txt...");
		try
		{
			user = getUser();
		}
		catch (Exception e)
		{
			System.out.println("Something went wrong reading user.txt. Please check it.\n"
					+ "Here is the stack trace if that helps you work out what went wrong:\n");
			e.printStackTrace();
			return;
		}
		System.out.println("Successfully read user.txt");
		System.out.println("Welcome, " + user.getUsername() + ", to Temp Notifier.\n"
				+ "To quit please go into Task Manager and close the Java program that's running\n"
				+ "(see the thread on GBAtemp, or the readme in the repo, for details)");
		
		
		
		
		
		
		System.out.println("Reading in cookie if there is one...");
		File file = new File("cookies.file");
    	Set<Cookie> cookies = null;

    	if(!file.createNewFile() && file.length() > 0)
    	{
	    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
	    	try
	    	{
	    		while(true)
	    		{
	    			cookies = (Set<Cookie>) in.readObject();
	    		}
	    	}
	    	catch(Exception e)
	    	{
	    		//do nothing
	    	}
	    	finally
	    	{
	    		if(in != null) in.close();
	    	}
    	}
    	
    	WebClient webClient = initialiseClient();
    	
    	if(cookies != null && cookies.size() > 0)
    	{
    		Iterator<Cookie> i = cookies.iterator();
	    	while(i.hasNext())
	    	{
	    		webClient.getCookieManager().addCookie(i.next());
	    	}
    	}
    	
    	try
    	{
    		messagePage = webClient.getPage("https://gbatemp.net/conversations/");
    	}
    	catch(com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException e) //have to log in
    	{
    		System.out.println("Failed to open 'https://gbatemp.net/conversations/', so logging in...");
    		HtmlPage page = webClient.getPage("https://www.gbatemp.net/login");
        	HtmlForm form = page.getHtmlElementById("pageLogin");
        	HtmlSubmitInput button = form.getInputByValue("Log in");
            HtmlTextInput textUsername = form.getInputByName("login");
            HtmlPasswordInput textPassword = form.getInputByName("password");
            HtmlCheckBoxInput checkStay = form.getInputByName("remember");

            textUsername.type(user.getUsername());
            textPassword.type(user.getPassword());
            checkStay.setChecked(true);
            
            button.click();
            
            //save cookie for next time
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream("cookies.file"));
            Set<Cookie> temp = webClient.getCookieManager().getCookies();
            out.writeObject(temp);
            out.close();
            
            messagePage = webClient.getPage("https://gbatemp.net/conversations/");
    	}
    	
    	System.out.println("Now checking for messages...");
    	

		
		
		
		while(true)
		{
			List<HtmlElement> messages = messagePage.getByXPath("//li[contains(@id,'conversation')]");
	    	if(latestMessage == null)
	    	{
	    		latestMessage = messages.get(0);
	    		TimeUnit.SECONDS.sleep(10);
				messagePage = webClient.getPage("https://gbatemp.net/conversations/");
				continue;
	    	}
	    	if(!latestMessage.getTextContent().trim().equals(messages.get(0).getTextContent().trim()))
	    	{
	    		HtmlAnchor a = messages.get(0).getFirstByXPath("//dl[@class='lastPostInfo']//a");
	    		String author = a.getTextContent().trim();
	    		
	    		a = messages.get(0).getFirstByXPath("//div[@class='listBlock main']//div[@class='titleText']//h3//a");
	    		String subject = a.getTextContent().trim();
	    		
	    		a = messages.get(0).getFirstByXPath("//div[@class='listBlock main']//div[@class='titleText']//div[@class='secondRow']//a[@class='faint']");
	    		String link = a.getHrefAttribute();
	    		
	    		String m = "New message from " + author 
	    				+ "\nSubject: " + subject
	    				+ "\nhttps://gbatemp.net/" + link;
	    		String encodedUrl = URLEncoder.encode(m, "UTF-8");
	    		sendMessage(encodedUrl, user.getApiKey(), user.getPhone());
	    		latestMessage = messages.get(0);
	    	}

			TimeUnit.SECONDS.sleep(10);
			messagePage = webClient.getPage("https://gbatemp.net/conversations/");
		}

		

	}
	
	private static WebClient initialiseClient()
	{
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
	    webClient.getOptions().setThrowExceptionOnScriptError(false);
	    webClient.getOptions().setCssEnabled(false);
	    webClient.getOptions().setJavaScriptEnabled(false);
	    return webClient;
	}


	private static User getUser() throws IOException {
		FileReader input = new FileReader("user.txt");
		BufferedReader bufRead = new BufferedReader(input);
		String myLine = bufRead.readLine();

		String[] array1 = myLine.split("\\|");

		bufRead.close();

		return new User(array1[0], array1[1], array1[2], array1[3]);
	}

	private static void sendMessage(String message, String apiKey, String phone)
			throws Exception {

		String url = "https://platform.clickatell.com/messages/http/send?apiKey="
				+ apiKey + "==&to=" + phone + "&content=" + message;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());
	}

}
