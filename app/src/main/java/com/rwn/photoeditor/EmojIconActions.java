package com.rwn.photoeditor;

import android.view.View;
import android.widget.ImageView;

public abstract class EmojIconActions {
    public EmojIconActions(Emoji_Activity emoji_activity, View root_view, EmojiconEditText emojiconEditText, ImageView emojiImageView) {
    }

    public void closeEmojIcon() {
    }

    public void ShowEmojIcon() {

    }

    public void getClass(int ic_launcher, int ic_launcher1) {
    }

    public void setUseSystemEmoji(KeyboardListenIer b) {

    }

        public abstract void onKeyboardOpen();

        public abstract void onKeyboardClose();

    public abstract void setUseSystemEmoji(boolean b);
}

