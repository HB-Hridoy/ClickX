package com.hridoy.clickx;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.YailProcedure;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@DesignerComponent(
		version = 49,
		versionName = "v1.0.0",
		description = "Advanced Gesture Engine featuring Dual-Mode Layout Interception Mechanics with Complete Bulk-List Iteration Operations and Zero Animation Overhead.",
		iconName = "icon.png"
)
public class ClickX extends AndroidNonvisibleComponent {

	private static final String TAG = "ClickX";
	private final ComponentContainer container;

	// Registration Core Tracking Repositories
	private final Map<String, FastTouchListener> listeners = new ConcurrentHashMap<>();
	private final Map<AndroidViewComponent, String> componentToInternalIdMap = new ConcurrentHashMap<>();
	private final Map<String, AndroidViewComponent> internalIdToComponentMap = new ConcurrentHashMap<>();
	private final Map<String, YailProcedure> internalToCallbackMap = new ConcurrentHashMap<>();

	// Intercept Container Registry
	private final Map<View, AndroidViewComponent> interceptFrameToComponentMap = new ConcurrentHashMap<>();

	public ClickX(ComponentContainer container) {
		super(container.$form());
		this.container = container;
	}

	@SimpleEvent(description = "Fires when an execution block mapping failure or validation argument constraint triggers.")
	public void ErrorOccurred(final String errorFrom, final String error) {
		com.google.appinventor.components.runtime.EventDispatcher.dispatchEvent(this, "ErrorOccurred", errorFrom, error);
	}

	// ================================================================
	// STANDARD INDIVIDUAL COMPONENT ROUTE LAYER
	// ================================================================

	@SimpleFunction(
			description = "Binds a swift, zero-latency click callback directly to an individual target component or a list of components."
	)
	public void AddClickListener(Object component, final YailProcedure callback) {
		if (callback == null) {
			ErrorOccurred("AddClickListener", "Callback procedure cannot be null");
			return;
		}

		if (component instanceof YailList) {
			multipleClickListener((YailList) component, callback);
			return;
		}

		if (!(component instanceof AndroidViewComponent)) {
			ErrorOccurred("AddClickListener", "Invalid component parameter target asset configuration type.");
			return;
		}

		AndroidViewComponent viewComponent = (AndroidViewComponent) component;
		if (componentToInternalIdMap.containsKey(viewComponent)) {
			ErrorOccurred("AddClickListener", "Component is already monitored.");
			return;
		}

		registerSimpleComponent(viewComponent, callback);
	}

	private void registerSimpleComponent(AndroidViewComponent viewComponent, YailProcedure callback) {
		try {
			String internalId = UUID.randomUUID().toString();
			FastTouchListener listener = new FastTouchListener(viewComponent, internalId);
			listeners.put(internalId, listener);
			componentToInternalIdMap.put(viewComponent, internalId);
			internalIdToComponentMap.put(internalId, viewComponent);
			internalToCallbackMap.put(internalId, callback);
		} catch (Exception e) {
			ErrorOccurred("AddClickListener", "Exception: " + e.getMessage());
		}
	}

	private void multipleClickListener(YailList components, final YailProcedure callback) {
		if (components == null || components.size() == 0) {
			ErrorOccurred("AddClickListener", "Component list collection is empty.");
			return;
		}

		try {
			Object[] elements = components.toArray();
			for (Object element : elements) {
				if (element instanceof AndroidViewComponent) {
					AndroidViewComponent child = (AndroidViewComponent) element;
					if (!componentToInternalIdMap.containsKey(child)) {
						registerSimpleComponent(child, callback);
					}
				}
			}
		} catch (Exception e) {
			ErrorOccurred("AddClickListener", "Exception: " + e.getMessage());
		}
	}

	// ================================================================
	// FULL CLICK BLOCK MODE 1: ABSOLUTE OVERLAY INTERCEPT
	// ================================================================

	@SimpleFunction(
			description = "Injects an absolute Intercept Frame into a layout or a list of layouts. Clicking anywhere within these layout bounds runs the callback block immediately—CHILD INTERACTIVE CLICKS WILL BE COMPLETELY BLOCKED."
	)
	public void AddFullClickListener(Object layoutComponent, final YailProcedure callback) {
		if (callback == null) {
			ErrorOccurred("AddFullClickListener", "Callback procedure cannot be null");
			return;
		}

		if (layoutComponent instanceof YailList) {
			Object[] elements = ((YailList) layoutComponent).toArray();
			for (Object element : elements) {
				if (element instanceof AndroidViewComponent) {
					applyAbsoluteIntercept((AndroidViewComponent) element, callback);
				}
			}
			return;
		}

		if (layoutComponent instanceof AndroidViewComponent) {
			applyAbsoluteIntercept((AndroidViewComponent) layoutComponent, callback);
		} else {
			ErrorOccurred("AddFullClickListener", "Invalid target layout component parameter type.");
		}
	}

