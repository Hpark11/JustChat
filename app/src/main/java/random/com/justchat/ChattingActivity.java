package random.com.justchat;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import random.com.justchat.databinding.ActivityChattingBinding;
import random.com.justchat.util.Codes;

/**
 * Created by hpark_ipl on 2017. 10. 18..
 */

public class ChattingActivity extends AppCompatActivity {
    ActivityChattingBinding b;
    private final String TAG = this.getClass().getSimpleName();
    private final boolean DEGUG = true;
    private SocketIO socket;

    private int type = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_chatting);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket = null;
    }

    private void initView() {
        b.newChatButton.setOnTouchListener(onNewChatTouchListener);
        b.optionsButton.setOnTouchListener(onOptionsTouchListener);
        b.sendEventTextView.setOnTouchListener(onSendTouchListener);

        reconnect();
    }

    private void reconnect() {
        try {
            socket = new SocketIO("http://52.79.121.11:27800");
            socket.connect(ioCallback);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        type = -1;
    }

    private final Handler chatMessagehandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            chatBubble(msg.obj.toString(), type == msg.what);
            return false;
        }
    });

    private IOCallback ioCallback = new IOCallback() {
        @Override
        public void onDisconnect() {
            Log.d(TAG, "Disconnected");
        }

        @Override
        public void onConnect() {
            socket.emit("roomCheck");
        }

        @Override
        public void onMessage(String data, IOAcknowledge ioAcknowledge) {
            Log.d(TAG, "Server said: " + data + "ioAcknowledge: " + ioAcknowledge.toString());
        }

        @Override
        public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {
            Log.d(TAG, jsonObject.toString());
        }

        @Override
        public void on(String event, IOAcknowledge ioAcknowledge, Object... objects) {
            String data = (new Gson()).toJson(objects);
            try {
                JSONArray array = new JSONArray(data);
                for (int i = 0; i < array.length(); i++) {
                    if (event.equals(Codes.Event.completeMatch.val) && type == -1) {
                        JSONObject json = (array.getJSONObject(i)).getJSONObject("nameValuePairs");
                        if (json.getString(Codes.Key.resultCode.val).equals(Codes.success)) {
                            type = Integer.parseInt(json.getString(Codes.Key.isRoom.val));
                        }
                    } else if (event.equals(Codes.Event.receiveMessage.val)) {
                        final String[] divided = (array.getString(i)).split("\\|");
                        if (divided.length >= 2) {
                            final int recType = Integer.parseInt(divided[0]);
                            final String message = divided[1];
                            chatMessagehandler.sendMessage(chatMessagehandler.obtainMessage(recType, message));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(SocketIOException e) {
            e.printStackTrace();
        }
    };

    private View.OnTouchListener onNewChatTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            socket.disconnect();
            reconnect();
            return false;
        }
    };

    private View.OnTouchListener onOptionsTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (socket != null) {
                socket.emit(Codes.Event.sendMessage.val, "Hello~!~!");
            }
            return false;
        }
    };

    private View.OnTouchListener onSendTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (socket != null) socket.emit(Codes.Event.sendMessage.val, type + "|" + b.contentEditText.getText().toString());
                    b.contentEditText.setText("");
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        }
    };


    public void chatBubble(String message, boolean type){
        TextView textView = new TextView(ChattingActivity.this);
        textView.setText(message);
        textView.setMaxWidth(600);
        textView.setPadding(32,18,32,18);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.setMargins(15,15,15,15);

        lp2.weight = 1.0f;

        if(type == false) {
            lp2.gravity = Gravity.LEFT;
            textView.setMaxHeight(getResources().getDimensionPixelSize(R.dimen.chat_bubble_max_height));
            textView.setBackgroundResource(R.drawable.text_box_stranger);
        } else {
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.text_box_you);
            textView.setTextColor(getColor(R.color.white));
        }

        textView.setLayoutParams(lp2);
        b.insideLayout.addView(textView);
        b.chatScrollView.fullScroll(View.FOCUS_FORWARD);
    }
}
