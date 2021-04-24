package mobile.android.upf.data.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Order {

    private String id;
    private String user_id;
    private String restaurant_id;

    public Order() {
    }

    public Order(String id, String user_id, String restaurant_id) {
        this.id = id;
        this.user_id = user_id;
        this.restaurant_id = restaurant_id;
    }

    public Order(String user_id, String restaurant_id) {
        this.user_id = user_id;
        this.restaurant_id = restaurant_id;
        long tsLong = System.currentTimeMillis()/1000;
        String ts = Long.toString(tsLong);
        this.id = md5(user_id+restaurant_id+ts);
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getRestaurant_id() {
        return restaurant_id;
    }

    public void setRestaurant_id(String restaurant_id) {
        this.restaurant_id = restaurant_id;
    }
}
