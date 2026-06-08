package com.hridoy.clickx;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.YailProcedure;
import com.hridoy.clickx.helpers.FullClickType;
import com.hridoy.clickx.helpers.InteractionType;

@DesignerComponent(
		version = 43,
		versionName = "41.0",
		description = "Target-Isolated Native Gesture Engine with Frame Interception, Selective Bubbling, and Simplified Callback Pipelines.",
		iconName = "icon.png"
)
public class ClickX extends AndroidNonvisibleComponent {

	private final ComponentContainer container;

	public ClickX(ComponentContainer container) {
		super(container.$form());
		this.container = container;
	}

	// ================================================================
	// ANIMATION HELPER STRINGS (PROPERTY GETTERS)
	// ================================================================

	@SimpleProperty(description = "Returns the 'SHRINK' animation identifier token block.")
	public String AnimationShrink() {
		return "SHRINK";
	}

	@SimpleProperty(description = "Returns the standard 'RIPPLE' animation identifier token block.")
	public String AnimationRipple() {
		return "RIPPLE";
	}

	@SimpleProperty(description = "Returns the native android 'SYSTEM_RIPPLE' boundary overlay token block.")
	public String AnimationSystemRipple() {
		return "SYSTEM_RIPPLE";
	}

	@SimpleProperty(description = "Returns a fallback 'NONE' visual animation processing block.")
	public String AnimationNone() {
		return "NONE";
	}

	// ================================================================
	// PUBLIC API FUNCTIONS WITH EXPLICIT DESCRIPTIONS
	// ================================================================

	@SimpleFunction(
			description = "Generates a configuration profile dictionary for custom touch ripple behaviors.\n\n" +
					"PARAMETERS:\n" +
					"• color (Number): The raw ARGB color integer for the ripple fluid wave splash.\n" +
					"• bounded (Boolean): True bounds the ripple inside component borders; False allows circular boundary overflow look."
	)
	public YailDictionary CustomRipple(int color, boolean bounded) {
		YailDictionary config = new YailDictionary();
		config.put("TYPE", "CUSTOM_RIPPLE");
		config.put("COLOR", color);
		config.put("BOUNDED", bounded);
		return config;
	}

	@SimpleFunction(
			description = "Registers components for advanced gesture tracking filters with custom animation profile rules.\n\n" +
					"PARAMETERS:\n" +
					"• componentOrList (Any): A single visible view component or a YailList array block of components.\n" +
					"• animation (Animation): The feedback selection block (Shrink, Ripple, or your CustomRipple profile block).\n" +
					"• interactionType (InteractionType): The active listener gesture filter rule (e.g., CLICK, LONG_PRESS, SWIPE).\n" +
					"• fullClickType (FullClickType): Structural mode layout settings (None, FullClick, or FullClickExceptComponents).\n" +
					"• callback (Procedure): The custom event pipeline callback socket.\n\n" +
					"CALLBACK RETURNS:\n" +
					"• Takes 0 parameters -> Fires inline block utilities.\n" +
					"• Takes 1 parameter -> Returns [component] (The interacted user interface object reference target)."
	)
	public void AdvancedInteraction(
			final Object componentOrList,
			final Object animation,
			@Options(InteractionType.class) final String interactionType,
			@Options(FullClickType.class) final int fullClickType,
			final YailProcedure callback) {

		if (componentOrList == null) return;
		if (!isCallbackValid("AdvancedInteraction", callback, 1)) return;

		if (componentOrList instanceof YailList) {
			Object[] elements = ((YailList) componentOrList).toArray();
			for (int i = 1; i < elements.length; i++) {
				Object item = elements[i];
				if (item instanceof AndroidViewComponent) {
					AndroidViewComponent comp = (AndroidViewComponent) item;
					if (comp.getView() != null) {
						setupComponentPipeline(comp, animation, interactionType, fullClickType, callback);
					}
				}
			}
		} else if (componentOrList instanceof AndroidViewComponent) {
			AndroidViewComponent comp = (AndroidViewComponent) componentOrList;
			if (comp.getView() != null) {
				setupComponentPipeline(comp, animation, interactionType, fullClickType, callback);
			}
		}
	}

