package net.skycase.smsbot;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.skycase.smsstuff.SmsException;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;

/**
 * Only SPb edition! Note: No necesserity to adjust sending time!
 * @author Artem
 */
public class MtsBot extends SmsBot {
	
	public MtsBot(String def, String phoneNumber, String message) {
		super(def, phoneNumber, message);	
	}
	
	protected void init() throws SmsException {
		baseUrl = "http://www.spb.mts.ru";
		initPageUrl = "http://www.spb.mts.ru/sendform_sms.htm"; //"http://localhost:8084/SmsServer/tele2Test";
		maxSmsLength = 160;
		
		if (message.length() > maxSmsLength) {
			throw new SmsException("Too long message for number " + phoneNumber + " . Max lenth value is " + maxSmsLength);
		}
	}
	
	@Override
	public String doFirstStep() throws SmsException {
		init();
		getInitPage();
				
		String action = getElement("<FORM name=send onsubmit=\"return test(this)\" action=", " method=POST>");
		String postUrl = baseUrl + action;
		
		//Set input parameters:
		NameValuePair[] postParameters = {
			new NameValuePair("To", "7" + def + phoneNumber), //TODO: do constant!
			new NameValuePair("Msg", message),
			new NameValuePair("count", String.valueOf(message.length())),
			new NameValuePair("SMSHour", "19"), //TODO: modify!
			new NameValuePair("SMSMinute", "30"),
			new NameValuePair("SMSDay", "10"),
			new NameValuePair("SMSMonth", "11"),
			new NameValuePair("SMSYear", "2007") };
		
		postData(postUrl, postParameters);
		
		return null; //signal that: no code image!
	}
	
	public static void main(String[] args) {
		try {
			MtsBot bot = new MtsBot("911", "7209245", "Test_en. Тест_рус.");
			bot.doFirstStep();
		} catch (SmsException ex) {
			Logger.getLogger(MtsBot.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}