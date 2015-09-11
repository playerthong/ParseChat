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
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    ThreadPoolExecutor mThreadPool;


    public ParseChat2Json() {
        // Instantiates the queue of Runnables as a LinkedBlockingQueue
        mWorkQueue = new LinkedBlockingQueue<Runnable>();

        // Creates a thread pool manager
        mThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
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
        Pattern pattern = Pattern.compile(mentionPattern + "|" + linkPattern + "|" + emoticonsPattern);
        Matcher matcher = pattern.matcher(message);
        JSONArray mentionArray = new JSONArray();
        final JSONArray linkArray = new JSONArray();
        JSONArray emoticonsArray = new JSONArray();
        while (matcher.find()) {
            String content = matcher.group(0);
            //If content is link, jsoup will parse html of link to get title
            if (content.contains("http") || content.contains("https")) {
                JSONObject linkJson = null;
                try {
                    linkJson = getTitleFromLink(content);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                linkArray.put(linkJson);

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
        }
        if (linkArray.length() > 0) {
            try {
                data.put("links", mentionArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    /**
     * Get title from one link with Jsoup library
     * @param url
     * @return
     */
    public JSONObject getTitleFromLink(String url) throws IOException, JSONException {
        Document doc = Jsoup.connect(url).get();
        String title = doc.tagName();
        JSONObject linkJson = new JSONObject();
        linkJson.put("url", url);
        linkJson.put("title", title);
        return  linkJson;
    }

    public void parseAsyncChat2Json(String message,Response response) {
        JSONObject data = new JSONObject();
        String mentionPattern = "@[^\\W]*";//pattern to get mention in string example @chris
        String linkPattern = "http:\\/\\/[^\\s]*|https:\\/\\/[^\\s]*";//pattern to get link in string
        String emoticonsPattern = "\\((.*?)\\)";//get all string in ( and ) for emoticons
        Pattern pattern = Pattern.compile(mentionPattern + "|" + linkPattern + "|" + emoticonsPattern);
        Matcher matcher = pattern.matcher(message);
        JSONArray mentionArray = new JSONArray();
        final JSONArray linkArray = new JSONArray();
        JSONArray emoticonsArray = new JSONArray();
        while (matcher.find()) {
            String content = matcher.group(0);
            //If content is link, jsoup will parse html of link to get title
            if (content.contains("http") || content.contains("https")) {
                totalGetTitleFromLink++;
                //create thread to get link json
                mThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject linkJson = null;
                            try {
                                linkJson = getTitleFromLink(content);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
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
                e.printStackTrace();
            }
        }
        if (linkArray.length() > 0) {
            try {
                data.put("links", mentionArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public interface Response{
        public void onResponse(JSONObject link);
        public void onError(Exception ex);
        public void onSuccess(JSONObject result);
    }
}
