package com.doodlyz.vlove;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VloveUtils {
    /**
     * Find and return "Post ID" and "Channel Code" based on the given <i><b>link</b></i>.
     * @param link to process.
     * @return array of string consist of:
     * <ul>
     *     <li>[0] => Post ID</li>
     *     <li>[1] => Channel Code</li>
     * </ul>
     * *note: returned output always not null, <i>but</i> items on the output can be null.
     */
    public static String[] getPIDandCC(String link) {
        String postId, channelCode;
        postId = channelCode = null;
        Pattern regex = Pattern.compile("https?://channels\\.vlive\\.tv/([A-Z0-9]+)/fan/([0-9.]+)");
        Matcher matcher = regex.matcher(link);

        if (matcher.find()) {
            postId = matcher.group(2);
            channelCode = matcher.group(1);
        }

        return new String[] { postId, channelCode };
    }
}
