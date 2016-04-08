Leanback Launcher

This really is just a hack hobby project on constructing a leanback launcher example. 

Hacked up from the standard ATV leanback browser sample.

Has a function to have a favorites row.

VERY IMPORTANT:
TO INSTALL:
After installing and running, type
"adb shell pm hide com.google.android.leanbacklauncher"

otherwise leanback will stay as the home launcher.

To bring leanback back...
adb shell pm unhide com.google.android.leanbacklauncher

Note - if programatic install, need to run:
   Intent intend = new Intent(Intent.ACTION_MAIN, null);
   intend.addCategory(Intent.CATEGORY_HOME);
   List<ResolveInfo> availableActivities = ctx.getPackageManager().queryIntentActivities(intend, 0);
and check for availableActivities.size() > 1


