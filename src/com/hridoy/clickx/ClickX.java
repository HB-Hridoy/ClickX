package com.hridoy.clickx;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.YailList;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@DesignerComponent(
		version = 9,
		versionName = "1.1",
		description = "Developed by Hridoy using Fast.",
		iconName = "icon.png"
)
public class ClickX extends AndroidNonvisibleComponent {

	private static final String TAG = "ClickX";

	private final Map<String, ClickListener> listeners = new ConcurrentHashMap<>();
	private final Map<AndroidViewComponent, String> componentToInternalIdMap = new ConcurrentHashMap<>();
	private final Map<String, String> internalToUserIdMap = new ConcurrentHashMap<>();
	private final Map<String, AndroidViewComponent> internalIdToComponentMap = new ConcurrentHashMap<>();

	public ClickX(ComponentContainer container) {
		super(container.$form());
	}

	@SimpleEvent
	public void Clicked(final AndroidViewComponent component, final String id) {
		com.google.appinventor.components.runtime.EventDispatcher.dispatchEvent(this, "Clicked", component, id);
	}

	@SimpleEvent
	public void LongClicked(final AndroidViewComponent component, final String id) {
		com.google.appinventor.components.runtime.EventDispatcher.dispatchEvent(this, "LongClicked", component, id);
	}

	@SimpleEvent
	public void ErrorOccurred(final String errorFrom, final String error) {
		com.google.appinventor.components.runtime.EventDispatcher.dispatchEvent(this, "ErrorOccurred", errorFrom, error);
	}

	@SimpleFunction
	public void AddClickListener(Object component, String userId, boolean animation) {
		if (component instanceof YailList) {
			multipleClickListener((YailList) component, userId, animation);
			return;
		}

		if (!(component instanceof AndroidViewComponent)) {
			ErrorOccurred("AddClickListener", "Invalid component type");
			return;
		}

		AndroidViewComponent viewComponent = (AndroidViewComponent) component;

		if (componentToInternalIdMap.containsKey(viewComponent)) {
			ErrorOccurred("AddClickListener", "Component already registered");
			return;
		}

		try {
			String internalId = UUID.randomUUID().toString();

			ClickListener listener = new ClickListener(viewComponent, internalId);
			listeners.put(internalId, listener);
			componentToInternalIdMap.put(viewComponent, internalId);
			internalToUserIdMap.put(internalId, userId);
			internalIdToComponentMap.put(internalId, viewComponent);

			if (animation) {
				ClickShrinkEffect.applyClickShrinkSelf(viewComponent.getView());
			}
		} catch (Exception e) {
			ErrorOccurred("AddClickListener", "Exception: " + e.getMessage());
		}
	}

	private void multipleClickListener(YailList components, String userId, boolean animation) {
		if (components == null || components.size() == 0) {
			ErrorOccurred("AddClickListener", "Component list is empty");
			return;
		}

		try {
			AndroidViewComponent parent = (AndroidViewComponent) components.getObject(0);

			for (int i = 0; i < components.size(); i++) {
				AndroidViewComponent child = (AndroidViewComponent) components.getObject(i);

				if (componentToInternalIdMap.containsKey(child)) {
					continue;
				}

				String internalId = UUID.randomUUID().toString();

				ClickListener listener = new ClickListener(child, internalId);
				listeners.put(internalId, listener);
				componentToInternalIdMap.put(child, internalId);
				internalToUserIdMap.put(internalId, userId);
				internalIdToComponentMap.put(internalId, child);

				if (animation) {
					ClickShrinkEffect.applyClickShrinkTarget(child.getView(), parent.getView());
				}
			}
		} catch (Exception e) {
			ErrorOccurred("AddClickListener", "Exception: " + e.getMessage());
		}
	}

	public void HandleClick(String internalId) {
		String userId = internalToUserIdMap.get(internalId);
		AndroidViewComponent component = internalIdToComponentMap.get(internalId);

		if (userId != null && component != null) {
			Clicked(component, userId);
		} else {
			ErrorOccurred("HandleClick", "Unknown internal ID: " + internalId);
		}
	}

	public void HandleLongClick(String internalId) {
		String userId = internalToUserIdMap.get(internalId);
		AndroidViewComponent component = internalIdToComponentMap.get(internalId);

		if (userId != null && component != null) {
			LongClicked(component, userId);
		} else {
			ErrorOccurred("HandleLongClick", "Unknown internal ID: " + internalId);
		}
	}

	@SimpleFunction
	public void FullClickable(final String userId, AndroidViewComponent parentLayout, boolean animation) {
		try {
			View parentView = parentLayout.getView();
			setupFullClickableListeners(parentView, userId, animation);
		} catch (Exception e) {
			ErrorOccurred("FullClickable", "Exception: " + e.getMessage());
		}
	}

	private void setupFullClickableListeners(View view, String userId, boolean animation) {
		if (view == null) return;

		AndroidViewComponent comp = findComponentByView(view);

		if (comp != null) {
			view.setClickable(true);
			view.setLongClickable(true);
			view.setOnClickListener(v -> Clicked(comp, userId));
			view.setOnLongClickListener(v -> {
				LongClicked(comp, userId);
				return true;
			});

			if (animation) {
				ClickShrinkEffect.applyClickShrinkSelf(view);
			}
		}

		if (view instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) view;
			for (int i = 0; i < vg.getChildCount(); i++) {
				setupFullClickableListeners(vg.getChildAt(i), userId, animation);
			}
		}
	}

	private AndroidViewComponent findComponentByView(View view) {
		for (Map.Entry<AndroidViewComponent, String> entry : componentToInternalIdMap.entrySet()) {
			if (entry.getKey().getView() == view) {
				return entry.getKey();
			}
		}
		return null;
	}

	@SimpleFunction
	public boolean InstanceOf(Component component1, Component component2) {
		if (component1 == null || component2 == null) return false;
		return component1.getClass().getSimpleName().equals(component2.getClass().getSimpleName());
	}

	private final class ClickListener implements View.OnClickListener, View.OnLongClickListener {
		final String internalId;
		final AndroidViewComponent component;
		private final View targetView;

		ClickListener(AndroidViewComponent component, String internalId) {
			this.component = component;
			this.internalId = internalId;
			this.targetView = getFinalView(component);
			this.targetView.setOnClickListener(this);
			this.targetView.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View view) {
			HandleClick(internalId);
		}

		@Override
		public boolean onLongClick(View view) {
			HandleLongClick(internalId);
			return true;
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
					Log.e(TAG, "getFinalView: " + e.getMessage());
				}
			}

			return view;
		}
	}
}