	private void applyAbsoluteIntercept(AndroidViewComponent layoutComponent, YailProcedure callback) {
		View rawView = layoutComponent.getView();
		if (!(rawView instanceof ViewGroup)) {
			ErrorOccurred("AddFullClickListener", "Target must inherit structural arrangement properties.");
			return;
		}

		final ViewGroup layoutGroup = (ViewGroup) rawView;
		clearExistingStructureWrappers(layoutGroup, layoutComponent);

		try {
			String internalId = UUID.randomUUID().toString();
			componentToInternalIdMap.put(layoutComponent, internalId);
			internalIdToComponentMap.put(internalId, layoutComponent);
			internalToCallbackMap.put(internalId, callback);

			int childCount = layoutGroup.getChildCount();
			View[] children = new View[childCount];
			ViewGroup.LayoutParams[] childParams = new ViewGroup.LayoutParams[childCount];
			for (int i = 0; i < childCount; i++) {
				children[i] = layoutGroup.getChildAt(i);
				childParams[i] = children[i].getLayoutParams();
			}
			layoutGroup.removeAllViews();

			InterceptFrameLayout interceptor = new InterceptFrameLayout(container.$context());
			interceptFrameToComponentMap.put(interceptor, layoutComponent);

			for (int i = 0; i < children.length; i++) {
				interceptor.addView(children[i], childParams[i] != null ? childParams[i] :
						new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			}

			layoutGroup.addView(interceptor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

			final String finalId = internalId;
			interceptor.setClickable(true);
			interceptor.setOnTouchListener(new View.OnTouchListener() {
				private long lastClickTime = 0;
				private static final long DEBOUNCE_THRESHOLD = 10;

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) return true;
					if (event.getAction() == MotionEvent.ACTION_UP) {
						long currentTime = System.currentTimeMillis();
						if (currentTime - lastClickTime > DEBOUNCE_THRESHOLD) {
							lastClickTime = currentTime;
							HandleClick(finalId);
						}
						return true;
					}
					return false;
				}
			});

		} catch (Exception e) {
			ErrorOccurred("AddFullClickListener", "Exception: " + e.getMessage());
		}
	}

	// ================================================================
	// FULL CLICK BLOCK MODE 2: BACKGROUND PASS-THROUGH LOGIC
	// ================================================================

	@SimpleFunction(
			description = "Binds zero-latency touch listeners directly onto the background layout surface of a single arrangement or a list of arrangements. Tapping layout whitespace runs the callback—ACTIVE CHILD CLICKS (Buttons, Switches, etc.) WILL CONTINUE TO WORK PERFECTLY."
	)
	public void AddFullClickListenerExceptClickableComponents(Object layoutComponent, final YailProcedure callback) {
		if (callback == null) {
			ErrorOccurred("AddFullClickListenerExceptClickableComponents", "Callback procedure cannot be null");
			return;
		}

		if (layoutComponent instanceof YailList) {
			Object[] elements = ((YailList) layoutComponent).toArray();
			for (Object element : elements) {
				if (element instanceof AndroidViewComponent) {
					applyPassThroughBackground((AndroidViewComponent) element, callback);
				}
			}
			return;
		}

		if (layoutComponent instanceof AndroidViewComponent) {
			applyPassThroughBackground((AndroidViewComponent) layoutComponent, callback);
		} else {
			ErrorOccurred("AddFullClickListenerExceptClickableComponents", "Invalid target layout type.");
		}
	}

