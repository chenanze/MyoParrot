package chenanze.com;

import android.app.Application;

import chenanze.com.utils.SpUtil;

/**
 * Created by duian on 2016/12/10.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SpUtil.init(this);
    }
}
