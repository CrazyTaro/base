package com.taro.base.helper;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Created by taro on 2017/3/10.
 */

public class MultiChoiceHelper {
    private boolean mIsSelectedMode = false;
    protected TreeSet<Integer> mSelectedIndexSet = null;

    private OnMultiItemChosenListener mChosenListener = null;

    public MultiChoiceHelper() {
        Comparator<Integer> comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (o1.equals(o2)) {
                    return 0;
                } else {
                    return o1 > o2 ? 1 : -1;
                }
            }
        };
        mSelectedIndexSet = new TreeSet<>(comparator);
    }

    public void setOnMultiItemChosenListener(OnMultiItemChosenListener listener) {
        mChosenListener = listener;
    }

    /**
     * 设置当前是否选择模式
     *
     * @param isChosenMode
     */
    public void setIsChosenMode(boolean isChosenMode) {
        if (mIsSelectedMode != isChosenMode) {
            mIsSelectedMode = isChosenMode;
        }
    }

    /**
     * 获取当前是否选择模式
     *
     * @return
     */
    public boolean isChosenMode() {
        return mIsSelectedMode;
    }

    /**
     * 改变当前项的选中状态
     *
     * @param position 当前项位置
     * @return 返回此item之前的选中状态
     */
    private boolean changedIndexChosenStatus(int position) {
        boolean isSelected = isIndexChosen(position);
        isSelected = !isSelected;
        if (isSelected) {
            chooseIndex(position);
        } else {
            unchosenIndex(position);
        }
        return isSelected;
    }

    /**
     * 全选
     */
    public void chooseAll(int totalCount) {
        for (int i = 0; i < totalCount; i++) {
            mSelectedIndexSet.add(i);
        }
    }

    /**
     * 取消全选
     */
    public void unchosenAll() {
        mSelectedIndexSet.clear();
    }

    /**
     * 选中某项
     *
     * @param position
     */
    public void chooseIndex(int position) {
        mSelectedIndexSet.add(position);
    }

    /**
     * 取消某项的选中状态
     *
     * @param position
     */
    public void unchosenIndex(int position) {
        mSelectedIndexSet.remove(position);
    }

    /**
     * 获取当前的所有选中项
     *
     * @return
     */
    public TreeSet<Integer> getChosenIndexs() {
        return mSelectedIndexSet;
    }


    /**
     * 获取选中的item数量
     *
     * @return
     */
    public int getChosenCount() {
        return mSelectedIndexSet.size();
    }

    /**
     * 判断当前位置的项目是否被选中
     *
     * @param position
     * @return
     */
    public boolean isIndexChosen(int position) {
        return mSelectedIndexSet.contains(position);
    }


    /**
     * 某项进行点击操作
     *
     * @param position 点击位置
     * @return
     */
    public boolean onItemClick(int position, Object... params) {
        //是否需要更新选中状态
        boolean isChangedStatus = false;
        if (mChosenListener != null) {
            boolean isChosen = isIndexChosen(position);
            int count = getChosenCount();
            isChangedStatus = mChosenListener.interceptChosenStatusOnClick(position, isChosen, count, isChosen ? count - 1 : count + 1, params);
        }
        if (mIsSelectedMode && !isChangedStatus) {
            changedIndexChosenStatus(position);
            return true;
        } else {
            return false;
        }
    }

    public interface OnMultiItemChosenListener {
        public boolean interceptChosenStatusOnClick(int position, boolean isChosenBefore, int chosenCountBefore, int chosenCountAfter, Object... params);
    }
}
