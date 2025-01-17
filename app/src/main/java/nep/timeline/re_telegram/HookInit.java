package nep.timeline.re_telegram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import nep.timeline.re_telegram.application.ApplicationLoaderHook;
import nep.timeline.re_telegram.features.AllowMoveAllChatFolder;
import nep.timeline.re_telegram.features.AntiAntiForward;
import nep.timeline.re_telegram.features.AntiRecallWithDatabase;
import nep.timeline.re_telegram.features.NEWAntiRecall;
import nep.timeline.re_telegram.features.NoSponsoredMessages;
import nep.timeline.re_telegram.features.ProhibitChannelSwitching;
import nep.timeline.re_telegram.features.UseSystemTypeface;
import nep.timeline.re_telegram.utils.ClientChecker;
import nep.timeline.re_telegram.utils.Utils;

public class HookInit implements IXposedHookLoadPackage {
    // 需要钩入的应用包名
    private static final List<String> hookPackages = Arrays.asList(
        "org.telegram.messenger", "org.telegram.messenger.web", "org.telegram.messenger.beta",
        "org.telegram.plus", "org.telegram.mdgram", "tw.nekomimi.nekogram", "com.cool2645.nekolite",
        "com.exteragram.messenger", "org.forkgram.messenger", "org.forkclient.messenger",
        "org.forkclient.messenger.beta", "me.onlyfire.yukigram.beta", "com.iMe.android.web",
        "com.radolyn.ayugram", "it.octogram.android", "xyz.nextalone.nnngram", "it.belloworld.mercurygram"
    );

    // 定制化包名
    private static final List<String> hookPackagesCustomization = Arrays.asList(
        "xyz.nextalone.nagram", "nekox.messenger", "com.xtaolabs.pagergram", "nu.gpu.nagram",
        "nekox.messenger.broken"
    );

    // 其他配置
    private static final List<String> notNeedHideStories = Arrays.asList(
        "tw.nekomimi.nekogram", "com.exteragram.messenger"
    );
    public static final boolean DEBUG_MODE = false;

    // 获取钩入包名列表
    public final List<String> getHookPackages() {
        List<String> hookPackagesLocal = new ArrayList<>(hookPackages);
        hookPackagesLocal.addAll(hookPackagesCustomization);
        return hookPackagesLocal;
    }

    // 判断是否仅需要 AntiRecall
    private boolean onlyNeedAR(String pkgName) {
        return hookPackagesCustomization.contains(pkgName);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 判断包名是否在钩入列表中
        if (getHookPackages().contains(lpparam.packageName)) {
            if (DEBUG_MODE)
                Utils.log("Trying to hook app: " + lpparam.packageName);

            Utils.pkgName = lpparam.packageName;
            ClassLoader classLoader = lpparam.classLoader;

            // 初始化应用加载钩子
            ApplicationLoaderHook.init(classLoader);

            // 特定客户端初始化
            if (lpparam.packageName.equals("nu.gpu.nagram")) {
                // 为 nu.gpu.nagram 包添加特定初始化
                NEWAntiRecall.initUI(classLoader);
                NEWAntiRecall.initProcessing(classLoader);
                NEWAntiRecall.init(classLoader);
                NEWAntiRecall.initAutoDownload(classLoader);
            } else if (ClientChecker.check(ClientChecker.ClientType.Yukigram)) {
                AntiRecallWithDatabase.initUI(classLoader);
                AntiRecallWithDatabase.initProcessing(classLoader);
                AntiRecallWithDatabase.init(classLoader);
            } else {
                NEWAntiRecall.initUI(classLoader);
                NEWAntiRecall.initProcessing(classLoader);
                NEWAntiRecall.init(classLoader);
                NEWAntiRecall.initAutoDownload(classLoader);
            }

            // 为 Nekogram 执行特定初始化
            if (!ClientChecker.check(ClientChecker.ClientType.Nekogram))
                AllowMoveAllChatFolder.init(classLoader);

            // 其他模块的初始化
            if (!onlyNeedAR(lpparam.packageName)) {
                if (!ClientChecker.check(ClientChecker.ClientType.MDgram))
                    UseSystemTypeface.init(classLoader);

                if (!lpparam.packageName.equals("xyz.nextalone.nnngram")) {
                    if (!ClientChecker.check(ClientChecker.ClientType.Nekogram))
                        ProhibitChannelSwitching.init(classLoader);

                    if (!notNeedHideStories.contains(lpparam.packageName))
                        HideStories.init(classLoader);

                    NoSponsoredMessages.init(classLoader);
                }

                AntiAntiForward.init(classLoader);
            }
        }
    }
}
