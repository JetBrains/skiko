package org.jetbrains.skiko;

import android.view.GestureDetector;
import android.view.MotionEvent;

import org.jetbrains.annotations.Nullable;

/**
 * This workaround needs to support both Android SDK 33 (or lower) and SDK 34 (or higher)
 */
@SuppressWarnings("NullableProblems")
public abstract class WorkaroundSimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override
    final public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
        return onScrollWorkaround(event1, event2, distanceX, distanceY);
    }

    @Override
    final public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        return onFlingWorkaround(event1, event2, velocityX, velocityY);
    }

    /**
     * This workaround helps to support @Nullable and @NonNull first argument
     */
    abstract boolean onScrollWorkaround(@Nullable MotionEvent event1, MotionEvent event2, float distanceX, float distanceY);

    /**
     * This workaround helps to support @Nullable and @NonNull first argument
     */
    abstract boolean onFlingWorkaround(@Nullable MotionEvent event1, MotionEvent event2, float distanceX, float distanceY);

}
