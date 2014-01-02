package hu.u_szeged.inf.aramis;

import com.google.inject.Module;

import java.util.List;

import roboguice.application.RoboApplication;

public class MainApplication extends RoboApplication {
    @Override
    protected void addApplicationModules(List<Module> modules) {
        modules.add(new AppModule());
    }

}
