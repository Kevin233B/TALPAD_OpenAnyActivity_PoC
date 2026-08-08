import java.lang.reflect.Method;

public class OpenNativeSettingsMain {
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final String EXTERNAL_FRAGMENT =
            "com.android.xpadsettings.widget.menu.XpadExternalEnterFragment";
    private static final String TARGET_ACTIVITY =
            "com.android.settings.homepage.SettingsHomepageActivity";
    private static final String[] XPAD_ENTRIES = {
            "com.android.xpadsettings.XpadSettings$Companion$XpadDisplayActivity",
            "com.android.xpadsettings.XpadSettings$Companion$XpadScreenRefreshRateActivity",
            "com.android.xpadsettings.XpadSettings$Companion$XpadLockScreenTimeActivity",
            "com.android.xpadsettings.XpadSettings$Companion$XpadIntentInterceptor",
            "com.android.xpadsettings.XpadSettings$Companion$XpadAppMetaDetailActivity",
            "com.android.xpadsettings.XpadSettings$Companion$XpadNotificationDetailActivity",
    };
    private static final int FLAG_ACTIVITY_NEW_TASK = 0x10000000;

    public static void main(String[] args) {
        Object context = getApplicationContext();
        if (context == null) {
            System.out.println("拿不到 App Context（当前不是 Android 应用进程，或 hidden API 被拦）");
            return;
        }
        for (String entry : XPAD_ENTRIES) {
            try {
                startActivity(context, entry);
                System.out.println("已通过 " + entry + " 打开原生设置主页");
                return;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        System.out.println("打开失败");
    }

    private static Object getApplicationContext() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object application = activityThreadClass.getMethod("currentApplication").invoke(null);
            if (application == null) {
                return null;
            }
            Class<?> contextClass = Class.forName("android.content.Context");
            return contextClass.getMethod("getApplicationContext").invoke(application);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void startActivity(Object context, String entry) throws Exception {
        Class<?> intentClass = Class.forName("android.content.Intent");
        Object intent = intentClass.getConstructor().newInstance();

        Class<?> componentNameClass = Class.forName("android.content.ComponentName");
        Object componentName = componentNameClass
                .getConstructor(String.class, String.class)
                .newInstance(SETTINGS_PACKAGE, entry);
        intentClass.getMethod("setComponent", componentNameClass).invoke(intent, componentName);

        intentClass.getMethod("putExtra", String.class, String.class)
                .invoke(intent, ":settings:show_fragment", EXTERNAL_FRAGMENT);
        intentClass.getMethod("putExtra", String.class, String.class)
                .invoke(intent, ":settings:component_package", SETTINGS_PACKAGE);
        intentClass.getMethod("putExtra", String.class, String.class)
                .invoke(intent, ":settings:component_class", TARGET_ACTIVITY);
        intentClass.getMethod("addFlags", int.class).invoke(intent, FLAG_ACTIVITY_NEW_TASK);

        Class<?> contextClass = Class.forName("android.content.Context");
        Method startActivity = contextClass.getMethod("startActivity", intentClass);
        startActivity.invoke(context, intent);
    }
}
