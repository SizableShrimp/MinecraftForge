package net.minecraftforge.fml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class CrashReportCallables {
    private static final List<ICrashCallable> crashCallables = Collections.synchronizedList(new ArrayList<>());

    public static void registerCrashCallable(ICrashCallable callable)
    {
        crashCallables.add(callable);
    }

    public static void registerCrashCallable(String headerName, Callable<String> reportGenerator) {
        registerCrashCallable(new ICrashCallable() {
            @Override
            public String getLabel() {
                return headerName;
            }
            @Override
            public String call() throws Exception {
                return reportGenerator.call();
            }
        });
    }

    public static List<ICrashCallable> allCrashCallables() {
        return List.copyOf(crashCallables);
    }
}
