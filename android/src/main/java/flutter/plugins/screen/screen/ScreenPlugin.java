package flutter.plugins.screen.screen;

import android.app.Activity;
import android.provider.Settings;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * ScreenPlugin
 */
public class ScreenPlugin implements FlutterPlugin, MethodCallHandler {

    private Activity activity ;
    private MethodChannel methodChannel ;

    private ScreenPlugin(Activity activity){
        this.activity = activity;
    }

    public static void registerWith(Registrar registrar) {
        final ScreenPlugin instance = new ScreenPlugin(registrar.activity());
        instance.onAttachedToEngine(registrar.messenger());
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch(call.method){
            case "brightness":
                result.success(getBrightness());
                break;
            case "setBrightness":
                double brightness = call.argument("brightness");
                WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
                layoutParams.screenBrightness = (float)brightness;
                activity.getWindow().setAttributes(layoutParams);
                result.success(null);
                break;
            case "isKeptOn":
                int flags = activity.getWindow().getAttributes().flags;
                result.success((flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0) ;
                break;
            case "keepOn":
                Boolean on = call.argument("on");
                if (on) {
                    System.out.println("Keeping screen on ");
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                else{
                    System.out.println("Not keeping screen on");
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                result.success(null);
                break;

            default:
                result.notImplemented();
                break;
        }
    }

    private float getBrightness(){
        float result = activity.getWindow().getAttributes().screenBrightness;
        if (result < 0) { // the application is using the system brightness
            try {
                result = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) / (float)255;
            } catch (Settings.SettingNotFoundException e) {
                result = 1.0f;
                e.printStackTrace();
            }
        }
        return result;
    }

    private void onAttachedToEngine(BinaryMessenger messenger){
        methodChannel = new MethodChannel(messenger, "github.com/clovisnicolas/flutter_screen");
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getBinaryMessenger());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        methodChannel = null ;
        activity = null ;
    }

}
