package chenanze.com.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Administrator on 2016/4/5.
 */
public class SpUtil {
    static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getMode() {
        return prefs.getString("mode", "normal");
    }

    public static void setMode(String content) {
        prefs.edit().putString("mode", content).commit();
    }
}
