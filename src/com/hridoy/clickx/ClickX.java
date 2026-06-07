package com.hridoy.clickx;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.YailProcedure;
import com.hridoy.clickx.helpers.Animation;
import com.hridoy.clickx.helpers.InteractionType;

@DesignerComponent(
		version = 31,
		versionName = "30.0",
		description = "Target-Isolated Native Gesture Engine with Frame Interception Architecture.",
		iconName = "icon.png"
)
public class ClickX extends AndroidNonvisibleComponent {

	private static final int DATA_TAG_KEY = 0x7f0a0001;
	private final ComponentContainer container;

	public ClickX(ComponentContainer container) {
		super(container.$form());
		this.container = container;
	}

	// ================================================================
	// PRIVATE HELPERS / ENGINE MECHANICS
	// ================================================================

	private boolean isCallbackValid(String op, YailProcedure cb, int expected) {
		if (cb == null) {
			ErrorOccurred(op, "Callback execution attempt failed: Target anonymous block reference is null.");
			return false;
		}
		if (cb.numArgs() != expected) {
			ErrorOccurred(op, "Structural Block Mismatch Error: Expected a callback block with exactly " + expected + " parameters, but received " + cb.numArgs() + ".");
			return false;
		}
		return true;
	}

	private void applyNativeRipple(View view, int color, boolean bounded) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ColorStateList colorStateList = ColorStateList.valueOf(color);
			Drawable currentBackground = view.getBackground();
			Drawable maskDrawable = bounded ? new ShapeDrawable(new RectShape()) : null;

			RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, currentBackground, maskDrawable);
			view.setBackground(rippleDrawable);
			view.setClickable(true);
		}
	}

	private void dispatchCallback(YailProcedure callback, AndroidViewComponent component, Object data) {
		if (callback == null) return;

		if (callback.numArgs() == 0) {
			callback.call();
		} else {
			callback.call(component, data);
		}
	}

	/**
	 * Engine Pipeline Core with Intercepting Frame Layout Hijack for full click layouts.
	 */
	private void setupComponentPipeline(
			final AndroidViewComponent component,
			final Object data,
			final Object animationTypeOrConfig,
			final String interactionType,
			final boolean fullClickable,
			final YailProcedure callback) {

		if (callback == null) return;

		View targetView = component.getView();

		// Handle the Full Clickable design pattern by inserting an intercepting wrapper layer
		if (fullClickable && targetView instanceof ViewGroup) {
			ViewGroup layoutGroup = (ViewGroup) targetView;

			// Check if we already wrapped this view to prevent adding duplicate layers
			if (!(layoutGroup.getChildAt(0) instanceof InterceptFrameLayout)) {
				View innerContent = layoutGroup.getChildAt(0);
				layoutGroup.removeView(innerContent);

				InterceptFrameLayout interceptor = new InterceptFrameLayout(container.$context());
				interceptor.addView(innerContent, new FrameLayout.LayoutParams(-1, -1));
				layoutGroup.addView(interceptor, new FrameLayout.LayoutParams(-1, -1));

				targetView = interceptor; // Re-target gesture engine straight onto our wrapper layer
			} else {
				targetView = layoutGroup.getChildAt(0);
			}
		}

		final View finalActiveView = targetView;
		finalActiveView.setTag(DATA_TAG_KEY, data);

		final ClickShrinkEffect shrinkEngine = new ClickShrinkEffect(finalActiveView);
		boolean shouldApplyShrink = false;

		try {
			if (animationTypeOrConfig instanceof Animation) {
				Animation effect = (Animation) animationTypeOrConfig;
				if (effect == Animation.Shrink) shouldApplyShrink = true;
				else if (effect == Animation.Ripple) applyNativeRipple(finalActiveView, 0x20000000, true);
			} else if (animationTypeOrConfig instanceof String) {
				String style = ((String) animationTypeOrConfig).toUpperCase().trim();
				if (style.equals("SHRINK")) shouldApplyShrink = true;
				else if (style.equals("RIPPLE")) applyNativeRipple(finalActiveView, 0x20000000, true);
			} else if (animationTypeOrConfig instanceof YailDictionary) {
				YailDictionary dict = (YailDictionary) animationTypeOrConfig;
				String type = dict.get("TYPE") != null ? dict.get("TYPE").toString() : "";
				if (type.equals("CUSTOM_RIPPLE")) {
					int color = ((Number) dict.get("COLOR")).intValue();
					boolean bounded = (Boolean) dict.get("BOUNDED");
					applyNativeRipple(finalActiveView, color, bounded);
				}
			}
		} catch (Exception e) {
			ErrorOccurred("AdvancedInteraction", "Configuration parse exception: " + e.getMessage());
		}

		final boolean runShrink = shouldApplyShrink;

		final GestureDetector gestureDetector = new GestureDetector(finalActiveView.getContext(), new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (interactionType.equals("CLICK")) {
					if (runShrink) shrinkEngine.grow();
					dispatchCallback(callback, component, finalActiveView.getTag(DATA_TAG_KEY));
					return true;
				}
				return false;
			}
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (interactionType.equals("DOUBLE_CLICK")) {
					if (runShrink) shrinkEngine.grow();
					dispatchCallback(callback, component, finalActiveView.getTag(DATA_TAG_KEY));
					return true;
				}
				return false;
			}
			@Override
			public void onLongPress(MotionEvent e) {
				if (interactionType.equals("LONG_PRESS")) {
					if (runShrink) shrinkEngine.grow();
					dispatchCallback(callback, component, finalActiveView.getTag(DATA_TAG_KEY));
				}
			}
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (runShrink) shrinkEngine.grow();
				if (e1 == null || e2 == null) return false;

				float diffY = e2.getY() - e1.getY();
				float diffX = e2.getX() - e1.getX();

				boolean triggered = false;
				if (Math.abs(diffX) > Math.abs(diffY)) {
					if (diffX > 0 && interactionType.equals("SWIPE_RIGHT")) triggered = true;
					else if (diffX < 0 && interactionType.equals("SWIPE_LEFT")) triggered = true;
				} else {
					if (diffY > 0 && interactionType.equals("SWIPE_DOWN")) triggered = true;
					else if (diffY < 0 && interactionType.equals("SWIPE_UP")) triggered = true;
				}

				if (triggered) {
					dispatchCallback(callback, component, finalActiveView.getTag(DATA_TAG_KEY));
					return true;
				}
				return false;
			}
		});

		finalActiveView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!interactionType.equals("TOUCH_DOWN") && !interactionType.equals("TOUCH_UP")) {
					gestureDetector.onTouchEvent(event);
				}

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (runShrink) shrinkEngine.shrink();
						if (interactionType.equals("TOUCH_DOWN")) {
							dispatchCallback(callback, component, finalActiveView.getTag(DATA_TAG_KEY));
						}
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (runShrink) shrinkEngine.grow();
						if (interactionType.equals("TOUCH_UP")) {
							dispatchCallback(callback, component, finalActiveView.getTag(DATA_TAG_KEY));
						}
						break;
				}
				return true;
			}
		});
	}

	// ================================================================
	// CUSTOM INTERCEPTING FRAME COMPONENT
	// ================================================================

	private static class InterceptFrameLayout extends FrameLayout {
		public InterceptFrameLayout(Context context) {
			super(context);
			setClickable(true);
			setFocusable(true);
		}

		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			// Force intercept all downstream coordinates to bypass child view focus completely
			return true;
		}
	}

	// ================================================================
	// PUBLIC API METHODS
	// ================================================================

	@SimpleFunction(description = "Generates a configuration dictionary for customized ripple effects.")
	public YailDictionary CustomRipple(int color, boolean bounded) {
		YailDictionary config = new YailDictionary();
		config.put("TYPE", "CUSTOM_RIPPLE");
		config.put("COLOR", color);
		config.put("BOUNDED", bounded);
		return config;
	}

	@SimpleFunction(
			description = "Registers a single component or a YailList of components for specialized interaction tracking. Callback requires exactly 2 parameters."
	)
	public void AdvancedInteraction(
			final Object componentOrList,
			final Object data,
			final Object animation,
			@Options(InteractionType.class) final String interactionType,
			final boolean fullClickable,
			final YailProcedure callback) {

		if (componentOrList == null) return;
		if (!isCallbackValid("AdvancedInteraction", callback, 2)) return;

		if (componentOrList instanceof YailList) {
			Object[] elements = ((YailList) componentOrList).toArray();
			for (int i = 1; i < elements.length; i++) {
				Object item = elements[i];
				if (item instanceof AndroidViewComponent) {
					AndroidViewComponent comp = (AndroidViewComponent) item;
					if (comp.getView() != null) {
						setupComponentPipeline(comp, data, animation, interactionType, fullClickable, callback);
					}
				}
			}
		} else if (componentOrList instanceof AndroidViewComponent) {
			AndroidViewComponent comp = (AndroidViewComponent) componentOrList;
			if (comp.getView() != null) {
				setupComponentPipeline(comp, data, animation, interactionType, fullClickable, callback);
			}
		} else {
			ErrorOccurred("AdvancedInteraction", "Invalid target type. Expected Component or YailList.");
		}
	}

	@SimpleFunction(
			description = "Registers a standard click on a single component or a YailList of components with a ripple overlay."
	)
	public void SimpleClick(final Object componentOrList, final YailProcedure callback) {
		if (componentOrList == null) return;
		if (!isCallbackValid("SimpleClick", callback, 0)) return;

		if (componentOrList instanceof YailList) {
			Object[] elements = ((YailList) componentOrList).toArray();
			for (int i = 1; i < elements.length; i++) {
				if (elements[i] instanceof AndroidViewComponent) {
					AndroidViewComponent comp = (AndroidViewComponent) elements[i];
					if (comp.getView() != null) {
						setupComponentPipeline(comp, null, "RIPPLE", "CLICK", false, callback);
					}
				}
			}
		} else if (componentOrList instanceof AndroidViewComponent) {
			AndroidViewComponent comp = (AndroidViewComponent) componentOrList;
			if (comp.getView() != null) {
				setupComponentPipeline(comp, null, "RIPPLE", "CLICK", false, callback);
			}
		} else {
			ErrorOccurred("SimpleClick", "Invalid target type. Expected Component or YailList.");
		}
	}

	@SimpleFunction(
			description = "Registers a click on a layout container component or a YailList of layout containers, intercepting nested children clicks cleanly."
	)
	public void SimpleFullClick(final Object componentOrList, final YailProcedure callback) {
		if (componentOrList == null) return;
		if (!isCallbackValid("SimpleFullClick", callback, 0)) return;

		if (componentOrList instanceof YailList) {
			Object[] elements = ((YailList) componentOrList).toArray();
			for (int i = 1; i < elements.length; i++) {
				if (elements[i] instanceof AndroidViewComponent) {
					AndroidViewComponent comp = (AndroidViewComponent) elements[i];
					if (comp.getView() != null) {
						setupComponentPipeline(comp, null, "RIPPLE", "CLICK", true, callback);
					}
				}
			}
		} else if (componentOrList instanceof AndroidViewComponent) {
			AndroidViewComponent comp = (AndroidViewComponent) componentOrList;
			if (comp.getView() != null) {
				setupComponentPipeline(comp, null, "RIPPLE", "CLICK", true, callback);
			}
		} else {
			ErrorOccurred("SimpleFullClick", "Invalid target type. Expected Component or YailList.");
		}
	}

	@SimpleEvent(description = "Fires when verification architecture breaks or operations exception occurs.")
	public void ErrorOccurred(final String errorFrom, final String error) {
		EventDispatcher.dispatchEvent(this, "ErrorOccurred", errorFrom, error);
	}
}