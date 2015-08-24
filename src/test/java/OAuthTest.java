//import com.intuit.utils.ReconnectResponse;
//import oauth.signpost.OAuthConsumer;
//import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.log4j.Logger;
//import org.junit.Test;
//
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.Unmarshaller;
//import javax.xml.bind.UnmarshallerHandler;
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.StringReader;
//
///**
// * Created by aslawson on 7/24/15.
// */
//public class OAuthTest {
//
//    String consumer_key = "qyprdA3TIPb9lWNlOZ1RhvUu4yuOxW";
//    String consumer_secret = "4TGg5ZeEN2nPlYOzFwhddLhxkxNkZs9IK0N0jAEt";
//    String access_token = "qyprdYOJwl51PdB0jGcwOSRNOf94v68UNs4BwmEzFToMnBYp";
//    String access_secret = "l6z4UqxR6r9HCAWNOeqly25Am7RTzjjqHFM1WW15";
//    public static final Logger LOG = Logger.getLogger(OAuthTest.class);
//
//    @Test
//    public void MyOAuthTest() throws Exception {
//        String disconnectURL = "https://appcenter.intuit.com/api/v1/connection/reconnect";
//        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumer_key,
//                consumer_secret);
//
//        consumer.setTokenWithSecret(access_token, access_secret);
//
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        HttpGet request = new HttpGet(disconnectURL);
//
//        consumer.sign(request);
//
//        HttpResponse response = httpClient.execute(request);
//
//        InputStream is = response.getEntity().getContent();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//        String line;
//
//        String xml = "";
//
//        while ((line = reader.readLine()) != null) {
//            xml += line;
//        }
//
//        System.out.println("Raw xml = " + xml);
//
//
//            JAXBContext jaxbContext = JAXBContext.newInstance(ReconnectResponse.class);
//            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//            UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
//
//            StringReader stringReader = new StringReader(xml);
//            ReconnectResponse rr = (ReconnectResponse) unmarshaller.unmarshal(stringReader);
//
//            LOG.info("OAuthToken: " + rr.getOAuthToken());
//            LOG.info("OAuthSecret: " + rr.getOAuthTokenSecret());
//            LOG.info("ErrorCode: " + rr.getErrorCode());
//            LOG.info("ErrorMessage: " + rr.getErrorMessage());
//            LOG.info("ServerTime: " + rr.getServerTime());
//
//            //Confirm disconnect succeeded
//            if(rr.getErrorCode() == 0) {
//
//            }
//
//    }
//
//}
