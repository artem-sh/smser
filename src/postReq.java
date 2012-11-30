
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * @author Artem
 */

public class postReq {
	public static void main(String[] args) {
		    try {
        // Construct data
        String data = URLEncoder.encode("message", "CP1251") + "=" + URLEncoder.encode("Хуй там был!", "CP1251");
        data += "&" + URLEncoder.encode("phone", "CP1251") + "=" + URLEncoder.encode("6773551", "CP1251");
        data += "&" + URLEncoder.encode("remainchars", "CP1251") + "=" + URLEncoder.encode("149", "CP1251");
        data += "&" + URLEncoder.encode("Prefix", "CP1251") + "=" + URLEncoder.encode("7951", "CP1251");
    
        // Send data
        //URL url = new URL("http://localhost:8084/SmsServer/tele2Test");
        URL url = new URL("http://www.rocc.ru/cgi-bin/sms33.cgi");
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();
    
        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            // Process line...
			System.out.println(line);
        }
        wr.close();
        rd.close();
    } catch (Exception e) {
    }
	}
		
		
		
		
			/*String content = "message=" + "Кто это тут? Это Я!" +
					"Prefix=" + "7951";
			*/

}
