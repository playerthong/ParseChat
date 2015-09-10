package net.thinktodo.parsechatdemo.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * Created by nmthong on 9/9/2015.
 */
public class ParseChat2Json {
    /**
     * Parse from message with Mention, Emoticons, Links to JSON object. This function must run in background
     *
     * @param message Example "@bob @john (success) such a cool feature; https://twitter.com/jdorfman/status/430511497475670016"
     * @return JSONObject has structure like {"mentions":["chris"],"emoticons":["megusta","coffee"],"links":["url":"http://www.nbcolympics.com","title":"NBC Olympics | 2014 NBC Olympics in Sochi Russia"],}
     */
    public static JSONObject parseSyncChat2Json(String message) {
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
            System.out.println(matcher.group(0));
            //If content is link, jsoup will parse html of link to get title
            if (content.contains("http") || content.contains("https")) {
                try {
                    Document doc = Jsoup.connect(content).get();
                    String title = doc.tagName();
                    JSONObject linkJson = new JSONObject();
                    linkJson.put("url", content);
                    linkJson.put("title", title);
                    linkArray.put(linkJson);
                } catch (Exception ex) {

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
}
