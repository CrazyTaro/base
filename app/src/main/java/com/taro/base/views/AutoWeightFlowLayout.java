package com.taro.base.views;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * 多功能自动填充流式viewGroup.<br>
 * 1.可自动换行(至少指定一边是固定的或已知长度)<br>
 * 2.自动计算viewGroup需要的高度或宽度<br>
 * 3.自动计算按列显示元素(类似gridView,但是根据item数量自动计算高度)<br>
 * <br>
 * 支持的布局类型<br>
 * 1.horizontal-flow-layout:横向流式布局,自动换行(宽度需要固定)<br>
 * 2.vertical-flow-layout:竖向流式布局,自动换列(高度需要固定)<br>
 * 3.horizontal-grid-layout:横向均分列布局,自动换行(宽度需要固定)<br>
 * 4.vertical-grid-layout:竖向均分行布局,自动换列(高度需要固定)<br>
 * Created by taro on 16/9/19.
 */
public class AutoWeightFlowLayout extends ViewGroup {
    public static final String ATTR_GRID = "span";
    public static final String ATTR_RELATIVE_WEIGHT = "relativeWeight";
    public static final String ATTR_ORIENTATION = "orientation";
    public static final String ATTR_GRID_GRAVITY = "gridGravity";
    public static final String ATTR_GRID_RELATIVE_GRAVITY = "gridRelativeGravity";
    public static final String ATTR_HORIZONTAL_GRAVITY = "horizontalGravity";
    public static final String ATTR_VERTICAL_GRAVITY = "verticalGravity";
    public static final String ATTR_IS_RESIZE = "resize";
    public static final String ATTR_MARGIN = "margin";
    public static final String ATTR_MARGIN_ALL = "marginAll";
    public static final String[] ATTRS = {ATTR_GRID, ATTR_RELATIVE_WEIGHT, ATTR_ORIENTATION, ATTR_HORIZONTAL_GRAVITY, ATTR_VERTICAL_GRAVITY, ATTR_GRID_GRAVITY, ATTR_GRID_RELATIVE_GRAVITY, ATTR_IS_RESIZE, ATTR_MARGIN, ATTR_MARGIN_ALL};

    //当使用均分布局时,子view在单元格中的布局方式
    //当使用流式布局时,设置指定方向的布局方式
    private int mHorizontalGravity = Gravity.START;
    private int mVerticalGravity = Gravity.START;
    private int mGridLayoutGravity = Gravity.CENTER;
    private int mGridRelativeGravity = Gravity.CENTER;

    private int mGridCount = -1;
    private int mTotalLineCount = 0;
    //相对于固定边的大小
    private float mWeightRate = 0;
    private int mRelativeSize = 0;
    private boolean mIsResize = false;
    private boolean mIsVertical = false;
    private boolean mIsLayout = false;
    //viewGroup的padding
    private Rect mPadding = new Rect();
    //统一的子view使用的margin
    private Rect mMargin = new Rect();
    private Rect mRecycleRect = new Rect();
    private Point mRecyclePoint = new Point();
    private Point mInternalOffset = new Point();
    private SparseArray<Rect> mSpArrLayoutCache;
    private SparseArray<Point> mSpArrLineCount;

    private OnItemClickListener mItemClickListener = null;

    public AutoWeightFlowLayout(Context context) {
        super(context);
    }

