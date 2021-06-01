/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 10.06.20 23:22
 */
package com.xpresent.xpresent.model.vk;

public class VKUser {
    private int id = 0;
    private String firstName;
    private String lastName;
    private String phone;
    private String  photo;

    public VKUser(int uid, String[] params){
        id = uid;
        firstName = params[0];
        lastName = params[1];
        photo = params[2];
        phone = params[3]+" "+params[4];
    }

    public int getId() {
        return id;
    }

    public String getFio() {
        return firstName+" "+lastName;
    }

    public String getPhone(){
        return phone;
    }

    public String getPhoto(){
        return photo;
    }
}
