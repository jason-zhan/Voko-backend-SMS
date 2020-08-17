package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingfront.service.ShortLinkService;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/*
createURL(api_dev_key, original_url, custom_alias=None, user_name=None, expire_date=None)

originalURL(url_key)

deleteURL(api_dev_key, url_key)
Shortening: Take a url and return a much shorter url.
Ex: http://www.interviewbit.com/courses/programming/topics/time-complexity/ => http://goo.gl/GUKA8w/
Redirection: Take a short url and redirect to the original url.
Ex: http://goo.gl/GUKA8w => http://www.interviewbit.com/courses/programming/topics/time-complexity/
Custom url: Allow the users to pick custom shortened url.
Ex: http://www.interviewbit.com/courses/programming/topics/time-complexity/ => http://goo.gl/ib-time
Analytics: Usage statistics for site owner.
Ex: How many people clicked the shortened url in the last day?
Gotcha: What if two people try to shorten the same URL?
 */
@RestController
@RequestMapping("/api/ShortLink")
public class ShortLinkController {

    @Autowired
    private ShortLinkService shortLinkService;
    /*
    ## api/shorten
curl -X "POST" "http://localhost:4000/api/shorten" \
      -H 'Content-Type: application/json; charset=utf-8' \
      -d $'{
  "url": "https://www.github.com"
}'
## Mutil api/shorten
curl -X "POST" "http://localhost:4000/api/shorten/" \
      -H 'Content-Type: application/json; charset=utf-8' \
      -d $'{
  "url": [
    "https://gitlab.com",
    "https://github.com"
  ]
}'
## api/original
curl -X "POST" "http://localhost:4000/api/original/" \
      -H 'Content-Type: application/json; charset=utf-8' \
      -d $'{
  "url": "http://localhost:4000/zRa"
}'
     */

    @RequestMapping("/shortURL")
    @ResponseBody
    public ReturnEntity getShortURL(@RequestBody String longURL) {
        String shortUrl = shortLinkService.getShortURL(longURL);
        System.out.print(shortUrl);
        return ReturnEntity.success(shortUrl);
    }

    @RequestMapping("/custom")
    @ResponseBody
    public ReturnEntity getCustom(@RequestBody String longURL, @RequestBody String custom) {
        String shortUrl = shortLinkService.getShortURLCust("http://google.com/test/2", "abcdef");
        System.out.print(shortUrl);
        return ReturnEntity.success(shortUrl);
    }
    @RequestMapping(value = "/{base62:[0-9a-zA-Z]+$}", method = RequestMethod.GET)
    public ReturnEntity getLongURL(@RequestBody String shortURL) {
        String longURL = shortLinkService.getLongURL(shortURL);
        System.out.print(longURL);
        return ReturnEntity.success(longURL);
    }
}
