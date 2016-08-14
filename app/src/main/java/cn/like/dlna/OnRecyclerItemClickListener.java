package cn.like.dlna;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by like on 16/8/3.
 */
public abstract class OnRecyclerItemClickListener implements RecyclerView.OnItemTouchListener{

    private GestureDetectorCompat gestureDetectorCompat;
    private RecyclerView recyclerView;

    public OnRecyclerItemClickListener(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        gestureDetectorCompat = new GestureDetectorCompat(recyclerView.getContext(),
                new ItemTouchHelperGestureListener());
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetectorCompat.onTouchEvent(e);
        return false;
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetectorCompat.onTouchEvent(e);
    }

    public abstract void onItemClick(RecyclerView.ViewHolder vh, int position, int type);

    public abstract void onItemLongClick(RecyclerView.ViewHolder vh, int position, int type);

    private class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (null != child) {
                int position = recyclerView.getChildAdapterPosition(child);
                RecyclerView.ViewHolder vh = recyclerView.getChildViewHolder(child);
                int type = recyclerView.getAdapter().getItemViewType(position);
                onItemLongClick(vh, position, type);
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (null != child) {
                int position = recyclerView.getChildAdapterPosition(child);
                RecyclerView.ViewHolder vh = recyclerView.getChildViewHolder(child);
                int type = recyclerView.getAdapter().getItemViewType(position);
                onItemClick(vh, position, type);
            }
            return false;
        }
    }
}
