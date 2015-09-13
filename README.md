# ParseChat2Json for Android
ParseChat2Json a solution that takes a chat message string and returns a JSON string containing information about its contents. Special content to look for includes:
1. @mentions - A way to mention a user. Always starts with an '@' and ends when hitting a non-word character. (http://help.hipchat.com/knowledgebase/articles/64429-how-do-mentions-work-)
2. Emoticons - For this exercise, you only need to consider 'custom' emoticons which are alphanumeric strings, no longer than 15 characters, contained in parenthesis. You can assume that anything matching this format is an emoticon. (https://www.hipchat.com/emoticons)
3. Links - Any URLs contained in the message, along with the page's title.
ParseChat2Json used JSoup to get title from link

For example, calling your function with the following inputs should result in the corresponding return values.

Input: "@chris you around?"
Return (string):
{
  "mentions": [
    "chris"
  ]
}
 
Input: "Good morning! (megusta) (coffee)"
Return (string):
{
  "emoticons": [
    "megusta",
    "coffee"
  ]
}
 
Input: "Olympics are starting soon; http://www.nbcolympics.com"
Return (string):
{
  "links": [
    {
      "url": "http://www.nbcolympics.com",
      "title": "NBC Olympics | 2014 NBC Olympics in Sochi Russia"
    }
  ]
}
 
Input: "@bob @john (success) such a cool feature; https://twitter.com/jdorfman/status/430511497475670016"
Return (string):
{
  "mentions": [
    "bob",
    "john"
  ],
  "emoticons": [
    "success"
  ],
  "links": [
    {
      "url": "https://twitter.com/jdorfman/status/430511497475670016",
      "title": "Twitter / jdorfman: nice @littlebigdetail from ..."
    }
  ]
}


##Features
- Get title from link (http,https,www)
- Parse chat message to json with synchronization
- Parse chat message to json with asynchronization

Notice: Both of them must run in background in Android

#Usage
Example:  "@Thong, Olympics are starting soon; http://www.nbcolympics.com"

Step 1: Create parseChat2Json

String chatMessage="@Thong, Olympics are starting soon; http://www.nbcolympics.com";
ParseChat2Json parseChat2Json=new ParseChat2Json();

Step 2:
JSONObject result=parseChat2Json.parseSyncChat2Json(chatMessage);

Or

```java
parseChat2Json.parseAsyncChat2Json(chat, new ParseChat2Json.ParseChat2JsonResponse() {
            @Override
            public void onProgress(JSONObject link) {
                //onProgress was called when ParseChat2Json got title from one link
            }

            @Override
            public void onError(String type, Exception ex) {
                //TODO
            }

            @Override
            public void onSuccess(JSONObject json) {
				//json is data 	ParseChat2Json parsed from chat message
				//You can implement at here
            }
});
```


Result:
```json
{
    "mentions": [
        "Thong"
    ],
    "links": [
        {
            "title": "NBC Olympics | Home of the 2016 Olympic Games in Rio",
            "url": "http://www.nbcolympics.com"
        }
    ]
}
```


## License

    Copyright 2015, Thong Nguyen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.