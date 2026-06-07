package com.hridoy.clickx;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

public class ClickShrinkEffect {

    private static final float SHRINK_VALUE = 0.93f;
    private static final long DEFAULT_DURATION = 150L;

    private final View triggerView;
    private final WeakReference<View> weakTarget;
    private final long duration;

    private ClickShrinkEffect(View triggerView, View targetView, long duration) {
        this.triggerView = triggerView;
        this.weakTarget = new WeakReference<>(targetView);
        this.duration = duration;
        setupTouchListener();
    }

    public static ClickShrinkEffect applySelfShrink(View view) {
        return new ClickShrinkEffect(view, view, DEFAULT_DURATION);
    }

    public static ClickShrinkEffect applySelfShrink(View view, long duration) {
        return new ClickShrinkEffect(view, view, duration);
    }

    public static ClickShrinkEffect applyTargetShrink(View triggerView, View targetView) {
        return new ClickShrinkEffect(triggerView, targetView, DEFAULT_DURATION);
    }

    public static ClickShrinkEffect applyTargetShrink(View triggerView, View targetView, long duration) {
        return new ClickShrinkEffect(triggerView, targetView, duration);
    }

    private void setupTouchListener() {
        if (!triggerView.hasOnClickListeners()) {
            triggerView.setOnClickListener(v -> {});
        }

        triggerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Animator shrink = buildShrinkAnimator();
                if (shrink != null) shrink.start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                Animator grow = buildGrowAnimator();
                if (grow != null) grow.start();
            }
            return false;
        });
    }

    private Animator buildShrinkAnimator() {
        View target = weakTarget.get();
        if (target == null) return null;

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, SHRINK_VALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, SHRINK_VALUE);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, scaleX, scaleY);
        animator.setDuration(duration);
        return animator;
    }

    private Animator buildGrowAnimator() {
        View target = weakTarget.get();
        if (target == null) return null;

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, SHRINK_VALUE, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, SHRINK_VALUE, 1f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, scaleX, scaleY);
        animator.setDuration(duration);
        return animator;
    }

    // Convenience wrappers for AI2 or Java users

    public static View applyClickShrinkSelf(View view) {
        applySelfShrink(view);
        return view;
    }

    public static View applyClickShrinkSelf(View view, long duration) {
        applySelfShrink(view, duration);
        return view;
    }

    public static View applyClickShrinkTarget(View triggerView, View targetView) {
        applyTargetShrink(triggerView, targetView);
        return targetView;
    }

    public static View applyClickShrinkTarget(View triggerView, View targetView, long duration) {
        applyTargetShrink(triggerView, targetView, duration);
        return targetView;
    }
}
