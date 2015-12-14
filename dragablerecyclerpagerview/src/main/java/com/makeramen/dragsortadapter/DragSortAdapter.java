/*
 * Copyright (C) 2015 Vincent Mi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.makeramen.dragsortadapter;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import cat.helm.OverlapingObservableViewGroup.OverlappingObservableViewGroup;
import cat.helm.draggablerecyclerpagerview.DraggableRecyclerPagerView;


public abstract class DragSortAdapter<VH extends DragSortAdapter.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private static final String TAG = DragSortAdapter.class.getSimpleName();

    private final int SCROLL_AMOUNT = (int) (2 * Resources.getSystem().getDisplayMetrics().density);

    private final DragManager dragManager;
    private int scrollState = RecyclerView.SCROLL_STATE_IDLE;
    private final PointF lastTouchPoint = new PointF(); // used to create ShadowBuilder
    protected boolean canMove = true;
    private Handler handler;

    public DragSortAdapter(RecyclerView recyclerView) {
        setHasStableIds(true);
        dragManager = new DragManager(null, recyclerView, this);
        recyclerView.setOnDragListener(dragManager);

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                lastTouchPoint.set(e.getX(), e.getY());
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        handleScroll(recyclerView);
                    }
                });
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                scrollState = newState;
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        //   handleScroll(recyclerView);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        break;
                }
            }
        });

        handler = new Handler();

    }

    public DragSortAdapter(OverlappingObservableViewGroup viewGroup, RecyclerView recyclerView) {
        setHasStableIds(true);
        dragManager = new DragManager(viewGroup, recyclerView, this);
        recyclerView.setOnDragListener(dragManager);

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                lastTouchPoint.set(e.getX(), e.getY());
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        handleScroll(recyclerView);
                    }
                });
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                scrollState = newState;
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        //   handleScroll(recyclerView);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        break;
                }
            }
        });

        handler = new Handler();

    }

    /**
     * This should be reasonably performant as it gets called a lot on the UI thread.
     *
     * @return position of the item with the given id
     */
    public abstract int getPositionForId(long id);

    /**
     * This is called during the dragging event, the actual positions of the views and data need to
     * change in the adapter for the drag animations to look correct.
     *
     * @return true if the position can be moved from fromPosition to toPosition
     */
    public abstract boolean move(int fromPosition, int toPosition);

    /**
     * Called after a drop event, override to save changes after drop event.
     */
    public void onDrop() {
    }

    /**
     * You probably want to use this to set the currently dragging item to blank while it's being
     * dragged
     *
     * @return the id of the item currently being dragged or {@code RecyclerView.NO_ID } if not being
     * dragged
     */
    public long getDraggingId() {
        return dragManager.getDraggingId();
    }

    public PointF getLastTouchPoint() {
        return new PointF(lastTouchPoint.x, lastTouchPoint.y);
    }

    private void handleScroll(RecyclerView recyclerView) {
        if (scrollState != RecyclerView.SCROLL_STATE_IDLE) {
            return;
        }
        DragInfo lastDragInfo = dragManager.getLastDragInfo();
        if (lastDragInfo != null) {
            // handleDragScroll(recyclerView, lastDragInfo);
        }
    }

    void handleDragScroll(final RecyclerView rv, final DragInfo dragInfo) {
        Log.e("DragSortAdapter", "handleDragScroll" + "runing");
        if (dragInfo == null) return;
        if (rv.getLayoutManager().canScrollHorizontally()) {
            if (dragInfo.shouldScrollLeft()) {
                ((DraggableRecyclerPagerView) rv).scrollPreviousPage();

                dragManager.clearNextMove();
            } else if (dragInfo.shouldScrollRight(rv.getWidth())) {
                ((DraggableRecyclerPagerView) rv).scrollNextPage();
                dragManager.clearNextMove();
            }
        }
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public static abstract class ViewHolder extends RecyclerView.ViewHolder {

        final DragSortAdapter<?> adapter;

        public ViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
            super(itemView);
            this.adapter = dragSortAdapter;
        }


        public final void startDrag() {
            PointF touchPoint = adapter.getLastTouchPoint();
            int x = (int) (touchPoint.x - itemView.getX());
            int y = (int) (touchPoint.y - itemView.getY());

            Rect r = new Rect();
            itemView.getDrawingRect(r);

            startDrag(getShadowBuilder(itemView, new Point(r.centerX(), r.centerY())));
        }

        public View.DragShadowBuilder getShadowBuilder(View itemView, Point touchPoint) {
            return new DragSortShadowBuilder(itemView, touchPoint);
        }

        public final void startDrag(View.DragShadowBuilder dragShadowBuilder) {
            Point shadowSize = new Point();
            Point shadowTouchPoint = new Point();
            dragShadowBuilder.onProvideShadowMetrics(shadowSize, shadowTouchPoint);

            itemView.startDrag(null, dragShadowBuilder,
                    new DragInfo(getItemId(), shadowSize, shadowTouchPoint, adapter.getLastTouchPoint()), 0);

            adapter.notifyItemChanged(getAdapterPosition());

        }
    }
}