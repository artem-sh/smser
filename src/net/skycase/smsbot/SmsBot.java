package net.skycase.smsbot;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import net.skycase.smsstuff.SmsException;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public abstract class SmsBot {
	String def;
	String phoneNumber; //where to send SMS (in 7-digit format)
	String message; //text of SMS
	
	HttpClient client;
	String baseUrl; //Base URL for "action" in post request
	String initPageUrl; //URL for sending SMS
	StringBuffer pageBuffer; //Init page HTML source
	
	//A part of html code from url BEELINE_URL previous to Code Image file name. See example:
	//<TD>&nbsp;&nbsp;&nbsp;<IMG SRC="/servlet/send/confirm_code/C10TGhXx7A.gif" WIDTH="146" HEIGHT="46">&nbsp;&nbsp;&nbsp;</TD>
	String preimageHtmlCode;
	
	//Charater (s) that tells us about end of Code Image name
	String imgNameEscapeChar;
	int maxSmsLength;
			
	abstract void init() throws SmsException;
	void doSecondStep(String code) throws SmsException {};
	
	public SmsBot(String def, String phoneNumber, String message) {
		this.def = def;
		this.phoneNumber = phoneNumber;
		this.message = message;
		
		client = new HttpClient();
		client.getParams().setParameter("http.useragent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)");
	}
	
	/**
	 * 
	 * @return Init page (text). Never null.
	 * @throws net.skycase.smsstuff.SmsException
	 */
	StringBuffer getInitPage() throws SmsException {
		GetMethod method = new GetMethod(initPageUrl);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + method.getStatusLine());
				throw new SmsException("Cant't get init page to send SMS on number " + phoneNumber);
			}

			BufferedInputStream inResponseStream = new BufferedInputStream(method.getResponseBodyAsStream());
			pageBuffer = new StringBuffer();
			for (int c = inResponseStream.read(); c != -1; c = inResponseStream.read()) {
				pageBuffer.append((char) c);
			}
		} catch (HttpException e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
			throw new SmsException("Cant't get init page to send SMS on number " + phoneNumber);
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
			throw new SmsException("Cant't get init page to send SMS on number " + phoneNumber);
		} finally {
			method.releaseConnection();
		}
		
		return pageBuffer;
	}
	
	String getImageUrl() throws SmsException {
		int prePos = pageBuffer.indexOf(preimageHtmlCode);
		if (prePos == -1) {
			throw new SmsException("Cant't find html code from url " + initPageUrl + " previous to Code Image file name");
		}

		int nameStartPos = prePos + preimageHtmlCode.length();
		String imgUrlUniquePart = null;
		imgUrlUniquePart = pageBuffer.substring(nameStartPos, pageBuffer.indexOf(imgNameEscapeChar, nameStartPos));
		if (imgUrlUniquePart == null || "".equals(imgUrlUniquePart)) {
			throw new SmsException("Can't parse the Code Image file name from url BEELINE_URL");
		}
		//TODO: check this!
		return imgUrlUniquePart;
	}
	
	String getElement(String preHtml, String postHtml) throws SmsException {
		int prePos = pageBuffer.indexOf(preHtml);
		if (prePos == -1) {
			throw new SmsException("Cant't find preHtml " + preHtml + " from url " + initPageUrl);
		}

		int elementStartPos = prePos + preHtml.length();
		String element = null;
		element = pageBuffer.substring(elementStartPos, pageBuffer.indexOf(postHtml, elementStartPos));
		if (element == null || "".equals(element)) {
			throw new SmsException("Cant't find element from url " + initPageUrl + " . Previous HTML" +
					" code is " + preHtml + " and post HTML is " + postHtml);
		}
		
		return element;
	}
	
	/**
	 * 
	 * @return File name of dowloaded image (never null). Name format is: "cimg_" + String.valuOf(System.currentTimeMillis())
	 * @throws net.skycase.smsstuff.SmsException
	 */
	String getImage(String imgUrl) throws SmsException {
		String imgFileName;
		GetMethod getMethod = new GetMethod(imgUrl);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		try {
			int statusCode = client.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + getMethod.getStatusLine());
				throw new SmsException("Cannot download image code file for the phone " + phoneNumber);
			}

			BufferedInputStream bis = new BufferedInputStream(getMethod.getResponseBodyAsStream());
			imgFileName = "cimg_" + String.valueOf(System.currentTimeMillis());
			FileOutputStream fos = new FileOutputStream(imgFileName);
			for (int c = bis.read(); c != -1; c = bis.read()) {
				fos.write(c);
			}

			bis.close();
			fos.close();
		} catch (HttpException ex) {
			System.err.println("Fatal transport error: " + ex.getMessage());
			throw new SmsException("Cannot download image code file for the phone " + phoneNumber);
		} catch (IOException ex) {
			System.err.println("Fatal transport error: " + ex.getMessage());
			throw new SmsException("Cannot download image code file for the phone " + phoneNumber);
		} finally {
			getMethod.releaseConnection();
		}
		
		return imgFileName;
	}
	
	/**
	 * This if default fist step actions for the operators using code image.
	 * @return File name of downloaded code image or null if there is no necessity to fill code for SMS sending.
	 * @throws net.skycase.smsstuff.SmsException
	 */
	public String doFirstStep() throws SmsException {
		init();
		getInitPage();
		return getImage(getImageUrl());
	}
	
	void postData(String postUrl, NameValuePair[] postParameters) throws SmsException {
		PostMethod postMethod = new PostMethod(postUrl);
		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(2, false));
		postMethod.addRequestHeader("Content-Type", "text/html; charset=windows-1251");
		//postMethod.addRequestHeader("Content-Type", "text/html; charset=unicode");// --no SMS!
		//postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		
		postMethod.setRequestBody(postParameters);
		
		try {
			System.out.println(client.executeMethod(postMethod));
			System.out.println("TELE2 test: " + postMethod.getStatusLine().toString());
		} catch (HttpException ex) {
			throw new SmsException("Error during sending SMS for the phone " + phoneNumber);
		} catch (IOException ex) {
			throw new SmsException("Error during sending SMS for the phone " + phoneNumber);
		} finally {
			postMethod.releaseConnection();
		}
	}
}
