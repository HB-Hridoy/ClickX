package com.hridoy.clickx;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.annotations.SimpleFunction;

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


}
