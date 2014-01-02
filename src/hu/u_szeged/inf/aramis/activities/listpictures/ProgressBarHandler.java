package hu.u_szeged.inf.aramis.activities.listpictures;

import android.app.ProgressDialog;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;
import com.googlecode.androidannotations.annotations.UiThread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.u_szeged.inf.aramis.MainActivity;

@EBean
public class ProgressBarHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressBarHandler.class);
    @RootContext
    protected MainActivity context;
    private ProgressDialog progressDialog;

    @UiThread
    public void start() {
        LOGGER.info("Starting progress dialog");
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Processing...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait.");
        progressDialog.show();
    }

    @UiThread
    public void stop() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
