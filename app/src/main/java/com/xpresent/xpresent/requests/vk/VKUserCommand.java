/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 10.06.20 23:21
 */

package com.xpresent.xpresent.requests.vk;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.vk.api.sdk.VKApiManager;
import com.vk.api.sdk.VKApiResponseParser;
import com.vk.api.sdk.VKMethodCall;
import com.vk.api.sdk.exceptions.VKApiException;
import com.vk.api.sdk.internal.ApiCommand;
import com.xpresent.xpresent.model.vk.VKUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class VKUserCommand extends ApiCommand<VKUser> {
    private Context Activity;

    public VKUserCommand(Context activity){
        Activity = activity;
    }
    @Override
    protected VKUser onExecute(@NotNull VKApiManager vkApiManager) throws InterruptedException, IOException, VKApiException {
        VKMethodCall call = new VKMethodCall.Builder()
                .method("users.get")
                .args("fields", "photo_100,contacts")
                .args("lang", "ru")
                .version("5.110")
                .build();
        return vkApiManager.execute(call, new ResponseApiParser());
    }

    private class ResponseApiParser implements VKApiResponseParser<VKUser> {

        @Override
        public VKUser parse(String response) {
            try {
                JSONObject jo = new JSONObject(response);
                JSONArray users = jo.getJSONArray("response");
                JSONObject user = users.getJSONObject(0);
                int id = user.getInt("id");
                String[] params = new String[5];
                params[0] = user.getString("first_name");
                params[1] = user.getString("last_name");
                params[2] = user.getString("photo_100");
                params[3] = user.getString("mobile_phone");
                params[4] = user.getString("home_phone");
                return new VKUser(id, params);
            }catch(JSONException e) {
                e.printStackTrace();
                Log.e("VK User JSON", e.getMessage());
                Toast.makeText(Activity, e.getMessage(), Toast.LENGTH_LONG).show();
                return null;
            }
        }
    }
}
