package app.com.example.android.leanbacklauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

//TODO - hook up settings (no leanback launcher intent)
//TODO - figure out shieldhub and netflix
//TODO - add favorites add/remove

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

    public static List<Movie> list;

    private static PackageManager manager;
    public static ArrayList<ArrayList<AppDetail>> apps;

    public static ArrayList<ArrayList<AppDetail>> loadApps(Context ctx) {
        manager = ctx.getPackageManager();
        apps = new ArrayList<ArrayList<AppDetail>>();

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
            try {
                app.ai = manager.getApplicationInfo(app.name.toString(), PackageManager.GET_META_DATA);
                app.res = manager.getResourcesForApplication(ri.activityInfo.packageName);

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


}
