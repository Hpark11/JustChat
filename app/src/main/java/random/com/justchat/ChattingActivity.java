package random.com.justchat;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import random.com.justchat.databinding.ActivityChattingBinding;
import random.com.justchat.databinding.BubbleLeftLayoutBinding;
import random.com.justchat.databinding.BubbleRightLayoutBinding;
import random.com.justchat.util.Codes;

/**
 * Created by hpark_ipl on 2017. 10. 18..
 */
public class ChattingActivity extends AppCompatActivity {
    ActivityChattingBinding b;
    private final String TAG = this.getClass().getSimpleName();
    private final boolean DEBUG = true;
    private SocketIO socket;

    private int type = -1;

    private final String you = "You";
    private final String stranger = "Stranger";
    private final boolean current = false;

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
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.gradient_bar));

        b.newChatButton.setOnTouchListener(onNewChatTouchListener);
        b.optionsButton.setOnClickListener(onOptionsClickListener);
        b.sendEventTextView.setOnTouchListener(onSendTouchListener);

        reconnect();
    }

    private void reconnect() {
        try {
            socket = new SocketIO("http://13.124.222.195:27800");
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
            statusBubble(getString(R.string.status_connection_lost));
        }

        @Override
        public void onConnect() {
            socket.emit(Codes.Event.roomCheck.val);
            statusBubble(getString(R.string.status_waiting));
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

            if(event.equals(Codes.Event.disConnect.val)) {
                statusBubble(getString(R.string.status_connection_lost));
            }

            try {
                JSONArray array = new JSONArray(data);
                for (int i = 0; i < array.length(); i++) {
                    if (event.equals(Codes.Event.completeMatch.val)) {
                        JSONObject json = (array.getJSONObject(i)).getJSONObject("nameValuePairs");

                        if (json.getString(Codes.Key.resultCode.val).equals(Codes.success)) {
                            if(json.getString(Codes.Key.isRoom.val).equals("1")) {
                                statusBubble(getString(R.string.status_connected));
                            }

                            if(type == -1) {
                                if (json.getString(Codes.Key.resultCode.val).equals(Codes.success)) {
                                    type = Integer.parseInt(json.getString(Codes.Key.isRoom.val));
                                }
                            }
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

    private boolean isOptionOpened = false;
    private View.OnClickListener onOptionsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isOptionOpened = !isOptionOpened;
            if(isOptionOpened) {
//                b.optionBox.setVisibility(View.VISIBLE);
//                ConstraintSet set = new ConstraintSet();
//                set.constrainWidth(b.optionBox.getId(), getResources().getDimensionPixelSize(R.dimen.option_box_width));
//                set.constrainHeight(b.optionBox.getId(), getResources().getDimensionPixelSize(R.dimen.option_box_height));
//
//                set.connect(b.optionBox.getId(), ConstraintSet.LEFT, b.chatRootLayout.getId(), ConstraintSet.LEFT, 41);
//                set.connect(b.optionBox.getId(), ConstraintSet.BOTTOM, b.chatRootLayout.getId(), ConstraintSet.BOTTOM, 43);
//
//                set.applyTo(b.chatRootLayout);

                b.coverLayout.setVisibility(View.VISIBLE);
                b.optionsButton.setBackgroundResource(R.drawable.btn_file_x);
            } else {
                //b.optionBox.setVisibility(View.INVISIBLE);
                b.coverLayout.setVisibility(View.INVISIBLE);
                b.optionsButton.setBackgroundResource(R.drawable.btn_file);
            }
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

    public void statusBubble(final String message) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {



                NotifyTextView textView = new NotifyTextView(ChattingActivity.this);
                textView.setText(message);
                textView.setPadding(32,18,32,18);

                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp2.setMargins(15,15,15,15);
                lp2.weight = 1.0f;

                lp2.gravity = Gravity.CENTER;
                textView.setBackgroundResource(R.drawable.text_box_stranger);
                textView.setTextColor(ContextCompat.getColor(context, R.color.darkGray));
                textView.setLayoutParams(lp2);
                b.insideLayout.addView(textView);
            }
        });
    }

    BubbleLeftLayoutBinding leftBubble;
    BubbleRightLayoutBinding rightBubble;

    public void chatBubble(final String message, final boolean type) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                String time = ((int)calendar.get(Calendar.AM_PM) == 1? "AM": "PM") + " " +
                        calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE);

                if(type == false) {
                    leftBubble = BubbleLeftLayoutBinding.inflate(inflater, b.insideLayout, false);
                    leftBubble.contentTextView.setText(message);
                    leftBubble.timeTextView.setText(time);
                    b.insideLayout.addView(leftBubble.chatBubbleLayout);
                } else  {
                    rightBubble = BubbleRightLayoutBinding.inflate(inflater, b.insideLayout, false);
                    rightBubble.contentTextView.setText(message);
                    rightBubble.timeTextView.setText(time);
                    b.insideLayout.addView(rightBubble.chatBubbleLayout);
                }

                scorllDown();
            }
        });
    }


    protected void scorllDown() {
        TimerTask task;
        task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        b.chatScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 240);
    }


    private class NotifyTextView extends TextView {
        public NotifyTextView(Context context) {
            super(context);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            scorllDown();
        }
    }
}