    public AutoWeightFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes();
    }

    public AutoWeightFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes();
    }

    protected void initAttributes() {
        CharSequence attrs = this.getContentDescription();
        if (attrs != null && attrs.length() > 0) {
            StringBuilder builder = new StringBuilder(attrs);
            for (int i = 0; i < ATTRS.length; i++) {
                String value = getAttribute(builder, ATTRS[i]);
                setAttribute(value, ATTRS[i]);
            }
        }
    }

    private void setAttribute(@Nullable String value, @NonNull String attrName) {
        if (value == null || value.length() <= 0) {
            return;
        }
        int valueInt;
        float valueFloat;
        boolean valueBoolean;
        switch (attrName) {
            case ATTR_GRID:
                try {
                    valueInt = Integer.valueOf(value);
                    this.setGridCount(valueInt);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.setGridCount(0);
                }
                break;
            case ATTR_RELATIVE_WEIGHT:
                try {
                    valueFloat = Float.valueOf(value);
                    this.setRelativeWeightRate(valueFloat);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.setRelativeWeightRate(0);
                }
                break;
            case ATTR_GRID_GRAVITY:
                if (value.equals("center")) {
                    mGridLayoutGravity = Gravity.CENTER;
                } else if (value.equals("start") || value.equals("top") || value.equals("left")) {
                    mGridLayoutGravity = Gravity.START;
                } else if (value.equals("end") || value.equals("bottom") || value.equals("right")) {
                    mGridLayoutGravity = Gravity.END;
                }
                break;
            case ATTR_GRID_RELATIVE_GRAVITY:
                if (value.equals("center")) {
                    mGridRelativeGravity = Gravity.CENTER;
                } else if (value.equals("start") || value.equals("top") || value.equals("left")) {
                    mGridRelativeGravity = Gravity.START;
                } else if (value.equals("end") || value.equals("bottom") || value.equals("right")) {
                    mGridRelativeGravity = Gravity.END;
                }
                break;
            case ATTR_HORIZONTAL_GRAVITY:
                if (value.equals("center")) {
                    mHorizontalGravity = Gravity.CENTER;
                } else if (value.equals("start") || value.equals("top") || value.equals("left")) {
                    mHorizontalGravity = Gravity.START;
                } else if (value.equals("end") || value.equals("bottom") || value.equals("right")) {
                    mHorizontalGravity = Gravity.END;
                }
                break;
            case ATTR_VERTICAL_GRAVITY:
                if (value.equals("center")) {
                    mVerticalGravity = Gravity.CENTER;
                } else if (value.equals("start") || value.equals("top") || value.equals("left")) {
                    mVerticalGravity = Gravity.START;
                } else if (value.equals("end") || value.equals("bottom") || value.equals("right")) {
                    mVerticalGravity = Gravity.END;
                }
                break;
            case ATTR_ORIENTATION:
                if (value.equals("true") || value.equals("vertical")) {
                    this.setIsVerticalOrientation(true);
                } else if (value.equals("false") || value.equals("horizontal")) {
                    this.setIsVerticalOrientation(false);
                }
                break;
            case ATTR_IS_RESIZE:
                try {
                    valueBoolean = Boolean.valueOf(value);
                    this.setIsResize(valueBoolean);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.setIsResize(false);
                }
                break;
            case ATTR_MARGIN_ALL:
                try {
                    valueInt = Integer.valueOf(value);
                    this.setChildViewMargin(valueInt, valueInt, valueInt, valueInt);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.setChildViewMargin(0, 0, 0, 0);
                }
                break;
            case ATTR_MARGIN:
                try {
                    String[] margin = value.split(",");
                    if (margin.length == 4) {
                        int left = Integer.valueOf(margin[0]);
                        int top = Integer.valueOf(margin[1]);
                        int right = Integer.valueOf(margin[2]);
                        int bottom = Integer.valueOf(margin[3]);
                        this.setChildViewMargin(left, top, right, bottom);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    this.setChildViewMargin(0, 0, 0, 0);
                }
        }
    }

    /**
     * 获取给定属性的值
     *
     * @param strCache
     * @param attrName
     * @return
     */
    @Nullable
    private String getAttribute(@NonNull StringBuilder strCache, @NonNull String attrName) {
        int start = strCache.indexOf(attrName);
        if (start >= 0) {
            int end = strCache.indexOf(";", start);
            start += attrName.length() + 1;
            if (end >= 0) {
                return strCache.substring(start, end);
            }
        }
        return null;
    }

    /**
     * 设置需要划分的行或列的数量
     *
     * @param gridCount
     */
    public void setGridCount(int gridCount) {
        mGridCount = gridCount;
    }

    /**
     * 获取划分的行或列的数量
     *
     * @return
     */
    public int getGridCount() {
        return mGridCount;
    }

    /**
     * 设置相对的边比例
     *
     * @param weight
     */
    public void setRelativeWeightRate(float weight) {
        mWeightRate = weight;
    }

    /**
     * 获取相对边的比例
     *
     * @return
     */
    public float getRelativeWeightRate() {
        return mWeightRate;
    }

    /**
     * 设置布局方向
     *
     * @param isVertical
     */
    public void setIsVerticalOrientation(boolean isVertical) {
        mIsVertical = isVertical;
    }

    /**
     * 获取当前布局的状态是否为水平布局
     *
     * @return
     */
    public boolean isVerticalOrientation() {
        return mIsVertical;
    }

    /***
     * 设置是否允许根据子view的修改控件的高度
     *
     * @param isResize
     */
    public void setIsResize(boolean isResize) {
        mIsResize = isResize;
    }

    /**
     * 是否允许根据子view重新修改控件的高度
     *
     * @return
     */
    public boolean isResize() {
        return mIsResize;
    }

    /**
     * 若当前是grid网格布局,设置其指定边(长度固定的边)的元素对齐方式<br>
     *
     * @param gravity 建议使用 START 及 BOTTOM 进行设置,不需要考虑方向,默认即为左/上 和 右/下的对齐设置<br>
     *                <li>{@link Gravity#START}<br><li>
     *                <li>{@link Gravity#END}<br><li>
     *                <li>{@link Gravity#LEFT}<br><li>
     *                <li>{@link Gravity#RIGHT}<br><li>
     *                <li>{@link Gravity#TOP}<br><li>
     *                <li>{@link Gravity#BOTTOM}<br><li>
     */
    public void setGridGravity(int gravity) {
        mGridLayoutGravity = gravity;
    }

    /**
     * 若当前是grid网格布局,设置其未指定边(长度不固定的边)的元素对齐方式<br>
     * 仅在{@link #setRelativeWeightRate(float)}有效时此参数值有效
     *
     * @param gravity 建议使用 START 及 BOTTOM 进行设置,不需要考虑方向,默认即为左/上 和 右/下的对齐设置<br>
     *                <li>{@link Gravity#START}<br><li>
     *                <li>{@link Gravity#END}<br><li>
     *                <li>{@link Gravity#LEFT}<br><li>
     *                <li>{@link Gravity#RIGHT}<br><li>
     *                <li>{@link Gravity#TOP}<br><li>
     *                <li>{@link Gravity#BOTTOM}<br><li>
     */
    public void setGridRelativeGravity(int gravity) {
        mGridRelativeGravity = gravity;
    }

    /**
     * 若当前是grid网格布局,且{@link #setRelativeWeightRate(float)}有效,获取其指定边(长度固定的边)的元素对齐方式
     *
     * @return
     */
    public int getGridGravity() {
        return mGridLayoutGravity;
    }

    /**
     * 若当前是grid网格布局,且{@link #setRelativeWeightRate(float)}有效,获取其未指定边(长度不固定的边)的元素对齐方式
     *
     * @return
     */
    public int getGridRelativtGravity() {
        return mGridRelativeGravity;
    }

    /**
     * 设置子View被单击事件(也可以自己给添加的子view添加事件)
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return mItemClickListener;
    }

    /**
     * 设置子view统一的margin
     *
     * @param left   左边距
     * @param top    上边距
     * @param right  右边距
     * @param bottom 下边距
     */
    public void setChildViewMargin(int left, int top, int right, int bottom) {
        mMargin.set(left, top, right, bottom);
    }

    public void clearCache() {
        mSpArrLineCount.clear();
    }

    /**
     * 计算viewGroup的可绘制区域(不包括viewGroup本身的padding)
     *
     * @param view           viewGroup
     * @param measureSpec    来自父控件的measureSpec信息
     * @param isComputeWidth 是否计算宽,若true则计算结果返回的是viewGroup的可绘制宽度,false返回viewGroup的可绘制高度
     * @return
     */
    private int computeViewSizeWithoutPadding(@NonNull View view, int measureSpec, boolean isComputeWidth) {
        int paddingStart, paddingEnd;
        if (isComputeWidth) {
            paddingStart = view.getPaddingLeft();
            paddingEnd = view.getPaddingRight();
        } else {
            paddingStart = view.getPaddingTop();
            paddingEnd = view.getPaddingBottom();
        }
        return MeasureSpec.getSize(measureSpec) - paddingStart - paddingEnd;
    }

    /**
     * 布局子view,最后两个参数其实可以不由外层提供,通过计算可以得到childView的实际宽高,但是由于外层也需要使用到该值,所以没必要计算两次得到,直接由外层计算后提供
     *
     * @param childIndex
     * @param child       childView
     * @param childMargin 统一的childView的margin参数,由用户设置
     * @param params      当前childView的marginLayoutParams参数,用于正确绘制子view的margin
     * @param drawStartX  childView绘制的X轴开始位置,由上层计算后提供
     * @param drawStartY  childView绘制的Y轴开始位置
     * @param childWidth  childView的宽
     * @param childHeight childView的高
     */
    private void layoutChild(int childIndex, @NonNull View child, @NonNull Rect childMargin, @Nullable MarginLayoutParams params, int drawStartX, int drawStartY, int childWidth, int childHeight) {
        //是否需要进行布局,此变量的控件由viewGroup的绘制生命周期决定
        //当进行onMeasure时,不进行布局绘制;此时只是计算viewGroup占用的大小
        //当进行onLayout时,绘制布局,此时需要布局子view并绘制显示出来

        int left, top, right, bottom;
        //计算childView绘制的开始位置,除去父控件的padding距离及统一的childView的margin
        left = drawStartX + childMargin.left;
        top = drawStartY + childMargin.top;
        //当childView本身的margin参数存在时,再叠加上本身指定的margin参数
        left += params.leftMargin;
        top += params.topMargin;
        right = left + childWidth;
        bottom = top + childHeight;

        Rect layoutCache = mSpArrLayoutCache.get(childIndex);
        layoutCache.set(left, top, right, bottom);
//        //布局childView
//        child.layout(left, top, right, bottom);
    }

    /**
     * 横向布局计算,当设置为横向布局时,计算viewGroup需要绘制的高度并对子view进行layout(是否真的layout取决于父控件的绘制周期)<br>
     * 此方法只会在横向布局时并且不进行grid均分列时调用(只针对 horizontal-flow-layout一种情况)
     *
     * @param parent        父控件
     * @param drawOffsetX   界面绘制的开始偏移位置(用于调整绘制的子view位置在居中位置)
     * @param drawOffsetY
     * @param parentWidth   父控件可绘制的宽度
     * @param parentPadding 父控件的padding参数,用于精确地将childView绘制到期望的位置
     * @param childMargin   childView统一的margin参数
     */
    @NonNull
    private Point horizontalMeasure(@NonNull ViewGroup parent, int drawOffsetX, int drawOffsetY, int parentWidth, @NonNull Rect parentPadding, @NonNull Rect childMargin) {
        //获取childView数量
        int childCount = parent.getChildCount();
        //设置竖直方向的偏移量,用于决定布局方式
        int layoutHeight = drawOffsetY, layoutWidth = parentPadding.left + drawOffsetX,
                childWidth = 0, childHeight = 0,
                viewExpectSize = 0, maxSize = 0,
                childDrawSize = 0, lineCount = 0;
        //存在childView时进行绘制
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                //获取childView的margin参数(对象一定存在,但是可能为0)
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                //获取childView计算需要的宽高大小
                childWidth = child.getMeasuredWidth();
                childHeight = child.getMeasuredHeight();
                childDrawSize = childWidth + params.leftMargin + params.rightMargin;
                //若横向布局当前的childView后超过父控件的宽度,则换行
                if (layoutWidth + childDrawSize > parentWidth) {
                    //换行回调,给出当前的行号,该行最后一项childView的index,该行的最大值
                    onChangedLine(lineCount, i - 1, maxSize);
                    lineCount++;
                    viewExpectSize = Math.max(viewExpectSize, layoutWidth);
                    layoutWidth = parentPadding.left + drawOffsetX;
                    //换行时需要加上当前的行中最大值及childView统一的margin参数
                    layoutHeight = layoutHeight + maxSize + childMargin.top + childMargin.bottom;
                    //当前行缓存的最大高度值置0
                    maxSize = 0;
                }
                //布局childView(可能会绘制也可能不会,取决定于全局变量 isLayout)
                layoutChild(i, child, childMargin, params, layoutWidth, layoutHeight + parentPadding.top, childWidth, childHeight);
                //更新绘制的后的起始X绘制位置
                layoutWidth = layoutWidth + childWidth + params.leftMargin + params.rightMargin + childMargin.left + childMargin.right;
                //保存当前行中childView的最大高度值
                maxSize = Math.max(maxSize, childHeight + params.topMargin + params.bottomMargin);
            }
            //补上最后一行,之前的换行都在行之间的(类似分隔线,不将最后一行计算在内)
            onChangedLine(lineCount, childCount - 1, maxSize);
        }
        //返回绘制后整个viewGroup需要的高度值(不包括padding,用于onMeasure过程的计算)
        layoutHeight += maxSize + childMargin.top + childMargin.bottom;
        mRecyclePoint.set(viewExpectSize, layoutHeight);
        return mRecyclePoint;
    }

    /**
     * 竖向布局计算,当设置为竖向布局时,计算viewGroup需要绘制的宽度并对子view进行layout(是否真的layout取决于父控件的绘制周期)<br>
     * 此方法只会在竖向布局时并且不进行grid均分列时调用(只针对 vertical-flow-layout一种情况)
     *
     * @param parent        父控件
     * @param drawOffsetX   界面绘制的开始偏移位置(用于调整绘制的子view位置在居中位置)
     * @param drawOffsetY
     * @param parentHeight  父控件可绘制的高度
     * @param parentPadding 父控件的padding参数,用于精确地将childView绘制到期望的位置
     * @param childMargin   childView统一的margin参数    @return 返回绘制后父控件需要的高度
     */
    @NonNull
    private Point verticalMeasure(@NonNull ViewGroup parent, int drawOffsetX, int drawOffsetY, int parentHeight, @NonNull Rect parentPadding, @NonNull Rect childMargin) {
        int childCount = parent.getChildCount();
        int layoutHeight = parentPadding.top + drawOffsetY, layoutWidth = drawOffsetX,
                childWidth = 0, childHeight = 0,
                viewExpectSize = 0, maxSize = 0,
                childDrawSize = 0, lineCount = 0;
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                childWidth = child.getMeasuredWidth();
                childHeight = child.getMeasuredHeight();
                childDrawSize = childHeight + params.topMargin + params.rightMargin;
                //若当前竖向布局后的childView的位置超过父控件的高度,则换列
                if (layoutHeight + childDrawSize > parentHeight) {
                    //换行回调,给出当前的行号,该行最后一项childView的index,该行的最大值
                    //此处为换列
                    onChangedLine(lineCount, i - 1, maxSize);
                    lineCount++;
                    viewExpectSize = Math.max(viewExpectSize, layoutHeight);
                    layoutHeight = parentPadding.top + drawOffsetY;
                    //计算下一列的开始绘制位置
                    layoutWidth = layoutWidth + maxSize + childMargin.left + childMargin.right;
                    maxSize = 0;
                }
                layoutChild(i, child, childMargin, params, layoutWidth + parentPadding.left, layoutHeight, childWidth, childHeight);
                layoutHeight = layoutHeight + childHeight + params.topMargin + params.bottomMargin + childMargin.top + childMargin.bottom;
                //计算当前view包括margin占用宽度与同一行的子view占用最大值比较
                maxSize = Math.max(maxSize, childWidth + params.leftMargin + params.rightMargin);
            }
            //补上最后一行,之前的换行都在行之间的(类似分隔线,不将最后一行计算在内)
            onChangedLine(lineCount, childCount - 1, maxSize);
        }
        //返回绘制后整个viewGroup需要的宽度值(用于onMeasure过程的计算)
        layoutWidth += maxSize + childMargin.left + childMargin.right - parentPadding.top;
        mRecyclePoint.set(layoutWidth, viewExpectSize);
        return mRecyclePoint;
    }

    /**
     * 计算分列/行后所有子view被均分成的行/列数量
     *
     * @param parent          父控件,用于获取所有子view的数量
     * @param viewCountInLine 均分的方向对应的允许放置的子view数量(即每行/列的item数量)
     * @return
     */
    private int computeLineCount(@NonNull ViewGroup parent, int viewCountInLine) {
        //总子view数量
        int childCount = parent.getChildCount();
        if (childCount > 0 && viewCountInLine > 0) {
            //向下取整得到行数
            int lineCount = childCount / viewCountInLine;
            //当所有行的子view数小于总子view数量时,说明需要再额外加1行
            if (lineCount * viewCountInLine < childCount) {
                lineCount++;
            }
            return lineCount;
        } else {
            return 0;
        }
    }

    /**
     * 计算某边(假设为宽)均分计算后的每个itemView占用的长度(则对应为宽度)
     *
     * @param parentWidth     父控件的宽
     * @param parentHeight    父控件的高
     * @param viewCountInSize 在该边允许填充的控件数量(一般来自{@link #setGridCount(int)}),类似列数或行数
     * @param isComputeWidth  是否对宽进行计算,true为对宽进行计算(以高为基准边,对应垂直分布);false为对高进行计算(以宽为基准边,对应水平分布)
     * @return
     */
    private int computeUnitSize(int parentWidth, int parentHeight, int viewCountInSize, boolean isComputeWidth) {
        if (viewCountInSize <= 0) {
            return 0;
        }
        if (isComputeWidth) {
            //分列,对宽进行均分计算
            return parentWidth / viewCountInSize;
        } else {
            //分行,对高进行均分计算
            return parentHeight / viewCountInSize;
        }
    }

    /**
     * grid方式的水平布局测量
     *
     * @param parent          父控件
     * @param drawOffset      竖直布局的偏移量,决定了总布局的方式
     * @param parentHeight    父控件的可绘制区域高度
     * @param unitSize        每列可绘制的最大高度
     * @param lineCount       布局总行数
     * @param viewCountInLine 每行中需要布局的列数
     * @param parentPadding   父控件的padding参数
     * @param childMargin     childView的margin参数
     * @return
     */
    private int spanHorizontalMeasure(@NonNull ViewGroup parent, int drawOffset, int parentHeight, int unitSize, int lineCount, int viewCountInLine, @NonNull Rect parentPadding, @NonNull Rect childMargin) {
        if (lineCount <= 0 || viewCountInLine <= 0 || unitSize <= 0) {
            return 0;
        } else {
            int childCount = parent.getChildCount();
            int layoutHeight = 0, maxSize = 0;
            for (int i = 0; i < lineCount; i++) {
                maxSize = forLoopChildInLineOnHorizontal(parentPadding, childMargin, layoutHeight + drawOffset, childCount, i, viewCountInLine, unitSize, mRelativeSize, parentHeight);
                layoutHeight = layoutHeight + maxSize + childMargin.top + childMargin.bottom;
                //换行回调,给出当前的行号,该行最后一项childView的index,该行的最大值
                onChangedLine(i, (i + 1) * viewCountInLine - 1, maxSize);
            }
            return layoutHeight;
        }
    }

    /**
     * grid方式的竖直布局测量
     *
     * @param parent          父控件
     * @param drawOffset      横向布局的偏移量,决定了总布局的方式
     * @param parentWidth     父控件的可绘制区域宽度
     * @param unitSize        每行可绘制的最大宽度
     * @param lineCount       布局总列数
     * @param viewCountInLine 每列中需要布局的行数
     * @param parentPadding   父控件的padding参数
     * @param childMargin     childView的margin参数
     * @return
     */
    private int spanVerticalMeasure(@NonNull ViewGroup parent, int drawOffset, int parentWidth, int unitSize, int lineCount, int viewCountInLine, @NonNull Rect parentPadding, @NonNull Rect childMargin) {
        if (lineCount <= 0 || viewCountInLine <= 0 || unitSize <= 0) {
            return 0;
        } else {
            int childCount = parent.getChildCount();
            int layoutWidth = 0, maxSize = 0;
            //按列进行布局
            for (int i = 0; i < lineCount; i++) {
                //每行进行独立的布局测量
                maxSize = forLoopChildInLineOnVertical(parentPadding, childMargin, layoutWidth + drawOffset, childCount, i, viewCountInLine, unitSize, mRelativeSize, parentWidth);
                //更新下一行需要布局的开始位置
                layoutWidth = layoutWidth + maxSize + childMargin.left + childMargin.right;
                //换行回调,给出当前的行号,该行最后一项childView的index,该行的最大值
                //此处为换列
                onChangedLine(i, (i + 1) * viewCountInLine - 1, maxSize);
            }
            //返回测量后得到的父控件需要的宽度
            return layoutWidth;
        }
    }


    /**
     * 循环遍历当前指定行(或者列)中的childView并进行布局,此方法针对水平均分布局方式(horizontal-grid-layout)
     *
     * @param parentPadding   父控件的padding参数
     * @param childMargin     childView统一的margin参数
     * @param layoutHeight    当前行开始布局的Y坐标位置
     * @param childCount      总childView数量
     * @param whichLine       当前列索引
     * @param viewCountInLine 当前行中需要布局的childView数量(这个值应该是固定的,与gridCount相同)
     * @param unitSize        当前行中每列的最大高度,由parentView的实际宽度与gridCount确定
     * @param parentHeight    父控件可绘制区域的宽度
     * @return
     */
    private int forLoopChildInLineOnHorizontal(@NonNull Rect parentPadding, @NonNull Rect childMargin, int layoutHeight, int childCount, int whichLine, int viewCountInLine, int unitSize, int relativeSize, int parentHeight) {
        //所有流程与竖直布局相同,只是布局方向不同而已
        int position = 0, edgeStartSize = 0,
                edgeEndSize = 0, maxSize = 0,
                childWidth = 0, childHeight = 0,
                childDrawSize = 0;
        layoutHeight += parentPadding.top;
        mRecycleRect.set(childMargin);
        for (int k = 0; k < viewCountInLine; k++) {
            position = whichLine * viewCountInLine + k;
            if (position >= childCount) {
                return maxSize;
            }
            View child = getChildAt(position);
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            //当前childView占用的空间大小(包括margin)
            childDrawSize = childWidth + params.leftMargin + params.rightMargin;
            if (childDrawSize >= unitSize) {
                setViewLayoutParams(child, unitSize, childHeight);
            } else {
                switch (mGridLayoutGravity) {
                    case Gravity.START:
                    case Gravity.LEFT:
                        edgeStartSize = 0;
                        edgeEndSize = 0;
                        break;
                    case Gravity.END:
                    case Gravity.RIGHT:
                        edgeStartSize = unitSize - childDrawSize;
                        edgeEndSize = 0;
                        break;
                    case Gravity.CENTER:
                        edgeStartSize = (unitSize - childDrawSize) / 2;
                        edgeEndSize = edgeStartSize;
                        break;
                }
                mRecycleRect.left += edgeStartSize;
                mRecycleRect.right += edgeEndSize;
            }

            if (relativeSize > 0) {
                childDrawSize = childHeight + params.topMargin + params.bottomMargin;
                if (childDrawSize < relativeSize) {
                    switch (mGridRelativeGravity) {
                        case Gravity.START:
                        case Gravity.TOP:
                            edgeStartSize = 0;
                            edgeEndSize = 0;
                            break;
                        case Gravity.END:
                        case Gravity.BOTTOM:
                            edgeStartSize = relativeSize - childHeight;
                            edgeEndSize = 0;
                            break;
                        case Gravity.CENTER:
                            edgeStartSize = (relativeSize - childHeight) / 2;
                            edgeEndSize = edgeStartSize;
                            break;
                    }
                    mRecycleRect.top += edgeStartSize;
                    mRecycleRect.bottom += edgeEndSize;
                }
                layoutChild(position, child, mRecycleRect, params, unitSize * k + parentPadding.left, layoutHeight, childWidth, childHeight);
                mRecycleRect.set(childMargin);
                maxSize = Math.max(maxSize, relativeSize);
                maxSize = Math.max(maxSize, childHeight + params.topMargin + params.bottomMargin);
            }
        }
        return maxSize;
    }

    /**
     * 循环遍历当前指定行(或者列)中的childView并进行布局,此方法针对竖直均分布局方式(vertical-grid-layout)
     *
     * @param parentPadding   父控件的padding参数
     * @param childMargin     childView统一的margin参数
     * @param layoutWidth     当前列开始布局的X坐标位置
     * @param childCount      总childView数量
     * @param whichLine       当前列索引
     * @param viewCountInLine 当前列中需要布局的childView数量(这个值应该是固定的,与gridCount相同)
     * @param unitSize        当前列中每行的最大宽度,由parentView的实际宽度与gridCount确定
     * @param parentWidth     父控件可绘制区域的宽度
     * @return
     */
    private int forLoopChildInLineOnVertical(@NonNull Rect parentPadding, @NonNull Rect childMargin, int layoutWidth, int childCount, int whichLine, int viewCountInLine, int unitSize, int relativeSize, int parentWidth) {
        int position = 0, edgeStartSize = 0,
                edgeEndSize = 0, maxSize = 0,
                childWidth = 0, childHeight = 0,
                //用于存放当前绘制childView占用的空间(包括margin)
                childDrawSize = 0;
        //开始绘制的位置需要排队parent的padding
        layoutWidth += parentPadding.left;
        mRecycleRect.set(childMargin);
        //遍历并布局当前行的所有childView
        for (int k = 0; k < viewCountInLine; k++) {
            //计算当前行的每一个childView在parent中的实际position
            position = whichLine * viewCountInLine + k;
            //当position已经达到parent的childView总数时,返回数据
            if (position >= childCount) {
                return maxSize;
            }
            View child = getChildAt(position);
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            //计算childView占用的空间,包括margin
            childDrawSize = childHeight + params.topMargin + params.bottomMargin;
            //当childView占用的空间大于单元格的大小时,必须设置childView的大小为单元格大小(不允许超过他)
            if (childDrawSize >= unitSize) {
                setViewLayoutParams(child, childWidth, unitSize);
            } else {
                //判断当前childView的布局方式
                switch (mGridLayoutGravity) {
                    case Gravity.START:
                    case Gravity.TOP:
                        edgeStartSize = 0;
                        edgeEndSize = 0;
                        break;
                    case Gravity.END:
                    case Gravity.BOTTOM:
                        edgeStartSize = unitSize - childDrawSize;
                        edgeEndSize = 0;
                        break;
                    case Gravity.CENTER:
                    default:
                        edgeStartSize = (unitSize - childDrawSize) / 2;
                        edgeEndSize = edgeStartSize;
                        break;
                }
                mRecycleRect.top += edgeStartSize;
                mRecycleRect.bottom += edgeEndSize;
            }

            if (relativeSize > 0) {
                childDrawSize = childWidth + params.leftMargin + params.rightMargin;
                if (childDrawSize < relativeSize) {
                    switch (mGridRelativeGravity) {
                        case Gravity.START:
                        case Gravity.TOP:
                            edgeStartSize = 0;
                            edgeEndSize = 0;
                            break;
                        case Gravity.END:
                        case Gravity.BOTTOM:
                            edgeStartSize = relativeSize - childWidth;
                            edgeEndSize = 0;
                            break;
                        case Gravity.CENTER:
                            edgeStartSize = (relativeSize - childWidth) / 2;
                            edgeEndSize = edgeStartSize;
                            break;
                    }
                    mRecycleRect.left += edgeStartSize;
                    mRecycleRect.right += edgeEndSize;
                }
            }
            //布局childView
            layoutChild(position, child, mRecycleRect, params, layoutWidth, unitSize * k + parentPadding.top, childWidth, childHeight);
            //保存当前行中布局的childView的最大宽度(用于切换下一列时使用)
            maxSize = Math.max(maxSize, relativeSize);
            maxSize = Math.max(maxSize, childWidth + params.leftMargin + params.rightMargin);
            //重置临时变量
            mRecycleRect.set(childMargin);
        }
        return maxSize;
    }


    //设置view的layoutParams
    private void setViewLayoutParams(View view, int newWidth, int newHeight) {
        if (view != null) {
            LayoutParams params = view.getLayoutParams();
            params.width = newWidth;
            params.height = newHeight;
            view.setLayoutParams(params);
        }
    }

    //设置view的layoutParams
    private void setViewLayoutParams(View view, int newWidth, int newHeight, @NonNull Point oldParams) {
        if (view != null) {
            LayoutParams params = view.getLayoutParams();
            oldParams.set(params.width, params.height);
            params.width = newWidth;
            params.height = newHeight;
            view.setLayoutParams(params);
        }
    }

    /**
     * 判断当前的childView是在单击的触发范围内
     *
     * @param child
     * @param clickX 单击点的X坐标
     * @param clickY 单击点的Y坐标
     * @return
     */
    private boolean checkIfChildViewClick(View child, float clickX, float clickY) {
        if (clickX < child.getLeft() || clickX > child.getRight() ||
                clickY < child.getTop() || clickY > child.getBottom()) {
            return false;
        } else {
            return true;
        }
    }

    private void initCacheContainer(int childCount) {
        if (mSpArrLayoutCache == null) {
            mSpArrLayoutCache = new SparseArray<>(childCount * 2);
        }
        if (mSpArrLineCount == null) {
            mSpArrLineCount = new SparseArray<>();
        }

        //layout时使用的布局界面
        int oldCount = mSpArrLayoutCache.size();
        oldCount = childCount - oldCount;
        if (oldCount > 0) {
            //缓存的view数量不够,添加缓存
            for (int i = 0; i < oldCount; i++) {
                mSpArrLayoutCache.put(childCount - i - 1, new Rect());
            }
        } else if (oldCount < 0) {
            oldCount *= -1;
            //缓存的view数量超出了,删除缓存
            for (int i = 0; i < oldCount; i++) {
                mSpArrLayoutCache.remove(childCount - i - 1);
            }
        }
    }

    /**
     * 换行/列时回调
     *
     * @param currentLine          当前行,从0开始
     * @param lastChildIndexInLine 当前行最后一个childView的index
     * @param lineHeight           当前行高(由该行中最大的childView高度或宽度确定)
     */
    protected void onChangedLine(int currentLine, int lastChildIndexInLine, int lineHeight) {
        Point linePoint = mSpArrLineCount.get(currentLine);
        if (linePoint == null) {
            linePoint = new Point();
        }
        linePoint.set(lastChildIndexInLine, lineHeight);
        mSpArrLineCount.put(currentLine, linePoint);
    }

    /**
     * 重写的子view大小计算方法
     *
     * @param child
     * @param parentWidthMeasureSpec
     * @param parentHeightMeasureSpec
     */
    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        //此方法与默认的计算方法最大的区别是判断并添加了子view的margin参数进行计算;
        //若不添加子view的margin参数进行计算,子view计算得到的绘制宽高都是会超过预期的

        //获取子view的布局参数
        final LayoutParams lp = child.getLayoutParams();
        int paddingWidth, paddingHeight;
        //获取父控件的padding参数
        paddingWidth = this.getPaddingLeft() + this.getPaddingRight();
        paddingHeight = this.getPaddingTop() + this.getPaddingBottom();
        //当子view的布局参数包含了margin
        if (lp instanceof MarginLayoutParams) {
            MarginLayoutParams params = (MarginLayoutParams) lp;
            //将子view要求的margin参数添加到padding的空白部分中
            paddingWidth += params.leftMargin + params.rightMargin;
            paddingHeight += params.topMargin + params.bottomMargin;
        }
        //对子view进行布局及计算
        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                paddingWidth, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                paddingHeight, lp.height);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //是否对childView进行layout布局
        mIsLayout = false;
        //childView的数量
        int childCount = this.getChildCount();
        int parentMeasureWidthSpec = widthMeasureSpec;
        int parentMeasureHeightSpec = heightMeasureSpec;
        final LayoutParams lp = this.getLayoutParams();
        widthMeasureSpec = getChildMeasureSpec(parentMeasureWidthSpec,
                this.getPaddingLeft() + this.getPaddingRight(), lp.width);
        heightMeasureSpec = getChildMeasureSpec(parentMeasureHeightSpec,
                this.getPaddingTop() + this.getPaddingBottom(), lp.height);
        //计算父控件实际可绘制的区域(包括去除了父控件的padding)
        //计算绘制宽
        int parentWidth = computeViewSizeWithoutPadding(this, widthMeasureSpec, true);
        //计算绘制高
        int parentHeight = computeViewSizeWithoutPadding(this, heightMeasureSpec, false);

        //保存父控件的padding数据
        mPadding.set(this.getPaddingLeft(), this.getPaddingTop(), this.getPaddingRight(), this.getPaddingBottom());

        //测量每个childView需要绘制的大小
        if (childCount > 0) {
            //初始化缓存layout的数据
            initCacheContainer(childCount);
            //当需要进行均分操作时
            if (mGridCount > 0) {
                int unitSize = 0, requestSize = 0;
                int childWidthSpec, childHeightSpec;
                //横向布局
                if (!mIsVertical) {
                    //计算一个均分的cell的边长大小
                    unitSize = computeUnitSize(parentWidth, 0, mGridCount, true);
                    requestSize = unitSize;
                    //实际绘制的区域大小(除去childView统一的margin参数)
                    unitSize = unitSize - mMargin.left - mMargin.right;
                    //计算指定的每一个childView宽度大小
                    childWidthSpec = MeasureSpec.makeMeasureSpec(unitSize, MeasureSpec.EXACTLY);
                    //计算并设置每一个childView的大小
                    if (mWeightRate > 0) {
                        //按比例显示界面,高度是由宽度决定的
                        requestSize = (int) (requestSize * mWeightRate);
                        mRelativeSize = requestSize - mMargin.top - mMargin.bottom;
                        childHeightSpec = MeasureSpec.makeMeasureSpec(mRelativeSize, MeasureSpec.EXACTLY);
                        for (int i = 0; i < childCount; i++) {
                            View child = this.getChildAt(i);
                            //设置chlidView的大小
//                            child.measure(childWidthSpec, childHeightSpec);
                            measureChild(child, childWidthSpec, childHeightSpec);
                        }
                    } else {
                        //不按比例显示界面,view大小由自身确定
                        for (int i = 0; i < childCount; i++) {
                            View child = this.getChildAt(i);
                            //分别计算每个childView的高度大小
//                            childHeightSpec = MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY);
                            //实际上是使用了父控件本身的高度参数
                            childHeightSpec = heightMeasureSpec;
                            //设置childView的大小
//                            child.measure(childWidthSpec, childHeightSpec);
                            measureChild(child, childWidthSpec, childHeightSpec);
                        }
                    }
                } else {
                    //垂直布局
                    unitSize = computeUnitSize(0, parentHeight, mGridCount, false);
                    requestSize = unitSize;
                    unitSize = unitSize - mMargin.top - mMargin.bottom;
                    //计算指定的每一个childView高度大小
                    childHeightSpec = MeasureSpec.makeMeasureSpec(unitSize, MeasureSpec.EXACTLY);
                    if (mWeightRate > 0) {
                        requestSize = (int) (requestSize * mWeightRate);
                        mRelativeSize = requestSize - mMargin.left - mMargin.right;
                        childWidthSpec = MeasureSpec.makeMeasureSpec(mRelativeSize, MeasureSpec.EXACTLY);
                        for (int i = 0; i < childCount; i++) {
                            View child = this.getChildAt(i);
//                            child.measure(childWidthSpec, childHeightSpec);
                            measureChild(child, childWidthSpec, childHeightSpec);
                        }
                    } else {
                        //计算并设置每一个childView的大小
                        for (int i = 0; i < childCount; i++) {
                            View child = this.getChildAt(i);
//                            childWidthSpec = MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), MeasureSpec.EXACTLY);
                            //使用父控件本身的宽度参数
                            childWidthSpec = widthMeasureSpec;
//                            child.measure(childWidthSpec, childHeightSpec);
                            measureChild(child, childWidthSpec, childHeightSpec);

                        }
                    }
                }
            } else {
                //普通的流式布局,全部交给控件默认的计算操作去完成
                //计算childView的大小
                measureChildren(widthMeasureSpec, heightMeasureSpec);
            }
        }

        //测量viewGroup本身,实际上已经包含了view的布局处理
        if (childCount > 0) {
            int drawOffsetX = 0, drawOffsetY = 0;
            //判断界面总的布局方式,水平方向的布局
            switch (mHorizontalGravity) {
                case Gravity.START:
                case Gravity.LEFT:
                    drawOffsetX = 0;
                    break;
                case Gravity.END:
                case Gravity.RIGHT:
                    drawOffsetX = mInternalOffset.x * 2;
                    break;
                case Gravity.CENTER:
                default:
                    drawOffsetX = mInternalOffset.x;
                    break;
            }
            //垂直方向的布局
            switch (mVerticalGravity) {
                case Gravity.START:
                case Gravity.TOP:
                    drawOffsetY = 0;
                    break;
                case Gravity.END:
                case Gravity.BOTTOM:
                    drawOffsetY = mInternalOffset.y * 2;
                    break;
                case Gravity.CENTER:
                default:
                    drawOffsetY = mInternalOffset.y;
                    break;
            }


            if (mGridCount <= 0) {
                if (!mIsVertical) {
                    //横向布局测量父控件需要的高度
                    mRecyclePoint = horizontalMeasure(this, drawOffsetX, drawOffsetY, parentWidth, mPadding, mMargin);
                    //当要求重新设置未确定边大小时/或者当未确定边大小小于当前测量值时
                    if (mIsResize || parentHeight <= mRecyclePoint.y) {
                        //重新设置未确定边大小为测量值
                        parentHeight = mRecyclePoint.y;
                    } else if (parentHeight > mRecyclePoint.y) {
                        //若不需要重新设置,保存未确定边的原始大小与测量值的差
                        mInternalOffset.y = (parentHeight - mRecyclePoint.y) / 2;
                    }
                    //当已确定边大于当前测量值时
                    if (parentWidth > mRecyclePoint.x) {
                        mInternalOffset.x = (parentWidth - mRecyclePoint.x) / 2;
                    }
                } else {
                    //竖向布局测量父控件需要的宽度
                    mRecyclePoint = verticalMeasure(this, drawOffsetX, drawOffsetY, parentHeight, mPadding, mMargin);
                    if (mIsResize || parentWidth <= mRecyclePoint.x) {
                        //重新设置未确定边大小为测量值
                        parentWidth = mRecyclePoint.x;
                    } else if (parentWidth > mRecyclePoint.x) {
                        //若不需要重新设置,保存未确定边原始大小与测量值的差
                        mInternalOffset.x = (parentWidth - mRecyclePoint.x) / 2;
                    }
                    //当已确定边大于当前测量值时
                    if (parentHeight > mRecyclePoint.y) {
                        mInternalOffset.y = (parentHeight - mRecyclePoint.y) / 2;
                    }
                }
            } else {
                int unitSize, lineCount = 0, resize = 0;
                //计算均分后childView需要布局的行(或列)数
                lineCount = computeLineCount(this, mGridCount);
                if (!mIsVertical) {
                    //横向布局及计算父控件的绘制高度
                    unitSize = computeUnitSize(parentWidth, 0, mGridCount, true);
                    //测量后需要的界面高
                    resize = spanHorizontalMeasure(this, drawOffsetY, parentHeight, unitSize, lineCount, mGridCount, mPadding, mMargin);
                    //若当前的界面需要重新设置或者是当前界面的高度小于测量后的高度
                    if (mIsResize || parentHeight <= resize) {
                        //重新设置高度
                        parentHeight = resize;
                    } else {
                        //否则保存原始界面高度超过的测量后高度的部分(用于布局方式的切换)
                        mInternalOffset.y = (parentHeight - resize) / 2;
                    }

                    //不存在对确定边大小的与测量值的缓存比较,因为均分布局必须是确定边的大小已经固定为基准进行的布局
                } else {
                    //竖向布局及计算父控件的绘制宽度
                    unitSize = computeUnitSize(0, parentHeight, mGridCount, false);
                    //测量后需要的界面宽度
                    resize = spanVerticalMeasure(this, drawOffsetX, parentWidth, unitSize, lineCount, mGridCount, mPadding, mMargin);
                    //若当前的界面需要重新设置或者是当前界面的宽度小于测量后的宽度
                    if (mIsResize || parentWidth <= resize) {
                        //重新设置宽度
                        parentWidth = resize;
                    } else {
                        //否则保存原始界面宽度超过的测量后宽度的部分
                        mInternalOffset.x = (parentWidth - resize) / 2;
                    }
                }
            }
        }

        //以上测量时使用的都是父控件的实际绘制区域大小,所以最终父控件占用的绘制区域需要加上自己的padding参数
        parentHeight += mPadding.top + mPadding.bottom;
        parentWidth += mPadding.left + mPadding.right;
        //设置父控件测量后的大小
        this.setMeasuredDimension(parentWidth, parentHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = this.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Rect layoutCache = mSpArrLayoutCache.get(i);
            View child = this.getChildAt(i);
            child.layout(layoutCache.left, layoutCache.top, layoutCache.right, layoutCache.bottom);
        }

