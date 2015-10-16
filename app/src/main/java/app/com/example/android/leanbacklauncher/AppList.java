package app.com.example.android.leanbacklauncher;

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

//TODO - hook up settings (no leanback launcher intent)
//TODO - figure out shieldhub and netflix
//TODO - add favorites add/remove (and store in prefs)

public final class AppList {
    public static final String APP_CATEGORY[] = {
            "Favorites",
            "ShieldHUB",
            "Games",
            "Apps",
    };

    public final static int CAT_FAVORITES = 0;
    public final static int CAT_SHUB = 1;
    public final static int CAT_GAMES = 2;
    public final static int CAT_APPS = 3;

    public final static String SHUB = "SHIELD Hub";

    private static PackageManager manager;
    public static ArrayList<ArrayList<AppDetail>> apps;

    public static ArrayList<ArrayList<AppDetail>> loadApps(Context ctx) {
        manager = ctx.getPackageManager();
        apps = new ArrayList<ArrayList<AppDetail>>();

        Set<String> favorites = loadFavoritesSharedPreferences(ctx);

        Intent intend = new Intent(Intent.ACTION_MAIN, null);
        intend.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);

        //first set up each row with an array list
        for (int i = 0; i < APP_CATEGORY.length; i++) {
            //set up base array list...
            apps.add(i, new ArrayList<AppDetail>());
        }

        //next, set up apps, shieldhub, games
        List<ResolveInfo> availableActivities = manager.queryIntentActivities(intend, 0);
        //loop through all apps...
        for (int j = 0; j < availableActivities.size(); j++) {
            ResolveInfo ri = availableActivities.get(j);

            AppDetail app = new AppDetail();
            app.label = ri.loadLabel(manager);
            app.name = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(manager);
            app.banner = ri.activityInfo.loadBanner(manager);
            //hmm - for some apps, this not getting us data...
            if (app.banner == null) {
                //go in deeper to find different activities that can be launched.
            }

            if (app.label.equals("LeanbackLauncher")) break;    //don't add this app

            try {
                app.ai = manager.getApplicationInfo(app.name.toString(), PackageManager.GET_META_DATA);
                app.res = manager.getResourcesForApplication(ri.activityInfo.packageName);

                //check for favorites...
                if (favorites != null) {
                    if (favorites.contains(app.label.toString())) {
                        apps.get(CAT_FAVORITES).add(app);
                    }
                }
                
                if (app.label.equals(SHUB)) {
                    apps.get(CAT_SHUB).add(app);
                } else if ((app.ai.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                    apps.get(CAT_GAMES).add(app);
                } else {
                    apps.get(CAT_APPS).add(app);
                }

            } catch (PackageManager.NameNotFoundException e) {

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
