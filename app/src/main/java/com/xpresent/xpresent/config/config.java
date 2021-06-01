/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 26.05.20 14:16
 */

package com.xpresent.xpresent.config;

public abstract class config {
    /* SERVER */
    public static String HOST_NAME = "https://xpresent.ru/";  // server address
    //public static String HOST_NAME = "http://10.0.2.2/";    // localhost
    public static String APP_URL = "clientapp/";              // url for current app

    /* PAYMENT */
    public static String PASSWORD_APP = "Gk3#!gkde&3r4f";                   // password for App
    public static String TINKOFF_TERMINAL_KEY = "1590986024422";            // Tinkoff terminal key
    public static String TINKOFF_PASSWORD = "fqld3jfrcc9xw7ki";             // Tinkoff password
    public static String TINKOFF_TERMINAL_DEMO_KEY = "1590986024422DEMO";   // Tinkoff terminal DEMO key
    public static String TINKOFF_DEMO_PASSWORD = "pg7v48cmgiw16h5v";        // Tinkoff password DEMO
    public static String TINKOFF_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5yse9ka3ZQE0feuGtemYv3IqOlLck8zHUM7lTr0za6lXTszRSXfUO7jMb+L5C7e2QNFs+7sIX2OQJ6a+HG8kr+jwJ4tS3cVsWtd9NXpsU40PE4MeNr5RqiNXjcDxA+L4OsEm/BlyFOEOh2epGyYUd5/iO3OiQFRNicomT2saQYAeqIwuELPs1XpLk9HLx5qPbm8fRrQhjeUD5TLO8b+4yCnObe8vy/BMUwBfq+ieWADIjwWCMp2KTpMGLz48qnaD9kdrYJ0iyHqzb2mkDhdIzkim24A3lWoYitJCBrrB2xM05sm9+OdCI1f7nPNJbl5URHobSwR94IRGT7CJcUjvwIDAQAB";
    /* VIEW */
    public static int ELEMENTS_ON_PAGE = 12;        // number of downloaded elements from server
    public static int REVIEWS_ON_PAGE = 2;          // how much reviews to display on impression page
    public static int NUM_IMG_IN_REVIEW = 3;        // maximum number of images in review
    /* CATEGORY img */
    public static int CATEGORY_IMG_WIDTH = 555;
    public static int CATEGORY_IMG_HEIGHT = 180;
    /* IMPRESSION img */
    public static int IMPRESSION_IMG_WIDTH = 555;
    public static int IMPRESSION_IMG_HEIGHT = 180;
    public static int IMPRESSION_IMG_HEIGHT_ITEM = 260;
    /* SET img */
    public static int SET_IMG_WIDTH = 257;
    public static int SET_IMG_HEIGHT = 203;
    public static int SET_IMP_IMG_HEIGHT = 100;
    /* REVIEW img */
    public static int REVIEW_MIN_IMG_WIDTH = 52;
    public static int REVIEW_MIN_IMG_HEIGHT = 52;
    public static int REVIEW_IMG_WIDTH = 100;
    public static int REVIEW_IMG_HEIGHT = 100;
    /* ORDER img */
    public static int ORDER_MIN_IMG_WIDTH = 85;
    public static int ORDER_MIN_IMG_HEIGHT = 85;
    /* CASHBACK */
    public static double CASHBACK = 0.05;
    public static double MAX_CASHBACK_PERCENT = 0.5;     // Maximum % of discount when apply cashback
    /* OTHER */
    public static String RUB = "₽";
    public static String[] TIME = {"8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "00:00"};
}
