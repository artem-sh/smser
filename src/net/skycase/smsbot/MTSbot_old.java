package net.skycase.smsbot;

/**
 * @author Artem
 */

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class MTSbot_old {

	HttpClient client;
	String imgFileName;
	
	//URL for sending SMS
	public static final String MTS_URL = "http://sms.mts.ru/";
	
	//A part of html code from url MTS_URL previous to Code Image file URL. See example:
	//<img id="ctl00_ctl09_ctl00_ctl01_ctl00_ctl01_ctl00_ctl00_ctl00_ctl02_ctl00_ctl00_ctl00_ctl00_ctl01_imgCode" src="../pic.aspx?rand=633284509571966242" style="border-width:0px;" />
	//TODO! Check url (../pic or /pic)
	public static final String PREIMAGE_HTML_CODE = "/pic.aspx?rand=";
	
	//Charater that tells us about end of Code Image URL
	public static final String IMAGE_URL_ESCAPE_CHAR = "\"";
	
	//Base URL of Code Image. Exaple of full URL is:
	public static final String BASE_IMG_URL = MTS_URL + PREIMAGE_HTML_CODE;
	
	//File name extension for Code Image
	public static final String CODE_IMAGE_EXT = ".gif";
	

	public static void main(String[] args) {
		try {
			MTSbot_old bot = new MTSbot_old();
			bot.getInitPage();
			bot.getImage();
		} catch (SkyBotException ex) {
			Logger.getLogger(MTSbot_old.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public MTSbot_old() {
		client = new HttpClient();
	}

	public void getInitPage() throws SkyBotException {
		GetMethod method = new GetMethod(MTS_URL);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + method.getStatusLine());
			}

			BufferedInputStream inResponseStream = new BufferedInputStream(method.getResponseBodyAsStream());
			StringBuffer pageBuffer = new StringBuffer();
			for (int c = inResponseStream.read(); c != -1; c = inResponseStream.read()) {
				pageBuffer.append((char) c);
			}
            
			//get the name of Code Image
			int prePos = pageBuffer.indexOf(PREIMAGE_HTML_CODE);
			if (prePos == -1) {
				throw new SkyBotException("Cant't find html code from url MTS_URL previous to Code Image file url");
			}
			
			int nameStartPos = prePos + PREIMAGE_HTML_CODE.length();
			imgFileName = pageBuffer.substring(nameStartPos, pageBuffer.indexOf(IMAGE_URL_ESCAPE_CHAR, nameStartPos));
			if (imgFileName == null || "".equals(imgFileName)) {
				throw new SkyBotException("Can't parse the Code Image file name from url MTS_URL");
			}			
		} catch (HttpException e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
	}

	public void getImage() {
		GetMethod getMethod = new GetMethod(BASE_IMG_URL + imgFileName);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		
        try {
            int statusCode = client.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + getMethod.getStatusLine());
            }
			
            BufferedInputStream bis = new BufferedInputStream(getMethod.getResponseBodyAsStream());
            FileOutputStream fos = new FileOutputStream(imgFileName + CODE_IMAGE_EXT);
            for (int c = bis.read(); c != -1; c = bis.read()) {
				fos.write(c);
            }
			
            bis.close();
			fos.close();
            } catch (HttpException ex) {
				Logger.getLogger(MTSbot_old.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
				Logger.getLogger(MTSbot_old.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
				getMethod.releaseConnection();
		}
	}
	
	public void postData() {
		PostMethod postMethod = new PostMethod("http://localhost:8084/SmsServer/BalanceServlet");
		NameValuePair userid = new NameValuePair("login", "artem");
		NameValuePair password = new NameValuePair("password", "12345");
		postMethod.setRequestBody(new NameValuePair[]{userid, password});

		try {
			client.executeMethod(postMethod);
			System.out.println("Login form post: " + postMethod.getStatusLine().toString());
			postMethod.releaseConnection();
		} catch (HttpException ex) {
			Logger.getLogger(MTSbot_old.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(MTSbot_old.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}