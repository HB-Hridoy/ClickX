package com.hridoy.clickx;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.YailList;
import com.sumit1334.dynamicclickutil.ClickShrinkUtils;
import com.sumit1334.dynamicclickutil.ClickShrinkUtilsSingle;
import com.sumit1334.dynamicclickutil.DynamicClickUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

@DesignerComponent(
	version = 1,
	versionName = "1.0",
	description = "Developed by Hridoy using Fast.",
	iconName = "icon.png"
)
public class ClickX extends AndroidNonvisibleComponent {

  	public ClickX(ComponentContainer container) {
		super(container.$form());
	  }

	private final String TAG = "ClickX";
	private final boolean LOG_DEBUG = false;
	private final HashMap<String, ClickX.ClickListener> listeners = new HashMap<>();
	private final ArrayList<AndroidViewComponent> components = new ArrayList<>();
	private final HashMap<String, String> fullClickListeners = new HashMap<>();

	// ===============================================
	// PROPERTIES START
	// ===============================================

	// ===============================================
	// PROPERTIES END
	// ===============================================

	// ===============================================
	// METHODS START
	// ===============================================

	@SimpleFunction
	public void AddClickListener(AndroidViewComponent component, String id, Object data, boolean animation) {
		if (!(this.listeners.containsKey(id) || this.components.contains(component))) {
			this.listeners.put(id, new ClickX.ClickListener(component, data, id));
			this.components.add(component);
			if (animation){
				ClickShrinkUtilsSingle.applyClickShrink(component.getView());
			}

		} else{
			ErrorOccurred("AddClickListener", "Failed to add click listener of "+ id);
		}
	}

	@SimpleFunction
	public void FullClickListener(final YailList components, Object data, boolean animation){
		final YailList componentsSubList = (YailList)components;
		final AndroidViewComponent parentComponent = (AndroidViewComponent) componentsSubList.getObject(0);

		for (int j = 0; j < componentsSubList.size(); ++j) {
			final Object mComponent = componentsSubList.getObject(j);
			final AndroidViewComponent animChild = (AndroidViewComponent)mComponent;
			// Defining id for child views
			String id = data.toString() + "_" + j + "_fullClickable_Id_" + j;

			// Adding Click Listeners to child components
			if (!(this.listeners.containsKey(id) || this.components.contains(animChild))) {
				this.listeners.put(id, new DynamicClickUtil.ClickListener(animChild, data, id));
				this.components.add(animChild);
			}
			// Adding the animation
			if (animation){
				ClickShrinkUtils.applyClickShrink(animChild.getView(), parentComponent.getView());
			}
		}
	}

	@SimpleFunction(
			description = "Makes given view for full clickable\n" +
					" It will consume click and long click events of all (applicable) child views\n\n" +
					"Child views id will be - id_child1,id_child2 and so on\n" +
					"But parent layout id remain same"
	)
	public void FullClickable(final String id, Object data, AndroidViewComponent parentLayout, boolean animation) {
		View parentView = parentLayout.getView();
		parentView.setClickable(true);
		parentView.setLongClickable(true);
		parentView.setOnClickListener(new OnClickListener() {
			public void onClick(View var1x) {
				DynamicClickUtil.this.FullClicked(id, data.toString());
				//ClickUtil.this.Clicked(var1);
			}
		});
		parentView.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View var1x) {
				DynamicClickUtil.this.FullLongClicked(id, data.toString());
				//ClickUtil.this.LongClicked(var1);
				return true;
			}
		});

		if (animation){
			ClickShrinkUtilsSingle.applyClickShrink(parentLayout.getView());
		}

		//
		if (this.getViewsCount(parentView) > 0) {
			ViewGroup parentViewGroup = (ViewGroup)parentView;
			Iterator parentViewChilds = this.getViews(parentViewGroup).iterator();

			int childIndexCounter = 1; // Initialize the child index counter for id

			while(parentViewChilds.hasNext()) {
				View ChildView = (View) parentViewChilds.next();
				// Defining id for child views
				String childId = id + "_child" + childIndexCounter;

				ChildView.setClickable(true);
				ChildView.setLongClickable(true);
				ChildView.setOnClickListener(new OnClickListener() {
					public void onClick(View var1x) {
						DynamicClickUtil.this.FullClicked(childId, data.toString());
						//ClickUtil.this.Clicked(var1);
					}
				});
				ChildView.setOnLongClickListener(new OnLongClickListener() {
					public boolean onLongClick(View var1x) {
						DynamicClickUtil.this.FullLongClicked(childId, data.toString());
						//ClickUtil.this.LongClicked(var1);
						return true;
					}
				});
				//
				childIndexCounter++; // Increment the child index counter
				// Adding the animation
				if (animation){
					ClickShrinkUtils.applyClickShrink(ChildView, parentLayout.getView());
				}
			}
		}

	}

	@SimpleFunction
	public boolean InstanceOf(Component component1, Component component2) {
		return component1.getClass().getSimpleName().equals(component2.getClass().getSimpleName());
	}

	@SimpleFunction
	public void SetData(String id, Object data) {
		if (GetData(id) == data)
			return;
		this.listeners.get(id).data = data;
	}

	@SimpleFunction
	public Object GetData(String id) {
		return this.listeners.get(id).data;
	}

	// ===============================================
	// METHODS END
	// ===============================================

	// ===============================================
	// EVENTS START
	// ===============================================

	@SimpleEvent
	public void Clicked(final AndroidViewComponent component, final String id, final Object data) {
		EventDispatcher.dispatchEvent(this, "Clicked", component, id, data);
	}

	@SimpleEvent
	public void LongClicked(final AndroidViewComponent component, final String id, final Object data) {
		EventDispatcher.dispatchEvent(this, "LongClicked", component, id, data);
	}

	@SimpleEvent
	public void FullClicked(final String id, final Object data) {
		EventDispatcher.dispatchEvent(this, "FullClicked", id, data);
	}

	@SimpleEvent
	public void FullLongClicked(final String id, final Object data) {
		EventDispatcher.dispatchEvent(this, "FullLongClicked", id, data);
	}

	@SimpleEvent
	public void ErrorOccurred(final String errorFrom, final String error) {
		EventDispatcher.dispatchEvent(this, "ErrorOccurred", errorFrom, error);
	}

	// ===============================================
	// EVENTS END
	// ===============================================

}