//        //整个viewGroup是否需要进行布局操作
//        mIsLayout = true;
//
//        int childCount = this.getChildCount();
//        int parentWidth = this.getMeasuredWidth();
//        int parentHeight = this.getMeasuredHeight();
//        int drawOffsetX = 0, drawOffsetY = 0;
//        //布局childView
//        if (childCount > 0) {
//            //判断界面总的布局方式,水平方向的布局
//            switch (mHorizontalGravity) {
//                case Gravity.START:
//                case Gravity.LEFT:
//                    drawOffsetX = 0;
//                    break;
//                case Gravity.END:
//                case Gravity.RIGHT:
//                    drawOffsetX = mInternalOffset.x * 2;
//                    break;
//                case Gravity.CENTER:
//                default:
//                    drawOffsetX = mInternalOffset.x;
//                    break;
//            }
//            //垂直方向的布局
//            switch (mVerticalGravity) {
//                case Gravity.START:
//                case Gravity.TOP:
//                    drawOffsetY = 0;
//                    break;
//                case Gravity.END:
//                case Gravity.BOTTOM:
//                    drawOffsetY = mInternalOffset.y * 2;
//                    break;
//                case Gravity.CENTER:
//                default:
//                    drawOffsetY = mInternalOffset.y;
//                    break;
//            }
//
//            //若grid小于0,则说明是流式布局
//            if (mGridCount <= 0) {
//                if (!mIsVertical) {
//                    //水平方向
//                    //根据布局的方式绘制并布局
//                    horizontalMeasure(this, drawOffsetX, drawOffsetY, parentWidth, mPadding, mMargin);
//                } else {
//                    //垂直方向
//                    //根据布局的方式绘制并布局
//                    verticalMeasure(this, drawOffsetX, drawOffsetY, parentHeight, mPadding, mMargin);
//                }
//            } else {
//                //计算分成的行/列数
//                int unitSize, lineCount = 0;
//                lineCount = computeLineCount(this, mGridCount);
//                if (!mIsVertical) {
//                    //水平方向
//                    //计算单元格大小
//                    unitSize = computeUnitSize(parentWidth, 0, mGridCount, true);
//                    //根据布局的方式绘制并布局
//                    spanHorizontalMeasure(this, drawOffsetY, parentHeight, unitSize, lineCount, mGridCount, mPadding, mMargin);
//                } else {
//                    unitSize = computeUnitSize(0, parentHeight, mGridCount, false);
//                    spanVerticalMeasure(this, drawOffsetX, parentWidth, unitSize, lineCount, mGridCount, mPadding, mMargin);
//                }
//            }
//        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //只有在抬起时才会处理事件,否则按下及抬起会处理两次事件.
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isEnabled()) {
                // A disabled view that is clickable still consumes the touch
                // events, it just doesn't respond to them.
                return isClickable() || isLongClickable();
            }

            float x = event.getX();
            float y = event.getY();
            int childCount = getChildCount();

            //遍历并查找当前被单击位置的view是哪一个
            if (childCount > 0 && mItemClickListener != null) {
                if (!mIsVertical) {
                    for (int i = 0; i < childCount; i++) {
                        View child = getChildAt(i);
                        if (checkIfChildViewClick(child, x, y)) {
                            //查找到单击的view回调响应并结束遍历
                            mItemClickListener.onItemClick(child, i);
                            return true;
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 子view被单击选中事件监听
     */
    public interface OnItemClickListener {
        /**
         * itemView被选中事件
         *
         * @param childView 选中的view
         * @param position  该view在父控件中的位置
         */
        public void onItemClick(View childView, int position);
    }
}