	private void applyPassThroughBackground(AndroidViewComponent layoutComponent, YailProcedure callback) {
		View targetView = layoutComponent.getView();
		if (targetView == null) return;

		if (targetView instanceof ViewGroup) {
			clearExistingStructureWrappers((ViewGroup) targetView, layoutComponent);
		}

		try {
			String internalId = UUID.randomUUID().toString();
			componentToInternalIdMap.put(layoutComponent, internalId);
			internalIdToComponentMap.put(internalId, layoutComponent);
			internalToCallbackMap.put(internalId, callback);

			final String finalId = internalId;
			targetView.setClickable(true);
			targetView.setOnTouchListener(new View.OnTouchListener() {
				private long lastClickTime = 0;
				private static final long DEBOUNCE_THRESHOLD = 10;

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) return true;
					if (event.getAction() == MotionEvent.ACTION_UP) {
						long currentTime = System.currentTimeMillis();
						if (currentTime - lastClickTime > DEBOUNCE_THRESHOLD) {
							lastClickTime = currentTime;
							HandleClick(finalId);
						}
						return true;
					}
					return false;
				}
			});
		} catch (Exception e) {
			ErrorOccurred("AddFullClickListenerExceptClickableComponents", "Exception: " + e.getMessage());
		}
	}

	// ================================================================
	// TEARDOWN CLEANER & REMOVAL ROUTINES
	// ================================================================

	@SimpleFunction(
			description = "Dismantles touch hooks and safely tears down internal overlay frames to restore layouts back to standard stock behavior profiles."
	)
	public void RemoveInteraction(AndroidViewComponent component) {
		View targetView = component.getView();
		if (targetView == null) return;

		targetView.setOnTouchListener(null);
		String internalId = componentToInternalIdMap.remove(component);
		if (internalId != null) {
			listeners.remove(internalId);
			internalIdToComponentMap.remove(internalId);
			internalToCallbackMap.remove(internalId);
		}

		if (targetView instanceof ViewGroup) {
			clearExistingStructureWrappers((ViewGroup) targetView, component);
		}
	}

	private void clearExistingStructureWrappers(ViewGroup layoutGroup, AndroidViewComponent component) {
		if (layoutGroup.getChildCount() > 0 && layoutGroup.getChildAt(0) instanceof InterceptFrameLayout) {
			InterceptFrameLayout interceptor = (InterceptFrameLayout) layoutGroup.getChildAt(0);
			interceptFrameToComponentMap.remove(interceptor);
			interceptor.setOnTouchListener(null);

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
				ViewGroup.LayoutParams lp = params[i] != null ? params[i] :
						new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				layoutGroup.addView(children[i], lp);
			}
		}
	}

	public void HandleClick(String internalId) {
		YailProcedure callback = internalToCallbackMap.get(internalId);
		AndroidViewComponent component = internalIdToComponentMap.get(internalId);

		if (callback != null && component != null) {
			if (callback.numArgs() == 0) {
				callback.call();
			} else {
				callback.call(component);
			}
		}
	}

	// ================================================================
	// PRIVILEGED LAYOUT FRAME INTERCEPT DESIGNS
	// ================================================================

	private final class InterceptFrameLayout extends FrameLayout {
		public InterceptFrameLayout(Context context) {
			super(context);
			setClickable(true);
			setFocusable(true);
		}

		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			if (interceptFrameToComponentMap.containsKey(this)) {
				return true; // Lock down structural intercept pathing
			}
			return super.onInterceptTouchEvent(ev);
		}
	}

	// ================================================================
	// ZERO-LATENCY HARDWARE RAW TOUCH LOGIC STREAMS
	// ================================================================

	private final class FastTouchListener implements View.OnTouchListener {
		final String internalId;
		final AndroidViewComponent component;
		private final View targetView;
		private long lastClickTime = 0;
		private static final long DEBOUNCE_THRESHOLD = 10;

		FastTouchListener(AndroidViewComponent component, String internalId) {
			this.component = component;
			this.internalId = internalId;
			this.targetView = getFinalView(component);
			this.targetView.setClickable(true);
			this.targetView.setOnTouchListener(this);
		}

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					return true;

				case MotionEvent.ACTION_UP:
					long currentTime = System.currentTimeMillis();
					if (currentTime - lastClickTime > DEBOUNCE_THRESHOLD) {
						lastClickTime = currentTime;
						HandleClick(internalId);
					}
					return true;

				default:
					return false;
			}
		}

		private View getFinalView(AndroidViewComponent component) {
			View view = component.getView();
			final String className = component.getClass().getSimpleName();

			if ("MakeroidCardView".equals(className)) {
				if (view instanceof ViewGroup) {
					return ((ViewGroup) view).getChildAt(0);
				}
			}

			if ("HorizontalArrangement".equals(className) || "VerticalArrangement".equals(className)) {
				try {
					Method isCardMethod = component.getClass().getMethod("IsCard");
					if (isCardMethod != null && boolean.class.equals(isCardMethod.getReturnType())) {
						boolean isCard = (boolean) isCardMethod.invoke(component);
						if (isCard && view instanceof ViewGroup) {
							return ((ViewGroup) view).getChildAt(0);
						}
					}
				} catch (Exception e) {
					Log.e(TAG, "getFinalView exception details: " + e.getMessage());
				}
			}

			return view;
		}
	}
}