package net.thinktodo.parsechatdemo.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;


import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
/**
 * Created by thong.nguyen on 9/11/2015.
 */
public class ParseChat2JsonTest {

    @Test
    public void testGetTitleFromLink() {
        String url="http://www.nbcolympics.com";
        String result="NBC Olympics | Home of the 2016 Olympic Games in Rio";
        ParseChat2Json parseChat2Json=new ParseChat2Json();
        try {
            JSONObject link=parseChat2Json.getTitleFromLink(url);
            assertThat(link.getString("title"), is(result));
        } catch (IOException e) {
            assertThat("", is(result));
        } catch (JSONException e) {
            assertThat("", is(result));
        }
    }

    @Test
    public void testParseSyncChat2Json(){
        String chat="@Thong,@Bao Olympics are starting soon; hTtp://www.nbcolympics.com (boat)";
        String result="{\"mentions\":[\"Thong\",\"Bao\"],\"links\":[{\"title\":\"NBC Olympics | Home of the 2016 Olympic Games in Rio\",\"url\":\"http://www.nbcolympics.com\"}]}";
        ParseChat2Json parseChat2Json=new ParseChat2Json();
        JSONObject json=parseChat2Json.parseSyncChat2Json(chat);
        assertThat(json.toString(), is(result));
    }

    @Test
    public void testParseAsyncChat2Json(){
        final String[] data = new String[1];
        String chat="@Thong,@Bao Olympics are starting soon; hTtp://www.nbcolympics.com";
        final String result="{\"mentions\":[\"Thong\",\"Bao\"],\"links\":[{\"title\":\"NBC Olympics | Home of the 2016 Olympic Games in Rio\",\"url\":\"http://www.nbcolympics.com\"}]}";
        final ParseChat2Json parseChat2Json=new ParseChat2Json();
        final Object lock=new Object();
        parseChat2Json.parseAsyncChat2Json(chat, new ParseChat2Json.ParseChat2JsonResponse() {
            @Override
            public void onProgress(JSONObject link) {
                //TODO
            }

            @Override
            public void onError(String type, Exception ex) {
                //TODO
            }

            @Override
            public void onSuccess(JSONObject json) {
                data[0] =json.toString();
                synchronized(lock) {
                        lock.notify();
                }
            }
        });
        synchronized(lock) {
            try {
                lock.wait(); //
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(data==null){
            assertThat("", is(result));
        }else {
            assertThat(data[0], is(result));
        }
    }


}
