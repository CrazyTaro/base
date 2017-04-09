package com.taro.base.helper;

import android.graphics.Point;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

/**
 * Created by taro on 16/8/16.
 * a builder for  simply creating spannable string for textView
 */
public class SpanTextViewHelper {
    private int mLastTextLength = 0;
    private SpannableStringBuilder mSpnBuilder;

    public SpanTextViewHelper() {
        mSpnBuilder = new SpannableStringBuilder();
    }

    public SpanTextViewHelper(String text) {
        this();
        this.append(text);
    }

    /**
     * append text and record the text length for the next operation(to set color or set textSize )
     *
     * @param text
     * @return
     */
    public SpanTextViewHelper append(@Nullable String text) {
        if (!TextUtils.isEmpty(text)) {
            mSpnBuilder.append(text);
            mLastTextLength = text.length();
        } else {
            mLastTextLength = 0;
        }
        return this;
    }

    /**
     * 设置下划线
     *
     * @return
     */
    public SpanTextViewHelper setUnderline() {
        if (mLastTextLength > 0) {
            int end = mSpnBuilder.length();
            int start = end - mLastTextLength;
            mSpnBuilder.setSpan(new UnderlineSpan(),
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return this;
    }

    /**
     * set textColor for the last append text
     *
     * @param color
     * @return
     */
    public SpanTextViewHelper setColor(@ColorInt int color) {
        if (mLastTextLength > 0) {
            int end = mSpnBuilder.length();
            int start = end - mLastTextLength;
            mSpnBuilder.setSpan(new ForegroundColorSpan(color),
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return this;
    }

    /**
     * set textColor for the last append text
     *
     * @param color
     * @param whichLine the required line number,always from 1 not 0
     * @return
     */
    public SpanTextViewHelper setColor(@ColorInt int color, int whichLine) {
        Point p = this.getLineStartIndex(null, whichLine);
        if (p != null) {
            mSpnBuilder.setSpan(new ForegroundColorSpan(color),
                    p.x,
                    p.y + p.x,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return this;
    }

    /**
     * set textSize for the last append text
     *
     * @param textSize
     * @param isDp     true if the textSize unit is dp, false if it is px
     * @return
     */
    public SpanTextViewHelper setTextSize(int textSize, boolean isDp) {
        if (mLastTextLength > 0) {
            int end = mSpnBuilder.length();
            int start = end - mLastTextLength;
            mSpnBuilder.setSpan(new AbsoluteSizeSpan(textSize, isDp),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return this;
    }

    /**
     * set textSize for the last append text
     *
     * @param textSize
     * @param isDp      true if the textSize unit is dp, false if it is px
     * @param whichLine the required line number,always from 1 not 0
     * @return
     */
    public SpanTextViewHelper setTextSize(int textSize, boolean isDp, int whichLine) {
        Point p = this.getLineStartIndex(null, whichLine);
        if (p != null) {
            mSpnBuilder.setSpan(new AbsoluteSizeSpan(textSize, isDp),
                    p.x,
                    p.y + p.x,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return this;
    }

    /**
     * find the char start index of required line
     *
     * @param p         point.x is the start index,point.y is the length
     * @param whichLine the required line number,always from 1 not 0
     * @return
     */
    private Point getLineStartIndex(Point p, int whichLine) {
        if (whichLine > 0) {
            String text = mSpnBuilder.toString();
            //split by line
            String[] strLines = text.split("\n");
            if (strLines.length <= whichLine) {
                int start = 0;
                int i = 0;
                for (; i < whichLine - 1; i++) {
                    //compute the start index
                    start += strLines[0].length() + 2;
                }
                //save the start index and the length of required line
                if (p == null) {
                    p = new Point(start, strLines[i].length());
                } else {
                    p.set(start, strLines[i].length());
                }
                return p;
            }
        }
        return null;
    }

    /**
     * set the spannable string to the textView and return the string at the same time
     * (you can reuse the spannable string)
     *
     * @param view
     * @return
     */
    public CharSequence build(TextView view) {
        if (view != null) {
            view.append(mSpnBuilder);
        }
        return mSpnBuilder;
    }

    /**
     * clear the text and span.just like a new builder.
     */
    public void clear() {
        mSpnBuilder.clear();
        mSpnBuilder.clearSpans();
    }

    /**
     * return the spannable string.
     * you can set this to textView and you will see your span string
     *
     * @return
     */
    public CharSequence build() {
        return mSpnBuilder;
    }

    /**
     * return all the text without any changed,just string
     *
     * @return
     */
    public String getText() {
        return mSpnBuilder.toString();
    }

    /**
     * return all the text length
     *
     * @return
     */
    public int length() {
        return mSpnBuilder.length();
    }

    /**
     * return the last append text length
     *
     * @return
     */
    public int lastTextLength() {
        return mLastTextLength;
    }
}
