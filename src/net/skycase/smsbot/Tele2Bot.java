package net.skycase.smsbot;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.skycase.smsstuff.SmsException;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;

/**
 * Tele2 code image is in ".gif" format.
 * @author Artem
 */
public class Tele2Bot extends SmsBot {
	
	public Tele2Bot(String def, String phoneNumber, String message) {
		super(def, phoneNumber, message);	
	}
	
	protected void init() throws SmsException {
		baseUrl = "http://www.tele2.ru/956/";
		initPageUrl = "http://www.tele2.ru/956/Article.aspx"; //"http://localhost:8084/SmsServer/tele2Test";
		maxSmsLength = 198;
		
		if (message.length() > maxSmsLength) {
			throw new SmsException("Too long message for number " + phoneNumber + " . Max lenth value is " + maxSmsLength);
		}
	}
	
	@Override
	String getImageUrl() throws SmsException {
		//this is always true for Tele2
		return "http://tele2.ru/controls/ImageCode.aspx";
	}
	
	@Override
	void doSecondStep(String code) throws SmsException {
		//Find all needed elements to form POST request:
		//find action and, as result, destination URL for POST request
		String action = getElement("<form name=\"__aspnetForm\" method=\"post\" action=\"", "\"");
		String postUrl = baseUrl + action;
		
		//Set input parameters:
		NameValuePair[] postParameters = {
			new NameValuePair("__VIEWSTATE", getElement("name=\"__VIEWSTATE\" value=\"", "\"")),
			new NameValuePair("SmsSender:PrePhone", def),
			new NameValuePair("SmsSender:phoneNumber", phoneNumber),
			new NameValuePair("SmsSender:smsText", message),
			new NameValuePair("SmsSender:code", code),
			new NameValuePair("SmsSender:Button1", "Отправить") };
		
		postData(postUrl, postParameters);
		
	}
	
	public static void main(String[] args) {
		try {
			Tele2Bot bot = new Tele2Bot("904", "6148112", "Preved medved. Превед медвед.");
			System.out.println("Image file name is " + bot.doFirstStep());
			
			bot.doSecondStep(JOptionPane.showInputDialog("CODE!"));
		} catch (SmsException ex) {
			Logger.getLogger(Tele2Bot.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}