package com.taro.base.helper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.taro.base.R;
import com.taro.base.views.FixedBottomSheetDialog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.util.TypedValue.applyDimension;

/**
 * 通用的底部弹出选择对话框
 * Created by taro on 2017/2/10.
 */

public class BottomChoiceDialogHelper implements View.OnClickListener {
    /**
     * 使用默认设置时正常序列
     */
    public static final int SETTING_SEQUENCE_ORDER = 0;
    /**
     * 使用默认设置时反序列
     */
    public static final int SETTING_SEQUENCE_REVERSE = 1;
    /**
     * 使用默认设置时取消文本控件的ID
     */
    public static final int DEFAULT_CANCEL_TEXT_ID = android.support.v7.appcompat.R.id.cancel_action;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {SETTING_SEQUENCE_ORDER, SETTING_SEQUENCE_REVERSE})
    public @interface SettingSequence {
    }

    /**
     * 尺寸单位
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {UNIT_DP, UNIT_SP, UNIT_PX})
    public @interface DimensionUnit {
    }

    /**
     * 尺寸单位,DP
     */
    public static final int UNIT_DP = TypedValue.COMPLEX_UNIT_DIP;
    /**
     * 尺寸单位,SP
     */
    public static final int UNIT_SP = TypedValue.COMPLEX_UNIT_SP;
    /**
     * 尺寸单位,PX
     */
    public static final int UNIT_PX = TypedValue.COMPLEX_UNIT_PX;

    View mContentView;
    TextView mTvCancel;
    RecyclerView mRvContent;
    View mVDivider;

    private Activity mAct = null;
    private FixedBottomSheetDialog mDialog = null;
    private InnerAdapter mAdapter = null;
    private OnItemChosenListener mItemChosenListener;
    private OnCancelClickListener mCancelListener;

    private DefaultItemDecoration mDefaultDecoration = null;

    public BottomChoiceDialogHelper(Activity context) {
        mAct = context;

        mDialog = new FixedBottomSheetDialog(context);
        mDialog.setContentView(createContentView(context));
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);

        //item填充对象
        mAdapter = new InnerAdapter(context, this);
        mRvContent.setLayoutManager(new LinearLayoutManager(context));
        mRvContent.setAdapter(mAdapter);

        mTvCancel.setOnClickListener(this);
    }

    private View createContentView(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#f8f8f8"));

        RecyclerView rv = new RecyclerView(context);
        LinearLayout.LayoutParams rvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rvParams.setMargins(0, 0, 0, 0);
        rv.setLayoutParams(rvParams);

        View divider = new View(context);
        int dividerHeight = (int) TypedValue.applyDimension(UNIT_DP, 5, context.getResources().getDisplayMetrics());
        divider.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight));

        TextView tvCancel = new TextView(context);
        tvCancel.setId(DEFAULT_CANCEL_TEXT_ID);
        int tvHeight = (int) TypedValue.applyDimension(UNIT_DP, 50, context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tvHeight);
        tvParams.setMargins(0, 0, 0, 0);
        tvCancel.setLayoutParams(tvParams);
        tvCancel.setGravity(Gravity.CENTER);
        tvCancel.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        tvCancel.setText("取消");
        tvCancel.setTextSize(UNIT_SP, 18);

        layout.addView(rv);
        layout.addView(divider);
        layout.addView(tvCancel);

        mContentView = layout;
        mRvContent = rv;
        mTvCancel = tvCancel;
        mVDivider = divider;
        return layout;
    }

    public BottomChoiceDialogHelper setBackgroundDrawable(@DrawableRes int drawRes) {
        mContentView.setBackgroundResource(drawRes);
        return this;
    }

    public BottomChoiceDialogHelper setBackgroundColor(@ColorInt int color) {
        mContentView.setBackgroundColor(color);
        return this;
    }

    public BottomChoiceDialogHelper setChoiceBackgroundDrawable(@DrawableRes int drawRes) {
        mRvContent.setBackgroundResource(drawRes);
        return this;
    }

    public BottomChoiceDialogHelper setChoiceBackgroundColor(@ColorInt int color) {
        mRvContent.setBackgroundColor(color);
        return this;
    }

    public BottomChoiceDialogHelper setCancelBackgroundDrawable(@DrawableRes int drawRes) {
        mTvCancel.setBackgroundResource(drawRes);
        return this;
    }

    public BottomChoiceDialogHelper setCancelBackgroundColor(@ColorInt int color) {
        mTvCancel.setBackgroundColor(color);
        return this;
    }

    private ViewGroup.MarginLayoutParams computeMargin(ViewGroup.LayoutParams params, float value, boolean isDp, int gravity) {
        ViewGroup.MarginLayoutParams marginParams = null;
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            marginParams = new ViewGroup.MarginLayoutParams(params);
        } else if (params instanceof ViewGroup.MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams) params;
        } else {
            marginParams = new ViewGroup.MarginLayoutParams(params);
        }
        int size = (int) value;
        if (isDp) {
            size = (int) TypedValue.applyDimension(UNIT_DP, value, mAct.getResources().getDisplayMetrics());
        }

        int left, top, right, bottom;
        if (gravity == -1) {
            left = size;
            top = size;
            right = size;
            bottom = size;
        } else {
            left = marginParams.leftMargin;
            top = marginParams.topMargin;
            right = marginParams.rightMargin;
            bottom = marginParams.bottomMargin;
            switch (gravity) {
                case Gravity.LEFT:
                    left = size;
                    break;
                case Gravity.TOP:
                    top = size;
                    break;
                case Gravity.RIGHT:
                    right = size;
                    break;
                case Gravity.BOTTOM:
                    bottom = size;
                    break;
            }
        }
        marginParams.setMargins(left, top, right, bottom);
        return marginParams;
    }

    public BottomChoiceDialogHelper setChoiceMargin(float value, boolean isDp) {
        ViewGroup.MarginLayoutParams marginParams = computeMargin(mRvContent.getLayoutParams(), value, isDp, -1);
        mRvContent.setLayoutParams(marginParams);
        return this;
    }

    public BottomChoiceDialogHelper setChoiceMargin(float value, boolean isDp, int gravity) {
        ViewGroup.MarginLayoutParams marginParams = computeMargin(mRvContent.getLayoutParams(), value, isDp, gravity);
        mRvContent.setLayoutParams(marginParams);
        return this;
    }

    public BottomChoiceDialogHelper setCancelMargin(float value, boolean isDp) {
        ViewGroup.MarginLayoutParams params = computeMargin(mTvCancel.getLayoutParams(), value, isDp, -1);
        mTvCancel.setLayoutParams(params);
        return this;
    }

    public BottomChoiceDialogHelper setCancelMargin(float value, boolean isDp, int gravity) {
        ViewGroup.MarginLayoutParams params = computeMargin(mTvCancel.getLayoutParams(), value, isDp, gravity);
        mTvCancel.setLayoutParams(params);
        return this;
    }

    /**
     * 添加指定位置的item的样式设置,返回被移除的item样式,若不存在时返回null
     *
     * @param index   指定位置,从0开始
     * @param setting item样式
     * @return
     */
    public BottomChoiceDialogHelper addItemSetting(int index, @NonNull ItemSetting setting) {
        mAdapter.addItemSetting(index, setting);
        return this;
    }

    /**
     * 移除指定位置的item的样式设置,返回被移除的item样式,若不存在时返回null
     *
     * @param index 指定位置,从0开始
     * @return
     */
    public ItemSetting removeItemSetting(int index) {
        return mAdapter.removeItemSetting(index);
    }

    /**
     * 清除所有的item样式
     *
     * @param isContainSpecify 是否包括第一项和最后一项特殊的item样式也清除
     * @return
     */
    public BottomChoiceDialogHelper clearItemSetting(boolean isContainSpecify) {
        mAdapter.clearItemSetting(isContainSpecify);
        return this;
    }

    /**
     * 设置样式应用的顺序,可以顺序应用,也可以逆序应用
     *
     * @param sequence 顺序类型
     * @return
     */
    public BottomChoiceDialogHelper setItemSettingSequence(@SettingSequence int sequence) {
        mAdapter.setItemSettingSequence(sequence);
        return this;
    }

    /**
     * 设置item文本颜色
     *
     * @param color
     * @return
     */
    public BottomChoiceDialogHelper setTextColor(@ColorInt int color) {
        mAdapter.setTextColor(color);
        return this;
    }


    /**
     * 设置分隔线是否可见
     *
     * @param visibility
     * @return
     */
    public BottomChoiceDialogHelper setDividerVisible(int visibility) {
        mVDivider.setVisibility(visibility);
        return this;
    }

    /**
     * 设置取消按钮与操作的分隔线颜色
     *
     * @param color
     * @return
     */
    public BottomChoiceDialogHelper setDividerColor(@ColorInt int color) {
        mVDivider.setBackgroundColor(color);
        return this;
    }

    /**
     * 设置取消按钮与操作的分隔线颜色
     *
     * @param colorRes
     * @return
     */
    public BottomChoiceDialogHelper setDividerColorRes(@ColorRes int colorRes) {
        mVDivider.setBackgroundColor(mAct.getResources().getColor(colorRes));
        return this;
    }

    /**
     * 设置取消按钮的颜色
     */
    public BottomChoiceDialogHelper setCancelColor(@ColorInt int color) {
        mTvCancel.setTextColor(color);
        return this;
    }

    /**
     * 设置取消按钮的颜色
     *
     * @param colorRes
     * @return
     */
    public BottomChoiceDialogHelper setCancelColorRes(@ColorRes int colorRes) {
        mTvCancel.setTextColor(mAct.getResources().getColor(colorRes));
        return this;
    }

    /**
     * 设置取消按钮可见状态
     *
     * @param visible
     * @return
     */
    public BottomChoiceDialogHelper setCancelVisible(int visible) {
        mTvCancel.setVisibility(visible);
        return this;
    }

    /**
     * 设置取消文本的样式
     *
     * @param typefaceStyle
     * @return
     */
    public BottomChoiceDialogHelper setCancelTextStyle(int typefaceStyle) {
        mTvCancel.setTypeface(null, typefaceStyle);
        return this;
    }

    /**
     * 设置取消文本样式为粗体
     *
     * @return
     */
    public BottomChoiceDialogHelper setCancelTextBold() {
        setCancelTextStyle(Typeface.BOLD);
        return this;
    }


    /**
     * 获取取消文本
     *
     * @return
     */
    public TextView getCancelText() {
        return mTvCancel;
    }

    public View getDivider() {
        return mVDivider;
    }

    /**
     * 设置item文本颜色
     *
     * @param colorRes
     * @return
     */
    public BottomChoiceDialogHelper setTextColorRes(@ColorRes int colorRes) {
        mAdapter.setTextColorRes(colorRes);
        return this;
    }

    /**
     * 设置item文本字体大小,此方法使用单位为px
     *
     * @param textSize
     * @return
     */
    public BottomChoiceDialogHelper setTextSize(float textSize) {
        mAdapter.setTextSize(textSize);
        return this;
    }

    /**
     * 设置item文本字体大小
     *
     * @param textSize
     * @param unit     指定使用的单位
     * @return
     */
    public BottomChoiceDialogHelper setTextSize(float textSize, @DimensionUnit int unit) {
        float value = TypedValue.applyDimension(unit, textSize, mAct.getResources().getDisplayMetrics());
        mAdapter.setTextSize(value);
        return this;
    }

    /**
     * 设置item文本字体大小
     *
     * @param res
     * @return
     */
    public BottomChoiceDialogHelper setTextSizeRes(@DimenRes int res) {
        float value = mAct.getResources().getDimensionPixelSize(res);
        mAdapter.setTextSize(value);
        return this;
    }

    /**
     * 设置默认的item显示的数据,默认情况下为仅一个字符串,如果需要显示其它内容请使用{@link OnItemDelegate}
     *
     * @param strList
     * @return
     */
    public BottomChoiceDialogHelper setDefaultItemList(List<String> strList) {
        mAdapter.setDefaultItemList(strList);
        return this;
    }

    /**
     * 设置默认的item显示的数据,默认情况下为仅一个字符串,如果需要显示其它内容请使用{@link OnItemDelegate}
     *
     * @param strArr
     * @return
     */
    public BottomChoiceDialogHelper setDefaultItemList(String[] strArr) {
        mAdapter.setDefaultItemList(strArr);
        return this;
    }

    /**
     * 设置默认的item显示的数据,默认情况下为仅一个字符串,如果需要显示其它内容请使用{@link OnItemDelegate}
     *
     * @param strRes
     * @return
     */
    public BottomChoiceDialogHelper setDefaultItemList(int[] strRes) {
        mAdapter.setDefaultItemList(strRes);
        return this;
    }

    /**
     * 设置item显示的数据,此处应该是未默认数据,数据可以是任何类型,并且需要实现相关的数据绑定{@link OnItemDelegate}及viewHolder创建<br>
     * 如果仅需要显示单个字符串,建议使用默认数据设置{@link #setDefaultItemList(int[])}
     *
     * @param list
     * @return
     */
    public BottomChoiceDialogHelper setItemList(List<? extends Object> list) {
        mAdapter.setItemList(list);
        return this;
    }

    /**
     * 添加自定义的itemDecoration
     *
     * @param decoration
     * @return
     */
    public BottomChoiceDialogHelper addItemDecoration(RecyclerView.ItemDecoration decoration) {
        mRvContent.addItemDecoration(decoration);
        return this;
    }

    /**
     * 移除使用的自定义itemDecoration
     *
     * @param decoration
     * @return
     */
    public BottomChoiceDialogHelper removeItemDecoration(RecyclerView.ItemDecoration decoration) {
        mRvContent.removeItemDecoration(decoration);
        return this;
    }

    /***
     * 启用默认的itemDecoration
     *
     * @return
     */
    public BottomChoiceDialogHelper enabledDeafultItemDecoration() {
        if (mDefaultDecoration == null) {
            mDefaultDecoration = new DefaultItemDecoration();
            mRvContent.addItemDecoration(mDefaultDecoration);
        }
        mRvContent.removeItemDecoration(mDefaultDecoration);
        mRvContent.addItemDecoration(mDefaultDecoration);
        return this;
    }

    /**
     * 启用默认的itemDecoration,并设置其使用的颜色及分隔线大小
     *
     * @param color        分隔线颜色
     * @param dividerWidth 分隔线大小
     * @return
     */
    public BottomChoiceDialogHelper enabledDeafultItemDecoration(@ColorInt int color, int dividerWidth) {
        if (mDefaultDecoration == null) {
            mDefaultDecoration = new DefaultItemDecoration(color, dividerWidth);
        } else {
            mDefaultDecoration.setColor(color);
            mDefaultDecoration.setDivderWidth(dividerWidth);
        }
        mRvContent.removeItemDecoration(mDefaultDecoration);
        mRvContent.addItemDecoration(mDefaultDecoration);
        return this;
    }

    /**
     * 取消默认的itemDecoration
     *
     * @return
     */
    public BottomChoiceDialogHelper disabledDefaultItemDecoration() {
        mRvContent.removeItemDecoration(mDefaultDecoration);
        return this;
    }

    /**
     * 设置item项目被选中的回调事件
     *
     * @param listener
     * @return
     */
    public BottomChoiceDialogHelper setOnItemChosenListener(OnItemChosenListener listener) {
        mItemChosenListener = listener;
        return this;
    }

    /**
     * 设置取消按钮的监听事件
     *
     * @param listener
     * @return
     */
    public BottomChoiceDialogHelper setOnCancelClickListener(OnCancelClickListener listener) {
        mCancelListener = listener;
        return this;
    }

    public BottomChoiceDialogHelper show() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
        return this;
    }


    /**
     * 设置最后一项item的部分样式设置,此样式的优先级高于普通的样式设置<br>
     * 目前仅支持设置textColor,textSize,backgroundColor
     *
     * @param setting
     * @return
     */
    public BottomChoiceDialogHelper setLastItemSetting(ItemSetting setting) {
        mAdapter.setLastItemSetting(setting);
        return this;
    }

    /**
     * 设置第一项item的部分样式设置,此样式的优先级高于普通的样式设置<br>
     * 目前仅支持设置textColor,textSize,backgroundColor
     *
     * @param setting
     * @return
     */
    public BottomChoiceDialogHelper setFirstItemSetting(ItemSetting setting) {
        mAdapter.setFirstItemSetting(setting);
        return this;
    }

    /**
     * 设置item的代理处理对象,包括创建viewHolder及绑定自定义数据等
     *
     * @param delegate
     * @return
     */
    public BottomChoiceDialogHelper setItemDelegate(OnItemDelegate delegate) {
        mAdapter.setItemDelegate(delegate);
        return this;
    }

    public void dismiss() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        //取消按钮
        if (v.getId() == DEFAULT_CANCEL_TEXT_ID) {
            if (mCancelListener != null) {
                if (mCancelListener.onCancelClick(this, v)) {
                    this.dismiss();
                }
            } else {
                this.dismiss();
            }
        } else {
            if (mItemChosenListener != null) {
                //item项目点击事件
                RecyclerView.ViewHolder viewHolder = mRvContent.getChildViewHolder(v);
                int position = mRvContent.getChildAdapterPosition(v);
                Object data = mAdapter.getItem(position);
                if (mItemChosenListener.onBottomItemChosen(this, v, viewHolder, position, data)) {
                    this.dismiss();
                }
            }
        }
    }


    /**
     * 取消按钮被点击时回调
     */
    public interface OnCancelClickListener {
        /**
         * 取消按钮被点击时回调,回调后需要关闭对话框返回true,否则返回false
         *
         * @param view
         * @return
         */
        public boolean onCancelClick(BottomChoiceDialogHelper dialog, View view);
    }

    /**
     * 项目被选中的回调监听事件
     */
    public interface OnItemChosenListener {
        /**
         * 项目被选中时的回调,回调后需要关闭对话框返回true,否则返回false
         *
         * @param dialog     dialog处理类(不是真正的dialog)
         * @param view       当前item的contentView
         * @param viewHolder 当前item的viewHolder,可能是null
         * @param position   当前item的position,可能不存在,不存在时为-1;
         * @param obj        当前item对应的数据对象,可能为null
         */
        public boolean onBottomItemChosen(BottomChoiceDialogHelper dialog, View view, RecyclerView.ViewHolder viewHolder, int position, @Nullable Object obj);
    }

    /**
     * item的自定义处理代理接口
     */
    public interface OnItemDelegate {
        /**
         * 获取对应位置的itemViewType
         *
         * @param position
         * @return
         */
        public int getItemViewType(int position);

        /**
         * 创建自定义的viewHolder
         *
         * @param parent
         * @param viewType
         * @return
         */
        public RecyclerView.ViewHolder onItemCreate(ViewGroup parent, int viewType);

        /**
         * 数据绑定
         *
         * @param holder
         * @param obj
         * @param position
         */
        public void onItemBind(RecyclerView.ViewHolder holder, @Nullable Object obj, int position);
    }

    private static class DefaultItemDecoration extends RecyclerView.ItemDecoration {
        private Paint mPaint;
        private int mDivderWidth;

        public DefaultItemDecoration() {
            this(Color.parseColor("#979797"), 1);
        }

        public DefaultItemDecoration(@ColorInt int color, int width) {
            mDivderWidth = width;

            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(color);
        }

        public void setColor(@ColorInt int color) {
            mPaint.setColor(color);
        }

        public void setDivderWidth(int width) {
            mDivderWidth = width;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            for (int i = 0; i < state.getItemCount(); i++) {
                //最后一项不画
                if (i + 1 != state.getItemCount()) {
                    View view = parent.getChildAt(i);
                    c.drawRect(view.getLeft(), view.getBottom(), view.getRight(), view.getBottom() + mDivderWidth, mPaint);
                }
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0, 0, 0, mDivderWidth);
        }
    }

    /**
     * item辅助设置类,可设置textColor,textSize,backgroundColor
     */
    public static class ItemSetting {
        public static final int INVALID_VALUE = Integer.MIN_VALUE;

        public int mTextColor = Integer.MIN_VALUE;
        public int mBackgroundColor = Integer.MIN_VALUE;
        public float mTextSize = Integer.MIN_VALUE;

        public static ItemSetting createColorSetting(int color) {
            return new ItemSetting(color, INVALID_VALUE, INVALID_VALUE, UNIT_PX);
        }

        public static ItemSetting createColorResSetting(@NonNull Context context, @ColorRes int colorRes) {
            return new ItemSetting(context, colorRes, INVALID_VALUE, INVALID_VALUE);
        }

        public ItemSetting() {
        }

        public ItemSetting(@NonNull Context context, @ColorRes int colorRes) {
            this(context, colorRes, INVALID_VALUE, INVALID_VALUE);
        }

        public ItemSetting(int color, int backgroundColor, float textSize, @DimensionUnit int unit) {
            mTextColor = color;
            mBackgroundColor = backgroundColor;
            mTextSize = TypedValue.applyDimension(unit, textSize, Resources.getSystem().getDisplayMetrics());
        }

        public ItemSetting(@NonNull Context context, @ColorRes int colorRes, @ColorRes int bgColorRes, @DimenRes int textSizeRes) {
            if (colorRes != INVALID_VALUE) {
                mTextColor = context.getResources().getColor(colorRes);
            }
            if (bgColorRes != INVALID_VALUE) {
                mBackgroundColor = context.getResources().getColor(bgColorRes);
            }
            if (textSizeRes != INVALID_VALUE) {
                mTextSize = context.getResources().getDimensionPixelSize(textSizeRes);
            }
        }

        public boolean isValidTextColor() {
            return mTextColor != Integer.MIN_VALUE;
        }

        public boolean isValidTextSize() {
            return mTextSize != Integer.MIN_VALUE;
        }

        public boolean isValidBackgroundColor() {
            return mBackgroundColor != Integer.MIN_VALUE;
        }
    }

    //内部使用的Adapter
    private static class InnerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private float mItemTextSize = 0;
        private int mItemTextColor = 0;

        //最后一项item的样式
        private ItemSetting mLastItemSetting;
        //第一项item的样式
        private ItemSetting mFirstItemSetting;

        private OnItemDelegate mItemDelegate;
        private View.OnClickListener mClickListener;
        private Context mContext;

        private ArrayMap<Integer, ItemSetting> mSettingMap;
        private int mSettingSequence = SETTING_SEQUENCE_ORDER;
        private List<? extends Object> mDatas;

        public InnerAdapter(@NonNull Context context, @NonNull View.OnClickListener listener) {
            mContext = context;
            mClickListener = listener;

            float defaultTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, mContext.getResources().getDisplayMetrics());
            int defaultTextColor = Color.parseColor("#333333");

            this.setTextSize(defaultTextSize);
            this.setTextColor(defaultTextColor);
        }

        public ItemSetting addItemSetting(int index, @NonNull ItemSetting setting) {
            if (mSettingMap == null) {
                mSettingMap = new ArrayMap<>(100);
            }
            return mSettingMap.put(index, setting);
        }

        public ItemSetting removeItemSetting(int index) {
            return mSettingMap.remove(index);
        }

        public void clearItemSetting(boolean isContainsSpecify) {
            mSettingMap.clear();
            if (isContainsSpecify) {
                mFirstItemSetting = null;
                mLastItemSetting = null;
            }
        }

        public void setItemSettingSequence(@SettingSequence int sequence) {
            mSettingSequence = sequence;
        }

        public void setDefaultItemList(List<String> strList) {
            mDatas = strList;
        }

        public void setDefaultItemList(String[] strArr) {
            if (strArr != null) {
                mDatas = Arrays.asList(strArr);
            } else {
                mDatas = null;
            }
        }

        public void setDefaultItemList(int[] strRes) {
            if (strRes != null) {
                List<String> list = new ArrayList<>(strRes.length);
                for (int i = 0; i < strRes.length; i++) {
                    list.add(mContext.getResources().getString(strRes[i]));
                }
                mDatas = list;
            } else {
                mDatas = null;
            }
        }

        public void setItemList(List<? extends Object> list) {
            mDatas = list;
        }

        public Object getItem(int position) {
            if (position < getItemCount() && position >= 0) {
                return mDatas.get(position);
            } else {
                return null;
            }
        }

        public void setLastItemSetting(ItemSetting setting) {
            mLastItemSetting = setting;
        }

        public void setFirstItemSetting(ItemSetting setting) {
            mFirstItemSetting = setting;
        }

        public void setItemDelegate(OnItemDelegate delegate) {
            mItemDelegate = delegate;
        }

        public void setTextSize(float textSize) {
            if (mItemTextSize != textSize) {
                mItemTextSize = textSize;
                InnerViewHolder.mParentTextSize = mItemTextSize;
                if (this.hasObservers()) {
                    this.notifyDataSetChanged();
                }
            }
        }

        public void setTextColor(@ColorInt int color) {
            if (mItemTextColor != color) {
                mItemTextColor = color;
                InnerViewHolder.mParentTextColor = mItemTextColor;
                if (this.hasObservers()) {
                    this.notifyDataSetChanged();
                }
            }
        }

        public void setTextColorRes(@ColorRes int colorRes) {
            int newColor = mContext.getResources().getColor(colorRes);
            this.setTextColor(newColor);
        }

        @Override
        public int getItemViewType(int position) {
            if (mItemDelegate != null) {
                return mItemDelegate.getItemViewType(position);
            } else {
                return super.getItemViewType(position);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder = null;
            if (mItemDelegate != null) {
                viewHolder = mItemDelegate.onItemCreate(parent, viewType);
            } else {
                viewHolder = InnerViewHolder.createViewHolder(parent.getContext());
            }
            viewHolder.itemView.setOnClickListener(mClickListener);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Object data = getItem(position);
            if (mItemDelegate != null) {
                //若数据代理存在,调用自定义绑定数据
                mItemDelegate.onItemBind(holder, data, position);
            } else {
                //若不存在,使用默认的viewHolder
                InnerViewHolder innerHolder = (InnerViewHolder) holder;
                //更新item的全局样式
                innerHolder.updateView();
                innerHolder.mTvText.setText(data == null ? "" : data.toString());

                //更新item的样式
                if (mSettingMap != null) {
                    ItemSetting setting = null;
                    //顺序获取样式
                    if (mSettingSequence == SETTING_SEQUENCE_ORDER) {
                        setting = mSettingMap.get(position);
                    } else if (mSettingSequence == SETTING_SEQUENCE_REVERSE) {
                        //逆序获取样式
                        setting = mSettingMap.get(getItemCount() - 1 - position);
                    }
                    //设置样式
                    if (setting != null) {
                        innerHolder.updateView(setting);
                    }
                }

                //判断是否第一项item,调用独立的样式修改
                if (mFirstItemSetting != null && position == 0) {
                    innerHolder.updateView(mFirstItemSetting);
                }

                //判断是否最后一项item,调用独立的样式修改
                if (mLastItemSetting != null && position == getItemCount() - 1) {
                    innerHolder.updateView(mLastItemSetting);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mDatas == null ? 0 : mDatas.size();
        }
    }

    private static class InnerViewHolder extends RecyclerView.ViewHolder {
        public static float mParentTextSize = 0;
        public static int mParentTextColor = 0;
        private TextView mTvText;

        public static InnerViewHolder createViewHolder(Context context) {
            TextView tv = new TextView(context);

            float defaultHeight = applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics());
            ViewGroup.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) defaultHeight);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(params);
            return new InnerViewHolder(tv);
        }

        private InnerViewHolder(TextView itemView) {
            super(itemView);
            this.mTvText = itemView;
            this.updateView();
        }

        public void updateView() {
            mTvText.setTextColor(mParentTextColor);
            mTvText.setTextSize(UNIT_PX, mParentTextSize);
        }

        public void updateView(@NonNull ItemSetting setting) {
            if (setting.isValidTextColor()) {
                mTvText.setTextColor(setting.mTextColor);
            }
            if (setting.isValidBackgroundColor()) {
                mTvText.setBackgroundColor(setting.mBackgroundColor);
            }
            if (setting.isValidTextSize()) {
                mTvText.setTextSize(UNIT_PX, setting.mTextSize);
            }
        }
    }

}
