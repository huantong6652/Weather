
// Huantong Ji
// Program 2
// commands that help to run this code
// javac Rest.java
// java Rest (city)

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.util.regex.Pattern.compile;

public class Rest {
    private static HttpURLConnection connection = null;

    public static void main(String[] args) throws InterruptedException {
        String str = args[0];
        str = str.replaceAll(" ","");// replace space

        String url1 = "https://geoapi.qweather.com/v2/city/lookup?location="
                + str
                + "PLACE_KEY_HERE";

        // Retrieve json1 from url1
        String json1 = httpRequest(url1);
        String[] weather = {"temp","speed","all"};// json keys
        String[] weather1 = {"Temperature","Wind speed","Cloudiness"};
        String[] unit  = {" C"," m/s"," %"};

        String url2  = "https://api.openweathermap.org/data/2.5/weather?lat="
                + jsonScan(json1,"lat")
                + "&lon="
                + jsonScan(json1,"lon")
                + "PLACE_KEY_HERE";

        // Retrieve json2 from url2
        String json2 = httpRequest(url2);

        String temp;
        for(int i = 0;i < weather.length;i++){
            temp = jsonScan(json2,weather[i]);
            if(temp  != null){
                System.out.println(weather1[i] + " :   "+ temp + unit[i]);
            }
        }

        if(jsonScan(json1,"adm1") != null)
            System.out.println("State: "+ jsonScan(json1,"adm1"));
    }

    // handle json
    public static String jsonScan(String content,String text){
        if(content == null){
            return null;
        }
        Pattern pattern_a = compile("\"" + text + "\":(.*?)(:?,|})");
        Matcher matcher_a = pattern_a.matcher(content);

        String str = null;
        if (matcher_a.find()){
            str = matcher_a.group(1);
            if(str.charAt(0) == '"') {
                str = str.substring(1,str.length()-1);
            }
        }
        return str;
    }

    // retrieve json from given url
    public static String httpRequest(String url){
        StringBuilder content = new StringBuilder();
        int n = 0;
        try{
            URL u = new URL(url);
            connection = (HttpURLConnection)u.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("content-encoding", "ascii");
            connection.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36");
            connection.setRequestProperty("accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            while(n++ < 3){
                if (connection.getResponseCode() == 200){
                    InputStream in = connection.getInputStream();
                    if(connection.getContentEncoding() != null && connection.getContentEncoding().equals("gzip")) {
                        return uncompressToString(in,"utf-8");
                    } else {
                        return isToString(in);
                    }
                } else {
                    System.out.println("ResponseCode:" + connection.getResponseCode() + "   retry:" + n);
                    Thread.currentThread().sleep(3000);
                }

                if(n == 2 && connection.getResponseCode() != 200) {
                    System.out.println("city not found");
                }

                if(connection.getResponseCode() / 100 == 5){
                    Thread.currentThread().sleep(n * 2000);
                }
            }
        } catch(IOException | InterruptedException e){
            e.printStackTrace();
        } finally{
            if(connection != null){
                connection.disconnect();
            }
        }
        return null;
    }

    // uncompress string
    public static String uncompressToString(InputStream in,String charset) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GZIPInputStream gunzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = gunzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString(charset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // stream String
    public static String isToString(InputStream in){
        StringBuilder content = new StringBuilder();
        try {
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            String line = null;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
