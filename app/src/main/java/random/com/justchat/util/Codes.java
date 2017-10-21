package random.com.justchat.util;

/**
 * Created by hpark_ipl on 2017. 10. 19..
 */

public final class Codes {
    public enum Event {
        roomCheck("roomCheck"),
        completeMatch("completeMatch"),
        disConnect("disConnect"),
        sendMessage("sendMessage"),
        receiveMessage("receiveMessage");

        public final String val;

        Event(String val) { this.val = val; }
    }

    public enum Key {
        resultCode("result_code"),
        isRoom("is_room");

        public final String val;
        Key(String val) { this.val = val; }
    }

    public static final String success = "1";
    public static final String fail = "1";



}
