package com.muki;

import java.util.ArrayList;

/**
 * Created by Aleksanteri on 26/11/2016.
 */

public class TwitterFeed {
    public String username;
    public ArrayList<Tweet> tweets = new ArrayList<Tweet>();
    public boolean isDefined() {
        return this.username != null;
    }
}
