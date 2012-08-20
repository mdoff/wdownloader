package net.mdoff.dwrzuta;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: mdoff
 * Date: 03.08.12
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */
public class Dwrzuta {
    private URL downloadLink = null;
    private ArrayList<URL> downloadURL = new ArrayList<URL>();
    private FileOutputStream downloadFile = null;
    private String name=null;
    enum Type{AUDIO,VIDEO};

   public Type downloadType;

    Dwrzuta(URL link) throws URISyntaxException, MalformedURLException {
        downloadLink = prepere(link);
    }
    Dwrzuta(URL link, FileOutputStream file) throws MalformedURLException, URISyntaxException {
        downloadLink = prepere(link);
        downloadFile = file;
    }
    private URL prepere(URL link) throws URISyntaxException, MalformedURLException {
        Pattern pattern = Pattern.compile("wrzuta.pl/film/");
        Matcher matcher = pattern.matcher(link.toURI().toString());
        if(matcher.find()){
            downloadType = Type.VIDEO;
            pattern = Pattern.compile("http://(.*wrzuta.pl)/film/(.*)/.*");
            matcher = pattern.matcher(link.toURI().toString());
            matcher.find();
            link = new URL("http://"+matcher.group(1)+"/xml/kontent/"+matcher.group(2)+"/wrzuta.pl/ca/"+ Integer.toString((int)(Math.random()*10000)));
            //System.out.println(link.toURI());
        }
        else{
            downloadType = Type.AUDIO;
            pattern = Pattern.compile("http://(.*wrzuta.pl)/audio/(.*)/.*");
            matcher = pattern.matcher(link.toURI().toString());
            matcher.find();
            link = new URL("http://"+matcher.group(1)+"/xml/kontent/"+matcher.group(2)+"/wrzuta.pl/ca/"+ Integer.toString((int)(Math.random()*10000)));
            //System.out.println(link.toURI());
        }


        return link;
    }
    public void fetch() throws Exception{
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        conn = (HttpURLConnection) downloadLink.openConnection();
        conn.setRequestMethod("GET");
        //User-Agent must be always present!
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:14.0) Gecko/20100101 Firefox/14.0.1");

        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = rd.readLine()) != null) {
            result += line;
        }
        rd.close();


        Pattern pattern = Pattern.compile("Materiał został usunięty");
        Matcher matcher = pattern.matcher(result);

        if(matcher.find())
            throw new Exception("404 - file not found.");

        if(downloadType==Type.AUDIO){

            pattern = Pattern.compile("<name><!.CDATA.(.*)..></name>");
            matcher = pattern.matcher(result);
            matcher.find();
            name = matcher.group(1);

            pattern = Pattern.compile("<fileId>.*?(http.*)]]></fileId>");
            matcher = pattern.matcher(result);

            matcher.find();
            downloadURL.add(new URL( matcher.group(1)));

        }
        else if(downloadType==Type.VIDEO){
            pattern = Pattern.compile("<file>.*<name><!.CDATA.(.*)..></name>");
            matcher = pattern.matcher(result);
            matcher.find();
            name = matcher.group(1);
            //System.out.println(name);
            pattern = Pattern.compile("<storeIds>.*");
            matcher = pattern.matcher(result);

            matcher.find();

            pattern = Pattern.compile("<file[HIM].*?>(.*?)</file.*?>");
            matcher = pattern.matcher(matcher.group());
            while (matcher.find()){
                Pattern hd = Pattern.compile(".*?_h5>.*");
                if(!hd.matcher(matcher.group()).find()){
//                    System.out.println(matcher.group());
                    Pattern urls = Pattern.compile(".*<!.CDATA.(http:.*?)..>.*");
                    Matcher u = urls.matcher(matcher.group());
                    u.find();
                    boolean add = true;
                    for(URL test: downloadURL){
                        if(test.sameFile(new URL(u.group(1)))){
                            add = false;
                            break;
                        }
                    }
                    if(add)
                        downloadURL.add(new URL(u.group(1)));
                }
            }


        }

    }
    public void download() throws Exception{


    }
    public void setFile(FileOutputStream file){
        downloadFile = file;
    }

    public ArrayList<URL> getDownloadURL(){
           return downloadURL;
    }
    public String getName(){
        return name;
    }


}
