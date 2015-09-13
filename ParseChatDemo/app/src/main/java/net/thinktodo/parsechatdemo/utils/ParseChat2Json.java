package net.thinktodo.parsechatdemo.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by nmthong on 9/9/2015.
 */
public class ParseChat2Json {

    int totalGetTitleFromLink;
    int countGetTitleFromLink;
    // A queue of Runnables
    private final BlockingQueue<Runnable> mWorkQueue;
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 10;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    ThreadPoolExecutor mThreadPool;

    public ParseChat2Json() {
        // Instantiates the queue of Runnables as a LinkedBlockingQueue
        mWorkQueue = new LinkedBlockingQueue<Runnable>();

        // Creates a thread pool manager
        mThreadPool = new ThreadPoolExecutor(
                5,       // Initial pool size. Since Android 1.6, the core pore size is 5, and the maximum pool size is 128
                128,     // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mWorkQueue);
    }

    /**
     * Parse from message with Mention, Emoticons, Links to JSON object. This function must run in background
     * @param message Example "@bob @john (success) such a cool feature; https://twitter.com/jdorfman/status/430511497475670016"
     * @return JSONObject has structure like {"mentions":["chris"],"emoticons":["megusta","coffee"],"links":["url":"http://www.nbcolympics.com","title":"NBC Olympics | 2014 NBC Olympics in Sochi Russia"],}
     */
    public JSONObject parseSyncChat2Json(String message) {
        JSONObject data = new JSONObject();
        String mentionPattern = "@[^\\W]*";//pattern to get mention in string example @chris
        String linkPattern = "http:\\/\\/[^\\s]*|https:\\/\\/[^\\s]*";//pattern to get link in string
        String emoticonsPattern = "\\((.*?)\\)";//get all string in ( and ) for emoticons
        Pattern pattern = Pattern.compile(mentionPattern + "|" + linkPattern + "|" + emoticonsPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        JSONArray mentionArray = new JSONArray();
        final JSONArray linkArray = new JSONArray();
        JSONArray emoticonsArray = new JSONArray();
        while (matcher.find()) {
            String content = matcher.group(0);
            //If content is link, jsoup will parse html of link to get title
            if (content.contains("http") || content.contains("https") || content.contains("www")) {
                JSONObject linkJson = null;
                try {
                    linkJson = getTitleFromLink(content);
                    linkArray.put(linkJson);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (content.contains("@")) {
                String mention = content.replace("@", "");
                mentionArray.put(mention);
            } else {
                String emoticon = content.replace("(", "").replace(")", "");
                emoticonsArray.put(emoticon);
            }
        }
        if (mentionArray.length() > 0) {
            try {
                data.put("mentions", mentionArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            //do nothing
        }
        if (linkArray.length() > 0) {
            try {
                data.put("links", linkArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            //do nothing
        }
        if (emoticonsArray.length() > 0) {
            try {
                data.put("emoticons", emoticonsArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            //do nothing
        }

        return data;
    }

    /**
     * Get title from one link with Jsoup library. It can work with format http:// ,https://, www
     * @param url
     * @return
     */
    public JSONObject getTitleFromLink(String url) throws IOException, JSONException {
        if (url.substring(0,3).toLowerCase().equals("www")) {
            url="http://"+url;//if url has format with www.
        }else{
            //keep original link
        }
        Document doc = Jsoup.connect(url).timeout(10000).get();
        String title = doc.title();
        JSONObject linkJson = new JSONObject();
        linkJson.put("url", url.toLowerCase());
        linkJson.put("title", title);
        return  linkJson;
    }

    /**
     * Parse from message with Mention, Emoticons, Links to JSON object. This function must run in background. parseAsyncChat2Json can help get link faster than parseSyncChat2Json with mutil thread.
     * @param message Example "@bob @john (success) such a cool feature; https://twitter.com/jdorfman/status/430511497475670016"
     * @return JSONObject has structure like {"mentions":["chris"],"emoticons":["megusta","coffee"],"links":["url":"http://www.nbcolympics.com","title":"NBC Olympics | 2014 NBC Olympics in Sochi Russia"],}
     */
    public void parseAsyncChat2Json(String message, final ParseChat2JsonResponse response) {
        totalGetTitleFromLink=0;
        countGetTitleFromLink=0;
        totalGetTitleFromLink++;
        final JSONObject data = new JSONObject();
        String mentionPattern = "@[^\\W]*";//pattern to get mention in string example @chris
        String linkPattern = "http:\\/\\/[^\\s]*|https:\\/\\/[^\\s]*|www\\.[^\\s]*";//pattern to get link in string
        String emoticonsPattern = "\\((.*?)\\)";//get all string in ( and ) for emoticons
        Pattern pattern = Pattern.compile(mentionPattern + "|" + linkPattern + "|" + emoticonsPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        JSONArray mentionArray = new JSONArray();
        final JSONArray linkArray = new JSONArray();
        JSONArray emoticonsArray = new JSONArray();
        while (matcher.find()) {
            final String content = matcher.group(0);
            //If content is link, jsoup will parse html of link to get title
            if (content.contains("http") || content.contains("https") || content.contains("www")) {
                totalGetTitleFromLink++;
                //create thread to get link json
                mThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject linkJson = null;
                            try {
                                linkJson = getTitleFromLink(content);
                                linkArray.put(linkJson);
                                if(response!=null) response.onProgress(linkJson);
                            } catch (IOException e) {
                                response.onError("mentions", e);
                            } catch (JSONException e) {
                                response.onError("mentions", e);
                            }
                            countGetTitleFromLink++;
                            if(countGetTitleFromLink==totalGetTitleFromLink){
                                if (linkArray.length() > 0) {
                                    try {
                                        data.put("links", linkArray);
                                    } catch (JSONException e) {
                                        response.onError("links",e);
                                    }
                                }else{
                                    //do nothing
                                    //don't add link to data
                                }
                                if(response!=null) response.onSuccess(data);
                            }
                        }
                });
            } else if (content.contains("@")) {
                String mention = content.replace("@", "");
                mentionArray.put(mention);
            } else {
                String emoticon = content.replace("(", "").replace(")", "");
                emoticonsArray.put(emoticon);
            }
        }
        if (mentionArray.length() > 0) {
            try {
                data.put("mentions", mentionArray);
            } catch (JSONException e) {
               response.onError("mentions",e);
            }
        }else{
            //do nothing
        }
        if (emoticonsArray.length() > 0) {
            try {
                data.put("emoticons", emoticonsArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            //do nothing
        }
        countGetTitleFromLink++;
        if(countGetTitleFromLink==totalGetTitleFromLink){
            if(response!=null) response.onSuccess(data);
        }else{
            //waiting finish all thread and will call back
        }
    }


    public interface ParseChat2JsonResponse {
        //onProgress was called when one thread finish get link via jsoup
        public void onProgress(JSONObject link);
        //onError was called when one thread has error
        public void onError(String type,Exception ex);
        //onSuccess was called when it finish get all title from links
        public void onSuccess(JSONObject result);
    }
}
