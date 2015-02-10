import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Crawler {

	/**
	 * @param args
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) throws UnknownHostException,IOException {
		
		WebPageCrawler wCrawler = new WebPageCrawler("cs5700.ccs.neu.edu",80);
		wCrawler.login();
		wCrawler.searchFlags();

	}

}

class WebPageCrawler {
	
	private String host;
	
	private int port;
	
	private Socket clientSocket;
	
	private BufferedReader in;
	
	private PrintWriter out;
	
	private String currenthtmlfile;
	
	private HashMap<String,String> cookies = new HashMap<String, String>();
	
	private HashSet<String> visitedURL = new HashSet<String>();
	
	private Queue<String> unvisitedURL = new LinkedList<String>();
	
	private LinkedList<String> flags = new LinkedList<String>();
	
	public WebPageCrawler(String host, int port) {
		// TODO Auto-generated method stub
		this.host = host;
		this.port = port;
	}
	
	public void login() throws UnknownHostException, IOException{
		connectServer();
		sendMsg("GET /accounts/login/ HTTP/1.1\nHost: cs5700.ccs.neu.edu\n\n");
		String htmlString = receiveHTMLFile();
		fetchCookie(htmlString);
		
		connectServer();
		String content = "csrfmiddlewaretoken=" + cookies.get("csrftoken") + "&username=001140016&password=CTOQGEK3&next=%2Ffakebook%2F";
		int contentLength = content.length();
		
		String str = "POST /accounts/login/?next=/fakebook HTTP/1.1\nHost: cs5700.ccs.neu.edu\nReferer: http://cs5700.ccs.neu.edu/accounts/login/\n" +
				"Cookie: csrftoken=" + cookies.get("csrftoken") + "; sessionid=" + cookies.get("sessionid") + "\nConnection: Keep-alive\nUser-Agent: HTTPTool/1.1\n" +
						"Content-Type: application/x-www-form-urlencoded\nContent-Length: " + contentLength +"\n\n" + content + "\n";
		//System.out.println(str);
		sendMsg(str);
		htmlString = receiveHTMLFile();
		
		fetchCookie(htmlString);
		
		System.out.println(cookies);
	}
	
	public void searchFlags()  {
		String rootURL = "/fakebook/";
		unvisitedURL.add(rootURL);
		visitedURL.add(rootURL);
		String retryURL = null;
		while (flags.size()!= 5){
			try {
			if (unvisitedURL.isEmpty())
				break;
			String currentURL = unvisitedURL.poll();
		//	System.out.println(currentURL);
			downloadPages(currentURL);
			retryURL = currentURL;
			System.out.println(unvisitedURL.size() +"---"+ visitedURL.size()+"----" + flags.size()+flags);
			//System.out.print(visitedURL.size());
			fetchFlags();
		} catch(Exception e) {
			System.out.println("exception,retry" + e);
			unvisitedURL.add(retryURL);
		}
		}
		if(flags.size()==5)
		System.out.println("Successfully");
		System.out.println(flags);
	}
	private void downloadPages(String currentURL) throws UnknownHostException, IOException{
		connectServer();
		sendMsg("GET " + currentURL + " HTTP/1.1\nHost: cs5700.ccs.neu.edu\n" + 
		"Cookie: sessionid=" + cookies.get("sessionid") + "\n\n");
		currenthtmlfile = receiveHTMLFile();
	     
		fetchURL(currenthtmlfile);
	}
	private void fetchFlags() {
		// TODO Auto-generated method stub
		//regular expression
		//<h2 class='secret_flag' style="color:red">FLAG: 64-characters-of-random-alphanumerics</h2>
		String pattern = "<h2 class='secret_flag' style=\"color:red\">FLAG: [0-9a-zA-Z]{64}</h2>";
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(currenthtmlfile);
		while(matcher.find()){
			String tmp = matcher.group();
	//		if (!flags.contains(tmp)){
				flags.add(tmp);
	//		}
			
		}
	}

	private void fetchURL(String htmlfile){
		// TODO Auto-generated method stub
		String regex = "<a(.*?)a>";
		String patternString = "\"/fakebook(.*?)\"";
		StringBuilder builder = new StringBuilder();
		Pattern p = Pattern.compile(regex);
		String tmp;
		//while (htmlfile != null){
			Matcher m = p.matcher(htmlfile);
			while (m.find()){
	//			unvisitedURL.add(m.group());
				tmp = m.group();
			//	if (!visitedURL.contains(tmp)){
					//unvisitedURL.add(tmp);
					//visitedURL.add(tmp);
					//System.out.println(tmp);
					builder.append(tmp);
				//}	
			}
		  String preparsedstr = builder.toString();
		  
		  p = Pattern.compile(patternString);
		  m = p.matcher(preparsedstr);
		  while (m.find()){
			  tmp = m.group();
			  tmp = tmp.substring(1,tmp.length()-1);
		//	  System.out.println(tmp);
			  if (!visitedURL.contains(tmp)){
					unvisitedURL.add(tmp);
					visitedURL.add(tmp);
					//System.out.println(unvisitedURL);
			  }
						}
		//}
		//String url = "";
		//return url;
	}


	private void connectServer() throws UnknownHostException, IOException {
		clientSocket = new Socket(host, port);

		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		out = new PrintWriter(clientSocket.getOutputStream(), true);
	}
	
	private void sendMsg(String message) {
		out.println(message);
	}
	
	private String receiveHTMLFile() throws IOException {
		
		String responseLine;
		StringBuilder builder = new StringBuilder();
		
		while((responseLine = in.readLine()) != null)
		{
		//	System.out.println(responseLine);
			builder.append(responseLine);
			if(responseLine.contains("/html>"))
				break;
		}
		return builder.toString();
	}
	
	private void fetchCookie(String htmlfile) {
		
		String csrftoken = "";
		String sessionid = "";
		
		int csrfindex = htmlfile.indexOf("csrftoken");
		int sessionindex = htmlfile.indexOf("sessionid");
		if (csrfindex >= 0){
			csrftoken = htmlfile.substring(csrfindex+10,csrfindex+42);
		}
		if (sessionindex >= 0){
			sessionid = htmlfile.substring(sessionindex+10,sessionindex+42);
		}
		
		cookies.put("csrftoken", csrftoken);
		cookies.put("sessionid", sessionid);
		
		System.out.println(csrftoken);
		System.out.println(sessionid);
	}

}