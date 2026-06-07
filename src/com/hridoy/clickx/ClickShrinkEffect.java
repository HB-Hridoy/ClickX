package com.hridoy.clickx;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import java.lang.ref.WeakReference;

public class ClickShrinkEffect {

    private static final float SHRINK_VALUE = 0.93f;
    private static final long DEFAULT_DURATION = 150L;

    private final WeakReference<View> weakTarget;
    private final long duration;

    // Change constructor to just hold the view reference
    public ClickShrinkEffect(View targetView, long duration) {
        this.weakTarget = new WeakReference<>(targetView);
        this.duration = duration;
    }

    public ClickShrinkEffect(View targetView) {
        this(targetView, DEFAULT_DURATION);
    }

    public void shrink() {
        View target = weakTarget.get();
        if (target == null) return;

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, target.getScaleX(), SHRINK_VALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, target.getScaleY(), SHRINK_VALUE);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, scaleX, scaleY);
        animator.setDuration(duration);
        animator.start();
    }

    public void grow() {
        View target = weakTarget.get();
        if (target == null) return;

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, target.getScaleX(), 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, target.getScaleY(), 1f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, scaleX, scaleY);
        animator.setDuration(duration);
        animator.start();
    }
}