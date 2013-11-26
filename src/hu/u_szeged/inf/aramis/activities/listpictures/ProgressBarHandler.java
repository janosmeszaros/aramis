package hu.u_szeged.inf.aramis.activities.listpictures;

import android.app.ProgressDialog;
import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressBarHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressBarHandler.class);

    private static ProgressDialog progressDialog;

    public static void start(Context context) {
        LOGGER.info("Starting progress dialog");
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Processing...");
        progressDialog.setMessage("Please wait.");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    public static void stop() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

}
