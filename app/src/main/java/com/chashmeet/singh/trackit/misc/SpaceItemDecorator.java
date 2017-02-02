package com.chashmeet.singh.trackit.misc;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class SpaceItemDecorator extends RecyclerView.ItemDecoration
{
    private final int mSpace;
    int mOrientation = -1;
    private boolean mShowFirstDivider = false;
    private boolean mShowLastDivider = false;

    public SpaceItemDecorator(Context context, AttributeSet attrs) {
        mSpace = 0;
    }
    public SpaceItemDecorator(Context context, AttributeSet attrs, boolean showFirstDivider,
                               boolean showLastDivider) {
        this(context, attrs);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
    }

    public SpaceItemDecorator(int spaceInPx)
    {
        mSpace = spaceInPx;
    }
    public SpaceItemDecorator(int spaceInPx, boolean showFirstDivider,
                               boolean showLastDivider)
    {
        this(spaceInPx);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
    }

    public SpaceItemDecorator(Context ctx, int resId)
    {
        mSpace = ctx.getResources().getDimensionPixelSize(resId);
    }
    public SpaceItemDecorator(Context ctx, int resId, boolean showFirstDivider,
                               boolean showLastDivider)
    {
        this(ctx, resId);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state)
    {
        if (mSpace == 0) {
            return;
        }

        if (mOrientation == -1)
            getOrientation(parent);

        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION || (position == 0 && !mShowFirstDivider)) {
            return;
        }

        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.top = mSpace;
            if (mShowLastDivider && position == (state.getItemCount() - 1)) {
                outRect.bottom = outRect.top;
            }
        } else {
            outRect.left = mSpace;
            if (mShowLastDivider && position == (state.getItemCount() - 1)) {
                outRect.right = outRect.left;
            }
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (mOrientation == -1) {
            if (parent.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
                mOrientation = layoutManager.getOrientation();
            } else {
                throw new IllegalStateException(
                        "DividerItemDecoration can only be used with a LinearLayoutManager.");
            }
        }
        return mOrientation;
    }
}