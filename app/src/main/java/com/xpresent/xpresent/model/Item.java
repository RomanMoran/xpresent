/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 04.06.20 12:52
 */

package com.xpresent.xpresent.model;

import java.util.Map;

public class Item {
    private Map<String,String> Params;
    private int position;

    public Item(Map<String,String> params, int i) {
        Params = params;
        position = i;
    }

    public String getParam(String key){
        return Params.get(key);
    }

    public void setParam(String key, String value){
        Params.put(key, value);
    }

    public int getPosition() {
        return position;
    }

}