	@SimpleFunction(
			description = "Binds a straightforward, lightning-fast single-tap click event directly over elements with zero visual animation overhead.\n\n" +
					"PARAMETERS:\n" +
					"• componentOrList (Any): A specific target layout/component or a YailList grouping of components.\n" +
					"• callback (Procedure): The touch event handler block execution slot.\n\n" +
					"CALLBACK RETURNS:\n" +
					"• Takes 0 parameters -> Runs straightforward standalone operations.\n" +
					"• Takes 1 parameter -> Returns [component] (The specific object reference that was clicked)."
	)
	public void SimpleClick(final Object componentOrList, final YailProcedure callback) {
		if (componentOrList == null) return;
		if (!isCallbackValid("SimpleClick", callback, 1)) return;

		if (componentOrList instanceof YailList) {
			Object[] elements = ((YailList) componentOrList).toArray();
			for (int i = 1; i < elements.length; i++) {
				if (elements[i] instanceof AndroidViewComponent) {
					AndroidViewComponent comp = (AndroidViewComponent) elements[i];
					if (comp.getView() != null) {
						setupComponentPipeline(comp, "NONE", "CLICK", 0, callback);
					}
				}
			}
		} else if (componentOrList instanceof AndroidViewComponent) {
			AndroidViewComponent comp = (AndroidViewComponent) componentOrList;
			if (comp.getView() != null) {
				setupComponentPipeline(comp, "NONE", "CLICK", 0, callback);
			}
		}
	}

	@SimpleFunction(
			description = "Hijacks layout structures using an absolute intercept frame overlay container. Clicking anywhere within these boundaries runs the callback instantly without animations, capturing touches completely away from inner children components.\n\n" +
					"PARAMETERS:\n" +
					"• componentOrList (Any): A layout block profile container or a YailList collection of layout wrappers.\n" +
					"• callback (Procedure): The intercept execution process callback slot.\n\n" +
					"CALLBACK RETURNS:\n" +
					"• Takes 0 parameters -> Runs global design workflow macros.\n" +
					"• Takes 1 parameter -> Returns [layout] (The root layout component shell object that intercepted the touch vector stream)."
	)
	public void SimpleFullClick(final Object componentOrList, final YailProcedure callback) {
		if (componentOrList == null) return;
		if (!isCallbackValid("SimpleFullClick", callback, 1)) return;

		if (componentOrList instanceof YailList) {
			Object[] elements = ((YailList) componentOrList).toArray();
			for (int i = 1; i < elements.length; i++) {
				if (elements[i] instanceof AndroidViewComponent) {
					AndroidViewComponent comp = (AndroidViewComponent) elements[i];
					if (comp.getView() != null) {
						setupComponentPipeline(comp, "NONE", "CLICK", 1, callback);
					}
				}
			}
		} else if (componentOrList instanceof AndroidViewComponent) {
			AndroidViewComponent comp = (AndroidViewComponent) componentOrList;
			if (comp.getView() != null) {
				setupComponentPipeline(comp, "NONE", "CLICK", 1, callback);
			}
		}
	}

	@SimpleFunction(
			description = "Registers layout click interception layers selectively. Tapping empty whitespace areas runs the callback without animations while letting pre-assigned active interactive child widgets (like native Buttons, Switches, or Sliders) catch their own independent actions cleanly.\n\n" +
					"PARAMETERS:\n" +
					"• componentOrList (Any): A single valid layout object block or a configured YailList collection of layouts.\n" +
					"• callback (Procedure): The background touch handler block interface hook.\n\n" +
					"CALLBACK RETURNS:\n" +
					"• Takes 0 parameters -> Triggers basic decoupled data changes.\n" +
					"• Takes 1 parameter -> Returns [layout] (The primary background container parent object that caught the structural click event stream)."
	)
	public void SimpleFullClickExceptComponents(final Object componentOrList, final YailProcedure callback) {
		if (componentOrList == null) return;
		if (!isCallbackValid("SimpleFullClickExceptComponents", callback, 1)) return;

		if (componentOrList instanceof YailList) {
			Object[] elements = ((YailList) componentOrList).toArray();
			for (int i = 1; i < elements.length; i++) {
				if (elements[i] instanceof AndroidViewComponent) {
					AndroidViewComponent comp = (AndroidViewComponent) elements[i];
					if (comp.getView() != null) {
						setupComponentPipeline(comp, "NONE", "CLICK", 2, callback);
					}
				}
			}
		} else if (componentOrList instanceof AndroidViewComponent) {
			AndroidViewComponent comp = (AndroidViewComponent) componentOrList;
			if (comp.getView() != null) {
				setupComponentPipeline(comp, "NONE", "CLICK", 2, callback);
			}
		}
	}

	@SimpleFunction(
			description = "Dismantles touch gesture listening frameworks completely, strips tracking filters, and completely tears down any wrapper frame structural modifications to return view hierarchies back to clean Android stock standards.\n\n" +
					"PARAMETERS:\n" +
					"• componentOrList (Any): A single managed interface component or a YailList structure pool of components."
	)
	public void RemoveInteraction(final Object componentOrList) {
		if (componentOrList == null) return;

		if (componentOrList instanceof YailList) {
			Object[] elements = ((YailList) componentOrList).toArray();
			for (int i = 1; i < elements.length; i++) {
				if (elements[i] instanceof AndroidViewComponent) {
					resetComponentStructure((AndroidViewComponent) elements[i]);
				}
			}
		} else if (componentOrList instanceof AndroidViewComponent) {
			resetComponentStructure((AndroidViewComponent) componentOrList);
		}
	}

