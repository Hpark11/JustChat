package random.com.justchat;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_chatting);
        initView();
    }

    private void initView() {
        b.newChatButton.setOnTouchListener(onNewChatTouchListener);
        b.optionsButton.setOnTouchListener(onOptionsTouchListener);

        try {
            socket = new SocketIO("http://52.79.121.11:27800");
            socket.connect(ioCallback);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

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
            // 0이 최초 방 만든사람
            // 1이 방 입장하는 사람
            Log.d(TAG, "Server Triggered event: '" + event + "', Data : " + objects);
        }

        @Override
        public void onError(SocketIOException e) {
            e.printStackTrace();
        }
    };

    private View.OnTouchListener onNewChatTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (socket != null) {
                socket.emit(Codes.Event.disConnect.val);
            }
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

    public void chatBubble(String message, int type){
        TextView textView = new TextView(ChattingActivity.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.text_box_stranger);
        }
        else{
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.text_box_you);
        }

        textView.setLayoutParams(lp2);
        b.insideLayout.addView(textView);
        b.chatScrollView.fullScroll(View.FOCUS_DOWN);
    }
}
