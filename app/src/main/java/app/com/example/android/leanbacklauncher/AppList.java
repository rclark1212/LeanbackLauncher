package app.com.example.android.leanbacklauncher;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//TODO - figure out shieldhub (look at the oem intents)
//TODO - figure out search
//TODO - make launcher in manifest (look at the pano manifest)
//TODO - add a friend's recommendation row (like circles - it gets shared across the group). second line is "from"
//TODO - add following rows
//  Tablet apps (if any exist)
//  Friends (friends recommendations). Use hack netstore for load/save
//  Recommendations
//TODO - add config page to show/hide rows, turn off home screen
//TODO - add generic category backdrops for categories
//TODO - add content capability for rows
//TODO - add search
//TODO - add help/about button to give shortcut key instructions and simple summary
//TODO - auto-hide presentation of empty rows

//To make this an app, add the .LEANBACK_LAUNCHER to the maniifest
//<category android:name="android.intent.category.LEANBACK_LAUNCHER" />
//To make this a launcher, add the .HOME, .DEFAULT to the manifest
//<category android:name="android.intent.category.HOME" />
//<category android:name="android.intent.category.DEFAULT" />


public final class AppList {
    public static final String APP_CATEGORY[] = {
            "Favorites",
          /*  "Friends",
            "Recommendations", */
            "ShieldHUB",
            "Games",
            "Apps",
            "TabletApps"
    };

    public final static int CAT_FAVORITES = 0;
    public final static int CAT_SHUB = 1;
    public final static int CAT_GAMES = 2;
    public final static int CAT_APPS = 3;
    public final static int CAT_TABAPPS = 4;

    public final static String SHUB = "SHIELD Hub";

    private static PackageManager manager;
    public static ArrayList<ArrayList<AppDetail>> apps;
    public static AppDetail mLeanbackLauncher;

    public static ArrayList<ArrayList<AppDetail>> loadApps(Context ctx) {
        manager = ctx.getPackageManager();
        apps = new ArrayList<ArrayList<AppDetail>>();

        Set<String> favorites = loadFavoritesSharedPreferences(ctx);

        //first set up each row with an array list
        for (int i = 0; i < APP_CATEGORY.length; i++) {
            //set up base array list...
            apps.add(i, new ArrayList<AppDetail>());
        }

        //Okay - loop through getting intents twice. First to find leanback apps,
        //second, find tablet apps...
        for (int apptype = 0; apptype < 2; apptype++) {

            Intent intend = new Intent(Intent.ACTION_MAIN, null);

            if (apptype == 1) {
                intend.addCategory(Intent.CATEGORY_LAUNCHER);
            } else {
                intend.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
            }

            //next, set up apps, shieldhub, games
            List<ResolveInfo> availableActivities = manager.queryIntentActivities(intend, 0);
            //loop through all apps...
            for (int j = 0; j < availableActivities.size(); j++) {
                ResolveInfo ri = availableActivities.get(j);

                AppDetail app = new AppDetail();
                app.label = ri.loadLabel(manager);
                app.name = ri.activityInfo.packageName;
                app.name2 = ri.activityInfo.name;
                app.icon = ri.activityInfo.loadIcon(manager);
                app.banner = ri.activityInfo.loadBanner(manager);
                app.bIsApp = true;
                app.bIsTablet = false;

                //hmm - for some apps, this not getting us data...
                if (app.banner == null) {
                    //go in deeper to find different activities that can be launched.
                }

                try {
                    app.ai = manager.getApplicationInfo(app.name.toString(), PackageManager.GET_META_DATA);
                    app.res = manager.getResourcesForApplication(ri.activityInfo.packageName);

                    if (app.label.equals("LeanbackLauncher")) {
                        mLeanbackLauncher = app;
                        continue;    //don't add this app (but save it off)
                    }

                    //if we are going through tablet apps, lets punt if we already have it in leanback...
                    if (apptype == 1) {
                        Boolean bFound = false;
                        for (int k = 0; k < apps.get(CAT_APPS).size(); k++)
                        {
                            if (apps.get(CAT_APPS).get(k).name.equals(app.name)) {
                                bFound = true;
                                break;
                            }
                        }

                        if (bFound == true)
                            break;

                        app.bIsTablet = true;
                    }

                    //check for favorites...
                    if (favorites != null) {
                        if (favorites.contains(app.label.toString())) {
                            apps.get(CAT_FAVORITES).add(app);
                        }
                    }

                    if (app.label.equals(SHUB)) {
                        if (apptype == 0)
                            apps.get(CAT_SHUB).add(app);
                    } else if ((app.ai.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                        apps.get(CAT_GAMES).add(app);
                    } else if (apptype == 0){
                        apps.get(CAT_APPS).add(app);
                    } else {
                        apps.get(CAT_TABAPPS).add(app);
                    }

                } catch (PackageManager.NameNotFoundException e) {

                }
            }
        }

        return apps;
    }

    public static void saveFavoritesSharedPreferences(Context ctx) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

        //manually overwrite on each exit with current list...
        Set<String> favorites = new HashSet<String>();

        //loop through favorites
        for (int i = 0; i < AppList.apps.get(AppList.CAT_FAVORITES).size(); i++) {
            // get the app
            AppDetail app = AppList.apps.get(AppList.CAT_FAVORITES).get(i);

            //add it
            favorites.add(app.label.toString());
        }

        //and now save off this string set
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(ctx.getResources().getString(R.string.favorites_list), favorites);
        editor.commit();
    }

    //
    //Load the favorites list
    //
    private static Set<String> loadFavoritesSharedPreferences(Context ctx) {
        //Get the string from shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        Set<String> favorites = pref.getStringSet(ctx.getResources().getString(R.string.favorites_list), null);

        return favorites;
    }

}