	@SimpleEvent(description = "Fires when a structural pipeline initialization failure or internal component execution argument mismatch happens.")
	public void ErrorOccurred(final String errorFrom, final String error) {
		EventDispatcher.dispatchEvent(this, "ErrorOccurred", errorFrom, error);
	}

	// ================================================================
	// INTERCEPTING LOGIC ENGINE PIPELINES
	// ================================================================

	private static class InterceptFrameLayout extends FrameLayout {
		public InterceptFrameLayout(Context context) {
			super(context);
			setClickable(true);
			setFocusable(true);
		}
		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			return true;
		}
	}

	private boolean isCallbackValid(String op, YailProcedure cb, int maxExpected) {
		if (cb == null) {
			ErrorOccurred(op, "Callback is null.");
			return false;
		}
		if (cb.numArgs() > maxExpected) {
			ErrorOccurred(op, "Callback has too many parameters. Max allowed is " + maxExpected);
			return false;
		}
		return true;
	}

	private int getSystemRippleResource(Context context) {
		TypedValue typedValue = new TypedValue();
		if (context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)) {
			return typedValue.resourceId;
		}
		return 0;
	}

	private void applyNativeRipple(View view, int color, boolean bounded, boolean useSystemDefault) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if (useSystemDefault) {
				int resId = getSystemRippleResource(view.getContext());
				if (resId != 0) {
					Drawable systemRipple = view.getContext().getResources().getDrawable(resId, view.getContext().getTheme());
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						view.setForeground(systemRipple);
					} else {
						view.setBackground(systemRipple);
					}
					view.setClickable(true);
					return;
				}
			}

			ColorStateList colorStateList = ColorStateList.valueOf(color);
			Drawable currentBackground = view.getBackground();
			Drawable maskDrawable = bounded ? new ShapeDrawable(new RectShape()) : null;

			RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, currentBackground, maskDrawable);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				view.setForeground(rippleDrawable);
			} else {
				view.setBackground(rippleDrawable);
			}
			view.setClickable(true);
		}
	}

	private void applySelectiveClickable(final View parentView, View currentView) {
		if (currentView == null || currentView == parentView) return;

		if (!currentView.isClickable() && !currentView.isFocusable()) {
			currentView.setClickable(false);
			currentView.setFocusable(false);
		}

		if (currentView instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) currentView;
			for (int i = 0; i < group.getChildCount(); i++) {
				applySelectiveClickable(parentView, group.getChildAt(i));
			}
		}
	}

	private void resetComponentStructure(AndroidViewComponent component) {
		View targetView = component.getView();
		if (targetView == null) return;

		targetView.setOnTouchListener(null);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			targetView.setForeground(null);
		}

		if (targetView instanceof ViewGroup) {
			ViewGroup layoutGroup = (ViewGroup) targetView;
			if (layoutGroup.getChildCount() > 0 && layoutGroup.getChildAt(0) instanceof InterceptFrameLayout) {
				InterceptFrameLayout interceptor = (InterceptFrameLayout) layoutGroup.getChildAt(0);

				int childCount = interceptor.getChildCount();
				View[] children = new View[childCount];
				ViewGroup.LayoutParams[] params = new ViewGroup.LayoutParams[childCount];
				for (int i = 0; i < childCount; i++) {
					children[i] = interceptor.getChildAt(i);
					params[i] = children[i].getLayoutParams();
				}
				interceptor.removeAllViews();
				layoutGroup.removeView(interceptor);

				for (int i = 0; i < children.length; i++) {
					ViewGroup.LayoutParams lp = params[i] != null ? params[i] : new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
					layoutGroup.addView(children[i], lp);
				}
			}
		}
	}

	private void dispatchCallback(YailProcedure callback, AndroidViewComponent component) {
		if (callback == null) return;
		if (callback.numArgs() == 0) {
			callback.call();
		} else {
			callback.call(component);
		}
	}

	private void setupComponentPipeline(
			final AndroidViewComponent component,
			final Object animationTypeOrConfig,
			final String interactionType,
			final int executionMode,
			final YailProcedure callback) {

		if (callback == null) return;

		View targetView = component.getView();

		// MODE 0: Skips structural layouts. Only MODE 1 injects intercept frames.
		if (executionMode == 1 && targetView instanceof ViewGroup) {
			ViewGroup layoutGroup = (ViewGroup) targetView;
			if (layoutGroup.getChildAt(0) instanceof InterceptFrameLayout) {
				InterceptFrameLayout existing = (InterceptFrameLayout) layoutGroup.getChildAt(0);
				existing.setOnTouchListener(null);
				targetView = existing;
			} else {
				int childCount = layoutGroup.getChildCount();
				View[] children = new View[childCount];
				ViewGroup.LayoutParams[] childParams = new ViewGroup.LayoutParams[childCount];
				for (int i = 0; i < childCount; i++) {
					children[i] = layoutGroup.getChildAt(i);
					childParams[i] = children[i].getLayoutParams();
				}
				layoutGroup.removeAllViews();

				InterceptFrameLayout interceptor = new InterceptFrameLayout(container.$context());
				for (int i = 0; i < children.length; i++) {
					interceptor.addView(children[i], childParams[i] != null ? childParams[i] : new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				}
				layoutGroup.addView(interceptor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				targetView = interceptor;
			}
		}

		final View finalActiveView = targetView;
		final ClickShrinkEffect shrinkEngine = new ClickShrinkEffect(finalActiveView);
		boolean shouldApplyShrink = false;

		// ================================================================
		// ANIMATION CONFIG LAYER (ONLY RESOLVES VIA ADVANCED INTERACTION)
		// ================================================================
		try {
			if (animationTypeOrConfig instanceof String) {
				String style = ((String) animationTypeOrConfig).toUpperCase().trim();
				if (style.equals("SHRINK")) shouldApplyShrink = true;
				else if (style.equals("RIPPLE")) applyNativeRipple(finalActiveView, 0x20000000, true, false);
				else if (style.equals("SYSTEM_RIPPLE")) applyNativeRipple(finalActiveView, 0, true, true);
			} else if (animationTypeOrConfig instanceof YailDictionary) {
				YailDictionary dict = (YailDictionary) animationTypeOrConfig;
				String type = dict.get("TYPE") != null ? dict.get("TYPE").toString() : "";
				if (type.equals("CUSTOM_RIPPLE")) {
					int color = ((Number) dict.get("COLOR")).intValue();
					boolean bounded = (Boolean) dict.get("BOUNDED");
					applyNativeRipple(finalActiveView, color, bounded, false);
				}
			}
		} catch (Exception e) {
			ErrorOccurred("AdvancedInteraction", "Animation config error: " + e.getMessage());
		}

		final boolean runShrink = shouldApplyShrink;

		final GestureDetector gestureDetector = new GestureDetector(finalActiveView.getContext(),
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						return false; // Handled instantly inside raw listener below
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (interactionType.equals("DOUBLE_CLICK")) {
							if (runShrink) shrinkEngine.grow();
							dispatchCallback(callback, component);
							return true;
						}
						return false;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (interactionType.equals("LONG_PRESS")) {
							if (runShrink) shrinkEngine.grow();
							dispatchCallback(callback, component);
						}
					}

					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
						if (e1 == null || e2 == null) return false;
						float diffY = e2.getY() - e1.getY();
						float diffX = e2.getX() - e1.getX();
						String direction = (Math.abs(diffX) > Math.abs(diffY)) ? (diffX > 0 ? "SWIPE_RIGHT" : "SWIPE_LEFT") : (diffY > 0 ? "SWIPE_DOWN" : "SWIPE_UP");

						if (interactionType.equals("SWIPE") || interactionType.equals(direction)) {
							if (runShrink) shrinkEngine.grow();
							dispatchCallback(callback, component);
							return true;
						}
						return false;
					}
				});

		finalActiveView.setOnTouchListener(new View.OnTouchListener() {
			private long lastClickTime = 0;
			private static final long DEBOUNCE_THRESHOLD = 10;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				boolean gestureHandled = false;

				if (!interactionType.equals("CLICK") && !interactionType.equals("TOUCH_DOWN") && !interactionType.equals("TOUCH_UP")) {
					gestureHandled = gestureDetector.onTouchEvent(event);
				}

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (runShrink) shrinkEngine.shrink();
						if (interactionType.equals("TOUCH_DOWN")) {
							dispatchCallback(callback, component);
						}
						return true;

					case MotionEvent.ACTION_UP:
						if (runShrink) shrinkEngine.grow();

						if (interactionType.equals("CLICK")) {
							long currentTime = System.currentTimeMillis();
							if (currentTime - lastClickTime > DEBOUNCE_THRESHOLD) {
								lastClickTime = currentTime;
								dispatchCallback(callback, component);
							}
							return true;
						}

						if (interactionType.equals("TOUCH_UP")) {
							dispatchCallback(callback, component);
						}
						return true;

					case MotionEvent.ACTION_CANCEL:
						if (runShrink) shrinkEngine.grow();
						return true;

					case MotionEvent.ACTION_MOVE:
						return gestureHandled;

					default:
						return gestureHandled;
				}
			}
		});

		if (executionMode == 2) {
			applySelectiveClickable(finalActiveView, finalActiveView);
		}
	}
}