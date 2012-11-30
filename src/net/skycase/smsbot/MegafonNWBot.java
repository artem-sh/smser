package net.skycase.smsbot;

/**
 * @author Artem
 * 
 * This is Bot for North-West Megafon site (http://www.megafonnw.ru/info/rus/sms)
 */

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class MegafonNWBot {

	HttpClient client;
	String imgFileName;
	
	//URL for sending SMS
	public static final String MEGAFONNW_URL = "http://www.megafonnw.ru/info/rus/sms";
	
	//A part of html code from url MEGAFONNW_URL previous to Code Image url. See example:
	//<img border="0" height="18" width="43" src="/info/plain/smspic?code=87oX9koht3In"></td></tr>
	public static final String PREIMAGE_HTML_CODE = "servlet/send/confirm_code/";
	
	//Charater that tells us about end of Code Image name
	public static final String IMAGE_NAME_ESCAPE_CHAR = "\"";
	
	//Base URL of Code Image. Exaple of full URL is:
	//http://www.beonline.ru/servlet/send/confirm_code/dBlIR%5EPtY0.gif
	public static final String BASE_IMG_URL = "http://www.beonline.ru/" + PREIMAGE_HTML_CODE;
	

	public static void main(String[] args) {
		try {
			MegafonNWBot bot = new MegafonNWBot();
			bot.getInitPage();
		} catch (SkyBotException ex) {
			Logger.getLogger(MegafonNWBot.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public MegafonNWBot() {
		client = new HttpClient();
	}

	public void getInitPage() throws SkyBotException {
		GetMethod method = new GetMethod(MEGAFONNW_URL);
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
				throw new SkyBotException("Cant't find html code from url BEELINE_URL previous to Code Image file name");
			}
			
			int nameStartPos = prePos + PREIMAGE_HTML_CODE.length();
			imgFileName = pageBuffer.substring(nameStartPos, pageBuffer.indexOf(IMAGE_NAME_ESCAPE_CHAR, nameStartPos));
			if (imgFileName == null || "".equals(imgFileName)) {
				throw new SkyBotException("Can't parse the Code Image file name from url BEELINE_URL");
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
        // Provide custom retry handler is necessary
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        try {
            int statusCode = client.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + getMethod.getStatusLine());
            }
			
            BufferedInputStream bis = new BufferedInputStream(getMethod.getResponseBodyAsStream());
            FileOutputStream fos = new FileOutputStream(imgFileName);
            for (int c = bis.read(); c != -1; c = bis.read()) {
				fos.write(c);
            }
			
            bis.close();
			fos.close();
            } catch (HttpException ex) {
				Logger.getLogger(MegafonNWBot.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
				Logger.getLogger(MegafonNWBot.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
				// Release the connection.
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
			Logger.getLogger(MegafonNWBot.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(MegafonNWBot.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			postMethod.releaseConnection();
		}
	}
}